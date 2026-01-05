package fr.accentweb.speedMath.ui.friend;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.database.*;

import java.util.Collections;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.GameTimer;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.core.QuestionGenerator;
import fr.accentweb.speedMath.utils.AnimUtils;

public class FriendFragment extends Fragment {

    private static final String TAG = "FriendFragment";

    private String roomId;
    private String player; // "P1" or "P2"
    private String myPseudo;
    private String opponentPseudo;

    private DatabaseReference roomRef;
    private ValueEventListener roomListener;

    private TextView textQuestion, textTimer;
    private TextView textMyScore, textOpponentScore;
    private TextView textMyPseudo, textOpponentPseudo;
    private TextView textCombo;

    private CardView card1, card2, card3, card4;
    private TextView t1, t2, t3, t4;

    private LinearLayout overlay;
    private TextView textWinner;
    private Button btnReplay;

    private int correctAnswer;
    private int score = 0;
    private int combo = 0;
    private final int nbQuestions = 10;

    private QuestionGenerator questionGenerator;
    private GameTimer gameTimer;
    private PlayerManager playerManager;
    private FeedbackManager feedbackManager;
    private OnBackPressedCallback backPressedCallback;

    private boolean isGameFinished = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        if (getArguments() == null) {
            Navigation.findNavController(view).navigateUp();
            return;
        }

        playerManager = PlayerManager.getInstance(requireContext());
        Bundle args = getArguments();
        roomId = args.getString("roomId", "");
        player = args.getString("player", "");
        myPseudo = args.getString("myPseudo", playerManager.getOnlinePseudo());
        opponentPseudo = args.getString("opponentPseudo", "opponent");

        if (roomId == null || player == null || myPseudo == null || opponentPseudo == null) {
            Navigation.findNavController(view).navigateUp();
            return;
        }

        try {
            roomRef = FirebaseDatabase.getInstance()
                    .getReference("friend_rooms")
                    .child(roomId);
        } catch (Exception e) {
            Log.e(TAG, "Error during initialization", e);
            Navigation.findNavController(view).navigateUp();
            return;
        }
        playerManager = PlayerManager.getInstance(requireContext());

        // ----- UI -----
        textQuestion = view.findViewById(R.id.textQuestion);
        textTimer = view.findViewById(R.id.textTimer);

        textMyPseudo = view.findViewById(R.id.textMyPseudo);
        textOpponentPseudo = view.findViewById(R.id.textOpponentName);

        textMyScore = view.findViewById(R.id.textMyScore);
        textOpponentScore = view.findViewById(R.id.textOpponentScore);

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

        textMyPseudo.setText(myPseudo);
        textOpponentPseudo.setText(opponentPseudo);
        textMyScore.setText("0");
        textOpponentScore.setText("0");

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
        // ----- Timer -----
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> textTimer.setText(formatted));
        gameTimer.start();

        // ----- Questions -----
        questionGenerator = new QuestionGenerator(
                3,
                2,
                true,
                true,
                true,
                true,
                true,
                true
        );

        listenRoom();
        generateQuestion();
    }

    // ---------------------------------------------------

    private void listenRoom() {
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || isGameFinished) return;

                if (player == null) {
                    Log.e(TAG, "Player is null");
                    return;
                }

                String opponentScoreKey = player.equals("P1") ? "p2_score" : "p1_score";
                Long opponentScore = snapshot.child(opponentScoreKey).getValue(Long.class);
                if (opponentScore != null) {
                    textOpponentScore.setText(String.valueOf(opponentScore));
                }

                String state = snapshot.child("state").getValue(String.class);
                if (state != null && "finished".equals(state)) {
                    isGameFinished = true;
                    determineWinner(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
            }
        };

        if (roomRef != null) {
            roomRef.addValueEventListener(roomListener);
        }
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

        questionGenerator.setLevel(Math.toIntExact(100)/2);

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
        roomRef.child("winner").setValue(winnerField);
        roomRef.child("state").setValue("finished");
        feedbackManager.playLevelUpSound();
    }

    private void updateScore() {
        if (textMyScore != null) {
            textMyScore.setText(String.valueOf(score));
            String playerScoreField = player.equals("P1") ? "p1_score" : "p2_score";
            roomRef.child(playerScoreField).setValue(score);
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
        roomRef.child(opponentScoreField).setValue(nbQuestions);

        roomRef.child("winner").setValue(winnerField);
        roomRef.child("state").setValue("finished");

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (roomListener != null) {
            roomRef.removeEventListener(roomListener);
        }
        if (gameTimer != null) gameTimer.stop();
    }
}
