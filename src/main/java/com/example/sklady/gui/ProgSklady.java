package com.example.sklady.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProgSklady extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ProgSklady.class.getResource("prog-sklady.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1150, 650);
        stage.setTitle("ProgSklady");
        stage.setScene(scene);
        stage.show();
    }
}
