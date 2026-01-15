package com.example.ballbusters;

import javafx.scene.paint.Color;

public class FollowerBall {
    public double x, y;
    public double radius;
    public Color color;

    public FollowerBall(double x, double y, double radius, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    public void follow(double tx, double ty, double strength) {
        x += (tx - x) * strength;
        y += (ty - y) * strength;
    }
}
