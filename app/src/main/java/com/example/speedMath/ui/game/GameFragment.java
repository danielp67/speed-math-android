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
import com.example.speedMath.core.QuestionGenerator;

public class GameFragment extends Fragment {

    private TextView textQuestion, textResult;
    private EditText inputAnswer;
    private Button[] numberButtons = new Button[10];
    private Button buttonCancel, buttonClear, buttonValidate;

    private String gameMode;
    private int correctAnswer;

    private QuestionGenerator questionGenerator;

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

        // ----- Initialisation du QuestionGenerator -----
        boolean allowAdd = gameMode.equals("ADD") || gameMode.equals("ALL");
        boolean allowSub = gameMode.equals("SUB") || gameMode.equals("ALL");
        boolean allowMul = gameMode.equals("MUL") || gameMode.equals("ALL");
        boolean allowDiv = gameMode.equals("DIV") || gameMode.equals("ALL");

        questionGenerator = new QuestionGenerator(
                1,      // difficulty par défaut
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

        if (userAnswer == correctAnswer) textResult.setText("✔ Correct !");
        else textResult.setText("✘ Wrong (" + correctAnswer + ")");

        generateQuestion();
    }
}
