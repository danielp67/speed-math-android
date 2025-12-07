package com.example.speedMath.core;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;

import java.util.HashMap;

public class MatchmakingHelper {

    private static final String TAG = "MatchmakingHelper";
    private DatabaseReference dbRef;
    private String uid;
    private String pseudo;
    private MatchListener listener;

    public interface MatchListener {
        void onMatchFound(String matchId, String opponentUid, String opponentPseudo);
        void onError(String message);
    }

    public MatchmakingHelper(String uid, String pseudo) {
        this.uid = uid;
        this.pseudo = pseudo;
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void setMatchListener(MatchListener listener) {
        this.listener = listener;
    }

    public void findMatch() {
        DatabaseReference waitingRef = dbRef.child("waiting");

        // Ajout du joueur à la file d'attente
        String waitingKey = waitingRef.push().getKey();
        long ts = System.currentTimeMillis();
        HashMap<String, Object> playerData = new HashMap<>();
        playerData.put("uid", uid);
        playerData.put("pseudo", pseudo);
        playerData.put("ts", ts);

        waitingRef.child(waitingKey).setValue(playerData)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (listener != null) listener.onError("Erreur ajout à la file");
                        return;
                    }
                    // Ecoute la file d'attente pour trouver un adversaire
                    waitingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                HashMap<String, Object> p = (HashMap<String, Object>) snap.getValue();
                                if (p == null) continue;
                                String otherUid = (String) p.get("uid");
                                String otherPseudo = (String) p.get("pseudo");
                                if (!otherUid.equals(uid)) {
                                    // Match trouvé, créer match
                                    createMatch(otherUid, otherPseudo);
                                    waitingRef.child(waitingKey).removeValue(); // supprime moi-même
                                    waitingRef.child(snap.getKey()).removeValue(); // supprime adversaire
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (listener != null) listener.onError(error.getMessage());
                        }
                    });
                });
    }

    private void createMatch(String opponentUid, String opponentPseudo) {
        String matchId = dbRef.child("matches").push().getKey();
        if (matchId == null) {
            if (listener != null) listener.onError("Impossible de créer le match");
            return;
        }

        HashMap<String, Object> matchData = new HashMap<>();
        matchData.put("p1_uid", uid);
        matchData.put("p1_pseudo", pseudo);
        matchData.put("p2_uid", opponentUid);
        matchData.put("p2_pseudo", opponentPseudo);
        matchData.put("state", "playing");
        matchData.put("p1_score", 0);
        matchData.put("p2_score", 0);

        dbRef.child("matches").child(matchId).setValue(matchData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && listener != null) {
                        listener.onMatchFound(matchId, opponentUid, opponentPseudo);
                    } else if (listener != null) {
                        listener.onError("Erreur création match");
                    }
                });
    }
}
