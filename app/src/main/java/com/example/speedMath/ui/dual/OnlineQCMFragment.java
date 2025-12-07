package com.example.speedMath.ui.dual;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speedMath.R;
import com.example.speedMath.core.PlayerManager;
import com.example.speedMath.core.QuestionGenerator;
import com.example.speedMath.core.GameTimer;
import com.example.speedMath.utils.AnimUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OnlineQCMFragment extends Fragment {

    private static final String TAG = "OnlineQCM";

    // UI
    private TextView textQuestion, textResult, textTimer, textScoreRight, textOpponent;
    private CardView card1, card2, card3, card4;
    private TextView t1, t2, t3, t4;
    private TextView textCombo;

    // Game logic
    private QuestionGenerator questionGenerator;
    private GameTimer gameTimer;
    private PlayerManager playerManager;

    // Firebase
    private DatabaseReference dbRoot;
    private DatabaseReference waitingRef;
    private DatabaseReference gamesRef;
    private DatabaseReference myGameRef;

    private String myUid;
    private String myPseudo;
    private String opponentUid;
    private String opponentPseudo;
    private String myWaitingKey;   // push key in waiting/
    private String gameId;
    private boolean isHost = false;
    private boolean finished = false;

    // game state
    private int correctAnswer;
    private int score = 0;
    private int nbQuestions = 5; // default, récupérable depuis PlayerManager
    private int combo = 0;

    public OnlineQCMFragment() { /* empty */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qcm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbRoot = FirebaseDatabase.getInstance().getReference();
        waitingRef = dbRoot.child("waiting");
        gamesRef = dbRoot.child("online_games");

        playerManager = PlayerManager.getInstance(requireContext());
        myPseudo = playerManager.getPseudo() != null ? playerManager.getPseudo() : "Player";
        myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : playerManager.getPlayerId(); // fallback

        // UI refs
        textQuestion = view.findViewById(R.id.textQuestion);
        textResult = view.findViewById(R.id.textResult);
        textTimer = view.findViewById(R.id.textTimer);
        textScoreRight = view.findViewById(R.id.textScoreRight);
        textOpponent = view.findViewById(R.id.textResult); // remplace par un vrai textOpponent si possible
        textCombo = view.findViewById(R.id.textCombo);

        card1 = view.findViewById(R.id.cardOption1);
        card2 = view.findViewById(R.id.cardOption2);
        card3 = view.findViewById(R.id.cardOption3);
        card4 = view.findViewById(R.id.cardOption4);

        t1 = card1.findViewById(R.id.textOption);
        t2 = card2.findViewById(R.id.textOption);
        t3 = card3.findViewById(R.id.textOption);
        t4 = card4.findViewById(R.id.textOption);

        // click handlers
        card1.setOnClickListener(v -> submitAnswer(t1));
        card2.setOnClickListener(v -> submitAnswer(t2));
        card3.setOnClickListener(v -> submitAnswer(t3));
        card4.setOnClickListener(v -> submitAnswer(t4));

        // set nbQuestions from PlayerManager
        int settingNb = playerManager.getNbQuestions();
        switch (settingNb) {
            case 1: nbQuestions = 10; break;
            case 2: nbQuestions = 20; break;
            case 3: nbQuestions = 25; break;
            default: nbQuestions = 5;
        }
        textScoreRight.setText(score + "/" + nbQuestions);

        // Question generator (host will write actual questions into DB)
        questionGenerator = new QuestionGenerator(50, 2, true, true, true, true, true, true);

        // Timer
        gameTimer = new GameTimer();
        gameTimer.setListener((elapsed, formatted) -> {
            if (textTimer != null) textTimer.setText(formatted);
        });

        // start matchmaking
        findOpponentAndMatchmake();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup: remove waiting entry if exists
        if (myWaitingKey != null) waitingRef.child(myWaitingKey).removeValue();
        // detach listeners
        if (myGameRef != null) myGameRef.removeEventListener(gameListener);
    }

    // ---------- Matchmaking ----------
    private void findOpponentAndMatchmake() {
        // 1) push ourselves into waiting/
        Map<String, Object> me = new HashMap<>();
        me.put("uid", myUid);
        me.put("pseudo", myPseudo);
        me.put("ts", ServerValue.TIMESTAMP);

        myWaitingKey = waitingRef.push().getKey();
        waitingRef.child(myWaitingKey).setValue(me).addOnCompleteListener(t -> {
            if (!t.isSuccessful()) {
                Toast.makeText(requireContext(), "Erreur matchmaking", Toast.LENGTH_SHORT).show();
                return;
            }
            // 2) read waiting list and if there is another player, pair
            waitingRef.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || task.getResult() == null) {
                    // wait for others via listener
                    attachWaitingListener();
                    return;
                }
                DataSnapshot snap = task.getResult();
                // collect first two waiting keys (including ourselves)
                ArrayList<String> keys = new ArrayList<>();
                for (DataSnapshot c : snap.getChildren()) {
                    keys.add(c.getKey());
                }
                // if >1, we can make a match
                if (keys.size() >= 2) {
                    Collections.sort(keys); // deterministic order
                    String k1 = keys.get(0);
                    String k2 = keys.get(1);
                    // the player whose key equals k1 will be the host (creator)
                    // only the host creates the game node
                    if (k1.equals(myWaitingKey)) {
                        DataSnapshot c1 = snap.child(k1);
                        DataSnapshot c2 = snap.child(k2);
                        String uid1 = c1.child("uid").getValue(String.class);
                        String uid2 = c2.child("uid").getValue(String.class);
                        String pseudo1 = c1.child("pseudo").getValue(String.class);
                        String pseudo2 = c2.child("pseudo").getValue(String.class);
                        createGameWith(uidsToArray(uid1, uid2), new String[]{pseudo1, pseudo2});
                    } else {
                        // not host: attach listener to waiting node to see when host creates game
                        attachWaitingListener();
                    }
                } else {
                    // not enough players yet -> listen
                    attachWaitingListener();
                }
            });
        });
    }

    private void attachWaitingListener() {
        // If someone else joins, the code above (when that player pushes) will call get() on waiting
        // but to be robust, we also listen to childAdded: if a new child appears and we are second,
        // the earlier code will handle creation. This listener is mainly to stay updated & fallback.
        waitingRef.addValueEventListener(waitingListener);
    }

    private final ValueEventListener waitingListener = new ValueEventListener() {
        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (finished) return;
            if (!snapshot.exists()) return;
            ArrayList<String> keys = new ArrayList<>();
            for (DataSnapshot c : snapshot.getChildren()) keys.add(c.getKey());
            if (keys.size() >= 2) {
                Collections.sort(keys);
                String k1 = keys.get(0);
                String k2 = keys.get(1);
                // if we are k1 -> create, else wait for game node to appear
                if (k1.equals(myWaitingKey)) {
                    DataSnapshot c1 = snapshot.child(k1);
                    DataSnapshot c2 = snapshot.child(k2);
                    String uid1 = c1.child("uid").getValue(String.class);
                    String uid2 = c2.child("uid").getValue(String.class);
                    String pseudo1 = c1.child("pseudo").getValue(String.class);
                    String pseudo2 = c2.child("pseudo").getValue(String.class);
                    createGameWith(uidsToArray(uid1, uid2), new String[]{pseudo1, pseudo2});
                } else {
                    // do nothing; wait for the created game node
                }
            }
        }
        @Override public void onCancelled(@NonNull DatabaseError error) {}
    };

    private String[] uidsToArray(String a, String b) {
        return new String[]{a, b};
    }

    // ---------- Create Game ----------
    private void createGameWith(String[] uids, String[] pseudos) {
        // Only host reaches here (deterministic)
        isHost = true;
        // create a new game node
        DatabaseReference newGameRef = gamesRef.push();
        gameId = newGameRef.getKey();
        myGameRef = gamesRef.child(gameId);

        // setup players subnode
        Map<String, Object> players = new HashMap<>();
        players.put(uids[0], createPlayerMap(pseudos[0], 0));
        players.put(uids[1], createPlayerMap(pseudos[1], 0));

        Map<String, Object> initial = new HashMap<>();
        initial.put("players", players);
        initial.put("finished", false);
        initial.put("createdAt", ServerValue.TIMESTAMP);
        initial.put("currentIndex", 0);

        newGameRef.setValue(initial).addOnCompleteListener(t -> {
            if (!t.isSuccessful()) {
                Log.w(TAG, "createGame failed");
                return;
            }
            // remove both waiting entries
            // search waiting entries for these uids and remove them
            waitingRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DataSnapshot c : task.getResult().getChildren()) {
                        String uid = c.child("uid").getValue(String.class);
                        if (uid != null && (uid.equals(uids[0]) || uid.equals(uids[1]))) {
                            waitingRef.child(c.getKey()).removeValue();
                        }
                    }
                }
                // start game as host by pushing first question
                writeNextQuestionAsHost();
                attachGameListener(newGameRef);
            });
        });
    }

    private Map<String, Object> createPlayerMap(String pseudo, int score) {
        Map<String, Object> m = new HashMap<>();
        m.put("pseudo", pseudo);
        m.put("score", score);
        m.put("lastAnswer", null);
        return m;
    }

    // ---------- Join Game (non-host) ----------
    private void attachGameListener(DatabaseReference gameRef) {
        myGameRef = gameRef;
        myGameRef.addValueEventListener(gameListener);
        // start timer locally when game exists
        if (gameTimer != null) gameTimer.start();
    }

    private final ValueEventListener gameListener = new ValueEventListener() {
        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.exists()) return;

            // lazy read players to find opponent uid/pseudo
            DataSnapshot playersSnap = snapshot.child("players");
            if (playersSnap.exists()) {
                for (DataSnapshot p : playersSnap.getChildren()) {
                    String uid = p.getKey();
                    String pseudo = p.child("pseudo").getValue(String.class);
                    if (!uid.equals(myUid)) {
                        opponentUid = uid;
                        opponentPseudo = pseudo;
                    }
                }
            }
            // show opponent name
            if (opponentPseudo != null && textOpponent != null) {
                textOpponent.setText(opponentPseudo);
            }

            // read current question (host writes it in /currentQuestion)
            DataSnapshot qSnap = snapshot.child("currentQuestion");
            if (qSnap.exists()) {
                String expr = qSnap.child("expression").getValue(String.class);
                Long answerLong = qSnap.child("answer").getValue(Long.class);
                ArrayList<Long> choices = new ArrayList<>();
                for (DataSnapshot c : qSnap.child("choices").getChildren()) {
                    Long v = c.getValue(Long.class);
                    choices.add(v);
                }
                // set UI from db question
                if (expr != null && choices.size() >= 4 && answerLong != null) {
                    textQuestion.setText(expr);
                    correctAnswer = answerLong.intValue();
                    Collections.shuffle(choices); // optional shuffle client-side
                    t1.setText(String.valueOf(choices.get(0)));
                    t2.setText(String.valueOf(choices.get(1)));
                    t3.setText(String.valueOf(choices.get(2)));
                    t4.setText(String.valueOf(choices.get(3)));
                    setCardsClickable(true);
                }
            }

            // update opponent score if changed
            if (playersSnap.exists() && playersSnap.child(opponentUid).exists()) {
                Integer oppScore = playersSnap.child(opponentUid).child("score").getValue(Integer.class);
                // update UI, e.g. textOpponent
                if (oppScore != null && textOpponent != null) {
                    textOpponent.setText(opponentPseudo + " : " + oppScore);
                }
            }

            // check finished flag
            Boolean fin = snapshot.child("finished").getValue(Boolean.class);
            if (fin != null && fin && !finished) {
                finished = true;
                showEndScreenFromSnapshot(snapshot);
            }
        }
        @Override public void onCancelled(@NonNull DatabaseError error) {}
    };

    // ---------- Host writes question ----------
    private void writeNextQuestionAsHost() {
        if (!isHost || myGameRef == null) return;

        // generate question with QuestionGenerator
        QuestionGenerator.MathQuestion q = questionGenerator.generateQuestion();
        int answer = q.answer;

        // build choices (we assume q.answersChoice exists)
        ArrayList<Integer> choices = new ArrayList<>();
        choices.addAll(q.answersChoice);

        Map<String, Object> qmap = new HashMap<>();
        qmap.put("expression", q.expression);
        qmap.put("answer", answer);
        qmap.put("choices", choices);
        // push under /online_games/{gameId}/currentQuestion and increment currentIndex
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentQuestion", qmap);
        updates.put("currentIndex", ServerValue.increment(1));
        myGameRef.updateChildren(updates);
    }

    // ---------- Answer submission ----------
    private void submitAnswer(TextView selected) {
        if (finished || myGameRef == null) return;
        setCardsClickable(false);
        int value;
        try {
            value = Integer.parseInt(selected.getText().toString());
        } catch (Exception e) {
            return;
        }
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
            textCombo.setAlpha(0f);
        }
        // update our player node in DB
        Map<String, Object> upd = new HashMap<>();
        upd.put("score", score);
        upd.put("lastAnswer", value);
        myGameRef.child("players").child(myUid).updateChildren(upd);

        updateLocalScoreUI();

        // if we reached target -> finish game
        if (score >= nbQuestions) {
            finishGameAndSetWinner();
            return;
        }

        // If host, create next question after short delay so opponent sees last state
        if (isHost) {
            selected.postDelayed(this::writeNextQuestionAsHost, 800);
        }
    }

    private void updateLocalScoreUI() {
        if (textScoreRight != null) textScoreRight.setText(score + "/" + nbQuestions);
    }

    private void finishGameAndSetWinner() {
        if (myGameRef == null) return;
        // mark finished = true; optionally compute final scores & winner server-side (host)
        myGameRef.child("finished").setValue(true);
        // Push final timestamp
        myGameRef.child("finishedAt").setValue(ServerValue.TIMESTAMP);
        // as host you could compute winner here and write "result" node
        if (isHost) {
            // host can compute winner: read players and set winner string
            myGameRef.child("players").get().addOnCompleteListener(t -> {
                if (!t.isSuccessful() || t.getResult() == null) return;
                DataSnapshot snap = t.getResult();
                Integer s1 = snap.child(myUid).child("score").getValue(Integer.class);
                Integer s2 = snap.child(opponentUid).child("score").getValue(Integer.class);
                String winner;
                if (s1 == null) s1 = 0;
                if (s2 == null) s2 = 0;
                if (s1 > s2) winner = myUid;
                else if (s2 > s1) winner = opponentUid;
                else winner = "draw";
                myGameRef.child("result").setValue(winner);
            });
        }
    }

    private void showEndScreenFromSnapshot(DataSnapshot snap) {
        // show result to user
        String result = snap.child("result").getValue(String.class);
        // fallback: compare local scores
        if (result == null) {
            // simple fallback: compare scores locally
            // read opponent score:
            Integer oppScore = snap.child("players").child(opponentUid).child("score").getValue(Integer.class);
            if (oppScore == null) oppScore = 0;
            if (score > oppScore) textResult.setText("You win 🎉");
            else if (score < oppScore) textResult.setText("You lose ❌");
            else textResult.setText("Draw 🤝");
        } else if ("draw".equals(result)) {
            textResult.setText("Draw 🤝");
        } else if (result.equals(myUid)) {
            textResult.setText("You win 🎉");
        } else {
            textResult.setText("You lose ❌");
        }
        // stop timer
        if (gameTimer != null) gameTimer.stop();
    }

    private void setCardsClickable(boolean c) {
        if (card1 != null) card1.setClickable(c);
        if (card2 != null) card2.setClickable(c);
        if (card3 != null) card3.setClickable(c);
        if (card4 != null) card4.setClickable(c);
    }
}
