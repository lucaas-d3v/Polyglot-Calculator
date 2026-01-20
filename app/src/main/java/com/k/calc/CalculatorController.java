package com.k.calc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.k.calc.backend.EngineClient;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class CalculatorController {
    private final EngineClient engine = new EngineClient();

    public CalculatorController() {
    }

    public final void append(TextField display, String token) {
        display.appendText(token);
    }

    public final void backspace(TextField display) {
        String text = display.getText();
        if (isValidInput(text)) {
            return;
        }

        display.setText(text.substring(0, text.length() - 1));
    }

    public final void clear(TextField display, ListView<String> history) {
        display.clear();
        history.getItems().clear();
    }

    public final void eval(TextField display, ListView<String> history) {
        var expr = display.getText();
        if (isValidInput(expr))
            return;

        display.setDisable(true);

        engine.evalAsync(expr).whenComplete((value, err) -> {
            javafx.application.Platform.runLater(() -> {
                if (err == null) {
                    history.getItems().add(expr + " = " + value);
                    display.clear();
                } else {
                    history.getItems().add(expr + " = [ERRO] " + err.getMessage());

                }
                display.setDisable(false);
            });
        });

    }

    private final boolean isValidInput(String input) {
        return input == null || input.isEmpty();
    }

    private static Path resolveEnginePath() throws IOException {
        // 1) Tentativa: instalado junto do app (mesma pasta do executável)
        try {
            var uri = CalculatorController.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path jarDir = Path.of(uri).getParent();
            Path candidate = jarDir.resolve("native/linux-x86_64/calc_engine");
            if (Files.exists(candidate) && Files.isExecutable(candidate)) {
                return candidate;
            }
        } catch (Exception ignored) {
        }

        // 2) Dev mode: extrair do classpath/resources
        String resourcePath = "/native/linux-x86_64/calc_engine";
        InputStream engineStream = CalculatorController.class.getResourceAsStream(resourcePath);

        if (engineStream == null) {
            throw new IllegalStateException("Binário do engine não encontrado em: " + resourcePath);
        }

        // Extrair para um arquivo temporário
        Path tempEngine = Files.createTempFile("calc_engine", "");
        Files.copy(engineStream, tempEngine, StandardCopyOption.REPLACE_EXISTING);
        engineStream.close();

        // Tornar executável
        tempEngine.toFile().setExecutable(true);

        // Deletar ao sair da aplicação
        tempEngine.toFile().deleteOnExit();

        return tempEngine;
    }

    public void startEngine() {
        try {
            Path enginePath = resolveEnginePath();
            engine.start(enginePath.toString());
        } catch (Exception e) {
            throw new RuntimeException("Falha ao iniciar o binário engine", e);
        }
    }

    public void stopEngine() {
        try {
            engine.shutdown();
        } finally {
            engine.close();
        }
    }
}