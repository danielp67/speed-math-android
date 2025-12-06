package com.example.speedMath.ui.dual;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.speedMath.R;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.utils.AnimUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

public class OnlineQCMFragment extends Fragment {

    private TextView textQuestion, textResult, textTimer, textScoreRight, textCombo, textOpponentName;
    private CardView card1, card2, card3, card4;
    private TextView t1, t2, t3, t4;

    private QuestionGenerator questionGenerator;
    private GameTimer gameTimer;
    private PlayerManager playerManager;

    private int correctAnswer, score = 0, nbQuestions;
    private int combo = 0;
    private boolean myTurn = true;

    private String gameId, playerId, opponentId, opponentName;

    // Firebase
    private DatabaseReference gameRef;

    private Handler handler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qcm_online, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerManager = PlayerManager.getInstance(requireContext());
        gameId = getArguments() != null ? getArguments().getString("GAME_ID") : null;
        playerId = playerManager.getPlayerId();

        textQuestion = view.findViewById(R.id.textQuestion);
        textResult = view.findViewById(R.id.textResult);
        textTimer = view.findViewById(R.id.textTimer);
        textScoreRight = view.findViewById(R.id.textScoreRight);
        textCombo = view.findViewById(R.id.textCombo);

        card1 = view.findViewById(R.id.cardOption1);
        card2 = view.findViewById(R.id.cardOption2);
        card3 = view.findViewById(R.id.cardOption3);
        card4 = view.findViewById(R.id.cardOption4);

        t1 = card1.findViewById(R.id.textOption);
        t2 = card2.findViewById(R.id.textOption);
        t3 = card3.findViewById(R.id.textOption);
        t4 = card4.findViewById(R.id.textOption);

        // Click handlers
        card1.setOnClickListener(v -> sendAnswer(t1));
        card2.setOnClickListener(v -> sendAnswer(t2));
        card3.setOnClickListener(v -> sendAnswer(t3));
        card4.setOnClickListener(v -> sendAnswer(t4));

        // Initialisation Firebase
        gameRef = FirebaseDatabase.getInstance().getReference("online_games").child(gameId);

        setupGame();
        setupFirebaseListeners();
    }

    private void setupGame() {
        nbQuestions = 10; // ou récupérer depuis playerManager
        questionGenerator = new QuestionGenerator(50, 2, true, true, true, true, true, true);
        generateQuestion();

        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> textTimer.setText(formatted));
        gameTimer.start();
    }

    private void setupFirebaseListeners() {
        // Écoute des updates de la partie
        gameRef.child("players").child(opponentId).child("lastAnswer")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Ici on peut afficher le choix de l’adversaire si besoin
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        gameRef.child("players").child(opponentId).child("score")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int oppScore = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                        // Mettre à jour UI adversaire
                        textOpponentName.setText(opponentName + " : " + oppScore);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Listener fin de partie
        gameRef.child("finished").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean finished = snapshot.getValue(Boolean.class);
                if (finished != null && finished) showEndScreen();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void generateQuestion() {
        resetCardColors();
        textResult.setText("");
        setCardsClickable(true);

        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        textQuestion.setText(q.expression);
        correctAnswer = q.answer;
        Collections.shuffle(q.answersChoice);

        t1.setText(String.valueOf(q.answersChoice.get(0)));
        t2.setText(String.valueOf(q.answersChoice.get(1)));
        t3.setText(String.valueOf(q.answersChoice.get(2)));
        t4.setText(String.valueOf(q.answersChoice.get(3)));
    }

    private void sendAnswer(TextView selected) {
        setCardsClickable(false);
        int value = Integer.parseInt(selected.getText().toString());

        boolean correct = value == correctAnswer;
        if (correct) {
            score++;
            combo++;
            if (combo >= 2) {
                textCombo.setText("🔥 x" + combo + " !");
                AnimUtils.comboPop(textCombo);
            }
        } else {
            combo = 0;
            textCombo.setAlpha(0);
        }

        // Mettre à jour Firebase
        gameRef.child("players").child(playerId).child("score").setValue(score);
        gameRef.child("players").child(playerId).child("lastAnswer").setValue(value);

        if (score >= nbQuestions) {
            gameRef.child("finished").setValue(true);
        } else {
            handler.postDelayed(this::generateQuestion, 1000);
        }
    }

    private void setCardsClickable(boolean clickable) {
        card1.setClickable(clickable);
        card2.setClickable(clickable);
        card3.setClickable(clickable);
        card4.setClickable(clickable);
    }

    private void resetCardColors() {
        card1.setCardBackgroundColor(Color.WHITE);
        card2.setCardBackgroundColor(Color.WHITE);
        card3.setCardBackgroundColor(Color.WHITE);
        card4.setCardBackgroundColor(Color.WHITE);
    }

    private void showEndScreen() {
        // Overlay simple
        textResult.setText("🏆 Partie terminée !");
        textResult.setBackgroundColor(Color.parseColor("#AA000000"));
        textResult.setTextColor(Color.WHITE);
    }
}
