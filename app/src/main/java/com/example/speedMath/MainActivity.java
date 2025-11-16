package com.example.speedMath;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView textQuestion, textResult;
    private EditText inputAnswer;
    private Button buttonValidate;

    private int correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textQuestion = findViewById(R.id.textQuestion);
        textResult = findViewById(R.id.textResult);
        inputAnswer = findViewById(R.id.inputAnswer);
        buttonValidate = findViewById(R.id.buttonValidate);

        generateQuestion();

        buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = inputAnswer.getText().toString();

                if (value.isEmpty()) {
                    textResult.setText("?");
                    return;
                }

                int answer = Integer.parseInt(value);

                if (answer == correctAnswer) {
                    textResult.setText("✔️");
                } else {
                    textResult.setText("❌");
                }

                inputAnswer.setText("");
                generateQuestion();
            }
        });
    }

    private void generateQuestion() {
        Random r = new Random();
        int a = r.nextInt(20) + 1;
        int b = r.nextInt(20) + 1;

        String[] ops = {"+", "-", "×", "÷"};
        String op = ops[r.nextInt(ops.length)];

        switch (op) {
            case "+":
                correctAnswer = a + b;
                break;
            case "-":
                correctAnswer = a - b;
                break;
            case "×":
                correctAnswer = a * b;
                break;
            case "÷":
                if (b != 0) correctAnswer = a / b;
                else correctAnswer = 0;
                break;
        }

        textQuestion.setText(a + " " + op + " " + b + " = ?");
    }
}
