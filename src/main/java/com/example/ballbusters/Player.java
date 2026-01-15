package com.example.ballbusters;

import javafx.scene.paint.Color;

public class Player {
    public int id;
    public double x, y;
    public double width = 120;
    public double height = 15;
    public Color color;

    public Player(int id, double x, double y, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
    }
}
