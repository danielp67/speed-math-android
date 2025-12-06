package com.example.speedMath.core;

import android.os.Handler;
import android.os.Looper;

public class GameTimer {

    public interface TimerListener {
        void onTick(long elapsedMillis, String formatted);
    }

    private TimerListener listener;
    private Runnable finishListener;
    private long elapsedMillis = 0;
    private boolean running = false;

    private long maxDurationMillis = -1; // durée max optionnelle (-1 = infini)

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            elapsedMillis += 100;
            if (listener != null) listener.onTick(elapsedMillis, formatTime(elapsedMillis));

            // Vérifie si on a atteint la durée max
            if (maxDurationMillis > 0 && elapsedMillis >= maxDurationMillis) {
                running = false;
                if (finishListener != null) finishListener.run();
                return;
            }

            handler.postDelayed(this, 100);
        }
    };

    public void start() {
        if (running) return;
        running = true;
        handler.post(tickRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
    }

    public void reset() {
        elapsedMillis = 0;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public void setListener(TimerListener l) {
        this.listener = l;
    }

    public void setFinishListener(Runnable r) {
        this.finishListener = r;
    }

    public void setMaxDurationMillis(long maxMillis) {
        this.maxDurationMillis = maxMillis;
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000);
        int milliseconds = (int) (millis % 1000) / 100; // centièmes (2 chiffres)
        return String.format("%02d.%2d s", seconds, milliseconds);
    }
}
