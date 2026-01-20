package com.k.calc.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.k.calc.backend.EngineMessages.EvalRequest;
import com.k.calc.backend.EngineMessages.ShutdownRequest;

public class EngineClient implements Closeable {

    private final ObjectMapper json = new ObjectMapper();

    private final AtomicLong seq = new AtomicLong(1);
    private final Map<Long, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    private Process process;
    private BufferedWriter toEngine;
    private Thread readerThread;

    public void start(String... command) throws IOException {
        // Sobe o processo
        process = new ProcessBuilder(command)
                .redirectErrorStream(true) // junta stderr no stdout pra debug inicial
                .start();

        // Prepara os streams (UTF-8 e com buffer)
        toEngine = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader fromEngine = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        // Thread dedicada pra ler respostas sem bloquear a UI
        readerThread = new Thread(() -> readLoop(fromEngine), "engine-reader");
        readerThread.setDaemon(true);
        readerThread.start();

    }

    public CompletableFuture<String> evalAsync(String expr) {
        ensureStarted();

        long id = seq.getAndIncrement();

        CompletableFuture<String> fut = new CompletableFuture<>();
        pending.put(id, fut);

        try {
            EvalRequest req = new EvalRequest("eval", id, expr);
            sendLine(json.writeValueAsString(req));
        } catch (Exception e) {
            pending.remove(id);
            fut.completeExceptionally(e);
        }

        return fut;
    }

    public void shutdown() {
        if (process == null || toEngine == null) {

            return;
        }

        // shutdown “educado”
        if (process == null) {

            return;
        }

        try {
            long id = seq.getAndIncrement();
            ShutdownRequest req = new ShutdownRequest("shutdown", id);
            sendLine(json.writeValueAsString(req));
        } catch (Exception ignored) {
            // se falhar, vamos pro close forçado
        }
    }

    @Override
    public void close() {
        // fecha stdin -> engine deve sair por EOF (fallback)
        try {
            if (toEngine != null)
                toEngine.close();
        } catch (IOException ignored) {
        }

        if (process != null) {
            process.destroy();
        }
    }

    private void sendLine(String line) throws IOException {
        // protocolo: 1 JSON por linha (NDJSON)
        toEngine.write(line);
        toEngine.write("\n");
        toEngine.flush();
    }

    private void ensureStarted() {
        if (process == null || toEngine == null) {
            throw new IllegalStateException("Engine nao foi iniciado. Chame start(...) antes de evalAsync().");
        }
    }

    private void readLoop(BufferedReader fromEngine) {
        try {
            String line;
            while ((line = fromEngine.readLine()) != null) {
                handleLine(line);
            }
        } catch (Exception e) {
            // Se o engine morreu, completa pendentes com erro
            pending.forEach((id, fut) -> fut.completeExceptionally(e));
            pending.clear();
        }
    }

    private void handleLine(String line) throws Exception {
        JsonNode node = json.readTree(line);

        long id = node.get("id").asLong();
        boolean ok = node.get("ok").asBoolean();

        CompletableFuture<String> fut = pending.remove(id);
        if (fut == null) {
            return; // resposta atrasada ou id desconhecido
        }

        if (ok) {
            String value = node.get("value").asText();
            fut.complete(value);
        } else {
            JsonNode errNode = node.get("error");
            String code = errNode.get("code").asText();
            String msg = errNode.get("message").asText();
            fut.completeExceptionally(new RuntimeException(code + ": " + msg));
        }
    }
}
