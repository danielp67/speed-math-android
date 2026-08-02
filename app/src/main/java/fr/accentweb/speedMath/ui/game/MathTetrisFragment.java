package fr.accentweb.speedMath.ui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
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

public class MathTetrisFragment extends BaseGameFragment {

    private static final int COLS = 5;
    private static final int MAX_ROWS = 8;
    
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
    private int currentSum = 0;
    private int targetNumber;
    private boolean isGameOver = false;
    private int blockWidth;
    private int blockHeight;
    
    private final List<List<TextView>> columns = new ArrayList<>();
    private final List<TextView> selectedBlocks = new ArrayList<>();

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
        blockWidth = metrics.widthPixels / COLS;
        blockHeight = blockWidth; // Square blocks

        for (int i = 0; i < COLS; i++) columns.add(new ArrayList<>());

        btnRetry.setOnClickListener(v -> restartGame());
        startGame();
    }

    private void startGame() {
        score = 0; isGameOver = false; currentSum = 0;
        gameOverOverlay.setVisibility(View.GONE);
        setNextTarget();
        updateUI();
        startSpawning();
    }

    private void restartGame() {
        for (List<TextView> col : columns) {
            for (TextView tv : col) gameContainer.removeView(tv);
            col.clear();
        }
        selectedBlocks.clear();
        startGame();
    }

    private void setNextTarget() {
        targetNumber = 10 + random.nextInt(15);
        txtTargetNumber.setText(String.valueOf(targetNumber));
        currentSum = 0;
        clearSelection();
    }

    private void updateUI() {
        txtScore.setText(getString(R.string.tetris_sum_format, currentSum));
        txtLife.setText(getString(R.string.game_score_format, score));
        
        // Save score as streak
        playerManager.setCorrectAnswersStreak("TETRIS", 0, score);
    }

    private void startSpawning() {
        if (isGameOver) return;
        spawnBlock();
        long delay = Math.max(1200, 3500 - (score * 100L));
        gameHandler.postDelayed(this::startSpawning, delay);
    }

    private void spawnBlock() {
        if (isGameOver) return;
        
        int colIndex = random.nextInt(COLS);
        List<TextView> col = columns.get(colIndex);
        
        if (col.size() >= MAX_ROWS) {
            endGame();
            return;
        }

        int value = 1 + random.nextInt(9);
        TextView block = new TextView(requireContext());
        block.setText(String.valueOf(value));
        block.setTextColor(Color.WHITE);
        block.setTextSize(22);
        block.setGravity(android.view.Gravity.CENTER);
        block.setBackgroundResource(R.drawable.answer_card_bg);
        block.setTag(value);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(blockWidth - 4, blockHeight - 4);
        block.setLayoutParams(lp);
        block.setX(colIndex * blockWidth + 2);
        block.setY(-blockHeight);

        gameContainer.addView(block);
        col.add(block);
        block.setOnClickListener(v -> handleBlockClick((TextView) v));

        float targetY = dangerLine.getY() - (col.size() * blockHeight);
        block.animate()
                .translationY(targetY)
                .setDuration(Math.max(1000, 4000 - (score * 150L)))
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    private void handleBlockClick(TextView block) {
        if (isGameOver || selectedBlocks.contains(block)) return;

        int val = (int) block.getTag();
        currentSum += val;
        selectedBlocks.add(block);
        block.setBackgroundColor(Color.parseColor("#FFD700")); // Gold highlight
        block.setTextColor(Color.BLACK);

        if (currentSum == targetNumber) {
            score++;
            feedbackManager.playCorrectSound();
            clearSelectedBlocks();
            setNextTarget();
        } else if (currentSum > targetNumber) {
            feedbackManager.playWrongSound();
            clearSelection();
        }
        updateUI();
    }

    private void clearSelection() {
        for (TextView b : selectedBlocks) {
            b.setBackgroundResource(R.drawable.answer_card_bg);
            b.setTextColor(Color.WHITE);
        }
        selectedBlocks.clear();
        currentSum = 0;
    }

    private void clearSelectedBlocks() {
        for (TextView block : selectedBlocks) {
            int foundCol = -1;
            for (int i = 0; i < COLS; i++) {
                if (columns.get(i).remove(block)) {
                    foundCol = i;
                    break;
                }
            }
            gameContainer.removeView(block);
            if (foundCol != -1) shiftColumnDown(foundCol);
        }
        selectedBlocks.clear();
        currentSum = 0;
    }

    private void shiftColumnDown(int colIndex) {
        List<TextView> col = columns.get(colIndex);
        for (int i = 0; i < col.size(); i++) {
            TextView b = col.get(i);
            float newY = dangerLine.getY() - ((i + 1) * blockHeight);
            b.animate().translationY(newY).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void endGame() {
        isGameOver = true;
        gameHandler.removeCallbacksAndMessages(null);
        gameOverOverlay.setVisibility(View.VISIBLE);
        feedbackManager.playWrongSound();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gameHandler.removeCallbacksAndMessages(null);
    }
}
