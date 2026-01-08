package com.example.ballbusters;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class GameScene {

    private static final double PLAYER_RADIUS = 25;
    private static final double HEEL_WIDTH = 10;
    private static final double HEEL_HEIGHT = 20;
    private static final double MAX_SPEED = 5;

    private final Stage stage;
    private final Scene scene;
    private final Group root;

    // Rappresenta i giocatori
    private final Map<String, Player> players = new HashMap<>();

    private double mouseX;
    private double mouseY;

    public GameScene(Stage stage, String playerName) {
        this.stage = stage;
        root = new Group();
        scene = new Scene(root, 800, 600, Color.LIGHTGRAY);

        // Input mouse
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        // Crea giocatore locale
        Player local = new Player(playerName, 400, 300, Color.BLUE);
        players.put(playerName, local);
        root.getChildren().addAll(local.circle, local.heel, local.livesText);

        stage.setScene(scene);
        stage.setTitle("ballBusters - Gioco");

        // Ciclo di gioco
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(local);
                checkCollisions();
            }
        };
        timer.start();
    }

    /**
     * Aggiorna posizione palla locale
     */
    private void update(Player player) {
        double dx = mouseX - player.circle.getCenterX();
        double dy = mouseY - player.circle.getCenterY();

        // Limita velocità
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > MAX_SPEED) {
            dx = dx / distance * MAX_SPEED;
            dy = dy / distance * MAX_SPEED;
        }

        player.circle.setCenterX(player.circle.getCenterX() + dx);
        player.circle.setCenterY(player.circle.getCenterY() + dy);

        // Posiziona il tacco dietro la palla
        player.heel.setX(player.circle.getCenterX() - HEEL_WIDTH / 2);
        player.heel.setY(player.circle.getCenterY() + PLAYER_RADIUS);

        // Aggiorna testo vite
        player.livesText.setX(player.circle.getCenterX() - 15);
        player.livesText.setY(player.circle.getCenterY() - PLAYER_RADIUS - 5);
        player.livesText.setText("❤ " + player.lives);
    }

    /**
     * Controlla collisioni tra tacchi e palle
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
     * Classe interna che rappresenta un giocatore
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
            if (recentlyHit && now - hitCooldown < 500) return; // cooldown 0.5s

            lives--;
            recentlyHit = true;
            hitCooldown = now;

            if (lives <= 0) {
                circle.setFill(Color.GRAY);
                heel.setVisible(false);
            }
        }
    }

    public Scene getScene() {
        return scene;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }
}
