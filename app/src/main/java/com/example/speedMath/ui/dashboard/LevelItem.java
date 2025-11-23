package com.example.speedMath.ui.dashboard;

public class LevelItem {
    public int levelNumber;
    public String mode;
    public int requiredCorrect;

    public int difficulty;

    public enum Status {
        LOCKED, UNLOCKED, COMPLETED
    }
    public Status status;

    public LevelItem(int levelNumber, String mode, int requiredCorrect, int difficulty, Status status) {
        this.levelNumber = levelNumber;
        this.mode = mode;
        this.requiredCorrect = requiredCorrect;
        this.difficulty = difficulty;
        this.status = status;
    }
}
