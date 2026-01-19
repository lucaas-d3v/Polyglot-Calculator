package com.k.calc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        CalculatorController controller = new CalculatorController();
        CalculatorView view = new CalculatorView(controller);

        Scene scene = new Scene(view.getRoot(), 420, 560);

        stage.setTitle("Calculadora Poliglota");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}