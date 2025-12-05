package com.example.speedMath.ui.game;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.MainActivity;
import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.utils.AnimUtils;

public class GameFragment extends BaseGameFragment {

    private TextView textQuestion, textResult, textTimer, textScoreRight, textLevelNumber;
    private CardView[] cards = new CardView[10];
    private CardView cardCancel, cardClear, cardValidate;
    private TextView[] texts = new TextView[10];
    private TextView textCancel, textClear, textValidate;
    private String gameMode;
    private long elapsedMillis = 0;
    private int score = 0;

    private long correctAnswer;
    private int correctAnswersStreak, lastPlayedLevel, arcadeDifficulty;
    private PlayerManager playerManager;
    private GameTimer gameTimer;

    private QuestionGenerator questionGenerator;

    private SoundPool soundPool;
    private int soundCorrect, soundWrong, soundLevelUp;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_game, container, false);

        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";
        playerManager = PlayerManager.getInstance(requireContext());
        arcadeDifficulty = playerManager.getArcadeDifficulty();

        correctAnswersStreak = playerManager.getCorrectAnswersStreak(gameMode);
        lastPlayedLevel = playerManager.getLastPlayedLevel();

        textTimer = root.findViewById(R.id.textTimer);
        textScoreRight = root.findViewById(R.id.textScoreRight);
        textQuestion = root.findViewById(R.id.textQuestion);
        textLevelNumber = root.findViewById(R.id.textLevelNumber);
        textLevelNumber.setText(" ⭐⭐⭐");
        textResult = root.findViewById(R.id.textResult);
        textResult.setText("");


        // Boutons numériques 0-9
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("card" + i, "id", getActivity().getPackageName());
            cards[i] = root.findViewById(resID);
            texts[i] = cards[i].findViewById(R.id.textButton);
            texts[i].setText(i + "");
            int finalI = i;
            cards[i].setOnClickListener(v -> textResult.append(String.valueOf(finalI)));
        }

        // Boutons clavier
        cardCancel = root.findViewById(R.id.cardX);
        cardClear = root.findViewById(R.id.cardC);
        cardValidate = root.findViewById(R.id.cardOK);

        textCancel = cardCancel.findViewById(R.id.textButton);
        textClear = cardClear.findViewById(R.id.textButton);
        textValidate = cardValidate.findViewById(R.id.textButton);

        textCancel.setText("X");
        textClear.setText("C");
        textValidate.setText("OK");

        cardClear.setOnClickListener(v -> textResult.setText(""));
        cardCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardValidate.setOnClickListener(v -> checkAnswer());


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
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });
        gameTimer.start();

        // Score initial
        score = 0;
        updateScore();

        // ----- Initialisation du QuestionGenerator -----
        boolean allowAdd = gameMode.equals("ADD") || gameMode.equals("ALL");
        boolean allowSub = gameMode.equals("SUB") || gameMode.equals("ALL");
        boolean allowMul = gameMode.equals("MUL") || gameMode.equals("ALL");
        boolean allowDiv = gameMode.equals("DIV") || gameMode.equals("ALL");

        questionGenerator = new QuestionGenerator(
                arcadeDifficulty * 50,      // difficulty par défaut
                2 ,      // nombre d'opérandes
                false,  // pas de QCM ici
                allowAdd,
                allowSub,
                allowMul,
                allowDiv,
                true
        );

        generateQuestion();

        return root;
    }

    private void generateQuestion() {
        if(playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(textQuestion);

        if (arcadeDifficulty == 0) {
            questionGenerator.setLevel(score);
        }else {
            questionGenerator.setLevel(arcadeDifficulty*50);
        }
        // Génération via QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();

        textQuestion.setText(q.expression);
        correctAnswer = q.answer;
    }

    private void checkAnswer() {
        String userInput = textResult.getText().toString().trim();
        if (userInput.isEmpty()) return;

        long userAnswer = Long.parseLong(userInput);

        boolean isCorrect = userAnswer == correctAnswer;

        flashBorder(textResult, isCorrect);

        if (isCorrect)
        {   score++;
            playSound(soundCorrect);
            triggerCorrectFeedback(textValidate);
        } else {
            score = 0;
            elapsedMillis = 0;
            playSound(soundWrong);
            triggerWrongFeedback(textValidate);
        }
        updateScore();

        generateQuestion();

    }

    private void updateScore() {
        if (textScoreRight != null) {
            playerManager.setCorrectAnswersStreak(gameMode, score);
            textScoreRight.setText(score + "/" + playerManager.getCorrectAnswersStreak(gameMode));
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
