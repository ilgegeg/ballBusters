package com.example.ballbusters;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class GameScreen {

    private static final int W = 900;
    private static final int H = 600;

    private double mouseX = W / 2;
    private double mouseY = H / 2;

    private List<FollowerBall> balls = new ArrayList<>();

    public GameScreen(Stage stage) {

        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("BALL BUSTERS – CRAZY MODE 😈");
        stage.show();

        // TRACK MOUSE
        scene.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        scene.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        // CREATE BALL CHAIN
        for (int i = 0; i < 25; i++) {
            balls.add(new FollowerBall(
                    mouseX,
                    mouseY,
                    10 + i * 0.5,
                    Color.hsb(i * 12, 1, 1)
            ));
        }

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        }.start();
    }

    private void update() {
        double targetX = mouseX;
        double targetY = mouseY;

        for (int i = 0; i < balls.size(); i++) {
            FollowerBall b = balls.get(i);

            double strength = 0.25 - i * 0.006;
            if (strength < 0.05) strength = 0.05;

            b.follow(targetX, targetY, strength);

            targetX = b.x;
            targetY = b.y;
        }
    }

    private void render(GraphicsContext gc) {
        // FADING BACKGROUND (TRAIL EFFECT)
        gc.setFill(Color.rgb(10, 10, 20, 0.25));
        gc.fillRect(0, 0, W, H);

        // DRAW BALLS
        for (int i = balls.size() - 1; i >= 0; i--) {
            FollowerBall b = balls.get(i);
            gc.setFill(b.color);
            gc.fillOval(
                    b.x - b.radius,
                    b.y - b.radius,
                    b.radius * 2,
                    b.radius * 2
            );
        }
    }
}
