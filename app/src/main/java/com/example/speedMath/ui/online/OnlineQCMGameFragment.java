package com.example.speedMath.ui.online;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.speedMath.R;
import com.example.speedMath.core.GameTimer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OnlineQCMGameFragment extends Fragment {

    private TextView txtQuestion, txtOpponent, txtScore, txtTimer;
    private Button btnA, btnB, btnC, btnD;
    private ProgressBar progress;

    private String matchId;
    private String myUid;
    private String opponentUid;
    private DatabaseReference matchRef;

    private int score = 0;
    private GameTimer timer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online_qcm_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtQuestion = view.findViewById(R.id.txtQuestion);
        txtOpponent = view.findViewById(R.id.txtOpponent);
        txtScore = view.findViewById(R.id.txtScore);
        txtTimer = view.findViewById(R.id.txtTimer);

        btnA = view.findViewById(R.id.btnA);
        btnB = view.findViewById(R.id.btnB);
        btnC = view.findViewById(R.id.btnC);
        btnD = view.findViewById(R.id.btnD);

        progress = view.findViewById(R.id.progressBar);

        if (getArguments() != null) {
            matchId = getArguments().getString("matchId", "");
            myUid = getArguments().getString("uid", "");
            opponentUid = getArguments().getString("opponentUid", "");
        }

        matchRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        timer = new GameTimer();
        timer.setListener((elapsed, formatted) -> txtTimer.setText(formatted));
        timer.start();

        setupListeners();
    }

    private void setupListeners() {
        btnA.setOnClickListener(v -> sendAnswer("A"));
        btnB.setOnClickListener(v -> sendAnswer("B"));
        btnC.setOnClickListener(v -> sendAnswer("C"));
        btnD.setOnClickListener(v -> sendAnswer("D"));
    }

    private void listenMatchUpdates() {
        matchRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // Score
                if (snapshot.child("scores").hasChild(myUid)) {
                    score = snapshot.child("scores").child(myUid).getValue(Integer.class);
                    txtScore.setText("Score : " + score);
                }

                // Opponent name
                if (snapshot.child("players").hasChild(opponentUid)) {
                    String oppName = snapshot.child("players").child(opponentUid)
                            .child("pseudo").getValue(String.class);
                    txtOpponent.setText("VS " + oppName);
                }

                // Question text
                if (snapshot.child("questionText").exists()) {
                    String q = snapshot.child("questionText").getValue(String.class);
                    txtQuestion.setText(q);
                }

                // Answers buttons
                if (snapshot.child("answers").child(myUid).exists()) {
                    String answer = snapshot.child("answers").child(myUid).getValue(String.class);
                    if (!answer.equals("")) disableButtons();
                }

                // Game finished?
                if (snapshot.child("state").getValue(String.class).equals("finished")) {
                    endGame(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void sendAnswer(String answer) {
        disableButtons();
        matchRef.child("answers").child(myUid).setValue(answer);
    }

    private void endGame(DataSnapshot snapshot) {
        timer.stop();

        int myScore = snapshot.child("scores").child(myUid).getValue(Integer.class);
        int oppScore = snapshot.child("scores").child(opponentUid).getValue(Integer.class);

        String result;
        if (myScore > oppScore) result = "Victoire !";
        else if (myScore < oppScore) result = "Défaite...";
        else result = "Égalité";

        new AlertDialog.Builder(requireContext())
                .setTitle(result)
                .setMessage("Votre score : " + myScore + "\nScore adverse : " + oppScore)
                .setPositiveButton("OK", (d, which) -> requireActivity().onBackPressed())
                .show();
    }



    private void disableButtons() {
        btnA.setEnabled(false);
        btnB.setEnabled(false);
        btnC.setEnabled(false);
        btnD.setEnabled(false);
    }

    private void enableButtons() {
        btnA.setEnabled(true);
        btnB.setEnabled(true);
        btnC.setEnabled(true);
        btnD.setEnabled(true);
    }

}
