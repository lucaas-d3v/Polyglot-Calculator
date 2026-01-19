package com.k.calc;

import java.util.HashMap;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CalculatorView {
    private final BorderPane root = new BorderPane();

    private final TextField display = new TextField();
    private final ListView<String> history = new ListView<>();

    private final CalculatorController controller;

    private final HashMap<KeyCode, Runnable> actions = new HashMap<>(7);

    public CalculatorView(CalculatorController controller) {
        this.controller = controller;

        buildTop();
        buildCenter();
        wireEvents();

        actions.put(KeyCode.LEFT_PARENTHESIS, () -> controller.append(display, "("));
        actions.put(KeyCode.RIGHT_PARENTHESIS, () -> controller.append(display, ")"));

        actions.put(KeyCode.DIVIDE, () -> controller.append(display, "/"));
        actions.put(KeyCode.MULTIPLY, () -> controller.append(display, "*"));
        actions.put(KeyCode.PLUS, () -> controller.append(display, "+"));
        actions.put(KeyCode.PERIOD, () -> controller.append(display, "."));

        actions.put(KeyCode.ENTER, () -> {
            controller.eval(display, history);
        });
    }

    public final Parent getRoot() {
        return root;
    }

    private final void buildTop() {
        display.setPromptText("Digite a expressão (ex: (2 + 3) * 4 )");
        display.setFocusTraversable(true);

        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefHeight(56);

        // historico
        VBox top = new VBox(10, display, history);
        top.setPadding(new Insets(12));

        history.setPrefHeight(140);

        root.setTop(top);
    }

    private final void buildCenter() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        // (linha, coluna) -> botão
        addBtn(grid, "C", 0, 0, () -> controller.clear(display, history));
        addBtn(grid, "⌫", 0, 1, () -> controller.backspace(display));
        addBtn(grid, "(", 0, 2, () -> controller.append(display, "("));
        addBtn(grid, ")", 0, 3, () -> controller.append(display, ")"));

        addBtn(grid, "7", 1, 0, () -> controller.append(display, "7"));
        addBtn(grid, "8", 1, 1, () -> controller.append(display, "8"));
        addBtn(grid, "9", 1, 2, () -> controller.append(display, "9"));
        addBtn(grid, "/", 1, 3, () -> controller.append(display, "/"));

        addBtn(grid, "4", 2, 0, () -> controller.append(display, "4"));
        addBtn(grid, "5", 2, 1, () -> controller.append(display, "5"));
        addBtn(grid, "6", 2, 2, () -> controller.append(display, "6"));
        addBtn(grid, "*", 2, 3, () -> controller.append(display, "*"));

        addBtn(grid, "1", 3, 0, () -> controller.append(display, "1"));
        addBtn(grid, "2", 3, 1, () -> controller.append(display, "2"));
        addBtn(grid, "3", 3, 2, () -> controller.append(display, "3"));
        addBtn(grid, "-", 3, 3, () -> controller.append(display, "-"));

        addBtn(grid, "0", 3, 0, () -> controller.append(display, "0"));
        addBtn(grid, ".", 3, 1, () -> controller.append(display, "."));
        addBtn(grid, "=", 3, 2, () -> controller.eval(display, history));
        addBtn(grid, "+", 3, 3, () -> controller.append(display, "+"));

        // Deixa o grid no centro
        root.setCenter(grid);
    }

    private final void wireEvents() {
        display.setOnKeyPressed(e -> {
            Runnable action = actions.get(e.getCode()); // Pega o código da tecla
            if (action != null) {
                action.run();
            }
        });
    }

    private final void addBtn(GridPane grid, String text, int row, int col, Runnable action) {
        Button btn = new Button(text);

        btn.setPrefSize(90.0, 56.0);
        btn.setOnAction(e -> action.run());

        grid.add(btn, col, row);
    }
}
