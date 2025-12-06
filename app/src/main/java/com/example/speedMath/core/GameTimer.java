package com.example.speedMath.core;

import android.os.Handler;
import android.os.Looper;

public class GameTimer {

    public interface TimerListener {
        void onTick(long elapsedMillis, String formatted);
    }

    private TimerListener listener;
    private long elapsedMillis = 0;
    private boolean running = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            elapsedMillis += 100;
            if (listener != null) listener.onTick(elapsedMillis, formatTime(elapsedMillis));

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

    public long getElapsedMillis() {
        return elapsedMillis;
    }
    public void reset() {
        elapsedMillis = 0;
    }

    public void setListener(TimerListener l) {
        this.listener = l;
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000);
        int milliseconds = (int) (millis % 1000) / 100; // centièmes (2 chiffres)
        return String.format("%02d.%2d s", seconds, milliseconds);
    }
}
