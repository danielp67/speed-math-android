package com.example.speedMath.core;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;

import java.util.HashMap;

public class MatchmakingHelper {

    private static final String TAG = "MatchmakingHelper";
    private long rank;
    private long points;
    private DatabaseReference dbRef;
    private String uid;
    private String pseudo;
    private MatchListener listener;
    private String waitingKey;
    private ValueEventListener waitingListener;
    private boolean isMatchFound = false;

    public interface MatchListener {
        void onMatchFound(String matchId, String uid, String pseudo, long points, long rank, String opponentUid, String opponentPseudo, long opponentPoints, long opponentRank);
        void onError(String message);
    }

    public MatchmakingHelper(String uid, String pseudo, long points, long rank) {
        this.uid = uid;
        this.pseudo = pseudo;
        this.points = points;
        this.rank = rank;
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void setMatchListener(MatchListener listener) {
        this.listener = listener;
    }

    public void findMatch() {
        isMatchFound = false;
        DatabaseReference waitingRef = dbRef.child("waiting");

        waitingKey = waitingRef.push().getKey();
        long ts = System.currentTimeMillis();
        HashMap<String, Object> playerData = new HashMap<>();
        playerData.put("uid", uid);
        playerData.put("pseudo", pseudo);
        playerData.put("points", points);
        playerData.put("rank", rank);
        playerData.put("ts", ts);

        waitingRef.child(waitingKey).setValue(playerData).addOnFailureListener(e -> {
            if (listener != null) listener.onError("Erreur ajout à la file");
        });

        waitingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isMatchFound || snapshot.getChildrenCount() < 2) {
                    return;
                }

                DataSnapshot opponentSnap = null;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (!snap.getKey().equals(waitingKey)) {
                        opponentSnap = snap;
                        break;
                    }
                }

                if (opponentSnap != null) {
                    isMatchFound = true;
                    if (waitingListener != null) {
                        waitingRef.removeEventListener(waitingListener);
                    }

                    String opponentUid = opponentSnap.child("uid").getValue(String.class);
                    String opponentPseudo = opponentSnap.child("pseudo").getValue(String.class);
                    long opponentPoints = opponentSnap.child("points").getValue(Long.class);
                    long opponentRank = opponentSnap.child("rank").getValue(Long.class);

                    if (opponentUid == null) { // data inconsistency
                        isMatchFound = false; // reset and try again
                        return;
                    }

                    String matchId;
                    if (uid.compareTo(opponentUid) < 0) {
                        matchId = uid + "_" + opponentUid;
                        // I create the match and cleanup
                        createMatchWithId(matchId, uid, pseudo, points, rank, opponentUid, opponentPseudo, opponentPoints, opponentRank);
                        waitingRef.child(waitingKey).removeValue();
                        waitingRef.child(opponentSnap.getKey()).removeValue();
                    } else {
                        matchId = opponentUid + "_" + uid;
                    }

                    if (listener != null) {
                        listener.onMatchFound(matchId, uid, pseudo, points, rank, opponentUid, opponentPseudo, opponentPoints, opponentRank);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Matchmaking cancelled: " + error.getMessage());
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        };
        waitingRef.addValueEventListener(waitingListener);
    }

    public void cancelMatchmaking() {
        DatabaseReference waitingRef = dbRef.child("waiting");
        if (waitingListener != null) {
            waitingRef.removeEventListener(waitingListener);
            waitingListener = null;
        }
        if (waitingKey != null) {
            Log.d(TAG, "Cancelling matchmaking, removing user from waiting list.");
            waitingRef.child(waitingKey).removeValue();
            waitingKey = null;
        }
    }

    private void createMatchWithId(String matchId, String p1Uid, String p1Pseudo, long points, long rank, String p2Uid, String p2Pseudo, long opponentPoints, long opponentRank) {
        if (matchId == null) {
            if (listener != null) listener.onError("Impossible to create match");
            return;
        }

        HashMap<String, Object> matchData = new HashMap<>();
        matchData.put("p1_uid", p1Uid);
        matchData.put("p1_pseudo", p1Pseudo);
        matchData.put("p1_points", points);
        matchData.put("p1_ranking", rank);
        matchData.put("p2_uid", p2Uid);
        matchData.put("p2_pseudo", p2Pseudo);
        matchData.put("p2_points", opponentPoints);
        matchData.put("p2_ranking", opponentRank);
        matchData.put("state", "playing");
        matchData.put("p1_score", 0);
        matchData.put("p2_score", 0);
        matchData.put("winner", null);
        matchData.put("timestamp", System.currentTimeMillis());


        dbRef.child("matches").child(matchId).setValue(matchData)
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError("Error creating match");
                });
    }
}
