package com.example.ballbusters;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LobbyScreen {

    private LobbyClient client;
    private boolean isHost = false;
    private Stage stage;

    public LobbyScreen(Stage stage) {
        this.stage = stage;

        Button hostBtn = new Button("HOST");
        Button joinBtn = new Button("JOIN");
        Button startBtn = new Button("START GAME");

        TextField ipField = new TextField();
        ipField.setPromptText("IP host");

        TextArea log = new TextArea();
        log.setEditable(false);

        startBtn.setDisable(true);

        hostBtn.setOnAction(e -> {
            new LobbyServer().start();
            client = new LobbyClient("localhost", 5000, this::handleMessage);
            isHost = true;
            startBtn.setDisable(false);
            log.appendText("Sei HOST\n");
        });

        joinBtn.setOnAction(e -> {
            client = new LobbyClient(ipField.getText(), 5000, this::handleMessage);
            log.appendText("Connesso\n");
        });

        startBtn.setOnAction(e -> {
            if (isHost) client.send("START_GAME");
        });

        VBox root = new VBox(10,
                hostBtn, joinBtn, ipField,
                startBtn, log
        );

        stage.setScene(new Scene(root, 400, 400));
        stage.setTitle("Lobby");
        stage.show();
    }

    private void handleMessage(String msg) {
        if (msg.equals("START_GAME")) {
            new GameScreen(stage);
        }
    }
}
