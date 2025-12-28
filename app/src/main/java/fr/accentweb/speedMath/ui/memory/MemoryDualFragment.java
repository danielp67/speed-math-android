package fr.accentweb.speedMath.ui.memory;

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
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.GameTimer;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;
import fr.accentweb.speedMath.core.MemoryDifficulty;
import fr.accentweb.speedMath.utils.AnimUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryDualFragment extends BaseGameFragment {

    private GridLayout grid;
    private TextView textTimer, textTurn, textScoreP1, textScoreP2, textCombo;
    private List<Card> cards;
    private List<MaterialCardView> cardViews;

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
    private MemoryDifficulty difficulty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_memory_dual, container, false);
        int savedDifficulty = PlayerManager.getInstance(requireContext()).getMemoryDuoDifficulty();
        difficulty = MemoryDifficulty.values()[savedDifficulty];

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

        btnReplay.setOnClickListener(v -> {
            restartGame();
        });

        cardViews = new ArrayList<>();
        cards = generateCards();

        // Configurer la grille selon la difficulté
        grid.setColumnCount(difficulty.cols);
        grid.setRowCount(difficulty.rows);

        setupGrid();

        // Phase de prévisualisation
        previewCards();

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });
        gameTimer.start();

        // Initialiser l'UI
        updateUI();
        textCombo.setAlpha(0f);

        return root;
    }

    private void previewCards() {
        for (int i = 0; i < cards.size(); i++) {
            MaterialCardView cardView = cardViews.get(i);
            TextView text = cardView.findViewById(R.id.textCard);
            text.setText(cards.get(i).getContent());
            cardView.setClickable(false);
        }

        handler.postDelayed(() -> {
            for (MaterialCardView cardView : cardViews) {
                AnimUtils.flipToBack(cardView);
                cardView.setClickable(true);
            }
        }, difficulty.previewMs);
    }
    private void setupGrid() {
        grid.removeAllViews();
        cardViews.clear();

        int n = cards.size();
        for (int i = 0; i < n; i++) {
            final int idx = i;

            MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(requireContext())
                    .inflate(R.layout.memory_card, grid, false);

            cardView.setTag(i);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % difficulty.cols, 1f);
            params.rowSpec = GridLayout.spec(i / difficulty.cols, 1f);
            params.setMargins(8, 8, 8, 8);
            cardView.setLayoutParams(params);

            cardView.setOnClickListener(v -> {
                if (busy) return;
                onCardClicked(idx, cardView);
            });

            grid.addView(cardView);
            cardViews.add(cardView);
        }
    }

    private List<Card> generateCards() {
        List<Card> list = new ArrayList<>();
        QuestionGenerator generator = new QuestionGenerator(50, 2, false, true, true, true, true, true);
        List<Integer> usedResults = new ArrayList<>();

        int pairCount = (difficulty.cols * difficulty.rows) / 2;
        int index = 0;
        while (list.size() < pairCount * 2) {
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

    private void onCardClicked(int index, MaterialCardView cardView) {
        if (index < 0 || index >= cards.size()) return;
        Card card = cards.get(index);
        if (card.isFaceUp() || card.isMatched()) return;

        AnimUtils.flipToFront(cardView, card.getContent());
        card.setFaceUp(true);

        if (firstCard == null) {
            firstCard = card;
            firstIndex = index;
        } else if (secondCard == null && index != firstIndex) {
            secondCard = card;
            secondIndex = index;
            for (MaterialCardView cv : cardViews) cv.setClickable(false);
            handler.postDelayed(this::checkMatch, difficulty.previewMs);
        }
    }

    private void checkMatch() {
        if (firstCard == null || secondCard == null || firstIndex < 0 || secondIndex < 0) {
            busy = false;
            return;
        }

        boolean match = firstCard.getIndex() == secondCard.getIndex();
        MaterialCardView c1 = safeGetCard(firstIndex);
        MaterialCardView c2 = safeGetCard(secondIndex);

        if (match) {
            feedbackManager.playCorrectSound();
            firstCard.setMatched(true);
            secondCard.setMatched(true);

            int color = (playerTurn == 1)
                    ? getColor(requireContext(), R.color.correct)
                    : getColor(requireContext(), R.color.wrong);

            if (c1 != null) c1.setCardBackgroundColor(getColor(requireContext(), R.color.correct));
            if (c2 != null) c2.setCardBackgroundColor(getColor(requireContext(), R.color.correct));

            if (playerTurn == 1) scoreP1++;
            else scoreP2++;

            combo++;
            if (combo >= 2) {
                textCombo.setText("🔥 x" + combo + " !");
                textCombo.setAlpha(1f);
                AnimUtils.comboPop(textCombo);
            }
            feedbackManager.playCorrectSound();

        } else {
            firstCard.setFaceUp(false);
            secondCard.setFaceUp(false);

            if (c1 != null) AnimUtils.flipToBack(c1);
            if (c2 != null) AnimUtils.flipToBack(c2);

            feedbackManager.playWrongSound();
            combo = 0;
            textCombo.setAlpha(0f);
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

        for (MaterialCardView cv : cardViews) cv.setClickable(true);
        handler.postDelayed(() -> busy = false, 350);
        updateUI();
        busy = false;
    }

    @Nullable
    private MaterialCardView safeGetCard(int idx) {
        if (idx < 0 || idx >= cardViews.size()) return null;
        return cardViews.get(idx);
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
        gameTimer.stop();
        feedbackManager.playLevelUpSound();

        String winner;
        if (scoreP1 > scoreP2) winner = "🎉 Player 1 win !";
        else if (scoreP2 > scoreP1) winner = "🎉 Player 2 win !";
        else winner = "🤝 Draw !";

        textWinner.setText(winner);
        endOverlay.setVisibility(View.VISIBLE);
        endOverlay.setAlpha(0f);
        endOverlay.animate().alpha(1f).setDuration(400).start();
    }

    private void restartGame() {
        playerTurn = scoreP1 > scoreP2 ? 1 : 2;
        scoreP1 = 0;
        scoreP2 = 0;
        combo = 0;
        firstCard = null;
        secondCard = null;
        firstIndex = -1;
        secondIndex = -1;

        updateUI();
        textCombo.setAlpha(0f);

        endOverlay.setVisibility(View.GONE);

        cards = generateCards();

        setupGrid();

        gameTimer.reset();
        gameTimer.start();

        previewCards();
    }
}
