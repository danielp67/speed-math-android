package com.example.speedMath.ui.level;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
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

import com.example.speedMath.R;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.ScoreManager;

public class LevelFragment extends Fragment {

    private TextView textQuestion, textResult, textScoreRight, textTimer, textValidate, textHighScore;
    private CardView[] cards = new CardView[10];
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
    private CountUpTimer countUpTimer;
    private PlayerManager playerManager;

    private ScoreManager scoreManager;
    private SoundPool soundPool;
    private int soundCorrect, soundWrong, soundLevelUp;

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

        // Création du SoundPool
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(attrs)
                    .build();


        // Chargement des sons
        soundCorrect = soundPool.load(getContext(), R.raw.correct, 1);
        soundWrong   = soundPool.load(getContext(), R.raw.wrong, 1);
        soundLevelUp = soundPool.load(getContext(), R.raw.levelup, 1);


        // Timer
        countUpTimer = new CountUpTimer();
        countUpTimer.start();

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
            correctAnswerNbr++;
            playSound(soundCorrect);
            triggerCorrectFeedback(textValidate); // answerButton = bouton cliqué
        }
        else
        {
            playSound(soundWrong);
            triggerWrongFeedback(textValidate);
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
        playSound(soundLevelUp);
        triggerCorrectFeedback(textValidate);
        textValidate.setText("🎉 Level completed !");
        textValidate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold_accent));
        textValidate.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
        countUpTimer.stopTimer();
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

    private class CountUpTimer extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(100);
                    elapsedMillis += 100;
                    if (!isAdded() || getActivity() == null) continue;
                    getActivity().runOnUiThread(() -> textTimer.setText(formatTime(elapsedMillis)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopTimer() { running = false; }
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000);
        int milliseconds = (int) (millis % 1000) / 100; // centièmes (2 chiffres)

        return String.format("%02d.%2d s", seconds, milliseconds);
    }

    private void playSound(int soundId) {
        if (playerManager.isSoundEnabled() && soundPool != null) {
            soundPool.play(soundId, 0.75f, 0.75f, 1, 0, 1f);
        }
    }

    // Appeler quand réponse correcte
    private void triggerCorrectFeedback(View v) {
        if (playerManager.isHapticEnabled()) {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else if (playerManager.isVibrationEnabled()) {
            vibrate(50); // 50ms vibration
        }
    }

    // Appeler quand réponse incorrecte
    private void triggerWrongFeedback(View v) {
        if (playerManager.isHapticEnabled()) {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else if (playerManager.isVibrationEnabled()) {
            vibrate(100); // 100ms vibration
        }
    }

    private void vibrate(int durationMs) {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        }
    }

}
