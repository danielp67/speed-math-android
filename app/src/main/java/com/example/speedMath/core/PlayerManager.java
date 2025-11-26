package com.example.speedMath.core;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayerManager {

    private static final String PREF_NAME = "user_stats";
    private static PlayerManager instance;

    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_TIME_LEVEL_PREFIX = "time_level_";
    private static final String KEY_LAST_10 = "last_10";
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
            prefs.edit().putInt(KEY_CURRENT_LEVEL, level).apply();
        }
    }

    public int getCurrentLevel() {
        return prefs.getInt(KEY_CURRENT_LEVEL, 0);
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



/*    public void addAnswer(boolean correct) {
        String history = prefs.getString(KEY_LAST_10, "");
        if (history.isEmpty()) {
            history = "0,0,0,0,0,0,0,0,0,0";
        }

        String[] values = history.split(",");
        List<String> list = new ArrayList<>(Arrays.asList(values));

        if (list.size() >= 10) list.remove(0); // garder max 10
        list.add(correct ? "1" : "0");

        String newHistory = TextUtils.join(",", list);
        prefs.edit().putString(KEY_LAST_10, newHistory).apply();
    }

    public List<Boolean> getLast10Answers() {
        String history = prefs.getString(KEY_LAST_10, "");
        List<Boolean> list = new ArrayList<>();
        if (!history.isEmpty()) {
            String[] values = history.split(",");
            for (String v : values) {
                list.add(v.equals("1"));
            }
        }
        return list;
    }

    // ========================= POURCENTAGE DE REUSSITE =========================
    public int getSuccessPercent() {
        List<Boolean> last10 = getLast10Answers();
        if (last10.isEmpty()) return 0;

        int correct = 0;
        for (Boolean b : last10) if (b) correct++;

        return (correct * 100) / last10.size();
    }*/

    public void resetUserStats() {
        prefs.edit().clear().apply();
    }
}
