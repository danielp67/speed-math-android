package com.example.speedMath.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.speedMath.R;

import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView textQuestion, textResult;
    private EditText inputAnswer;
    private Button[] numberButtons = new Button[10];
    private Button buttonCancel, buttonClear, buttonValidate;

    private int correctAnswer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        inputAnswer = root.findViewById(R.id.inputAnswer);
        buttonValidate = root.findViewById(R.id.btn_validate);
        buttonCancel = root.findViewById(R.id.btn_cancel);
        buttonClear = root.findViewById(R.id.btn_correct);

        // Désactiver le clavier Android par défaut
        //inputAnswer.setShowSoftInputOnFocus(false);
/*        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            inputAnswer.setShowSoftInputOnFocus(false);
        } else {
            inputAnswer.setInputType(android.text.InputType.TYPE_NULL);
        }*/

        // Initialisation des boutons numériques (0 à 9)
        for (int i = 0; i <= 9; i++) {
            int resID = getResources().getIdentifier("btn" + i, "id", getActivity().getPackageName());
            numberButtons[i] = root.findViewById(resID);
            int finalI = i;
            numberButtons[i].setOnClickListener(v -> inputAnswer.append(String.valueOf(finalI)));
        }

        // Bouton Corriger (Clear)
        buttonClear.setOnClickListener(v -> inputAnswer.setText(""));

        // Bouton Stop
        buttonCancel.setOnClickListener(v -> getActivity().finish());

        // Text Result
        textResult.setText("");

        generateQuestion();

        buttonValidate.setOnClickListener(v -> checkAnswer());

        return root;
    }

    private void generateQuestion() {
        Random r = new Random();
        int a = r.nextInt(20) + 1;
        int b = r.nextInt(20) + 1;
        correctAnswer = a + b;

        textQuestion.setText(a + " + " + b + " = ?");
        inputAnswer.setText("");
    }

    private void checkAnswer() {
        String userInput = inputAnswer.getText().toString().trim();
        if (userInput.isEmpty()) return;

        int userAnswer = Integer.parseInt(userInput);

        if (userAnswer == correctAnswer) {
            textResult.setText("✔ Correct !");

        } else {
            textResult.setText("✘ Wrong (" + correctAnswer + ")");
        }

        generateQuestion();
    }
}
