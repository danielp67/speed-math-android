package fr.accentweb.speedMath.ui.daily;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;

public class DailyGameFragment extends BaseGameFragment {

    private FrameLayout gameContainer;
    private TextView txtTargetNumber, txtScore, txtLife;
    private LinearLayout gameOverOverlay;
    private Button btnRetry;
    private View dangerLine;
    private ImageView imgShip;

    private PlayerManager playerManager;
    private FeedbackManager feedbackManager;
    private QuestionGenerator questionGenerator;
    private final Random random = new Random();
    private final Handler spawnHandler = new Handler(Looper.getMainLooper());
    
    private int score = 0;
    private int lives = 3;
    private int targetNumber;
    private final int winGoal = 10;
    private boolean isGameOver = false;
    private int screenWidth;
    private final List<View> activeInvaders = new ArrayList<>();

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
        imgShip = view.findViewById(R.id.imgShip);

        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        questionGenerator = new QuestionGenerator(playerManager.getCurrentLevel() + 5, 2, false, true, true, true, false, true);
        btnRetry.setOnClickListener(v -> restartGame());
        startGame();
    }

    private void startGame() {
        score = 0; lives = 3; isGameOver = false;
        gameOverOverlay.setVisibility(View.GONE);
        updateUI();
        setNextTarget();
        startSpawning();
    }

    private void restartGame() {
        for (View v : new ArrayList<>(activeInvaders)) gameContainer.removeView(v);
        activeInvaders.clear();
        startGame();
    }

    private void setNextTarget() {
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        targetNumber = q.answer;
        txtTargetNumber.setText(String.valueOf(targetNumber));
    }

    private void updateUI() {
        txtScore.setText(getString(R.string.arcade_energy, score, winGoal));
        StringBuilder l = new StringBuilder();
        for (int i = 0; i < lives; i++) l.append("❤️");
        txtLife.setText(l.toString());
    }

    private void startSpawning() {
        if (isGameOver) return;
        spawnInvader();
        long delay = Math.max(800, 2500 - (score * 150));
        spawnHandler.postDelayed(this::startSpawning, delay);
    }

    private void spawnInvader() {
        if (isGameOver) return;
        boolean isCorrect = random.nextInt(3) == 0;
        String expr; int res;
        if (isCorrect) {
            int a = random.nextInt(Math.max(1, targetNumber));
            expr = a + " + " + (targetNumber - a);
            res = targetNumber;
        } else {
            QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
            expr = q.expression.replace(" = ?", "");
            res = q.answer;
            if (res == targetNumber) res++;
        }

        TextView invader = new TextView(requireContext());
        invader.setText(expr);
        invader.setTextColor(Color.WHITE);
        invader.setTextSize(20);
        invader.setPadding(35, 15, 35, 15);
        invader.setGravity(android.view.Gravity.CENTER);
        invader.setBackgroundResource(R.drawable.answer_card_bg);
        invader.setTag(res);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        invader.setLayoutParams(lp);
        invader.setX(random.nextInt(Math.max(1, screenWidth - 250)));
        invader.setY(-200);

        gameContainer.addView(invader);
        activeInvaders.add(invader);
        invader.setOnClickListener(v -> handleShoot((TextView) v));

        ObjectAnimator anim = ObjectAnimator.ofFloat(invader, "translationY", dangerLine.getY());
        anim.setDuration(Math.max(1500, 6000 - (score * 350)));
        anim.setInterpolator(new LinearInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                if (!isGameOver && gameContainer.indexOfChild(invader) != -1) {
                    if ((int)invader.getTag() == targetNumber) loseLife();
                    removeInvader(invader);
                }
            }
        });
        anim.start();
    }

    private void handleShoot(TextView invader) {
        if (isGameOver) return;
        imgShip.animate().x(invader.getX() + invader.getWidth()/2f - imgShip.getWidth()/2f).setDuration(100).start();
        if ((int) invader.getTag() == targetNumber) {
            score++; feedbackManager.playCorrectSound();
            invader.animate().scaleX(2.5f).scaleY(0.1f).alpha(0f).setDuration(150).withEndAction(() -> removeInvader(invader)).start();
            setNextTarget();
            if (score >= winGoal) winGame();
        } else {
            loseLife();
            invader.setBackgroundColor(Color.RED);
            invader.animate().translationYBy(-100).alpha(0).setDuration(200).withEndAction(() -> removeInvader(invader)).start();
        }
        updateUI();
    }

    private void loseLife() {
        lives--; feedbackManager.playWrongSound();
        ObjectAnimator shake = ObjectAnimator.ofFloat(gameContainer, "translationX", 0, 30);
        shake.setDuration(40);
        shake.setRepeatCount(5);
        shake.setRepeatMode(ObjectAnimator.REVERSE);
        shake.start();
        if (lives <= 0) endGame();
    }

    private void removeInvader(View v) { gameContainer.removeView(v); activeInvaders.remove(v); }
    private void endGame() { isGameOver = true; spawnHandler.removeCallbacksAndMessages(null); gameOverOverlay.setVisibility(View.VISIBLE); }

    private void winGame() {
        isGameOver = true;
        spawnHandler.removeCallbacksAndMessages(null);
        playerManager.setDailyChallengeWaitingClaim(true);
        Bundle bundle = new Bundle();
        bundle.putBoolean("SUCCESS", true);
        getParentFragmentManager().setFragmentResult("daily_result",bundle);
        feedbackManager.playLevelUpSound();
        Toast.makeText(getContext(), "You win ! Claim your reward !", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> { if(isAdded()) requireActivity().onBackPressed(); }, 1500);
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); spawnHandler.removeCallbacksAndMessages(null); }
}
