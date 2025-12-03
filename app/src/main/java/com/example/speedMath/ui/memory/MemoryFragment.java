package com.example.speedMath.ui.memory;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.speedMath.MainActivity;
import com.example.speedMath.R;
import com.example.speedMath.core.Card;
import com.example.speedMath.core.QuestionGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryFragment extends Fragment {

    private GridLayout grid;
    private TextView textMoves, textTimer;
    private List<Card> cards;
    private List<Button> buttons;
    private Card firstCard = null, secondCard = null;
    private int firstIndex = -1, secondIndex = -1;
    private int moves = 0;
    private long elapsedTime = 0;
    private Handler timerHandler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime += 1;
            textTimer.setText("Time: " + elapsedTime + "s");
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_memory, container, false);

        textMoves = root.findViewById(R.id.textMoves);
        textTimer = root.findViewById(R.id.textTimer);
        grid = root.findViewById(R.id.gridMemory);

        buttons = new ArrayList<>();
        cards = generateCards();

        for (int i = 0; i < cards.size(); i++) {
            Button btn = new Button(getContext());
            btn.setText(""); // face cachée
            btn.setTag(i);
            btn.setOnClickListener(v -> onCardClicked((int) v.getTag(), btn));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            btn.setLayoutParams(params);

            grid.addView(btn);
            buttons.add(btn);
        }

        timerHandler.postDelayed(timerRunnable, 1000);

        return root;
    }

    private List<Card> generateCards() {
        List<Card> list = new ArrayList<>();
        QuestionGenerator generator = new QuestionGenerator(50, 2, false, true, true, true, true, true);
        List<Integer> usedResults = new ArrayList<>();

        int index = 0;
        while (list.size() < 24) { // 12 paires
            QuestionGenerator.MathQuestion q = generator.generateQuestion();
            int answer = q.answer;

            if (!usedResults.contains(answer)) {
                usedResults.add(answer);
                index++;
                list.add(new Card(q.expression, Card.CardType.OPERATION, index));
                list.add(new Card(String.valueOf(answer), Card.CardType.RESULT, index));
            }
            // sinon rejeter et générer une autre question
        }

        Collections.shuffle(list);
        return list;
    }


    private void onCardClicked(int index, Button btn) {
        Card card = cards.get(index);
        if (card.isFaceUp() || card.isMatched()) return;

        card.setFaceUp(true);
        btn.setText(card.getContent());

        if (firstCard == null) {
            firstCard = card;
            firstIndex = index;
        } else if (secondCard == null) {
            secondCard = card;
            secondIndex = index;
            moves++;
            textMoves.setText("Moves: " + moves);

            new Handler().postDelayed(this::checkMatch, 500);
        }
    }

    private void checkMatch() {
        if (firstCard != null && secondCard != null) {
            boolean match = false;

            if ((firstCard.getType() == Card.CardType.OPERATION && secondCard.getType() == Card.CardType.RESULT &&
                    evaluate(firstCard.getContent()) == Integer.parseInt(secondCard.getContent()))
                    || (firstCard.getType() == Card.CardType.RESULT && secondCard.getType() == Card.CardType.OPERATION &&
                    evaluate(secondCard.getContent()) == Integer.parseInt(firstCard.getContent()))) {
                match = true;
            }

            if (match) {
                firstCard.setMatched(true);
                secondCard.setMatched(true);
                // Tu peux ajouter un flash correct ou un son ici
            } else {
                firstCard.setFaceUp(false);
                secondCard.setFaceUp(false);
                buttons.get(firstIndex).setText("");
                buttons.get(secondIndex).setText("");
            }

            firstCard = null;
            secondCard = null;
            firstIndex = -1;
            secondIndex = -1;
        }
    }

    private int evaluate(String expression) {
        // Nettoyer l'expression : enlever = ou ? si présent
        expression = expression.replace("=", "").replace("?", "").trim();

        try {
            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");
                return Integer.parseInt(parts[0].trim()) + Integer.parseInt(parts[1].trim());
            } else if (expression.contains("-")) {
                String[] parts = expression.split("-");
                return Integer.parseInt(parts[0].trim()) - Integer.parseInt(parts[1].trim());
            } else if (expression.contains("*")) {
                String[] parts = expression.split("X");
                return Integer.parseInt(parts[0].trim()) * Integer.parseInt(parts[1].trim());
            } else if (expression.contains("/")) {
                String[] parts = expression.split("÷");
                return Integer.parseInt(parts[0].trim()) / Integer.parseInt(parts[1].trim());
            } else {
                return Integer.parseInt(expression);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setNavigationEnabled(false);
        ((MainActivity) requireActivity()).animateNavigation(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setNavigationEnabled(true);
        ((MainActivity) requireActivity()).animateNavigation(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(timerRunnable);
    }
}
