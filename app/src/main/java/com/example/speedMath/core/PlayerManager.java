package com.example.speedMath.core;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayerManager {

    private static final String PREF_NAME = "user_stats";
    private static PlayerManager instance;
    private static final String KEY_BEST_LEVEL = "best_level";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_TIME_LEVEL_PREFIX = "time_level_";
    private static final String KEY_POINTS_LEVEL_PREFIX = "points_level_";
    private static final String KEY_ANSWERS_STREAK_PREFIX = "answers_streak_";
    private SharedPreferences prefs;

    private PlayerManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManager(context.getApplicationContext());
        }
        return instance;
    }

    // ========================= Level =========================
    public void setCurrentLevel(int level) {
        if(level > getCurrentLevel())
        {
            prefs.edit().putInt(KEY_BEST_LEVEL, level).apply();
        }
    }

    public int getCurrentLevel() {
        return prefs.getInt(KEY_BEST_LEVEL, 20);
    }

    // ========================= HighScore =========================
    public void setLevelHighScore(int level, long millis) {
        if (millis < getLevelHighScore(level) || getLevelHighScore(level) == 0)
        {
        prefs.edit().putLong(KEY_TIME_LEVEL_PREFIX + level, millis).apply();
        }
    }

    public long getLevelHighScore(int level) {
        return prefs.getLong(KEY_TIME_LEVEL_PREFIX + level, 0);
    }

    // ========================= HISTORY LAST 10 ANSWERS =========================

    public void setLastPlayedLevel(int level) {
        prefs.edit().putInt(KEY_CURRENT_LEVEL, level).apply();
    }

    public int getLastPlayedLevel() {
        return prefs.getInt(KEY_CURRENT_LEVEL, 0);
    }

    public void setCorrectAnswersStreak(String mode, int streak) {
        if(streak > getCorrectAnswersStreak(mode))
        {
            prefs.edit().putInt(KEY_ANSWERS_STREAK_PREFIX + mode, streak).apply();
        }
    }

    public int getCorrectAnswersStreak(String mode) {
        return prefs.getInt(KEY_ANSWERS_STREAK_PREFIX + mode, 0);
    }

    public void resetUserStats() {
        prefs.edit().clear().apply();
    }
}
