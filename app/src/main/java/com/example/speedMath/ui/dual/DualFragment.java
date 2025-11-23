package com.example.speedMath.ui.dual;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.speedMath.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DualFragment extends Fragment {

    // ================= PLAYER 1 =================
    private TextView p1Question, p1Result;
    private ArrayList<CardView> p1Cards = new ArrayList<>();
    private ArrayList<TextView> p1Texts = new ArrayList<>();
    private int p1CorrectAnswer;

    // ================= PLAYER 2 =================
    private TextView p2Question, p2Result;
    private ArrayList<CardView> p2Cards = new ArrayList<>();
    private ArrayList<TextView> p2Texts = new ArrayList<>();
    private int p2CorrectAnswer;

    private Random random = new Random();

    public DualFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // ====================== INIT PLAYER 1 ======================
        View p1 = v.findViewById(R.id.viewPlayer1);
        p1Question = p1.findViewById(R.id.textQuestion);
        p1Result = p1.findViewById(R.id.textResult);

        p1Cards.add(p1.findViewById(R.id.cardOption1));
        p1Cards.add(p1.findViewById(R.id.cardOption2));
        p1Cards.add(p1.findViewById(R.id.cardOption3));
        p1Cards.add(p1.findViewById(R.id.cardOption4));

        p1Texts.add(p1.findViewById(R.id.textOption1));
        p1Texts.add(p1.findViewById(R.id.textOption2));
        p1Texts.add(p1.findViewById(R.id.textOption3));
        p1Texts.add(p1.findViewById(R.id.textOption4));

        // Click listeners
        for (int i = 0; i < 4; i++) {
            final int index = i;
            p1Cards.get(i).setOnClickListener(view ->
                    checkAnswerPlayer1(p1Texts.get(index)));
        }

        // ====================== INIT PLAYER 2 ======================
        View p2 = v.findViewById(R.id.viewPlayer2);
        p2Question = p2.findViewById(R.id.textQuestion);
        p2Result = p2.findViewById(R.id.textResult);

        p2Cards.add(p2.findViewById(R.id.cardOption1));
        p2Cards.add(p2.findViewById(R.id.cardOption2));
        p2Cards.add(p2.findViewById(R.id.cardOption3));
        p2Cards.add(p2.findViewById(R.id.cardOption4));

        p2Texts.add(p2.findViewById(R.id.textOption1));
        p2Texts.add(p2.findViewById(R.id.textOption2));
        p2Texts.add(p2.findViewById(R.id.textOption3));
        p2Texts.add(p2.findViewById(R.id.textOption4));

        for (int i = 0; i < 4; i++) {
            final int index = i;
            p2Cards.get(i).setOnClickListener(view ->
                    checkAnswerPlayer2(p2Texts.get(index)));
        }

        // First questions
        generateQuestionPlayer1();
        generateQuestionPlayer2();
    }

    // =====================================================
    // PLAYER 1 : Question
    // =====================================================
    private void generateQuestionPlayer1() {
        resetCardColors(p1Cards);

        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        p1CorrectAnswer = a + b;

        p1Question.setText(a + " + " + b + " = ?");

        ArrayList<Integer> options = new ArrayList<>();
        options.add(p1CorrectAnswer);
        options.add(p1CorrectAnswer + 1);
        options.add(p1CorrectAnswer - 1);
        options.add(p1CorrectAnswer + 2);
        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            p1Texts.get(i).setText(String.valueOf(options.get(i)));
        }
    }

    // =====================================================
    // PLAYER 2 : Question
    // =====================================================
    private void generateQuestionPlayer2() {
        resetCardColors(p2Cards);

        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        p2CorrectAnswer = a + b;

        p2Question.setText(a + " + " + b + " = ?");

        ArrayList<Integer> options = new ArrayList<>();
        options.add(p2CorrectAnswer);
        options.add(p2CorrectAnswer + 1);
        options.add(p2CorrectAnswer - 1);
        options.add(p2CorrectAnswer + 2);
        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            p2Texts.get(i).setText(String.valueOf(options.get(i)));
        }
    }

    // =====================================================
    // PLAYER 1 : Answer Check
    // =====================================================
    private void checkAnswerPlayer1(TextView selected) {
        int value = Integer.parseInt(selected.getText().toString());

        if (value == p1CorrectAnswer) {
            p1Result.setText("✅");
            p1Result.setTextColor(Color.parseColor("#2ecc71"));
            highlightCorrect(selected);
        } else {
            p1Result.setText("❌");
            p1Result.setTextColor(Color.parseColor("#e74c3c"));
            highlightWrong(selected);
            highlightCorrectAnswer(p1Texts, p1Cards, p1CorrectAnswer);
        }

        selected.postDelayed(this::generateQuestionPlayer1, 1000);
    }

    // =====================================================
    // PLAYER 2 : Answer Check
    // =====================================================
    private void checkAnswerPlayer2(TextView selected) {
        int value = Integer.parseInt(selected.getText().toString());

        if (value == p2CorrectAnswer) {
            p2Result.setText("✅");
            p2Result.setTextColor(Color.parseColor("#2ecc71"));
            highlightCorrect(selected);
        } else {
            p2Result.setText("❌");
            p2Result.setTextColor(Color.parseColor("#e74c3c"));
            highlightWrong(selected);
            highlightCorrectAnswer(p2Texts, p2Cards, p2CorrectAnswer);
        }

        selected.postDelayed(this::generateQuestionPlayer2, 1000);
    }

    // =====================================================
    // HIGHLIGHT HELPERS
    // =====================================================
    private void highlightCorrect(TextView view) {
        CardView card = (CardView) view.getParent();
        card.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
    }

    private void highlightWrong(TextView view) {
        CardView card = (CardView) view.getParent();
        card.setCardBackgroundColor(Color.parseColor("#FFCDD2"));
    }

    private void highlightCorrectAnswer(
            ArrayList<TextView> answers,
            ArrayList<CardView> cards,
            int correctAnswer) {

        for (int i = 0; i < 4; i++) {
            int val = Integer.parseInt(answers.get(i).getText().toString());
            if (val == correctAnswer) {
                cards.get(i).setCardBackgroundColor(Color.parseColor("#A5D6A7"));
            }
        }
    }

    private void resetCardColors(ArrayList<CardView> cards) {
        for (CardView c : cards) {
            c.setCardBackgroundColor(Color.WHITE);
        }
    }
}
