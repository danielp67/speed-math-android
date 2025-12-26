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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.GameTimer;
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
    private MemoryDifficulty difficulty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_memory_dual, container, false);

        // Récupérer la difficulté depuis les arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("DIFFICULTY")) {
            difficulty = (MemoryDifficulty) args.getSerializable("DIFFICULTY");
        } else {
            // Difficulté par défaut si non fournie
            difficulty = MemoryDifficulty.MEDIUM;
        }

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
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_home);
        });

        buttons = new ArrayList<>();
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
        // Montrer toutes les cartes
        for (int i = 0; i < cards.size(); i++) {
            Button btn = buttons.get(i);
            btn.setText(cards.get(i).getContent());
            btn.setClickable(false);
        }

        // Cacher les cartes avec animation après le temps de prévisualisation
        handler.postDelayed(() -> {
            for (Button btn : buttons) {
                AnimUtils.flipToBack(btn);
                btn.setClickable(true);
            }
        }, difficulty.previewMs);
    }
    private void setupGrid() {
        grid.removeAllViews();
        buttons.clear();

        int n = cards.size();
        for (int i = 0; i < n; i++) {
            final int idx = i;
            Button btn = new Button(requireContext());
            btn.setText("");
            btn.setTag(i);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % difficulty.cols, 1f);
            params.rowSpec = GridLayout.spec(i / difficulty.cols, 1f);
            params.setMargins(8, 8, 8, 8);
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

    private void onCardClicked(int index, Button btn) {
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
            handler.postDelayed(this::checkMatch, difficulty.previewMs);
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

        } else {
            firstCard.setFaceUp(false);
            secondCard.setFaceUp(false);

            AnimUtils.flipToBack(buttons.get(firstIndex));
            AnimUtils.flipToBack(buttons.get(secondIndex));
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
}
