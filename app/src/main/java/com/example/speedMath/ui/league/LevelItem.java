package com.example.speedMath.ui.league;

public class LevelItem {
    public int levelNumber;
    public String mode;
    public long targetScore;

    public int difficulty;

    public enum Status {
        LOCKED, UNLOCKED, COMPLETED
    }
    public Status status;

    public LevelItem(int levelNumber, String mode, long targetScore, int difficulty, Status status) {
        this.levelNumber = levelNumber;
        this.mode = mode;
        this.targetScore = targetScore;
        this.difficulty = difficulty;
        this.status = status;
    }
}
