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
    private Button btnExit;

    private int correctAnswer;
    private int score = 0;
    private int combo = 0;
    private final int NB_QUESTIONS = 10;

    private QuestionGenerator questionGenerator;
    private GameTimer gameTimer;
    private PlayerManager playerManager;
    private FeedbackManager feedbackManager;

    private boolean gameFinished = false;

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
        Log.d("friend", "start to room - fragment");

        if (getArguments() == null) {
            Navigation.findNavController(view).navigateUp();
            return;
        }

        playerManager = PlayerManager.getInstance(requireContext());
        Bundle args = getArguments();
        roomId = args.getString("roomId", "");  // Valeur par défaut
        player = args.getString("player", "");  // Valeur par défaut
        myPseudo = args.getString("myPseudo", playerManager.getOnlinePseudo());
        opponentPseudo = args.getString("opponentPseudo", "opponent");

        // Vérification complète
        if (roomId.isEmpty()) {
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
        btnExit = view.findViewById(R.id.btnReplay);

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

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finishAndLeave();
                    }
                }
        );
    }

    // ---------------------------------------------------

    private void listenRoom() {
        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || gameFinished) return;

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
                    gameFinished = true;
                    showResult(snapshot);
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


    private void generateQuestion() {
        if (playerManager.isAnimationEnabled()) {
            AnimUtils.slideLeftRight(textQuestion);
        }

        resetCards();
        setClickable(true);

        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        correctAnswer = q.answer;
        textQuestion.setText(q.expression);

        Collections.shuffle(q.answersChoice);
        t1.setText(String.valueOf(q.answersChoice.get(0)));
        t2.setText(String.valueOf(q.answersChoice.get(1)));
        t3.setText(String.valueOf(q.answersChoice.get(2)));
        t4.setText(String.valueOf(q.answersChoice.get(3)));
    }

    private void checkAnswer(TextView selected) {
        setClickable(false);

        int value = Integer.parseInt(selected.getText().toString());
        if (value == correctAnswer) {
            score++;
            combo++;
            feedbackManager.playCorrectSound();
            highlight(selected, true);
            updateScore();
        } else {
            combo = 0;
            feedbackManager.playWrongSound();
            highlight(selected, false);
        }

        if (score >= NB_QUESTIONS) {
            finishGame();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::generateQuestion, 900);
        }
    }

    private void updateScore() {
        textMyScore.setText(String.valueOf(score));
        String key = player.equals("P1") ? "p1_score" : "p2_score";
        roomRef.child(key).setValue(score);
    }

    private void finishGame() {
        roomRef.child("state").setValue("finished");
        gameTimer.stop();
    }

    private void showResult(DataSnapshot snapshot) {
        gameTimer.stop();
        overlay.setVisibility(View.VISIBLE);

        long p1 = snapshot.child("p1_score").getValue(Long.class);
        long p2 = snapshot.child("p2_score").getValue(Long.class);

        boolean win =
                (player.equals("P1") && p1 > p2) ||
                        (player.equals("P2") && p2 > p1);

        textWinner.setText(
                win ? R.string.win_message :
                        (p1 == p2 ? R.string.draw_message : R.string.lose_message)
        );

        btnExit.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_home)
        );
    }

    private void finishAndLeave() {
        roomRef.child("state").setValue("finished");
        Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
    }

    private void resetCards() {
        card1.setCardBackgroundColor(Color.WHITE);
        card2.setCardBackgroundColor(Color.WHITE);
        card3.setCardBackgroundColor(Color.WHITE);
        card4.setCardBackgroundColor(Color.WHITE);
    }

    private void highlight(TextView view, boolean correct) {
        ((CardView) view.getParent())
                .setCardBackgroundColor(correct ? Color.parseColor("#A5D6A7") : Color.parseColor("#FFCDD2"));
    }

    private void setClickable(boolean enabled) {
        card1.setClickable(enabled);
        card2.setClickable(enabled);
        card3.setClickable(enabled);
        card4.setClickable(enabled);
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
