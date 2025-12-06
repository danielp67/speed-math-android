package com.example.speedMath.ui.memory;

import static androidx.core.content.ContextCompat.getColor;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.FeedbackManager;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.QuestionGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryDualFragment extends BaseGameFragment {

    private GridLayout grid;
    private TextView textTimer, textTurn, textScoreP1, textScoreP2;
    private List<Card> cards;
    private List<Button> buttons;

    private Card firstCard = null, secondCard = null;
    private int firstIndex = -1, secondIndex = -1;

    private int playerTurn = 1;
    private int scoreP1 = 0, scoreP2 = 0;

    private GameTimer gameTimer;
    private FeedbackManager feedbackManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_memory_dual, container, false);

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        textTimer = root.findViewById(R.id.textTimerDual);
        textTurn = root.findViewById(R.id.textTurn);
        textScoreP1 = root.findViewById(R.id.textScoreP1);
        textScoreP2 = root.findViewById(R.id.textScoreP2);
        grid = root.findViewById(R.id.gridMemoryDual);

        cards = generateCards();
        buttons = new ArrayList<>();

        for (int i = 0; i < cards.size(); i++) {
            Button btn = new Button(getContext());
            btn.setText("");
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

        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });
        gameTimer.start();

        updateUI();

        return root;
    }

    private List<Card> generateCards() {
        List<Card> list = new ArrayList<>();
        QuestionGenerator generator = new QuestionGenerator(50, 2, false, true, true, true, true, true);
        List<Integer> usedResults = new ArrayList<>();

        int index = 0;
        while (list.size() < 24) {
            QuestionGenerator.MathQuestion q = generator.generateQuestion();
            int answer = q.answer;

            if (!usedResults.contains(answer)) {
                usedResults.add(answer);
                index++;
                list.add(new Card(q.expression, Card.CardType.OPERATION, index));
                list.add(new Card(String.valueOf(answer), Card.CardType.RESULT, index));
            }
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

            new Handler().postDelayed(this::checkMatch, 400);
        }
    }

    private void checkMatch() {
        if (firstCard == null || secondCard == null) return;

        boolean match = firstCard.getIndex() == secondCard.getIndex();

        if (match) {
            feedbackManager.playCorrectSound();
            firstCard.setMatched(true);
            secondCard.setMatched(true);

            if (playerTurn == 1) {
                scoreP1++;
                buttons.get(firstIndex).setBackgroundColor(getColor(getContext(), R.color.correct));
                buttons.get(secondIndex).setBackgroundColor(getColor(getContext(), R.color.correct));
            } else {
                scoreP2++;
                buttons.get(firstIndex).setBackgroundColor(getColor(getContext(), R.color.wrong));
                buttons.get(secondIndex).setBackgroundColor(getColor(getContext(), R.color.wrong));
            }

        } else {
            // Mauvaises paires → cacher
            firstCard.setFaceUp(false);
            secondCard.setFaceUp(false);

            buttons.get(firstIndex).setText("");
            buttons.get(secondIndex).setText("");

            // Changement de joueur
            playerTurn = (playerTurn == 1) ? 2 : 1;
        }

        firstCard = null;
        secondCard = null;
        firstIndex = -1;
        secondIndex = -1;

        updateUI();
    }

    private void updateUI() {
        textTurn.setText("Tour : Joueur " + playerTurn);
        textScoreP1.setText("J1 : " + scoreP1);
        textScoreP2.setText("J2 : " + scoreP2);
    }
}
