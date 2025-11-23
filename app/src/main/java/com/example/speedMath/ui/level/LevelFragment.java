package com.example.speedMath.ui.level;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.QuestionGenerator;

import java.util.Random;

public class LevelFragment extends Fragment {

    private TextView textQuestion, textResult, textScoreRight, textTimer;
    private EditText inputAnswer;
    private CardView[] cards = new CardView[10];
    private CardView cardCancel, cardClear, cardValidate;
    private TextView[] texts = new TextView[10];
    private TextView textCancel, textClear, textValidate;
    private ProgressBar progressBar;

    private String gameMode;
    private int gameLevel, gameDifficulty, gameRequiredCorrect;
    private int correctAnswer;
    private int score = 0;
    private long elapsedMillis = 0;
    private boolean timerRunning = false;

    private QuestionGenerator questionGenerator;

    private CountUpTimer countUpTimer;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_level, container, false);

        // Récupère les params choisis
        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";
        gameLevel = getArguments() != null ? getArguments().getInt("LEVEL") : 1;
        gameRequiredCorrect = getArguments() != null ? getArguments().getInt("REQUIRED_CORRECT") : 5;
        gameDifficulty = getArguments() != null ? getArguments().getInt("DIFFICULTY") : 1;

        // Initialisation des vues
        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        inputAnswer = root.findViewById(R.id.inputAnswer);
        textScoreRight = root.findViewById(R.id.textScoreRight);
        progressBar = root.findViewById(R.id.progressScore);
        textTimer = root.findViewById(R.id.textTimer);


        // Désactive le clavier Android par défaut
        inputAnswer.setShowSoftInputOnFocus(false);


        // Boutons numériques 0-9
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("card" + i, "id", getActivity().getPackageName());
            cards[i] = root.findViewById(resID);
            texts[i] = cards[i].findViewById(R.id.textButton);
            texts[i].setText(i + "");
            int finalI = i;
            cards[i].setOnClickListener(v -> inputAnswer.append(String.valueOf(finalI)));
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

        cardClear.setOnClickListener(v -> inputAnswer.setText(""));
        cardCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardValidate.setOnClickListener(v -> checkAnswer());

        countUpTimer = new CountUpTimer();
        countUpTimer.start();
        // Générer première question et afficher score
        score = 0;
        updateScore();
        // ----- Initialisation du QuestionGenerator -----
        boolean allowAdd = gameMode.equals("ADD") || gameMode.equals("ALL");
        boolean allowSub = gameMode.equals("SUB") || gameMode.equals("ALL");
        boolean allowMul = gameMode.equals("MUL") || gameMode.equals("ALL");
        boolean allowDiv = gameMode.equals("DIV") || gameMode.equals("ALL");
        questionGenerator = new QuestionGenerator(
                gameDifficulty,      // difficulty par défaut
                2,      // nombre d'opérandes
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

        // Génération via QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();

        textQuestion.setText(q.expression + " ?");
        correctAnswer = q.answer;
        inputAnswer.setText("");
    }

    private void checkAnswer() {
        String userInput = inputAnswer.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        if (userAnswer == gameRequiredCorrect) {
            textResult.setText("✔ Good !");
            score++;
        } else {
            textResult.setText("✘  Wrong (" + correctAnswer + ")");
            score++;
        }

        updateScore();
        if (score >= gameRequiredCorrect) {
            // Niveau terminé
            textValidate.setText("🎉 Level completed !");
            countUpTimer.stopTimer();
            textValidate.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_dashboard);
            });;
        } else {
            generateQuestion();
        }
    }

    private void updateScore() {
        if (textScoreRight != null && progressBar != null) {
            textScoreRight.setText(score + "/" + gameRequiredCorrect);
            progressBar.setMax(gameRequiredCorrect);
            progressBar.setProgress(score);
        }
    }


    // Timer simple pour compter le temps écoulé
    private class CountUpTimer extends Thread {
        private boolean running = true;
        private long elapsedMillis = 0;

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(1000);
                    elapsedMillis += 1000;

                    // Vérification que le fragment est toujours attaché
                    if (!isAdded() || getActivity() == null) continue;

                    getActivity().runOnUiThread(() -> {
                        if (textTimer != null) {
                            textTimer.setText(formatTime(elapsedMillis));
                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopTimer() {
            running = false;
        }
    }


    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / 1000) / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
