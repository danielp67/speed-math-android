package fr.accentweb.speedMath.ui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
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

public class MathTetrisFragment extends BaseGameFragment {

    private FrameLayout gameContainer;
    private TextView txtTargetNumber, txtScore, txtLife;
    private LinearLayout gameOverOverlay;
    private Button btnRetry;
    private View dangerLine;

    private PlayerManager playerManager;
    private FeedbackManager feedbackManager;
    private final Random random = new Random();
    private final Handler gameHandler = new Handler(Looper.getMainLooper());
    
    private int score = 0;
    private int lives = 3;
    private int currentSum = 0;
    private int targetNumber;
    private boolean isGameOver = false;
    private int screenWidth;
    private final List<View> activeBlocks = new ArrayList<>();

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
        txtTargetNumber = view.findViewById(R.id.txtTargetNumber);
        txtScore = view.findViewById(R.id.txtScore);
        txtLife = view.findViewById(R.id.txtLife);
        gameOverOverlay = view.findViewById(R.id.gameOverOverlay);
        btnRetry = view.findViewById(R.id.btnRetry);
        dangerLine = view.findViewById(R.id.dangerLine);
        view.findViewById(R.id.imgShip).setVisibility(View.GONE);

        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        btnRetry.setOnClickListener(v -> restartGame());
        startGame();
    }

    private void startGame() {
        score = 0; lives = 3; isGameOver = false; currentSum = 0;
        gameOverOverlay.setVisibility(View.GONE);
        setNextTarget();
        updateUI();
        startSpawning();
    }

    private void restartGame() {
        for (View v : new ArrayList<>(activeBlocks)) gameContainer.removeView(v);
        activeBlocks.clear();
        startGame();
    }

    private void setNextTarget() {
        targetNumber = 10 + random.nextInt(20);
        txtTargetNumber.setText(String.valueOf(targetNumber));
        currentSum = 0;
    }

    private void updateUI() {
        txtScore.setText("Sum: " + currentSum + " (Score: " + score + ")");
        StringBuilder l = new StringBuilder();
        for (int i = 0; i < lives; i++) l.append("❤️");
        txtLife.setText(l.toString());
    }

    private void startSpawning() {
        if (isGameOver) return;
        spawnBlock();
        gameHandler.postDelayed(this::startSpawning, Math.max(1000, 3000 - (score * 50)));
    }

    private void spawnBlock() {
        if (isGameOver) return;
        int value = 1 + random.nextInt(9);
        TextView block = new TextView(requireContext());
        block.setText(String.valueOf(value));
        block.setTextColor(Color.WHITE);
        block.setTextSize(24);
        block.setPadding(30, 30, 30, 30);
        block.setGravity(android.view.Gravity.CENTER);
        block.setBackgroundResource(R.drawable.answer_card_bg);
        block.setTag(value);

        int blockSize = 150;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(blockSize, blockSize);
        block.setLayoutParams(lp);
        block.setX(random.nextInt(Math.max(1, screenWidth - blockSize)));
        block.setY(-blockSize);

        gameContainer.addView(block);
        activeBlocks.add(block);
        block.setOnClickListener(v -> handleBlockClick((TextView) v));

        ObjectAnimator anim = ObjectAnimator.ofFloat(block, "translationY", dangerLine.getY() - blockSize);
        anim.setDuration(Math.max(2000, 8000 - (score * 100)));
        anim.setInterpolator(new LinearInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                if (!isGameOver && gameContainer.indexOfChild(block) != -1) {
                    loseLife();
                    removeBlock(block);
                }
            }
        });
        anim.start();
    }

    private void handleBlockClick(TextView block) {
        if (isGameOver) return;
        int val = (int) block.getTag();
        currentSum += val;
        removeBlock(block);

        if (currentSum == targetNumber) {
            score++;
            feedbackManager.playCorrectSound();
            setNextTarget();
        } else if (currentSum > targetNumber) {
            loseLife();
            currentSum = 0;
        }
        updateUI();
    }

    private void loseLife() {
        lives--; feedbackManager.playWrongSound();
        if (lives <= 0) endGame();
    }

    private void removeBlock(View v) { gameContainer.removeView(v); activeBlocks.remove(v); }
    private void endGame() { isGameOver = true; gameHandler.removeCallbacksAndMessages(null); gameOverOverlay.setVisibility(View.VISIBLE); }

    @Override
    public void onDestroyView() { super.onDestroyView(); gameHandler.removeCallbacksAndMessages(null); }
}
