package com.example.speedMath.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;

import java.util.Random;

public class GameFragment extends Fragment {

    private TextView textQuestion, textResult;
    private EditText inputAnswer;
    private Button[] numberButtons = new Button[10];
    private Button buttonCancel, buttonClear, buttonValidate;

    private String gameMode;
    private int correctAnswer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_game, container, false);

        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";

        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        inputAnswer = root.findViewById(R.id.inputAnswer);
        buttonValidate = root.findViewById(R.id.btn_validate);
        buttonCancel = root.findViewById(R.id.btn_cancel);
        buttonClear = root.findViewById(R.id.btn_correct);

        inputAnswer.setShowSoftInputOnFocus(false);

        // Boutons numériques 0-9
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("btn" + i, "id", getActivity().getPackageName());
            numberButtons[i] = root.findViewById(resID);
            int finalI = i;
            numberButtons[i].setOnClickListener(v -> inputAnswer.append(String.valueOf(finalI)));
        }

        buttonClear.setOnClickListener(v -> inputAnswer.setText(""));
        buttonCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        buttonValidate.setOnClickListener(v -> checkAnswer());

        generateQuestion();

        return root;
    }

    private void generateQuestion() {
        Random r = new Random();
        int a = r.nextInt(20) + 1;
        int b = r.nextInt(20) + 1;
        char op;

        switch (gameMode) {
            case "ADD": op = '+'; correctAnswer = a + b; break;
            case "SUB": op = '-'; correctAnswer = a - b; break;
            case "MUL": op = '×'; correctAnswer = a * b; break;
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
     //   textResult.setText("");
    }

    private void checkAnswer() {
        String userInput = inputAnswer.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        if (userAnswer == correctAnswer) textResult.setText("✔ Correct !");
        else textResult.setText("✘ Wrong (" + correctAnswer + ")");

        generateQuestion();
    }
}
