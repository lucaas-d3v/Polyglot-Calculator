package com.k.calc.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.calc.backend.EngineMessages.ErrResponse;
import com.k.calc.backend.EngineMessages.OkResponse;

public class FakeEngine {

    public static void main(String[] args) throws IOException {
        ObjectMapper json = new ObjectMapper();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while ((line = in.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            JsonNode node = json.readTree(line);

            String op = node.get("op").asText();
            long id = node.get("id").asLong();

            if (op.equals("shutdown")) {
                OkResponse resp = new EngineMessages.OkResponse(id, true, "bye");
                System.out.println(json.writeValueAsString(resp));
                System.out.flush();

                break;
            }

            if (op.equals("eval")) {
                String expr = node.get("expr").asText();

                // Resposta fake: ecoa a expressão
                OkResponse resp = new EngineMessages.OkResponse(id, true, "echo: " + expr);
                System.out.println(json.writeValueAsString(resp));
                System.out.flush();
                continue;
            }

            // op desconhecida
            ErrResponse err = new EngineMessages.ErrResponse(
                    id,
                    false,
                    new EngineMessages.ErrorBody("UNKNOWN_OP", "Operacao nao suportada: " + op));
            System.out.println(json.writeValueAsString(err));
            System.out.flush();
        }
    }
}
