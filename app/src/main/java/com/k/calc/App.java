package com.k.calc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        CalculatorController controller = new CalculatorController();
        CalculatorView view = new CalculatorView(controller);

        controller.startEngine();

        Scene scene = new Scene(view.getRoot(), 420, 560);
        stage.setTitle("Calculadora Poliglota");
        stage.setScene(scene);
        scene.getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());

        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/logo_256.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o ícone: " + e.getMessage());
        }
        stage.setOnCloseRequest(e -> controller.stopEngine());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public String t() {
        return "app should have a greeting";
    }
}