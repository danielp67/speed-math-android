package com.example.speedMath.ui.online;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.BaseGameFragment;
import com.example.speedMath.core.FeedbackManager;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.utils.AnimUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

public class OnlineQCMFragment extends BaseGameFragment {

    private static final String TAG = "OnlineQCMFragment";
    private String matchId;
    private String opponentPseudo, player;
    private DatabaseReference matchRef;
    private ValueEventListener matchListener;
    private boolean isGameFinished = false;

    private TextView textQuestion, textTimer, textMyScore, textMyPseudo, textMyStats;
    private TextView textOpponentName, textOpponentScore, textOpponentStats;
    private CardView card1, card2, card3, card4;
    private TextView t1, t2, t3, t4;
    private int correctAnswer, nbQuestions, arcadeDifficulty;
    private QuestionGenerator questionGenerator;
    private GameTimer gameTimer;
    private PlayerManager playerManager;
    private int score = 0;
    private TextView textCombo;
    private int combo = 0;

    private Button btnReplay;
    private LinearLayout overlay;
    private TextView textWinner;
    private OnBackPressedCallback backPressedCallback;
    private String pseudo;
    private long points;
    private long rank;
    private long opponentPoints;
    private long opponentRank;
    private FeedbackManager feedbackManager;


    public OnlineQCMFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {}

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home) {
                    handleBackOrUpNavigation();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return inflater.inflate(R.layout.fragment_online_qcm, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            matchId = getArguments().getString("matchId");
            pseudo = getArguments().getString("myPseudo");
            points = getArguments().getLong("myPoints");
            rank = getArguments().getLong("myRank");
            opponentPseudo = getArguments().getString("opponentPseudo");
            opponentPoints = getArguments().getLong("opponentPoints");
            opponentRank = getArguments().getLong("opponentRank");
            player = getArguments().getString("player");
        }
        matchRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        playerManager = PlayerManager.getInstance(requireContext());
        arcadeDifficulty = playerManager.getArcadeDifficulty();
        nbQuestions = 5; // Default number of questions for an online match

        // UI references
        textQuestion = view.findViewById(R.id.textQuestion);
        textTimer = view.findViewById(R.id.textTimer);
        textMyPseudo = view.findViewById(R.id.textMyPseudo);
        textMyStats = view.findViewById(R.id.textMyStats);
        textMyScore = view.findViewById(R.id.textMyScore);
        textMyPseudo.setText(pseudo);
        textMyScore.setText(String.valueOf(score));
        textMyStats.setText(points + " pts  (#" + rank + ")");

        textCombo = view.findViewById(R.id.textCombo);
        textCombo.setAlpha(0);

        textOpponentName = view.findViewById(R.id.textOpponentName);
        textOpponentName.setText(opponentPseudo);
        textOpponentStats = view.findViewById(R.id.textOpponentStats);
        textOpponentScore = view.findViewById(R.id.textOpponentScore);
        textOpponentScore.setText("0");
        textOpponentStats.setText(opponentPoints + " pts  (#" + opponentRank + ")");

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

        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);

        // Callback pour le bouton back
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackOrUpNavigation();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                backPressedCallback
        );
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
        setupMatchListener();
        generateQuestion();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (matchListener != null) {
            matchRef.removeEventListener(matchListener);
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private void setupMatchListener() {
        matchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isGameFinished || !snapshot.exists()) {
                    return;
                }

                // Update opponent score on our screen
                String opponentScoreField = player.equals("P1") ? "p2_score" : "p1_score";
                if (snapshot.hasChild(opponentScoreField)) {
                    Long opponentScoreValue = snapshot.child(opponentScoreField).getValue(Long.class);
                    if (opponentScoreValue != null) {
                        textOpponentScore.setText(String.valueOf(opponentScoreValue));
                    }
                }

                // Check if both players have finished
                String state = snapshot.child("state").getValue(String.class);
                if ((state != null && state.equals("finished"))) {
                    isGameFinished = true;
                    determineWinner(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        };
        matchRef.addValueEventListener(matchListener);
    }

    private void determineWinner(DataSnapshot snapshot) {
        gameTimer.stop();
        setCardsClickable(false);

        Long p1Score = snapshot.child("p1_score").getValue(Long.class);
        Long p2Score = snapshot.child("p2_score").getValue(Long.class);

        long myFinalScore = player.equals("P1") ? (p1Score != null ? p1Score : 0) : (p2Score != null ? p2Score : 0);
        long opponentFinalScore = player.equals("P1") ? (p2Score != null ? p2Score : 0) : (p1Score != null ? p1Score : 0);

        int result;
        if (myFinalScore > opponentFinalScore) {
            result = R.string.win_message;
        } else if (myFinalScore < opponentFinalScore) {
            result = R.string.lose_message;
        } else {
            result = R.string.draw_message;
        }

        feedbackManager.playLevelUpSound();
        overlay.setAlpha(0f);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(1f).setDuration(500).start();
        textWinner.setText(result);
        btnReplay.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_home);
        });
    }

    private void generateQuestion() {
        if (playerManager.isAnimationEnabled()) AnimUtils.slideLeftRight(textQuestion);

        resetCardColors();
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

    private void checkAnswer(TextView selected) {
        setCardsClickable(false);

        int value = Integer.parseInt(selected.getText().toString());

        if (value == correctAnswer) {
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
            highlightWrong(selected);
            highlightCorrectAnswer();
            feedbackManager.playWrongSound();
        }

        if (score >= nbQuestions) {
            playerFinished();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::generateQuestion, 1000);
        }
    }

    private void playerFinished() {
        gameTimer.stop();
        setCardsClickable(false);
        String winnerField = player.equals("P1") ? "p1" : "p2";
        matchRef.child("winner").setValue(winnerField);
        matchRef.child("state").setValue("finished");
        feedbackManager.playLevelUpSound();
    }

    private void updateScore() {
        if (textMyScore != null) {
            textMyScore.setText(String.valueOf(score));
            String playerScoreField = player.equals("P1") ? "p1_score" : "p2_score";
            matchRef.child(playerScoreField).setValue(score);
        }
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

    private void declareForfeitLoss() {
        if (isGameFinished) return; // if already finished, do nothing

        Log.w(TAG, "Player quit the match → declaring forfeit loss.");

        String winnerField = player.equals("P1") ? "p2" : "p1";
        String opponentScoreField = player.equals("P1") ? "p2_score" : "p1_score";

        // give the opponent a point
        matchRef.child(opponentScoreField).setValue(nbQuestions);

        matchRef.child("winner").setValue(winnerField);
        matchRef.child("state").setValue("finished");

        isGameFinished = true;
    }


    private void handleBackOrUpNavigation() {
        declareForfeitLoss();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                NavController nav = Navigation.findNavController(requireView());
                nav.navigate(R.id.navigation_home);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating", e);
                requireActivity().finish();
            }
        }, 500);
    }

}