package com.example.speedMath.ui.online;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.utils.AnimUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

public class OnlineQCMFragment extends BaseGameFragment {

    private String matchId;
    private String myUid;
    private String opponentUid;
    private String opponentPseudo, player;
    private DatabaseReference matchRef;

    private TextView textQuestion, textResult, textTimer, textScoreRight;
    private TextView textOpponentName, textOpponentScore;
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

    public OnlineQCMFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_online_qcm, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            matchId = getArguments().getString("matchId");
            myUid = getArguments().getString("myUid");
            opponentUid = getArguments().getString("opponentUid");
            opponentPseudo = getArguments().getString("opponentPseudo");
            player = getArguments().getString("player");

        }
        matchRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        gameMode = getArguments() != null ? getArguments().getString("MODE") : "ALL";
        playerManager = PlayerManager.getInstance(requireContext());
        arcadeDifficulty = playerManager.getArcadeDifficulty();
        //nbQuestions = playerManager.getNbQuestions();
        nbQuestions = 0;
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
        textCombo.setAlpha(0);
        textOpponentName = view.findViewById(R.id.textOpponentName);
        textOpponentName.setText(opponentPseudo);
        textOpponentScore = view.findViewById(R.id.textOpponentScore);
        textOpponentScore.setText("0");




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

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
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
        generateQuestion();
    }

    private void generateQuestion() {
        if (playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(textQuestion);

        resetCardColors();
        textResult.setText("");
        setCardsClickable(true);

        if (arcadeDifficulty == 0) {
            questionGenerator.setLevel(score);
        } else {
            questionGenerator.setLevel(arcadeDifficulty * 50);
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
        } else {
            combo = 0;
            textCombo.setAlpha(0);
            textResult.setText("❌");
            highlightWrong(selected);
            highlightCorrectAnswer();
        }

        if (score >= nbQuestions) {
            levelCompleted();
        } else {
            selected.postDelayed(this::generateQuestion, 1000);
        }
    }

    private void updateScore() {
        if (textScoreRight != null) {
            // playerManager.setCorrectAnswersStreak(gameMode, score);
            textScoreRight.setText(score + "/" + nbQuestions);
        }
    }

    private void levelCompleted() {
        // Envoi du score à Firebase
      String player_score = player.equals("P1") ? "p1_score" : "p2_score";
      String opponent_score = player.equals("P1") ? "p2_score" : "p1_score";
      matchRef.child("state").setValue("finished");
        matchRef.child(player_score).setValue(score).addOnCompleteListener(task -> {
            // Lecture du score de l'adversaire
            matchRef.child(opponent_score).get().addOnCompleteListener(task2 -> {
                Integer opponentScore = 0;
                if (task2.getResult().getValue() != null) {
                    opponentScore = Integer.parseInt(task2.getResult().getValue().toString());
                }
                String result = score > opponentScore ? "🎉 Gagné !" : (score < opponentScore ? "💀 Perdu !" : "⚖️ Égalité !");
                TextView textResult = requireView().findViewById(R.id.textResult);
                textResult.setText(result);
            });
        });
    }

    // UI helpers
    private void highlightCorrect(TextView view) {
        ((CardView) view.getParent()).setCardBackgroundColor(Color.parseColor("#A5D6A7"));
    }

    private void highlightWrong(TextView view) {
        ((CardView) view.getParent()).setCardBackgroundColor(Color.parseColor("#FFCDD2"));
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

}


