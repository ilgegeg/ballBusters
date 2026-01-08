package com.example.ballbusters;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("ballBusters");
        title.setFont(new Font("Arial Black", 36));

        Button hostButton = new Button("Host Game");
        hostButton.setPrefWidth(220);

        TextField codeField = new TextField();
        codeField.setPromptText("Codice lobby");
        codeField.setMaxWidth(220);

        Button joinButton = new Button("Join Game");
        joinButton.setPrefWidth(220);

        Label status = new Label();

        hostButton.setOnAction(e -> {
            new LobbyScreen(stage, true, "Host");
        });

        joinButton.setOnAction(e -> {
            if (!codeField.getText().isEmpty()) {
                new LobbyScreen(stage, false, "Player");
            }
        });


        VBox root = new VBox(15, title, hostButton, new Label("— oppure —"),
                codeField, joinButton, status);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("ballBusters - Connessione");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}