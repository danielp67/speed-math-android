package fr.accentweb.speedMath.core;

import android.os.Handler;
import android.os.Looper;
import java.util.Random;

public class LocalBotManager {
    private static final double MEAN_DELAY_MS = 3500.0;  // Délai moyen
    private static final double STD_DEV_MS = 1500.0;    // Écart-type
    private static final int MIN_DELAY_MS = 1000;       // Délai minimum
    private static final int MAX_DELAY_MS = 10000;       // Délai maximum

    private int botScore = 0;
    private boolean running = false;
    private int playerScore = 0;
    private final String matchId;
    private final String botScoreField;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int nbQuestions = 7;

    public interface BotListener {
        void onBotScoreUpdated(int score);
        void onBotWin();
    }

    public LocalBotManager(String matchId, String playerId) {
        this.matchId = matchId;
        this.botScoreField = playerId.equals("P1") ? "p2_score" : "p1_score";
    }

    public void start(BotListener listener) {
        if (running) return;
        running = true;
        scheduleNextTick(listener);
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }

    public int getBotScore() {
        return botScore;
    }

    private void scheduleNextTick(BotListener listener) {
        if (!running) return;

        // Délai gaussien pour un comportement réaliste
        double delay = MEAN_DELAY_MS + random.nextGaussian() * STD_DEV_MS;
        int delayMs = (int) Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, delay));

        // Ajuste le délai si le bot est en retard
        if (botScore + 2 < playerScore) {
            delayMs = Math.max(1000, delayMs - 1000); // Réduit le délai
        }

        handler.postDelayed(() -> {
            if (!running) return;

            botScore++;
            listener.onBotScoreUpdated(botScore);

            // Vérifie si le bot a gagné
            if (botScore >= nbQuestions) {
                listener.onBotWin();
            } else {
                scheduleNextTick(listener);
            }
        }, delayMs);
    }
}
