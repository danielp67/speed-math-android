package com.example.speedMath.ui.level;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.MainActivity;
import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.FeedbackManager;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.ScoreManager;
import com.example.speedMath.utils.AnimUtils;

public class LevelFragment extends BaseGameFragment {

    private TextView textQuestion, textResult, textScoreRight, textTimer, textValidate, textHighScore;
    private final CardView[] cards = new CardView[10];
    private CardView cardCancel, cardClear, cardValidateCard;
    private TextView[] texts = new TextView[10];
    private TextView textCancel, textClear;
    private ProgressBar progressBar;

    private String gameMode;
    private int gameLevel, gameDifficulty, questionNbr, correctAnswerNbr;
    private long targetScore;
    private int correctAnswer;
    private int score = 0;
    private long elapsedMillis = 0;

    private QuestionGenerator questionGenerator;
    private PlayerManager playerManager;

    private ScoreManager scoreManager;
    private TextView textCombo;
    private int combo = 0;

    private GameTimer gameTimer;

    private FeedbackManager feedbackManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level, container, false);

        // Paramètres
        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";
        gameLevel = getArguments() != null ? getArguments().getInt("LEVEL") : 1;
        targetScore = getArguments() != null ? getArguments().getInt("TARGET_SCORE") : 5;
        gameDifficulty = getArguments() != null ? getArguments().getInt("DIFFICULTY") : 1;

        // Player manager
        playerManager = PlayerManager.getInstance(requireContext());
        playerManager.setLastPlayedLevel(gameLevel);

        // Feedback manager
        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        // Score manager
        scoreManager = new ScoreManager(gameLevel);

        // Vues
        textHighScore = root.findViewById(R.id.textHighScore);
        textHighScore.setText("High score : " + playerManager.getLevelHighScore(gameLevel) + " pts");
        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        textResult.setText("");
        textScoreRight = root.findViewById(R.id.textScoreRight);
        textTimer = root.findViewById(R.id.textTimer);
        progressBar = root.findViewById(R.id.progressScore);
        progressBar.setMax(100 * gameLevel);
        textCombo = root.findViewById(R.id.textCombo);

        // Boutons numériques
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("card" + i, "id", getActivity().getPackageName());
            cards[i] = root.findViewById(resID);
            texts[i] = cards[i].findViewById(R.id.textButton);
            texts[i].setText(String.valueOf(i));
            int finalI = i;
            cards[i].setOnClickListener(v -> textResult.append(String.valueOf(finalI)));
        }

        // Boutons fonction
        cardCancel = root.findViewById(R.id.cardX);
        cardClear = root.findViewById(R.id.cardC);
        cardValidateCard = root.findViewById(R.id.cardOK);

        textCancel = cardCancel.findViewById(R.id.textButton);
        textClear = cardClear.findViewById(R.id.textButton);
        textValidate = cardValidateCard.findViewById(R.id.textButton);

        textCancel.setText("X");
        textClear.setText("C");
        textValidate.setText("OK");

        cardClear.setOnClickListener(v -> textResult.setText(""));
        cardCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardValidateCard.setOnClickListener(v -> checkAnswer());

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });
        gameTimer.start();


        // Score initial
        score = 0;
        updateScore();

        // Question generator
        boolean allowAdd = gameMode.equals("ADD") || gameMode.equals("ALL");
        boolean allowSub = gameMode.equals("SUB") || gameMode.equals("ALL");
        boolean allowMul = gameMode.equals("MUL") || gameMode.equals("ALL");
        boolean allowDiv = gameMode.equals("DIV") || gameMode.equals("ALL");

        questionGenerator = new QuestionGenerator(
                gameLevel, 2, false,
                allowAdd, allowSub, allowMul, allowDiv, true
        );
        generateQuestion();

        return root;
    }

    private void generateQuestion() {
        if(playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(textQuestion);
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        questionNbr++;
        textQuestion.setText(q.expression);
        correctAnswer = q.answer;
    }

    private void checkAnswer() {
        String userInput = textResult.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        boolean isCorrect = userAnswer == correctAnswer;

        flashBorder(textResult, isCorrect);

        if (isCorrect)
        {
            combo++;
            if (combo >= 2 && playerManager.isAnimationEnabled()) { // combo commence à 2
                textCombo.setText("🔥 COMBO x" + combo + " !");
                AnimUtils.comboPop(textCombo);
            }
            correctAnswerNbr++;
            feedbackManager.playCorrectSound();
            feedbackManager.correctFeedback(textValidate);

        }
        else
        {
            combo = 0;
            textCombo.setAlpha(0);
            feedbackManager.playWrongSound();
            feedbackManager.wrongFeedback(textValidate);
        }
        score =  (int) scoreManager.setScore(isCorrect, elapsedMillis);
        updateScore();



        if (score >= 100 * gameLevel) {
            levelCompleted();
        } else {
            generateQuestion();
        }
    }

    private void levelCompleted() {
        feedbackManager.playLevelUpSound();
        feedbackManager.correctFeedback(textValidate);
        textValidate.setText("🎉 Level completed !");
        textValidate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold_accent));
        textValidate.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));

        if (gameTimer != null) gameTimer.stop();
        score = (int) scoreManager.setFinalBonus(questionNbr, correctAnswerNbr, elapsedMillis);
        updateScore();

        playerManager.setCurrentLevel(gameLevel);
        playerManager.setLevelHighScore(gameLevel, score);

        textValidate.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_dashboard);
        });
    }

    private void updateScore() {
        if (textScoreRight != null && progressBar != null) {
            textScoreRight.setText(score + " pts" );
            progressBar.setProgress(score);
        }
    }

    private void flashBorder(TextView view, boolean isCorrect) {
        Drawable originalBackground = view.getBackground();
        int borderColor = isCorrect ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(8, borderColor);

        view.setBackground(gd);

        // Après 0,5s → retour à la normale
        new Handler().postDelayed(() -> {
            view.setBackground(originalBackground);
            view.setText("");
        }, 500);
    }

}
