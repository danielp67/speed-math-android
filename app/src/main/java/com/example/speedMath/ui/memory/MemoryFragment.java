package com.example.speedMath.ui.memory;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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

import com.example.speedMath.R;
import com.example.speedMath.ui.memory.Card;
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

public class MemoryFragment extends Fragment {

    private GridLayout grid;
    private TextView textMoves, textTimer, textScore, textCombo;
    private List<Card> cards;
    private List<Button> buttons;

    private Card firstCard = null, secondCard = null;
    private int firstIndex = -1, secondIndex = -1;

    private int moves = 0;
    private int score = 0;
    private int combo = 0;

    private boolean busy = false; // empêche les clics pendant animations

    private final Handler handler = new Handler();

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
        Random random = new Random();
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
        flipToFront(btn, card.getContent(), () -> {
            // after flip completed
            card.setFaceUp(true);

            if (firstCard == null) {
                firstCard = card;
                firstIndex = index;
            } else if (secondCard == null && index != firstIndex) {
                secondCard = card;
                secondIndex = index;
                moves++;
                updateMoves();

                // disable further clicks while checking
                busy = true;
                handler.postDelayed(this::checkMatch, 500);
            }
        });
    }

    private void checkMatch() {
        try {
            if (firstCard == null || secondCard == null) return;

            boolean match = firstCard.getIndex() == secondCard.getIndex();

            if (match) {
                // mark matched
                firstCard.setMatched(true);
                secondCard.setMatched(true);

                // coloriser en vert (ou couleur joueur)
                Button b1 = safeGetButton(firstIndex);
                Button b2 = safeGetButton(secondIndex);
                if (b1 != null) b1.setBackgroundColor(Color.parseColor("#A5D6A7")); // green
                if (b2 != null) b2.setBackgroundColor(Color.parseColor("#A5D6A7"));

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
                if (b1 != null) flipToBack(b1);
                if (b2 != null) flipToBack(b2);

                // mark faceDown in model
                firstCard.setFaceUp(false);
                secondCard.setFaceUp(false);
            }
        } finally {
            // reset selection
            firstCard = null;
            secondCard = null;
            firstIndex = -1;
            secondIndex = -1;
            // unlock clicks after animations done
            handler.postDelayed(() -> busy = false, 350);
        }
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

    // ----- Flip animation helpers (3D) -----
    // flip to front: rotate Y 0 -> 90 (hide), change text, rotate 270 -> 360 (show)
    private void flipToFront(Button btn, String frontText, Runnable onComplete) {
        if (btn == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ObjectAnimator rot1 = ObjectAnimator.ofFloat(btn, "rotationY", 0f, 90f);
        rot1.setDuration(120);
        rot1.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                // set front content
                btn.setText(frontText);
                // continue rotation from 270 to 360 (makes illusion of flipping)
                ObjectAnimator rot2 = ObjectAnimator.ofFloat(btn, "rotationY", -90f, 0f);
                rot2.setDuration(120);
                rot2.start();
                if (onComplete != null) onComplete.run();
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
        rot1.start();
    }

    // flip to back: rotate Y 0 -> 90 (hide), clear text, rotate -90 -> 0
    private void flipToBack(Button btn) {
        if (btn == null) return;

        ObjectAnimator rot1 = ObjectAnimator.ofFloat(btn, "rotationY", 0f, 90f);
        rot1.setDuration(120);
        rot1.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                btn.setText(""); // back side
                ObjectAnimator rot2 = ObjectAnimator.ofFloat(btn, "rotationY", -90f, 0f);
                rot2.setDuration(120);
                rot2.start();
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
        rot1.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (gameTimer != null) gameTimer.stop();
        if (feedbackManager != null) feedbackManager.release();
    }
}
