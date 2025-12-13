package com.example.speedMath.core;

public class ScoreManager {

    private long score = 0;
    private int correctStreak = 0;
    private long lastAnswerTime = 0;
    private final int level;

    public ScoreManager(int level) {
        this.level = level;
    }

    // Valeur de base d’une question selon le niveau
    private long getBasePoints() {
        return 20 + (level * 5);  // ex : lvl10 → 150 pts
    }

    // Bonus temps scalable
    private long getTimeBonus(long deltaMs) {
        long baseBonus;
        if (deltaMs < 2500)      baseBonus = 15;
        else if (deltaMs < 5000) baseBonus = 10;
        else if (deltaMs < 10000) baseBonus = 5;
        else if (deltaMs < 20000) baseBonus = 3;
        else if (deltaMs < 30000) baseBonus = 2;
        else return 0;

        // scaling bonus : bonus * (1 + level*0.1)
        return (long) (baseBonus * (1f + (level * 0.10f)));
    }

    /**
     * Calcul du score pour UNE réponse.
     */
    public long setScore(boolean isCorrect, long currentTimeMs) {

        long delta = currentTimeMs - lastAnswerTime;
        lastAnswerTime = currentTimeMs;

        if (isCorrect) {

            // Streak
            correctStreak++;
            long streakBonus = (correctStreak - 1) * 5; // simple, modifiable

            // Calcul points
            score += getBasePoints();
            score += streakBonus;
            score += getTimeBonus(delta);

        } else {
            // Mauvaise réponse
            correctStreak = 0;
            score -= (20 + (level * 5));      // pénalité ajustable
        }

        return score;
    }

    public long setFinalBonus(int totalQuestions, int correctAnswers, long totalTimeMs) {

        long timeBonus = 0;
        long  accuracyBonus = 0;

        if(correctAnswers == totalQuestions) {
            accuracyBonus = getBasePoints();
        }

        if (totalTimeMs <= 10000 + (2000L * level)) {
            timeBonus = getBasePoints();
        }

        score += timeBonus + accuracyBonus;
        //score = 100 * level + timeBonus + accuracyBonus;

        return score;
    }

    public long getScore() {
        return score;
    }
}
