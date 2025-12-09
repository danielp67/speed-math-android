package com.example.speedMath.ui.memory;

import static androidx.core.content.ContextCompat.getColor;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MemoryFragment extends BaseGameFragment {

    private GridLayout grid;
    private TextView textMoves, textTimer, textScore, textCombo;
    private List<Card> cards;
    private List<Button> buttons;

    private Card firstCard = null, secondCard = null;
    private int firstIndex = -1, secondIndex = -1;

    private int moves = 0;
    private int score = 0;
    private int combo = 0;

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
        View root = inflater.inflate(R.layout.fragment_memory, container, false);

        textMoves = root.findViewById(R.id.textMoves);
        textTimer = root.findViewById(R.id.textTimer);
        textScore = root.findViewById(R.id.textScore);     // ajoute ce TextView dans ton layout
        textCombo = root.findViewById(R.id.textCombo);     // ajoute ce TextView dans ton layout
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
        updateMoves();
        updateScore();
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
        Set<Integer> usedResults = new HashSet<>();
        QuestionGenerator generator = new QuestionGenerator(50, 2, false, true, true, true, true, true);

        int pairIndex = 0;
        // On génère jusqu'à 12 paires avec résultats uniques
        while (list.size() < 24) {
            QuestionGenerator.MathQuestion q = generator.generateQuestion();
            int answer = q.answer;

            // si résultat déjà utilisé, on rejette
            if (usedResults.contains(answer)) continue;

            usedResults.add(answer);
            pairIndex++;
            // Card(String content, Card.CardType type, int index)
            list.add(new Card(q.expression, Card.CardType.OPERATION, pairIndex));
            list.add(new Card(String.valueOf(answer), Card.CardType.RESULT, pairIndex));
        }

        Collections.shuffle(list);
        return list;
    }

    private void onCardClicked(int index, Button btn) {
        // sécurité
        if (index < 0 || index >= cards.size()) return;
        Card card = cards.get(index);
        if (card.isFaceUp() || card.isMatched()) return;

        // flip to front (3D)

        AnimUtils.flipToFront(btn, card.getContent());

        card.setFaceUp(true);
        moves++;
        updateMoves();

        // selection
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

            if (firstCard == null || secondCard == null) return;

            boolean match = firstCard.getIndex() == secondCard.getIndex();

            if (match) {
                // mark matched
                firstCard.setMatched(true);
                secondCard.setMatched(true);

                // coloriser en vert (ou couleur joueur)
                Button b1 = safeGetButton(firstIndex);
                Button b2 = safeGetButton(secondIndex);
                if (b1 != null) b1.setBackgroundColor(getColor(requireContext(), R.color.correct));
                if (b2 != null) b2.setBackgroundColor(getColor(requireContext(), R.color.correct));

                // scoring & combo
                combo++;
                int basePoints = 10;
                int bonus = (combo - 1) * 5;
                int earned = basePoints + Math.max(0, bonus);
                score += earned;
                updateScore();

                // combo display
                if (combo >= 2) {
                    textCombo.setText("🔥 x" + combo + " !");
                    textCombo.setAlpha(1f);
                    AnimUtils.comboPop(textCombo);
                }

                // feedback
                feedbackManager.playCorrectSound();
                // optional haptic
                // feedbackManager.correctFeedback(b1 != null ? b1 : (b2 != null ? b2 : getView()));
            } else {
                // no match -> flip back both with animation
                Button b1 = safeGetButton(firstIndex);
                Button b2 = safeGetButton(secondIndex);

                // small wrong feedback
                feedbackManager.playWrongSound();
                combo = 0;
                textCombo.setAlpha(0f);

                // flip back with slight delay to let player see
                if (b1 != null) AnimUtils.flipToBack(b1);
                if (b2 != null) AnimUtils.flipToBack(b2);

                // mark faceDown in model
                firstCard.setFaceUp(false);
                secondCard.setFaceUp(false);
            }

        if (isGameFinished()) {
            showEndScreen();
            return;
        }
            // reset selection
            firstCard = null;
            secondCard = null;
            firstIndex = -1;
            secondIndex = -1;
            // unlock clicks after animations done
            for (Button button : buttons) {
                button.setClickable(true);
            }
            handler.postDelayed(() -> busy = false, 350);

    }

    // safe getter to avoid IndexOutOfBounds when indices are -1
    @Nullable
    private Button safeGetButton(int idx) {
        if (idx < 0 || idx >= buttons.size()) return null;
        return buttons.get(idx);
    }

    private void updateMoves() {
        if (textMoves != null) textMoves.setText("Moves: " + moves);
    }

    private void updateScore() {
        if (textScore != null) textScore.setText("Score: " + score);
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

        String winner = "🎉 You win !";

        textWinner.setText(winner);
        endOverlay.setVisibility(View.VISIBLE);
        endOverlay.setAlpha(0f);
        endOverlay.animate().alpha(1f).setDuration(400).start();
    }

}
