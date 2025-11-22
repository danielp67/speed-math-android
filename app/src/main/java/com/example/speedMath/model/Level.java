package com.example.speedMath.model;

public class Level {
    public int levelNumber;
    public String name;
    public String mode; // ADD, SUB, MUL, DIV, ALL
    public boolean unlocked;
    public boolean completed;

    public Level(int levelNumber, String name, String mode, boolean unlocked, boolean completed) {
        this.levelNumber = levelNumber;
        this.name = name;
        this.mode = mode;
        this.unlocked = unlocked;
        this.completed = completed;
    }
}
