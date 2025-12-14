package fr.accentweb.speedMath.ui.dual;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.GameTimer;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;
import fr.accentweb.speedMath.utils.AnimUtils;

import java.util.Collections;

public class QCMFragment extends BaseGameFragment {

    private TextView textQuestion, textResult, textTimer, textScoreRight;
    private CardView card1, card2, card3, card4;
    private TextView t1, t2, t3, t4;
    private int correctAnswer, nbQuestions, arcadeDifficulty;
    private long elapsedMillis = 0;
    private QuestionGenerator questionGenerator;
    private GameTimer gameTimer;
    private PlayerManager playerManager;
    private String gameMode;
    private int score = 0;
    private TextView textCombo;
    private int combo = 0;
    private Button btnReplay;
    private LinearLayout overlay;
    private TextView textWinner;
    private FeedbackManager feedbackManager;


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

        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";
        playerManager = PlayerManager.getInstance(requireContext());
        arcadeDifficulty = playerManager.getArcadeDifficulty();
        nbQuestions = playerManager.getNbQuestions();
        switch (nbQuestions) {
            case 1:
                nbQuestions = 10;
                break;
            case 2:
                nbQuestions = 20;
                break;
            case 3:
                nbQuestions = 25;
                break;
            default:
                nbQuestions = 5;
        }
        // UI references
        textQuestion = view.findViewById(R.id.textQuestion);
        textResult = view.findViewById(R.id.textResult);
        textTimer = view.findViewById(R.id.textTimer);
        textScoreRight = view.findViewById(R.id.textScoreRight);
        textScoreRight.setText(score + "/" + nbQuestions);
        textCombo = view.findViewById(R.id.textCombo);
        overlay = view.findViewById(R.id.localOverlay);
        textWinner = view.findViewById(R.id.textWinner);
        btnReplay = view.findViewById(R.id.btnReplay);

        card1 = view.findViewById(R.id.cardOption1);
        card2 = view.findViewById(R.id.cardOption2);
        card3 = view.findViewById(R.id.cardOption3);
        card4 = view.findViewById(R.id.cardOption4);

        t1 = card1.findViewById(R.id.textOption);
        t2 = card2.findViewById(R.id.textOption);
        t3 = card3.findViewById(R.id.textOption);
        t4 = card4.findViewById(R.id.textOption);

        // Click handlers
        card1.setOnClickListener(v -> checkAnswer(t1));
        card2.setOnClickListener(v -> checkAnswer(t2));
        card3.setOnClickListener(v -> checkAnswer(t3));
        card4.setOnClickListener(v -> checkAnswer(t4));

        btnReplay.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_home);
        });

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });
        gameTimer.start();

        questionGenerator = new QuestionGenerator(
                arcadeDifficulty*50,      // difficulty par défaut
                2,      // nombre d'opérandes
                true,  //  QCM
                true,
                true,
                true,
                true,
                true
        );
        generateQuestion();
    }


    private void generateQuestion() {
        if(playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(textQuestion);

        resetCardColors();
        textResult.setText("");
        setCardsClickable(true);

        if (arcadeDifficulty == 0) {
            questionGenerator.setLevel(score);
        }else {
            questionGenerator.setLevel(arcadeDifficulty*50);
        }

        // Génération via QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();

        textQuestion.setText(q.expression);
        correctAnswer = q.answer;

        // Shuffle display order
        Collections.shuffle(q.answersChoice);

        t1.setText(String.valueOf(q.answersChoice.get(0)));
        t2.setText(String.valueOf(q.answersChoice.get(1)));
        t3.setText(String.valueOf(q.answersChoice.get(2)));
        t4.setText(String.valueOf(q.answersChoice.get(3)));
    }


    // Handle user click
    private void checkAnswer(TextView selected) {
        setCardsClickable(false);

        int value = Integer.parseInt(selected.getText().toString());

        if (value == correctAnswer) {
            textResult.setText("✅");
            highlightCorrect(selected);
            score++;
            updateScore();
            combo++;
            if (combo >= 2 && playerManager.isAnimationEnabled()) { // combo commence à 2
                textCombo.setText("🔥 x" + combo + " !");
                AnimUtils.comboPop(textCombo);
            }
            feedbackManager.playCorrectSound();
        } else {
            combo = 0;
            textCombo.setAlpha(0);
            textResult.setText("❌");
            highlightWrong(selected);
            highlightCorrectAnswer();
            feedbackManager.playWrongSound();
        }

        if (score >= nbQuestions) {
            levelCompleted();
        } else {
            selected.postDelayed(this::generateQuestion, 1000);
        }
    }

    private void updateScore() {
        if (textScoreRight != null) {
            textScoreRight.setText(score + "/" + nbQuestions);
        }
    }

    private void levelCompleted() {
        setCardsClickable(false);
        showEndGame();
        feedbackManager.playLevelUpSound();
        if (gameTimer != null) gameTimer.stop();
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

    public void setCardsClickable(boolean clickable) {
        card1.setClickable(clickable);
        card2.setClickable(clickable);
        card3.setClickable(clickable);
        card4.setClickable(clickable);
    }

    private void showEndGame() {
        textWinner.setText(R.string.win_message);
        overlay.setAlpha(0f);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(1f).setDuration(500).start();
    }
}
