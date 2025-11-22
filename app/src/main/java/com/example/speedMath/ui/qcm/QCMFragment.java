package com.example.speedMath.ui.qcm;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.speedMath.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class QCMFragment extends Fragment {

    private TextView textQuestion, textResult;
    private CardView card1, card2, card3, card4;
    private TextView t1, t2, t3, t4;

    private int correctAnswer;
    private Random random = new Random();

    public QCMFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_qcm, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // UI references
        textQuestion = view.findViewById(R.id.textQuestion);
        textResult = view.findViewById(R.id.textResult);

        card1 = view.findViewById(R.id.cardOption1);
        card2 = view.findViewById(R.id.cardOption2);
        card3 = view.findViewById(R.id.cardOption3);
        card4 = view.findViewById(R.id.cardOption4);

        t1 = view.findViewById(R.id.textOption1);
        t2 = view.findViewById(R.id.textOption2);
        t3 = view.findViewById(R.id.textOption3);
        t4 = view.findViewById(R.id.textOption4);

        // Click handlers
        card1.setOnClickListener(v -> checkAnswer(t1));
        card2.setOnClickListener(v -> checkAnswer(t2));
        card3.setOnClickListener(v -> checkAnswer(t3));
        card4.setOnClickListener(v -> checkAnswer(t4));

        generateQuestion();
    }


    // Generate question + answers
    private void generateQuestion() {

        // Reset result text
        textResult.setText("");

        // Reset card colors
        resetCardColors();

        // Simple math question
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;

        correctAnswer = a + b;
        textQuestion.setText(a + " + " + b + " = ?");

        // Generate wrong answers
        ArrayList<Integer> answers = new ArrayList<>();
        answers.add(correctAnswer);

        while (answers.size() < 4) {
            int wrong = random.nextInt(20) + 1;
            if (wrong != correctAnswer && !answers.contains(wrong))
                answers.add(wrong);
        }

        // Shuffle display order
        Collections.shuffle(answers);

        t1.setText(String.valueOf(answers.get(0)));
        t2.setText(String.valueOf(answers.get(1)));
        t3.setText(String.valueOf(answers.get(2)));
        t4.setText(String.valueOf(answers.get(3)));
    }


    // Handle user click
    private void checkAnswer(TextView selected) {

        int value = Integer.parseInt(selected.getText().toString());

        if (value == correctAnswer) {
            textResult.setText("Bonne réponse !");
            textResult.setTextColor(Color.parseColor("#2ecc71")); // vert
            highlightCorrect(selected);
        } else {
            textResult.setText("Mauvaise réponse");
            textResult.setTextColor(Color.parseColor("#e74c3c")); // rouge
            highlightWrong(selected);
            highlightCorrectAnswer();
        }

        // Load next question after 1 sec
        selected.postDelayed(this::generateQuestion, 1000);
    }


    // UI helpers
    private void highlightCorrect(TextView view) {
        ((CardView)view.getParent()).setCardBackgroundColor(Color.parseColor("#A5D6A7"));
    }

    private void highlightWrong(TextView view) {
        ((CardView)view.getParent()).setCardBackgroundColor(Color.parseColor("#FFCDD2"));
    }

    private void highlightCorrectAnswer() {
        if (Integer.parseInt(t1.getText().toString()) == correctAnswer)
            card1.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
        if (Integer.parseInt(t2.getText().toString()) == correctAnswer)
            card2.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
        if (Integer.parseInt(t3.getText().toString()) == correctAnswer)
            card3.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
        if (Integer.parseInt(t4.getText().toString()) == correctAnswer)
            card4.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
    }

    private void resetCardColors() {
        card1.setCardBackgroundColor(Color.WHITE);
        card2.setCardBackgroundColor(Color.WHITE);
        card3.setCardBackgroundColor(Color.WHITE);
        card4.setCardBackgroundColor(Color.WHITE);
    }
}
