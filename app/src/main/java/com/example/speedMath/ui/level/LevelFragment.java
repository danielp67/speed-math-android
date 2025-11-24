package com.example.speedMath.ui.level;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.speedMath.core.StatsManager;

public class LevelFragment extends Fragment {

    private TextView textQuestion, textResult, textScoreRight, textTimer, textValidate;
    private CardView[] cards = new CardView[10];
    private CardView cardCancel, cardClear, cardValidateCard;
    private TextView[] texts = new TextView[10];
    private TextView textCancel, textClear;
    private ProgressBar progressBar;

    private String gameMode;
    private int gameLevel, gameDifficulty, gameRequiredCorrect;
    private int correctAnswer;
    private int score = 0;
    private long elapsedMillis = 0;

    private QuestionGenerator questionGenerator;
    private CountUpTimer countUpTimer;
    private StatsManager statsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level, container, false);

        // Stats manager
        statsManager = new StatsManager(requireContext());

        // Paramètres
        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";
        gameLevel = getArguments() != null ? getArguments().getInt("LEVEL") : 1;
        gameRequiredCorrect = getArguments() != null ? getArguments().getInt("REQUIRED_CORRECT") : 5;
        gameDifficulty = getArguments() != null ? getArguments().getInt("DIFFICULTY") : 1;

        // Vues
        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        textResult.setText("");
        textScoreRight = root.findViewById(R.id.textScoreRight);
        textTimer = root.findViewById(R.id.textTimer);
        progressBar = root.findViewById(R.id.progressScore);

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
                gameDifficulty, 2, false,
                allowAdd, allowSub, allowMul, allowDiv, true
        );
        generateQuestion();

        return root;
    }

    private void generateQuestion() {
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        textQuestion.setText(q.expression);
        correctAnswer = q.answer;
    }

    private void checkAnswer() {
        String userInput = textResult.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        boolean isCorrect = userAnswer == correctAnswer;

        flashBorder(textResult, isCorrect);

        // Ajout aux stats
        statsManager.addAnswer(isCorrect);

        if (isCorrect) score++;
        updateScore();

        if (score >= gameRequiredCorrect) {
            levelCompleted();
        } else {
            generateQuestion();
        }
    }

    private void levelCompleted() {
        textValidate.setText("🎉 Level completed !");
        textValidate.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
        countUpTimer.stopTimer();
        statsManager.setCurrentLevel(gameLevel);
        statsManager.setLevelTime(gameLevel, elapsedMillis);

        textValidate.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_dashboard);
        });
    }

    private void updateScore() {
        if (textScoreRight != null && progressBar != null) {
            textScoreRight.setText(score + "/" + gameRequiredCorrect);
            progressBar.setMax(gameRequiredCorrect);
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
                    Thread.sleep(1000);
                    elapsedMillis += 1000;
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
        int milliseconds = (int) (millis % 1000) / 10; // centièmes (2 chiffres)

        return String.format("%02d.%02d", seconds, milliseconds);
    }

}
