package com.example.ballbusters;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * LobbyScreen gestisce la lobby multiplayer di ballBusters
 * con lista giocatori, ruoli (host/client) e start della partita.
 */
public class LobbyScreen {

    private final ObservableList<String> players = FXCollections.observableArrayList();
    private final Stage stage;
    private final String playerName;
    private LobbyClient client;
    private final boolean isHost;

    private Button startButton; // Variabile di classe per abilitazione host

    /**
     * Costruttore LobbyScreen
     *
     * @param stage      Stage JavaFX
     * @param isHost     true se il client è host
     * @param playerName nome del giocatore
     */
    public LobbyScreen(Stage stage, boolean isHost, String playerName) {
        this.stage = stage;
        this.isHost = isHost;
        this.playerName = playerName;
        // UI
        Label title = new Label("Lobby - ballBusters");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label codeLabel = new Label("Connesso alla lobby");

        ListView<String> playerList = new ListView<>(players);
        playerList.setPrefHeight(200);

        startButton = new Button("Start Game");
        startButton.setDisable(!isHost); // solo host può iniziare

        startButton.setOnAction(e -> client.send("START"));

        VBox root = new VBox(10, title, codeLabel, playerList, startButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);

        // Connessione TCP al server
        client = new LobbyClient("localhost", 5000, this::handleMessage);

        // Invia JOIN con il nome del giocatore
        client.send("JOIN:" + playerName);
    }

    /**
     * Gestione dei messaggi ricevuti dal server TCP
     *
     * @param msg messaggio dal server
     */
    private void handleMessage(String msg) {
        // Lista giocatori aggiornata
        if (msg.startsWith("PLAYERS:")) {
            players.setAll(msg.substring(8).split(","));
        }

        // Partita avviata
        if (msg.equals("START")) {
            GameSceneMultiplayer game = new GameSceneMultiplayer(stage, playerName, "localhost", 6000);
            stage.setScene(game.getScene());
        }

        // Lobby piena
        if (msg.equals("FULL")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("Lobby piena!");
            alert.showAndWait();
        }

        // Gestione ruolo host
        if (msg.equals("ROLE:HOST")) {
            startButton.setDisable(false);
        }

        if (msg.equals("ROLE:CLIENT")) {
            startButton.setDisable(true);
        }

        // Messaggio di errore: start non consentito
        if (msg.equals("ERROR:NOT_HOST")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("Solo l'host può avviare la partita!");
            alert.showAndWait();
        }
    }
}
