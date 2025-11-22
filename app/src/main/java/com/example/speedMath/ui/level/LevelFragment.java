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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.R;

import java.util.Random;

public class LevelFragment extends Fragment {

    private TextView textQuestion, textResult, textScoreRight, textTimer;
    private EditText inputAnswer;
    private Button[] numberButtons = new Button[10];
    private Button buttonCancel, buttonClear, buttonValidate;
    private ProgressBar progressBar;

    private String gameMode;
    private int gameLevel, gameDifficulty, gameRequiredCorrect;
    private int correctAnswer;
    private int score = 0;
    private long elapsedMillis = 0;
    private boolean timerRunning = false;

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

        buttonValidate = root.findViewById(R.id.btn_validate);
        buttonCancel = root.findViewById(R.id.btn_cancel);
        buttonClear = root.findViewById(R.id.btn_correct);

        // Désactive le clavier Android par défaut
        inputAnswer.setShowSoftInputOnFocus(false);

        // Initialisation des boutons numériques
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("btn" + i, "id", getActivity().getPackageName());
            numberButtons[i] = root.findViewById(resID);
            int finalI = i;
            numberButtons[i].setOnClickListener(v -> inputAnswer.append(String.valueOf(finalI)));
        }

        // Bouton Corriger (Clear)
        buttonClear.setOnClickListener(v -> inputAnswer.setText(""));

        // Bouton Stop
        buttonCancel.setOnClickListener(v -> getActivity().onBackPressed());

        // Bouton Valider
        buttonValidate.setOnClickListener(v -> checkAnswer());

        countUpTimer = new CountUpTimer();
        countUpTimer.start();
        // Générer première question et afficher score
        score = 0;
        updateScore();
        generateQuestion();

        return root;
    }

    private void generateQuestion() {
        Random r = new Random();
        int a = r.nextInt(20)* gameDifficulty + 1;
        int b = r.nextInt(20)* gameDifficulty + 1;
        char op;

        switch (gameMode) {
            case "ADD":
                op = '+';
                correctAnswer = a + b;
                break;
            case "SUB":
                op = '-';
                correctAnswer = a - b;
                break;
            case "MUL":
                op = '×';
                correctAnswer = a * b;
                break;
            case "DIV":
                b = r.nextInt(19) + 1;
                correctAnswer = a / b;
                a = correctAnswer * b;
                op = '÷';
                break;
            default: // ALL
                int rand = r.nextInt(4);
                if (rand == 0) { op = '+'; correctAnswer = a + b; }
                else if (rand == 1) { op = '-'; correctAnswer = a - b; }
                else if (rand == 2) { op = '×'; correctAnswer = a * b; }
                else {
                    b = r.nextInt(19) + 1;
                    correctAnswer = a / b;
                    a = correctAnswer * b;
                    op = '÷';
                }
                break;
        }

        textQuestion.setText(a + " " + op + " " + b + " ?");
        inputAnswer.setText("");
        textResult.setText("");
    }

    private void checkAnswer() {
        String userInput = inputAnswer.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        if (userAnswer == gameRequiredCorrect) {
            textResult.setText("✔ Correct !");
            score++;
        } else {
            textResult.setText("✘ Wrong (" + correctAnswer + ")");
            score++;
        }

        updateScore();
        if (score >= gameRequiredCorrect) {
            // Niveau terminé
            buttonValidate.setText("🎉 Niveau terminé !");
            buttonValidate.setEnabled(false);
            countUpTimer.stop();

            buttonValidate.setEnabled(true);

            buttonValidate.setOnClickListener(v -> {
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
    private class CountUpTimer {
        private boolean running = false;

        public void start() {
            running = true;
            timerRunning = true;
            new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (running) {
                        elapsedMillis += 1000;
                        requireActivity().runOnUiThread(() ->
                                textTimer.setText(LevelFragment.this.formatTime(elapsedMillis))
                        );
                    }
                }
            }).start();
        }

        public void stop() {
            running = false;
            timerRunning = false;
        }
    }


    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / 1000) / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
