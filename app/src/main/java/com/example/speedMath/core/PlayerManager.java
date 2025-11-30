package com.example.speedMath.core;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import com.example.speedMath.R;

public class PlayerManager {

    private static final String PREF_NAME = "player_manager";
    private static PlayerManager instance;
    private static final String KEY_BEST_LEVEL = "best_level";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_TIME_LEVEL_PREFIX = "time_level_";
    private static final String KEY_POINTS_LEVEL_PREFIX = "points_level_";
    private static final String KEY_ANSWERS_STREAK_PREFIX = "answers_streak_";

    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_MUSIC = "music";
    private static final String KEY_VIBRATION = "vibration";

    private static final String KEY_ANIMATION = "animation";
    private static final String KEY_HAPTIC = "haptic";
    private MediaPlayer backgroundMusic = null;
    private SharedPreferences prefs;
    private Context context;

    private PlayerManager(Context context) {
        this.context = context.getApplicationContext();
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
        return prefs.getInt(KEY_BEST_LEVEL, 0);
    }

    // ========================= HighScore =========================
    public void setLevelHighScore(int level, long score) {
        if (score > getLevelHighScore(level))
        {
        prefs.edit().putLong(KEY_TIME_LEVEL_PREFIX + level, score).apply();
        }
    }

    public long getLevelHighScore(int level) {
        return prefs.getLong(KEY_TIME_LEVEL_PREFIX + level, 0);
    }

    // ========================= History Level =========================

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

    public int getTotalScore() {
        int totalScore = 0;
        for (int i = 1; i <= getCurrentLevel(); i++) {
            totalScore += getLevelHighScore(i);
        }
        return totalScore;
    }

    // ---- DARK MODE ----
    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    // ---- Effect ----
    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }


    public void setMusicEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply();
        if (enabled) startMusic();
        else stopMusic();
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(KEY_MUSIC, true);
    }

    public void startMusic() {
        if (backgroundMusic == null) {
            backgroundMusic = MediaPlayer.create(context, R.raw.music);
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.25f, 0.25f);
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    public void setAnimationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public boolean isAnimationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION, true);
    }


    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION, true);
    }

    public void setHapticEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply();
    }
    public boolean isHapticEnabled() {
        return prefs.getBoolean(KEY_HAPTIC, true);
    }
}
