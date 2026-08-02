package fr.accentweb.speedMath.ui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;

public class MathKartFragment extends BaseGameFragment {

    private FrameLayout gameContainer;
    private TextView txtScore, txtLife, txtExpression;
    private LinearLayout gameOverOverlay;
    private Button btnRetry;
    private ImageView imgKart;
    private View dangerLine;

    private PlayerManager playerManager;
    private FeedbackManager feedbackManager;
    private QuestionGenerator questionGenerator;
    private final Random random = new Random();
    private final Handler gameHandler = new Handler(Looper.getMainLooper());
    
    private int score = 0;
    private int lives = 3;
    private int speedKmh = 50;
    private boolean isGameOver = false;
    private int screenWidth;
    private final List<View> activeObstacles = new ArrayList<>();
    
    private static final int LANE_COUNT = 5;
    private int laneWidth;
    private int currentLane = 1; 

    private QuestionGenerator.MathQuestion currentQuestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerManager = PlayerManager.getInstance(requireContext());
        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        gameContainer = view.findViewById(R.id.gameContainer);
        txtScore = view.findViewById(R.id.txtScore);
        txtLife = view.findViewById(R.id.txtLife);
        txtExpression = view.findViewById(R.id.txtTargetNumber);
        gameOverOverlay = view.findViewById(R.id.gameOverOverlay);
        btnRetry = view.findViewById(R.id.btnRetry);
        dangerLine = view.findViewById(R.id.dangerLine);
        imgKart = view.findViewById(R.id.imgShip);
        
        imgKart.setImageResource(R.drawable.gamepad_solid_full); 
        imgKart.setColorFilter(Color.RED);

        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        laneWidth = screenWidth / LANE_COUNT;

        addRoadMarkings();

        questionGenerator = new QuestionGenerator(0, 2, false, true, true, false, false, true);
        
        gameContainer.setOnTouchListener((v, event) -> {
            if (isGameOver) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                float x = event.getX();
                if (x < laneWidth) moveKart(0);
                else if (x < laneWidth * 2) moveKart(1);
                else moveKart(2);
            }
            return true;
        });

        btnRetry.setOnClickListener(v -> restartGame());
        startGame();
    }

    private void addRoadMarkings() {
        for (int i = 1; i < LANE_COUNT; i++) {
            View line = new View(requireContext());
            line.setBackgroundColor(Color.parseColor("#33FFFFFF"));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(4, ViewGroup.LayoutParams.MATCH_PARENT);
            line.setLayoutParams(lp);
            line.setX(i * laneWidth);
            gameContainer.addView(line, 0);
        }
    }

    private void startGame() {
        score = 0; lives = 3; speedKmh = 50; isGameOver = false; currentLane = 1;
        gameOverOverlay.setVisibility(View.GONE);
        updateUI();
        imgKart.post(() -> moveKart(currentLane));
        startSpawning();
    }

    private void restartGame() {
        for (View v : new ArrayList<>(activeObstacles)) gameContainer.removeView(v);
        activeObstacles.clear();
        startGame();
    }

    private void moveKart(int lane) {
        currentLane = lane;
        float targetX = lane * laneWidth + (laneWidth / 2f) - (imgKart.getWidth() / 2f);
        imgKart.animate().x(targetX).setDuration(150).start();
    }

    private void updateUI() {
        txtScore.setText("Laps: " + score + " | " + speedKmh + " km/h");
        StringBuilder l = new StringBuilder();
        for (int i = 0; i < lives; i++) l.append("❤️");
        txtLife.setText(l.toString());
    }

    private void startSpawning() {
        if (isGameOver) return;
        spawnGates();
        long delay = Math.max(1500, 5000 - (speedKmh * 10L));
        gameHandler.postDelayed(this::startSpawning, delay);
    }

    private void spawnGates() {
        if (isGameOver) return;

        questionGenerator.setLevel(score * 2);
        currentQuestion = questionGenerator.generateQuestion();
        txtExpression.setText(currentQuestion.expression.replace(" = ?", ""));
        
        int correctLane = random.nextInt(LANE_COUNT);
        List<View> wave = new ArrayList<>();

        for (int i = 0; i < LANE_COUNT; i++) {
            TextView gate = new TextView(requireContext());
            boolean isCorrect = (i == correctLane);
            long val;
            
            if (isCorrect) {
                val = currentQuestion.answer;
            } else {
                val = currentQuestion.answer + (random.nextBoolean() ? 1 : -1) * (random.nextInt(5) + 1);
            }
            
            gate.setText(String.valueOf(val));
            gate.setTextColor(Color.WHITE);
            gate.setTextSize(24);
            gate.setGravity(android.view.Gravity.CENTER);
            gate.setBackgroundResource(R.drawable.answer_card_bg);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(laneWidth - 40, 150);
            gate.setLayoutParams(lp);
            gate.setX(i * laneWidth + 20);
            gate.setY(-200);

            gameContainer.addView(gate);
            activeObstacles.add(gate);
            wave.add(gate);
            
            long duration = Math.max(1200, 8000 - (speedKmh * 30L));
            int laneIdx = i;

            gate.animate()
                .translationY(dangerLine.getY())
                .setDuration(duration)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isGameOver || gameContainer.indexOfChild(gate) == -1) return;
                        
                        if (laneIdx == currentLane) {
                            if (isCorrect) {
                                score++;
                                speedKmh += 2;
                                playerManager.setCorrectAnswersStreak("KART", 0, score);
                                feedbackManager.playCorrectSound();
                                updateUI();
                            } else {
                                crash();
                            }
                        } else if (isCorrect) {
                            crash();
                        }
                        
                        removeWave(wave);
                    }
                }).start();
        }
    }

    private void removeWave(List<View> wave) {
        for (View v : wave) {
            gameContainer.removeView(v);
            activeObstacles.remove(v);
        }
    }

    private void crash() {
        lives--;
        speedKmh = Math.max(50, speedKmh - 10);
        feedbackManager.playWrongSound();
        updateUI();
        
        imgKart.animate().rotationBy(360).setDuration(400).start();
        
        if (lives <= 0) endGame();
    }

    private void endGame() {
        isGameOver = true;
        gameHandler.removeCallbacksAndMessages(null);
        gameOverOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gameHandler.removeCallbacksAndMessages(null);
    }
}
