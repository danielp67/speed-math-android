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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.FeedbackManager;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.utils.AnimUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryDualFragment extends BaseGameFragment {

    private GridLayout grid;
    private TextView textTimer, textTurn, textScoreP1, textScoreP2, textCombo;
    private List<Card> cards;
    private List<Button> buttons;

    private int combo = 0;
    private Card firstCard = null, secondCard = null;
    private int firstIndex = -1, secondIndex = -1;

    private int playerTurn = 1;
    private int scoreP1 = 0, scoreP2 = 0;
    private boolean busy = false;
    private final Handler handler = new Handler();
    private View endOverlay;
    private TextView textWinner;
    private Button btnReplay;

    private GameTimer gameTimer;
    private FeedbackManager feedbackManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_memory_dual, container, false);

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        textTurn = root.findViewById(R.id.textTurn);
        textScoreP1 = root.findViewById(R.id.textScoreP1);
        textScoreP2 = root.findViewById(R.id.textScoreP2);
        textCombo = root.findViewById(R.id.textCombo);
        grid = root.findViewById(R.id.gridMemory);
        endOverlay = root.findViewById(R.id.endOverlay);
        textWinner = root.findViewById(R.id.textWinner);
        btnReplay = root.findViewById(R.id.btnReplay);

        btnReplay.setOnClickListener(v ->
        {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_home);
        });

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        buttons = new ArrayList<>();
        cards = generateCards();

        setupGrid();

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });
        gameTimer.start();

        // init UI values
        updateUI();
        textCombo.setAlpha(0f);

        return root;
    }

    private void setupGrid() {
        grid.removeAllViews();
        buttons.clear();

        // GridLayout params reuse
        int n = cards.size();
        for (int i = 0; i < n; i++) {
            final int idx = i;
            Button btn = new Button(requireContext());
            btn.setText(""); // face cachée
            btn.setTag(i);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8,8,8,8);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                if (busy) return;
                onCardClicked(idx, btn);
            });

            grid.addView(btn);
            buttons.add(btn);
        }
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
        // sécurité
        if (index < 0 || index >= cards.size()) return;
        Card card = cards.get(index);
        if (card.isFaceUp() || card.isMatched()) return;


        AnimUtils.flipToFront(btn, card.getContent());

        card.setFaceUp(true);

        if (firstCard == null) {
            firstCard = card;
            firstIndex = index;
        } else if (secondCard == null && index != firstIndex) {
            secondCard = card;
            secondIndex = index;
            for (Button button : buttons) {
                button.setClickable(false);
            }
            handler.postDelayed(this::checkMatch, 400);
        }
    }

    private void checkMatch() {
        if (firstCard == null || secondCard == null || firstIndex < 0 || secondIndex < 0) {
            busy = false;
            return;
        }

        boolean match = firstCard.getIndex() == secondCard.getIndex();

        if (match) {
            feedbackManager.playCorrectSound();
            firstCard.setMatched(true);
            secondCard.setMatched(true);

            Button b1 = buttons.get(firstIndex);
            Button b2 = buttons.get(secondIndex);

            int color = (playerTurn == 1)
                    ? getColor(requireContext(), R.color.correct)
                    : getColor(requireContext(), R.color.wrong);

            b1.setBackgroundColor(color);
            b2.setBackgroundColor(color);

            if (playerTurn == 1) scoreP1++;
            else scoreP2++;

            combo++;
            if (combo >= 2) {
                textCombo.setText("🔥 x" + combo + " !");
                textCombo.setAlpha(1f);
                AnimUtils.comboPop(textCombo);
            }

            // feedback
            feedbackManager.playCorrectSound();

        } else {
            // Wrong match → flip back
            firstCard.setFaceUp(false);
            secondCard.setFaceUp(false);

            AnimUtils.flipToBack(buttons.get(firstIndex));
            AnimUtils.flipToBack(buttons.get(secondIndex));
            feedbackManager.playWrongSound();
            combo = 0;
            textCombo.setAlpha(0f);
            // Switch turn
            playerTurn = (playerTurn == 1 ? 2 : 1);
        }


        if (isGameFinished()) {
            showEndScreen();
            return;
        }

        firstCard = null;
        secondCard = null;
        firstIndex = -1;
        secondIndex = -1;

        for (Button button : buttons) {
            button.setClickable(true);
        }
        updateUI();
        busy = false;
    }


    private void updateUI() {
        textTurn.setText("Player " + playerTurn);
        textTurn.setBackgroundColor(getColor(requireContext(), playerTurn == 1 ? R.color.correct : R.color.wrong));
        AnimUtils.slideTextTurn(textTurn, playerTurn == 2);

        textScoreP1.setText("P1 : " + scoreP1);
        textScoreP2.setText("P2 : " + scoreP2);
    }

    private boolean isGameFinished() {
        for (Card c : cards) {
            if (!c.isMatched()) return false;
        }
        return true;
    }

    private void showEndScreen() {
        // Stop timer
        gameTimer.stop();

        String winner;
        if (scoreP1 > scoreP2) winner = "🎉 Player 1 win !";
        else if (scoreP2 > scoreP1) winner = "🎉 Player 2 win !";
        else winner = "🤝 Draw !";

        textWinner.setText(winner);

        endOverlay.setVisibility(View.VISIBLE);
        endOverlay.setAlpha(0f);
        endOverlay.animate().alpha(1f).setDuration(400).start();
    }

}
