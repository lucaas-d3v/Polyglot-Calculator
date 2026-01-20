package com.k.calc;

import java.util.HashMap;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

public class CalculatorView {

    private final BorderPane root = new BorderPane();

    private final TextField display = new TextField();
    private final ListView<String> history = new ListView<>();

    private final CalculatorController controller;

    private final HashMap<KeyCode, Runnable> actions = new HashMap<>(16);

    public CalculatorView(CalculatorController controller) {
        this.controller = controller;

        buildTop();
        buildCenter();

        // 1) Primeiro define ações
        setupActions();
        // 2) Depois liga eventos (pra não depender de "acaso")
        wireEvents();

        // estilos por classe (CSS)
        root.getStyleClass().add("app-root");
        display.getStyleClass().add("calc-display");
        history.getStyleClass().add("calc-history");
    }

    public final Parent getRoot() {
        return root;
    }

    public final TextField getDisplay() {
        return display;
    }

    public final ListView<String> getHistory() {
        return history;
    }

    private void buildTop() {
        display.setPromptText("Digite a expressão (ex: (2 + 3) * 4 )");
        display.setFocusTraversable(true);

        display.setAlignment(Pos.CENTER_RIGHT);
        display.setPrefHeight(56);

        VBox top = new VBox(12, display, history);
        top.setPadding(new Insets(16));
        top.getStyleClass().add("calc-top");

        history.setPrefHeight(140);

        root.setTop(top);
    }

    private void buildCenter() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.getStyleClass().add("calc-grid");

        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(25);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }

        for (int i = 0; i < 5; i++) {
            RowConstraints r = new RowConstraints();
            r.setPercentHeight(20);
            r.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(r);
        }

        grid.add(iconButton(Material2AL.CLEAR,
                () -> controller.clear(display, history),
                "calc-btn-clear"), 0, 0);

        grid.add(iconButton(Material2AL.BACKSPACE,
                () -> controller.backspace(display),
                "calc-btn-backspace"), 1, 0);

        addBtn(grid, "(", 0, 2, () -> controller.append(display, "("), BtnKind.NORMAL);
        addBtn(grid, ")", 0, 3, () -> controller.append(display, ")"), BtnKind.NORMAL);

        // --- Teclado Numérico e Operações ---

        addBtn(grid, "7", 1, 0, () -> controller.append(display, "7"), BtnKind.NUMBER);
        addBtn(grid, "8", 1, 1, () -> controller.append(display, "8"), BtnKind.NUMBER);
        addBtn(grid, "9", 1, 2, () -> controller.append(display, "9"), BtnKind.NUMBER);
        addBtn(grid, "/", 1, 3, () -> controller.append(display, "/"), BtnKind.OP);

        addBtn(grid, "4", 2, 0, () -> controller.append(display, "4"), BtnKind.NUMBER);
        addBtn(grid, "5", 2, 1, () -> controller.append(display, "5"), BtnKind.NUMBER);
        addBtn(grid, "6", 2, 2, () -> controller.append(display, "6"), BtnKind.NUMBER);
        addBtn(grid, "*", 2, 3, () -> controller.append(display, "*"), BtnKind.OP);

        addBtn(grid, "1", 3, 0, () -> controller.append(display, "1"), BtnKind.NUMBER);
        addBtn(grid, "2", 3, 1, () -> controller.append(display, "2"), BtnKind.NUMBER);
        addBtn(grid, "3", 3, 2, () -> controller.append(display, "3"), BtnKind.NUMBER);
        addBtn(grid, "-", 3, 3, () -> controller.append(display, "-"), BtnKind.OP);

        addBtn(grid, "0", 4, 0, () -> controller.append(display, "0"), BtnKind.NUMBER);
        addBtn(grid, ".", 4, 1, () -> controller.append(display, "."), BtnKind.NORMAL);

        // CORREÇÃO: Apenas UM botão de igual. Escolhi o de TEXTO pela legibilidade.
        addBtn(grid, "=", 4, 2, () -> controller.eval(display, history), BtnKind.EQUALS);

        addBtn(grid, "+", 4, 3, () -> controller.append(display, "+"), BtnKind.OP);

        root.setCenter(grid);
    }

    private enum BtnKind {
        NORMAL, NUMBER, OP, EQUALS, CLEAR, BACKSPACE
    }

    private Button iconButton(Material2AL icon, Runnable action, String... styleClasses) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);

        Button btn = new Button();
        btn.setGraphic(fontIcon);
        btn.setText(null);

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMaxHeight(Double.MAX_VALUE);

        btn.getStyleClass().add("calc-btn");
        for (String cls : styleClasses) {
            btn.getStyleClass().add(cls);
        }

        btn.setOnAction(e -> action.run());
        btn.setOnMousePressed(e -> tap(btn, 0.96));
        btn.setOnMouseReleased(e -> tap(btn, 1.0));

        return btn;
    }

    private void addBtn(GridPane grid, String text, int row, int col, Runnable action, BtnKind kind) {
        Button btn = new Button(text);

        // Grid sizing
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMaxHeight(Double.MAX_VALUE);

        // CSS classes
        btn.getStyleClass().add("calc-btn");
        switch (kind) {
            case NUMBER -> btn.getStyleClass().add("calc-btn-number");
            case OP -> btn.getStyleClass().add("calc-btn-op");
            case EQUALS -> btn.getStyleClass().add("calc-btn-equals");
            case CLEAR -> btn.getStyleClass().add("calc-btn-clear");
            case BACKSPACE -> btn.getStyleClass().add("calc-btn-backspace");
            default -> {
            }
        }

        // Micro-animação de clique: "tap"
        btn.setOnMousePressed(e -> tap(btn, 0.96));
        btn.setOnMouseReleased(e -> tap(btn, 1.0));

        btn.setOnAction(e -> action.run());
        grid.add(btn, col, row);
    }

    private void tap(Button btn, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(70), btn);
        st.setToX(scale);
        st.setToY(scale);
        st.playFromStart();
    }

    private void setupActions() {
        // Parênteses
        actions.put(KeyCode.LEFT_PARENTHESIS, () -> controller.append(display, "("));
        actions.put(KeyCode.RIGHT_PARENTHESIS, () -> controller.append(display, ")"));

        // Operadores / ponto
        actions.put(KeyCode.DIVIDE, () -> controller.append(display, "/"));
        actions.put(KeyCode.MULTIPLY, () -> controller.append(display, "*"));
        actions.put(KeyCode.PLUS, () -> controller.append(display, "+"));
        actions.put(KeyCode.MINUS, () -> controller.append(display, "-"));
        actions.put(KeyCode.PERIOD, () -> controller.append(display, "."));

        // Enter = avaliar
        actions.put(KeyCode.ENTER, () -> controller.eval(display, history));

        // Backspace do teclado
        actions.put(KeyCode.BACK_SPACE, () -> controller.backspace(display));
    }

    private void wireEvents() {
        display.setOnKeyPressed(e -> {
            Runnable action = actions.get(e.getCode());
            if (action != null) {
                action.run();
                e.consume(); // evita duplas interpretações em algumas teclas
            }
        });
    }
}
