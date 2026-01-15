package com.example.ballbusters;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

public class GameScreen {

    private static final int W = 900;
    private static final int H = 600;

    private LobbyClient client;
    private Set<KeyCode> keys = new HashSet<>();
    private Map<Integer, Player> players = new HashMap<>();
    private int myId = -1;

    public GameScreen(Stage stage, LobbyClient client) {
        this.client = client;

        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("BALL BUSTERS 🔥");
        stage.show();

        scene.setOnKeyPressed(e -> keys.add(e.getCode()));
        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));

        client.send("READY");

        new AnimationTimer() {
            public void handle(long now) {
                update();
                render(gc);
            }
        }.start();
    }

    private void update() {
        if (myId != -1 && players.containsKey(myId)) {
            Player me = players.get(myId);

            if (keys.contains(KeyCode.A)) {
                me.x -= 8;
                client.send("MOVE:" + myId + ":" + me.x);
            }
            if (keys.contains(KeyCode.D)) {
                me.x += 8;
                client.send("MOVE:" + myId + ":" + me.x);
            }
        }
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(15, 15, 30));
        gc.fillRect(0, 0, W, H);

        for (Player p : players.values()) {
            gc.setFill(p.color);
            gc.fillRoundRect(
                    p.x, p.y,
                    p.width, p.height,
                    20, 20
            );
        }
    }

    public void handleMessage(String msg) {

        if (msg.startsWith("ID:")) {
            myId = Integer.parseInt(msg.substring(3));
            return;
        }

        if (msg.startsWith("SPAWN:")) {
            String[] s = msg.split(":");
            int id = Integer.parseInt(s[1]);
            double x = Double.parseDouble(s[2]);
            double y = Double.parseDouble(s[3]);
            Color c = Color.valueOf(s[4]);

            players.put(id, new Player(id, x, y, c));
            return;
        }

        if (msg.startsWith("MOVE:")) {
            String[] s = msg.split(":");
            int id = Integer.parseInt(s[1]);
            double x = Double.parseDouble(s[2]);

            if (players.containsKey(id))
                players.get(id).x = x;
        }
    }
}
