package com.example.ballbusters;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public void start(Stage stage) {
        new LobbyScreen(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
