package com.example.speedMath.ui.dashboard;

public class LevelItem {
    public int levelNumber;
    public String mode;
    public int requiredCorrect;

    public int difficulty;

    public LevelItem(int levelNumber, String mode, int requiredCorrect, int difficulty) {
        this.levelNumber = levelNumber;
        this.mode = mode;
        this.requiredCorrect = requiredCorrect;
        this.difficulty = difficulty;
    }
}
