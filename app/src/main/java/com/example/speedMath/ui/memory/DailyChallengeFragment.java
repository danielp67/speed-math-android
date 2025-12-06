package com.example.speedMath.ui.memory;

import static androidx.core.content.ContextCompat.getColor;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.FeedbackManager;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.utils.AnimUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DailyChallengeFragment extends BaseGameFragment {

    private GridLayout grid;
    private TextView textTimer, textCombo;
    private List<Card> cards;
    private List<Button> buttons;
    private Card firstCard = null, secondCard = null;
    private int firstIndex = -1, secondIndex = -1;
    private int combo = 0;
    private GameTimer timer;
    private Handler handler = new Handler();
    private FeedbackManager feedbackManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_daily_challenge, container, false);

        textTimer = root.findViewById(R.id.textTimer);
        textCombo = root.findViewById(R.id.textCombo);
        grid = root.findViewById(R.id.gridMemory);

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        buttons = new ArrayList<>();
        cards = generateCards();
        setupGrid();

        timer = new GameTimer(); // 60s
        timer.setMaxDurationMillis(60000); // 1 minute par exemple
        timer.setListener((elapsed, formatted) -> textTimer.setText(formatted));
        timer.setFinishListener(() -> showEndScreen());
        timer.start();

        return root;
    }

    private List<Card> generateCards() {
        List<Card> list = new ArrayList<>();
        QuestionGenerator generator = new QuestionGenerator(20, 1, true, true, true, true, true, true);
        List<Integer> used = new ArrayList<>();
        int idx = 0;
        while (list.size() < 12) { // 6 paires
            QuestionGenerator.MathQuestion q = generator.generateQuestion();
            int answer = q.answer;
            if (!used.contains(answer)) {
                used.add(answer);
                idx++;
                list.add(new Card(q.expression, Card.CardType.OPERATION, idx));
                list.add(new Card(String.valueOf(answer), Card.CardType.RESULT, idx));
            }
        }
        Collections.shuffle(list);
        return list;
    }

    private void setupGrid() {
        grid.removeAllViews();
        buttons.clear();
        for (int i = 0; i < cards.size(); i++) {
            int idx = i;
            Button btn = new Button(requireContext());
            btn.setText("");
            btn.setOnClickListener(v -> onCardClicked(idx, btn));
            grid.addView(btn);
            buttons.add(btn);
        }
    }

    private void onCardClicked(int index, Button btn) {
        Card card = cards.get(index);
        if (card.isFaceUp() || card.isMatched()) return;

        AnimUtils.flipToFront(btn, card.getContent(), () -> {
            card.setFaceUp(true);
            if (firstCard == null) {
                firstCard = card;
                firstIndex = index;
            } else if (secondCard == null && index != firstIndex) {
                secondCard = card;
                secondIndex = index;
                handler.postDelayed(this::checkMatch, 400);
            }
        });
    }

    private void checkMatch() {
        boolean match = firstCard.getIndex() == secondCard.getIndex();
        if (match) {
            firstCard.setMatched(true);
            secondCard.setMatched(true);
            buttons.get(firstIndex).setBackgroundColor(getColor(requireContext(), R.color.correct));
            buttons.get(secondIndex).setBackgroundColor(getColor(requireContext(), R.color.correct));
            combo++;
            if (combo >= 2) {
                textCombo.setText("🔥 x" + combo);
                textCombo.setAlpha(1f);
                AnimUtils.comboPop(textCombo);
            }
            feedbackManager.playCorrectSound();
        } else {
            AnimUtils.flipToBack(buttons.get(firstIndex));
            AnimUtils.flipToBack(buttons.get(secondIndex));
            firstCard.setFaceUp(false);
            secondCard.setFaceUp(false);
            combo = 0;
            textCombo.setAlpha(0f);
            feedbackManager.playWrongSound();
        }
        firstCard = null;
        secondCard = null;
        firstIndex = -1;
        secondIndex = -1;

        if (cards.stream().allMatch(Card::isMatched)) showEndScreen();
    }

    private void showEndScreen() {
        // simple overlay TextView
        TextView end = new TextView(requireContext());
        end.setText("🎉 Bravo ! Tu as terminé !");
        end.setTextColor(getColor(requireContext(), R.color.white));
        end.setBackgroundColor(0x99000000); // semi-transparent noir
        end.setTextSize(28f);
        end.setPadding(16,16,16,16);
        end.setGravity(Gravity.CENTER);
        ((ViewGroup) grid.getParent()).addView(end,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));
    }
}

