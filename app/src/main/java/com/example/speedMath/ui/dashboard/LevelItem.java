package com.example.speedMath.ui.dashboard;

public class LevelItem {
    public int levelNumber;
    public String mode;
    public int requiredCorrect;

    public LevelItem(int levelNumber, String mode, int requiredCorrect) {
        this.levelNumber = levelNumber;
        this.mode = mode;
        this.requiredCorrect = requiredCorrect;
    }
}
