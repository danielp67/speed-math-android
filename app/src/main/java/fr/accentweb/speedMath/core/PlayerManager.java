package fr.accentweb.speedMath.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.MediaPlayer;

import com.google.firebase.database.DatabaseReference;

import java.util.Locale;

import fr.accentweb.speedMath.R;

public class PlayerManager {

    private static final String PREF_NAME = "player_manager";
    private static PlayerManager instance;
    private static final String KEY_BEST_LEVEL = "best_level";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_TIME_LEVEL_PREFIX = "time_level_";
    private static final String KEY_ANSWERS_STREAK_PREFIX = "answers_streak_";

    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_MUSIC = "music";
    private static final String KEY_VIBRATION = "vibration";

    private static final String KEY_ANIMATION = "animation";
    private static final String KEY_HAPTIC = "haptic";
    private static final String KEY_ARCADE_DIFFICULTY = "arcade_difficulty";
    private static final String KEY_NB_QUESTIONS = "nb_questions";

    private static final String KEY_ONLINE_UID = "online_uid";
    private static final String KEY_ONLINE_PSEUDO = "online_pseudo";
    private static final String KEY_ONLINE_SCORE = "online_score";
    private static final String KEY_ONLINE_PLAYED_MATCHES = "online_played_matches";
    private static final String KEY_ONLINE_WINS = "online_wins";
    private static final String KEY_ONLINE_LOSSES = "online_losses";
    private static final String KEY_ONLINE_DRAWS = "online_draws";
    private static final String KEY_LAST_CONNECTION = "last_connection";
    private static final String KEY_DAILY_MATCH_PLAYED = "daily_match_played";
    private static final String KEY_DAILY_MATCH_LIMIT = "daily_match_limit";

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

        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.stop();
            }
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION, true);
    }

    public void setAnimationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ANIMATION, enabled).apply();
    }

    public boolean isAnimationEnabled() {
        return prefs.getBoolean(KEY_ANIMATION, true);
    }


    public void setHapticEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply();
    }
    public boolean isHapticEnabled() {
        return prefs.getBoolean(KEY_HAPTIC, true);
    }

    public void setArcadeDifficulty(int difficulty) {
        prefs.edit().putInt(KEY_ARCADE_DIFFICULTY, difficulty).apply();
    }
    public int getArcadeDifficulty() {
        return prefs.getInt(KEY_ARCADE_DIFFICULTY, 0);
    }

    public void setNbQuestions(int nbQuestions) {
        prefs.edit().putInt(KEY_NB_QUESTIONS, nbQuestions).apply();
    }
    public int getNbQuestions() {
        return prefs.getInt(KEY_NB_QUESTIONS, 0);
    }


    // ========================= Online Stats =========================

    public String getOnlinePseudo() {
        return prefs.getString(KEY_ONLINE_PSEUDO, "");
    }

    public void setOnlinePseudo(String pseudo) {
        prefs.edit().putString(KEY_ONLINE_PSEUDO, pseudo).apply();
    }

    public String getOnlineUid() {
        return prefs.getString(KEY_ONLINE_UID, "");
    }

    public void setOnlineUid(String uid) {
        prefs.edit().putString(KEY_ONLINE_UID, uid).apply();
    }


    public int getOnlineScore() {
        return prefs.getInt(KEY_ONLINE_SCORE, 0);
    }

    public void setOnlineScore(int points) {
        prefs.edit().putInt(KEY_ONLINE_SCORE, points).apply();
    }

    public int getOnlinePlayedMatches() {
        return prefs.getInt(KEY_ONLINE_PLAYED_MATCHES, 0);
    }

    public void setOnlinePlayedMatches(int matches) {
        prefs.edit().putInt(KEY_ONLINE_PLAYED_MATCHES, matches).apply();
    }
    public int getOnlineWins() {
        return prefs.getInt(KEY_ONLINE_WINS, 0);
    }
    public void setOnlineWins(int wins) {
        prefs.edit().putInt(KEY_ONLINE_WINS, wins).apply();
    }

    public int getOnlineLosses() {
        return prefs.getInt(KEY_ONLINE_LOSSES, 0);
    }

    public void setOnlineLosses(int losses) {
        prefs.edit().putInt(KEY_ONLINE_LOSSES, losses).apply();
    }

    public int getOnlineDraws() {
        return prefs.getInt(KEY_ONLINE_DRAWS, 0);
    }

    public void setOnlineDraws(int draws) {
        prefs.edit().putInt(KEY_ONLINE_DRAWS, draws).apply();
    }

    public String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    public void syncOnlineData(DatabaseReference playerRef) {

        prefs.edit()
                .putString(KEY_LAST_CONNECTION, getTodayDate())
                .apply();

        playerRef.get().addOnSuccessListener(snapshot -> {

            int dailyPlayed = snapshot.child(KEY_DAILY_MATCH_PLAYED)
                    .getValue(Integer.class) != null
                    ? snapshot.child(KEY_DAILY_MATCH_PLAYED).getValue(Integer.class)
                    : 0;

            int dailyLimit = snapshot.child(KEY_DAILY_MATCH_LIMIT)
                    .getValue(Integer.class) != null
                    ? snapshot.child(KEY_DAILY_MATCH_LIMIT).getValue(Integer.class)
                    : 0;

            prefs.edit()
                    .putInt(KEY_DAILY_MATCH_PLAYED, dailyPlayed)
                    .putInt(KEY_DAILY_MATCH_LIMIT, dailyLimit)
                    .apply();
        });
    }


    public String getLastConnection() {
        return prefs.getString(KEY_LAST_CONNECTION, "");
    }
    public void setLastConnection(String date) {
        prefs.edit().putString(KEY_LAST_CONNECTION, date).apply();
    }

    public int getDailyMatchPlayed() {
        return prefs.getInt(KEY_DAILY_MATCH_PLAYED, 0);
    }
    public void setDailyMatchPlayed(int played) {
        prefs.edit().putInt(KEY_DAILY_MATCH_PLAYED, played).apply();
    }
    public void incrementDailyMatchPlayed() {
        prefs.edit().putInt(KEY_DAILY_MATCH_PLAYED, getDailyMatchPlayed() + 1).apply();
    }

    public int getDailyMatchLimit() {
        return prefs.getInt(KEY_DAILY_MATCH_LIMIT, 0);
    }
    public void setDailyMatchLimit(int limit) {
        prefs.edit().putInt(KEY_DAILY_MATCH_LIMIT, limit).apply();
    }

    public void setRank(int rank) {
        prefs.edit().putInt("rank", rank).apply();
    }
    public int getRank() {
        return prefs.getInt("rank", 999999);
    }

}
