package com.k.calc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Calculadora Poliglota");
        stage.setScene(new Scene(new Label("UI OK"), 320, 200));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
