package com.k.calc;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class CalculatorController {
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
        String expr = display.getText();

        if (isValidInput(expr)) {
            return;
        }

        // Ainda sem backend
        // Mais pra frente integração com rust
        String result = "[engine ainda não implementada]";

        history.getItems().add(expr + " = " + result);
        display.clear();
    }

    private final boolean isValidInput(String input) {
        return input == null || input.isEmpty();
    }
}
