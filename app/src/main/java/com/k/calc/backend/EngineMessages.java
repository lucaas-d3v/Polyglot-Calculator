package com.k.calc.backend;

public class EngineMessages {

    private EngineMessages() {
    }

    // requisições
    public record EvalRequest(String op, long id, String expr) {
    }

    public record ShutdownRequest(String op, long id) {
    }

    // respostas
    public record OkResponse(long id, boolean ok, String value) {
    }

    public record ErrorBody(String code, String message) {
    }

    public record ErrResponse(long id, boolean ok, ErrorBody error) {
    }
}
