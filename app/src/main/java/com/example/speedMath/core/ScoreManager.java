package com.example.speedMath.core;

public class ScoreManager {

    private final int level;
    private int correctStreak = 0;
    private long timeElapsedMs = 0;
    private boolean isCorrect = true;
    private long score = 0;
    private static final int basePointsPerQuestion = 10;
    private static final int streakBonus = 5;

    public ScoreManager(int level) {
        this.level = level;
    }

    public long setScore(boolean isCorrect, long timeElapsedMs) {

        this.isCorrect = isCorrect;

        if (isCorrect) {

            // --- Streak ---
            correctStreak++;
            score += basePointsPerQuestion + ((long) (correctStreak -1)* streakBonus );

            // --- Bonus temps ---
            long delta = timeElapsedMs - this.timeElapsedMs;
            this.timeElapsedMs = timeElapsedMs;

            // Bonus = premier seuil correspondant → pas de cumul
            if (delta < 2500) {
                score += 15;
            } else if (delta < 5000) {
                score += 10;
            } else if (delta < 10000) {
                score += 5;
            } else if (delta < 20000) {
                score += 3;
            } else if (delta < 30000) {
                score += 2;
            }
            // sinon 0 bonus

        } else {
            // Mauvaise réponse
            correctStreak = 0;
           // score -= 100;
        }

        return score;
    }

    public long setScoreBonus(int nbQuestions, int correctAnswerNbr, long timeElapsedMs) {
        this.timeElapsedMs = timeElapsedMs;

        if (timeElapsedMs < 10000 + 5000 * this.level) {
            score += 100 * this.level * correctAnswerNbr / nbQuestions;
        }
        return score;
    }


    // Getters pour UI
    public int getCorrectStreak() { return correctStreak; }
    public long getTimeElapsedMs() { return timeElapsedMs; }
    public int getLevel() { return level; }
    public boolean getIsCorrect() { return isCorrect; }
    public long getScore() { return score; }
}
