package fr.accentweb.speedMath.ui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private int speedKmh = 40;
    private final int winGoal = 10;
    private boolean isGameOver = false;
    private boolean isDaily = false;
    private int screenWidth;
    private final List<View> activeObstacles = new ArrayList<>();
    
    private static final int LANE_COUNT = 4; 
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

        isDaily = getArguments() != null && getArguments().getBoolean("IS_DAILY", false);

        gameContainer = view.findViewById(R.id.gameContainer);
        txtScore = view.findViewById(R.id.txtScore);
        txtLife = view.findViewById(R.id.txtLife);
        txtExpression = view.findViewById(R.id.txtTargetNumber);
        gameOverOverlay = view.findViewById(R.id.gameOverOverlay);
        btnRetry = view.findViewById(R.id.btnRetry);
        dangerLine = view.findViewById(R.id.dangerLine);
        imgKart = view.findViewById(R.id.imgShip);
        
        imgKart.setImageResource(R.drawable.gamepad_solid_full); 
        imgKart.setColorFilter(Color.parseColor("#FF4444"));

        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        laneWidth = screenWidth / LANE_COUNT;

        addRoadMarkings();

        questionGenerator = new QuestionGenerator(0, 2, true, true, true, false, false, true);
        
        gameContainer.setOnTouchListener((v, event) -> {
            if (isGameOver) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                float x = event.getX();
                int targetLane = (int) (x / laneWidth);
                if (targetLane >= LANE_COUNT) targetLane = LANE_COUNT - 1;
                moveKart(targetLane);
            }
            return true;
        });

        btnRetry.setOnClickListener(v -> restartGame());
        
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int h = gameContainer.getHeight();
                if (h > 0 && dangerLine.getY() > h / 2f) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (isAdded() && !isGameOver) {
                        startGame();
                    }
                }
            }
        });
    }

    private void addRoadMarkings() {
        if (!isAdded()) return;
        for (int i = 1; i < LANE_COUNT; i++) {
            View line = new View(requireContext());
            line.setBackgroundColor(Color.parseColor("#22FFFFFF"));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(4, ViewGroup.LayoutParams.MATCH_PARENT);
            line.setLayoutParams(lp);
            line.setX(i * laneWidth);
            gameContainer.addView(line, 0);
        }
    }

    private void startGame() {
        score = 0; lives = 3; speedKmh = 40; isGameOver = false; currentLane = 1;
        gameOverOverlay.setVisibility(View.GONE);
        updateUI();
        imgKart.post(() -> {
            if (imgKart.getWidth() > 0) moveKart(currentLane);
        });
        startSpawning();
    }

    private void restartGame() {
        gameHandler.removeCallbacksAndMessages(null);
        clearActiveObstacles();
        startGame();
    }
    
    private void clearActiveObstacles() {
        for (View v : new ArrayList<>(activeObstacles)) {
            v.animate().cancel();
            if (v.getParent() != null) gameContainer.removeView(v);
        }
        activeObstacles.clear();
    }

    private void moveKart(int lane) {
        currentLane = lane;
        float targetX = lane * laneWidth + laneWidth / 2f - imgKart.getWidth() / 2f;
        imgKart.animate().x(targetX).setDuration(120).start();
    }

    private void updateUI() {
        if (!isAdded()) return;
        if (isDaily) {
            txtScore.setText(getString(R.string.arcade_energy, score, winGoal));
        } else {
            txtScore.setText(getString(R.string.game_score_format, score) + " | " + speedKmh + " km/h");
        }
        StringBuilder l = new StringBuilder();
        for (int i = 0; i < lives; i++) l.append("❤️");
        txtLife.setText(l.toString());
        playerManager.setCorrectAnswersStreak("KART", 0, score);
    }

    private void startSpawning() {
        if (isGameOver || !isAdded()) return;
        if (activeObstacles.isEmpty()) {
            spawnGates();
        }
        gameHandler.postDelayed(this::startSpawning, 100);
    }

    private void spawnGates() {
        if (isGameOver || !isAdded()) return;

        final float targetY = dangerLine.getY();
        if (targetY <= 0) return;

        questionGenerator.setLevel(score * 2);
        currentQuestion = questionGenerator.generateQuestion();
        txtExpression.setText(currentQuestion.expression.replace(" = ?", ""));
        
        List<View> wave = new ArrayList<>();
        List<Integer> choices = currentQuestion.answersChoice;
        
        int correctIdx = -1;
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i) == currentQuestion.answer) {
                correctIdx = i;
                break;
            }
        }
        final int finalCorrectLane = correctIdx;
        final boolean[] waveProcessed = {false};

        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.textPrimary, typedValue, true);
        int textColor = typedValue.data;

        for (int i = 0; i < LANE_COUNT; i++) {
            if (!isAdded()) return;
            TextView gate = new TextView(requireContext());
            gate.setText(String.valueOf(choices.get(i)));
            gate.setTextColor(textColor);
            gate.setTextSize(22);
            gate.setGravity(android.view.Gravity.CENTER);
            gate.setBackgroundResource(R.drawable.arcade_card_bg);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(laneWidth - 30, 140);
            gate.setLayoutParams(lp);
            gate.setX(i * laneWidth + 15);
            gate.setY(-400); // Start higher off-screen

            gameContainer.addView(gate);
            activeObstacles.add(gate);
            wave.add(gate);

            // Faster initial speed: base 4200ms
            long duration = Math.max(1000, 7000 - (speedKmh * 30L));

            gate.animate()
                    .y(targetY)
                    .setDuration(duration)
                    .setInterpolator(new LinearInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (isGameOver || !isAdded() || waveProcessed[0] || gameContainer.indexOfChild(gate) == -1) return;
                            
                            waveProcessed[0] = true;
                            if (currentLane == finalCorrectLane) {
                                score++;
                                speedKmh += 5;
                                feedbackManager.playCorrectSound();
                                updateUI();
                                if (isDaily && score >= winGoal) {
                                    winDaily();
                                }
                            } else {
                                crash();
                            }
                            removeWave(wave);
                        }
                    });
        }
    }

    private void winDaily() {
        isGameOver = true;
        gameHandler.removeCallbacksAndMessages(null);
        playerManager.setDailyChallengeWaitingClaim(true);
        if (!isAdded()) return;
        Bundle bundle = new Bundle();
        bundle.putBoolean("SUCCESS", true);
        getParentFragmentManager().setFragmentResult("daily_result", bundle);
        feedbackManager.playLevelUpSound();
        Toast.makeText(getContext(), "DAILY CHALLENGE SUCCESS!", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) requireActivity().onBackPressed();
        }, 1500);
    }

    private void removeWave(List<View> wave) {
        for (View v : wave) {
            v.animate().setListener(null);
            v.animate().cancel();
            if (v.getParent() != null) gameContainer.removeView(v);
            activeObstacles.remove(v);
        }
    }

    private void crash() {
        lives--;
        speedKmh = Math.max(40, speedKmh - 10);
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
        clearActiveObstacles();
    }
}
