package fr.accentweb.speedMath.ui.dual;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.GameTimer;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;
import fr.accentweb.speedMath.utils.AnimUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DualFragment extends BaseGameFragment {

    // ================= PLAYER 1 =================
    private TextView p1TextQuestion, p1TextResult, p1TextTimer, p1TextScoreRight;
    private ArrayList<CardView> p1Cards = new ArrayList<>();
    private CardView p1Card1, p1Card2, p1Card3, p1Card4;
    private TextView p1Text1, p1Text2, p1Text3, p1Text4;
    private ArrayList<TextView> p1Texts = new ArrayList<>();
    private int p1CorrectAnswer;

    // ================= PLAYER 2 =================
    private TextView p2TextQuestion, p2TextResult, p2TextTimer, p2TextScoreRight;
    private ArrayList<CardView> p2Cards = new ArrayList<>();
    private CardView p2Card1, p2Card2, p2Card3, p2Card4;
    private TextView p2Text1, p2Text2, p2Text3, p2Text4;
    private ArrayList<TextView> p2Texts = new ArrayList<>();
    private int p2CorrectAnswer;
    private long elapsedMillis = 0;
    private QuestionGenerator questionGenerator;

    private GameTimer gameTimer;
    private int p1Score = 0, p2Score = 0, nbQuestions, arcadeDifficulty;
    private PlayerManager playerManager;

    private Random random = new Random();

    private TextView p1TextCombo, p2TextCombo;

    private LinearLayout p1Overlay, p2Overlay;
    private TextView p1TextWinner, p2TextWinner;
    private Button p1BtnReplay, p2BtnReplay;
    private int p1Combo = 0, p2Combo = 0;
    private FeedbackManager feedbackManager;

    public DualFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

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
        // ====================== INIT PLAYER 1 ======================
        View p1 = v.findViewById(R.id.viewPlayer1);
        p1TextQuestion = p1.findViewById(R.id.textQuestion);
        p1TextResult = p1.findViewById(R.id.textResult);
        p1TextTimer = p1.findViewById(R.id.textTimer);
        p1TextScoreRight = p1.findViewById(R.id.textScoreRight);
        p1TextCombo = p1.findViewById(R.id.textCombo);
        p1BtnReplay = p1.findViewById(R.id.btnReplay);
        p1Overlay = p1.findViewById(R.id.localOverlay);
        p1TextWinner = p1.findViewById(R.id.textWinner);

        p1Card1 = p1.findViewById(R.id.cardOption1);
        p1Card2 = p1.findViewById(R.id.cardOption2);
        p1Card3 = p1.findViewById(R.id.cardOption3);
        p1Card4 = p1.findViewById(R.id.cardOption4);
        p1Text1 = p1Card1.findViewById(R.id.textOption);
        p1Text2 = p1Card2.findViewById(R.id.textOption);
        p1Text3 = p1Card3.findViewById(R.id.textOption);
        p1Text4 = p1Card4.findViewById(R.id.textOption);
        p1Card1.setOnClickListener(view -> checkAnswerPlayer1(p1Text1));
        p1Card2.setOnClickListener(view -> checkAnswerPlayer1(p1Text2));
        p1Card3.setOnClickListener(view -> checkAnswerPlayer1(p1Text3));
        p1Card4.setOnClickListener(view -> checkAnswerPlayer1(p1Text4));

        p1Cards.add(p1Card1);
        p1Cards.add(p1Card2);
        p1Cards.add(p1Card3);
        p1Cards.add(p1Card4);
        p1Texts.add(p1Text1);
        p1Texts.add(p1Text2);
        p1Texts.add(p1Text3);
        p1Texts.add(p1Text4);

        p1BtnReplay.setOnClickListener(view ->
        {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.navigation_home);
        });


        // ====================== INIT PLAYER 2 ======================
        View p2 = v.findViewById(R.id.viewPlayer2);
        p2TextQuestion = p2.findViewById(R.id.textQuestion);
        p2TextResult = p2.findViewById(R.id.textResult);
        p2TextTimer = p2.findViewById(R.id.textTimer);
        p2TextScoreRight = p2.findViewById(R.id.textScoreRight);
        p2TextCombo = p2.findViewById(R.id.textCombo);
        p2BtnReplay = p2.findViewById(R.id.btnReplay);
        p2Overlay = p2.findViewById(R.id.localOverlay);
        p2TextWinner = p2.findViewById(R.id.textWinner);

        p2Card1 = p2.findViewById(R.id.cardOption1);
        p2Card2 = p2.findViewById(R.id.cardOption2);
        p2Card3 = p2.findViewById(R.id.cardOption3);
        p2Card4 = p2.findViewById(R.id.cardOption4);
        p2Text1 = p2Card1.findViewById(R.id.textOption);
        p2Text2 = p2Card2.findViewById(R.id.textOption);
        p2Text3 = p2Card3.findViewById(R.id.textOption);
        p2Text4 = p2Card4.findViewById(R.id.textOption);
        p2Card1.setOnClickListener(view -> checkAnswerPlayer2(p2Text1));
        p2Card2.setOnClickListener(view -> checkAnswerPlayer2(p2Text2));
        p2Card3.setOnClickListener(view -> checkAnswerPlayer2(p2Text3));
        p2Card4.setOnClickListener(view -> checkAnswerPlayer2(p2Text4));

        p2Cards.add(p2Card1);
        p2Cards.add(p2Card2);
        p2Cards.add(p2Card3);
        p2Cards.add(p2Card4);
        p2Texts.add(p2Text1);
        p2Texts.add(p2Text2);
        p2Texts.add(p2Text3);
        p2Texts.add(p2Text4);

        p2BtnReplay.setOnClickListener(view ->
                {
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.navigation_home);
                });

        p1TextScoreRight.setText(getString(R.string.score_format, p1Score, nbQuestions));
        p2TextScoreRight.setText(getString(R.string.score_format, p2Score, nbQuestions));

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (p1TextTimer != null) p1TextTimer.setText(formatted);
            if (p2TextTimer != null) p2TextTimer.setText(formatted);
        });
        gameTimer.start();

        questionGenerator = new QuestionGenerator(
                arcadeDifficulty * 50,      // difficulty par défaut
                2,      // nombre d'opérandes
                true,  //  QCM
                true,
                true,
                true,
                true,
                true
        );
        // First questions
        generateQuestionPlayer1();
        generateQuestionPlayer2();

    }

    // =====================================================
    // PLAYER 1 : Question
    // =====================================================
    private void generateQuestionPlayer1() {
        if(playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(p1TextQuestion);
        resetCardColors(p1Cards);
        p1TextResult.setText("");
        setP1CardsClickable(true);

        if (arcadeDifficulty == 0) {
            questionGenerator.setLevel(p1Score);
        }else {
            questionGenerator.setLevel(arcadeDifficulty*50);
        }

        // Génération via QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        p1TextQuestion.setText(q.expression);
        p1CorrectAnswer = q.answer;

        // Shuffle display order
        Collections.shuffle(q.answersChoice);

        p1Text1.setText(String.valueOf(q.answersChoice.get(0)));
        p1Text2.setText(String.valueOf(q.answersChoice.get(1)));
        p1Text3.setText(String.valueOf(q.answersChoice.get(2)));
        p1Text4.setText(String.valueOf(q.answersChoice.get(3)));

    }

    // =====================================================
    // PLAYER 2 : Question
    // =====================================================
    private void generateQuestionPlayer2() {
        if(playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(p2TextQuestion);
        resetCardColors(p2Cards);
        p2TextResult.setText("");
        setP2CardsClickable(true);

        if (arcadeDifficulty == 0) {
            questionGenerator.setLevel(p2Score);
        }else {
            questionGenerator.setLevel(arcadeDifficulty*50);
        }

        // Génération via QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        p2TextQuestion.setText(q.expression);
        p2CorrectAnswer = q.answer;

        // Shuffle display order
        Collections.shuffle(q.answersChoice);

        p2Text1.setText(String.valueOf(q.answersChoice.get(0)));
        p2Text2.setText(String.valueOf(q.answersChoice.get(1)));
        p2Text3.setText(String.valueOf(q.answersChoice.get(2)));
        p2Text4.setText(String.valueOf(q.answersChoice.get(3)));
    }

    // =====================================================
    // PLAYER 1 : Answer Check
    // =====================================================
    private void checkAnswerPlayer1(TextView selected) {
        int value = Integer.parseInt(selected.getText().toString());
        setP1CardsClickable(false);

        if (value == p1CorrectAnswer) {
            p1TextResult.setText("✅");
            highlightCorrect(selected);
            p1Score++;
            if(p1TextScoreRight != null) p1TextScoreRight.setText(getString(R.string.score_format, p1Score, nbQuestions));
            p1Combo++;
            if (p1Combo >= 2 && playerManager.isAnimationEnabled()) { // combo commence à 2
                p1TextCombo.setText("🔥x" + p1Combo + " !");
                AnimUtils.comboPop(p1TextCombo);
            }
        } else {
            p1Combo = 0;
            p1TextCombo.setAlpha(0);
            p1TextResult.setText("❌");
            highlightWrong(selected);
            highlightCorrectAnswer(p1Texts, p1Cards, p1CorrectAnswer);
        }

        if (p1Score >= nbQuestions) {
            levelCompleted();
        } else {
            selected.postDelayed(this::generateQuestionPlayer1, 1000);
        }
    }

    // =====================================================
    // PLAYER 2 : Answer Check
    // =====================================================
    private void checkAnswerPlayer2(TextView selected) {
        int value = Integer.parseInt(selected.getText().toString());
        setP2CardsClickable(false);

        if (value == p2CorrectAnswer) {
            p2TextResult.setText("✅");
            highlightCorrect(selected);
            p2Score++;
            if(p2TextScoreRight != null) p2TextScoreRight.setText(getString(R.string.score_format, p2Score, nbQuestions));
            p2Combo++;
            if (p2Combo >= 2 && playerManager.isAnimationEnabled()) { // combo commence à 2
                p2TextCombo.setText("🔥 x" + p2Combo + " !");
                AnimUtils.comboPop(p2TextCombo);
            }
        } else {
            p2Combo = 0;
            p2TextCombo.setAlpha(0);
            p2TextResult.setText("❌");
            highlightWrong(selected);
            highlightCorrectAnswer(p2Texts, p2Cards, p2CorrectAnswer);
        }

        if (p2Score >= nbQuestions) {
             levelCompleted();
        } else {
            selected.postDelayed(this::generateQuestionPlayer2, 1000);
        }
    }

    private void levelCompleted() {
        setP1CardsClickable(false);
        setP2CardsClickable(false);
        if (gameTimer != null) gameTimer.stop();
        showEndGame(p1Score, p2Score);
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

    public void setP1CardsClickable(boolean clickable) {
        p1Card1.setClickable(clickable);
        p1Card2.setClickable(clickable);
        p1Card3.setClickable(clickable);
        p1Card4.setClickable(clickable);
    }

    public void setP2CardsClickable(boolean clickable) {
        p2Card1.setClickable(clickable);
        p2Card2.setClickable(clickable);
        p2Card3.setClickable(clickable);
        p2Card4.setClickable(clickable);
    }

    private void showEndGame(int scorePlayer1, int scorePlayer2) {
        boolean isP1Winner = scorePlayer1 > scorePlayer2;
        feedbackManager.playLevelUpSound();

        p1TextWinner.setText(isP1Winner ? R.string.win_message : R.string.lose_message);
        p2TextWinner.setText(isP1Winner ? R.string.lose_message : R.string.win_message);

        // Animation pour le joueur 1
        p1Overlay.setAlpha(0f);
        p1Overlay.setVisibility(View.VISIBLE);
        p1Overlay.animate().alpha(1f).setDuration(500).start();

        // Animation pour le joueur 2
        p2Overlay.setAlpha(0f);
        p2Overlay.setVisibility(View.VISIBLE);
        p2Overlay.animate().alpha(1f).setDuration(500).start();
    }

}
