package com.k.calc;

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

    public void startEngine() {
        try {
            String path = System.getProperty("user.home") + "/Projetos/calc/engine/target/release/calc_engine";
            engine.start(path);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao iniciar o binario engine", e);
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
