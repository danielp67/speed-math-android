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
    private Button buttonValidate;

    private int correctAnswer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        textQuestion = root.findViewById(R.id.textQuestion);
        textResult = root.findViewById(R.id.textResult);
        inputAnswer = root.findViewById(R.id.inputAnswer);
        buttonValidate = root.findViewById(R.id.buttonValidate);

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
        textResult.setText("");
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
