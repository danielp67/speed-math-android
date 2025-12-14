package fr.accentweb.speedMath.core;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.RawRes;

public class FeedbackManager {

    private final Context context;
    private final PlayerManager playerManager;

    private SoundPool soundPool;
    private int soundCorrect, soundWrong, soundLevelUp;

    public FeedbackManager(Context context) {
        this.context = context;
        this.playerManager = PlayerManager.getInstance(context);
        initSoundPool();
    }

    private void initSoundPool() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();
    }

    public void loadSounds(@RawRes int correct, @RawRes int wrong, @RawRes int levelUp) {
        soundCorrect = soundPool.load(context, correct, 1);
        soundWrong   = soundPool.load(context, wrong, 1);
        soundLevelUp = soundPool.load(context, levelUp, 1);
    }

    public void playCorrectSound() {
        playSound(soundCorrect);
    }

    public void playWrongSound() {
        playSound(soundWrong);
    }

    public void playLevelUpSound() {
        playSound(soundLevelUp);
    }

    private void playSound(int soundId) {
        if (playerManager.isSoundEnabled() && soundPool != null) {
            soundPool.play(soundId, 0.75f, 0.75f, 1, 0, 1f);
        }
    }

    // Feedback général pour réponse correcte
    public void correctFeedback(View v) {
        if (playerManager.isHapticEnabled()) {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else if (playerManager.isVibrationEnabled()) {
            vibrate(50);
        }
    }

    // Feedback général pour réponse incorrecte
    public void wrongFeedback(View v) {
        if (playerManager.isHapticEnabled()) {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else if (playerManager.isVibrationEnabled()) {
            vibrate(100);
        }
    }

    private void vibrate(int durationMs) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        }
    }

    // Libération du SoundPool quand on n’en a plus besoin
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
