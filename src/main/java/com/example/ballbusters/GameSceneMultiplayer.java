package com.example.ballbusters;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GameSceneMultiplayer {

    private static final double PLAYER_RADIUS = 25;
    private static final double HEEL_WIDTH = 10;
    private static final double HEEL_HEIGHT = 20;
    private static final double MAX_SPEED = 5;

    private final Stage stage;
    private final Scene scene;
    private final Group root;

    private double mouseX;
    private double mouseY;

    private final String playerName;
    private final Map<String, Player> players = new HashMap<>();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameSceneMultiplayer(Stage stage, String playerName, String serverHost, int serverPort) {
        this.stage = stage;
        this.playerName = playerName;
        root = new Group();
        scene = new Scene(root, 800, 600, Color.LIGHTGRAY);

        // Crea giocatore locale
        Player local = new Player(playerName, 400, 300, Color.BLUE);
        players.put(playerName, local);
        root.getChildren().addAll(local.circle, local.heel, local.livesText);

        // Input mouse
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        stage.setScene(scene);
        stage.setTitle("ballBusters - Multiplayer");

        // Connessione TCP al server di gioco
        try {
            socket = new Socket(serverHost, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Invia il nome al server
            out.println("JOIN:" + playerName);

            // Thread per ricevere aggiornamenti dal server
            new Thread(this::listenServer).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ciclo di gioco
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(local);
                sendPosition(local);
                checkCollisions();
            }
        };
        timer.start();
    }

    /**
     * Invia la posizione della palla locale al server
     */
    private void sendPosition(Player local) {
        if (out != null) {
            out.println("POS:" + playerName + ":" + local.circle.getCenterX() + ":" + local.circle.getCenterY());
        }
    }

    /**
     * Aggiorna posizione palla locale
     */
    private void update(Player player) {
        double dx = mouseX - player.circle.getCenterX();
        double dy = mouseY - player.circle.getCenterY();

        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > MAX_SPEED) {
            dx = dx / distance * MAX_SPEED;
            dy = dy / distance * MAX_SPEED;
        }

        player.circle.setCenterX(player.circle.getCenterX() + dx);
        player.circle.setCenterY(player.circle.getCenterY() + dy);

        player.heel.setX(player.circle.getCenterX() - HEEL_WIDTH / 2);
        player.heel.setY(player.circle.getCenterY() + PLAYER_RADIUS);

        player.livesText.setX(player.circle.getCenterX() - 15);
        player.livesText.setY(player.circle.getCenterY() - PLAYER_RADIUS - 5);
        player.livesText.setText("❤ " + player.lives);
    }

    /**
     * Controlla collisioni tra tacchi e palle (locale)
     */
    private void checkCollisions() {
        for (Player p1 : players.values()) {
            for (Player p2 : players.values()) {
                if (p1 == p2) continue;

                if (p1.heel.getBoundsInParent().intersects(p2.circle.getBoundsInParent())) {
                    p2.hit();
                }
            }
        }
    }

    /**
     * Thread che ascolta il server e aggiorna posizioni degli altri giocatori
     */
    private void listenServer() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                String msg = line;
                Platform.runLater(() -> handleServerMessage(msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServerMessage(String msg) {
        if (msg.startsWith("POS:")) {
            String[] parts = msg.split(":");
            String name = parts[1];
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);

            if (!players.containsKey(name)) {
                Player newPlayer = new Player(name, x, y, Color.GREEN);
                players.put(name, newPlayer);
                root.getChildren().addAll(newPlayer.circle, newPlayer.heel, newPlayer.livesText);
            } else {
                Player p = players.get(name);
                p.circle.setCenterX(x);
                p.circle.setCenterY(y);
                p.heel.setX(x - HEEL_WIDTH / 2);
                p.heel.setY(y + PLAYER_RADIUS);
                p.livesText.setX(x - 15);
                p.livesText.setY(y - PLAYER_RADIUS - 5);
            }
        }
    }

    public Scene getScene() {
        return scene;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    /**
     * Classe interna Player
     */
    private static class Player {
        String name;
        int lives = 2;

        Circle circle;
        Rectangle heel;
        Text livesText;

        boolean recentlyHit = false;
        long hitCooldown = 0;

        Player(String name, double x, double y, Color color) {
            this.name = name;
            circle = new Circle(x, y, PLAYER_RADIUS, color);
            heel = new Rectangle(HEEL_WIDTH, HEEL_HEIGHT, Color.RED);
            heel.setX(x - HEEL_WIDTH / 2);
            heel.setY(y + PLAYER_RADIUS);

            livesText = new Text("❤ " + lives);
            livesText.setFont(Font.font(18));
            livesText.setFill(Color.BLACK);
            livesText.setX(x - 15);
            livesText.setY(y - PLAYER_RADIUS - 5);
        }

        void hit() {
            long now = System.currentTimeMillis();
            if (recentlyHit && now - hitCooldown < 500) return;

            lives--;
            recentlyHit = true;
            hitCooldown = now;

            if (lives <= 0) {
                circle.setFill(Color.GRAY);
                heel.setVisible(false);
            }
        }
    }
}
