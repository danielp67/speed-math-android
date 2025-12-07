package com.example.speedMath.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MatchmakingHelper {
    private static final String TAG = "MatchmakingHelper";

    private final DatabaseReference db;
    private final String uid;
    private final String pseudo;

    // key push dans /waiting pour pouvoir supprimer plus tard
    private String waitingKey = null;
    private DatabaseReference waitingRef;
    private ChildEventListener waitingListener;

    // évite double création de match
    private final AtomicBoolean matched = new AtomicBoolean(false);

    private MatchListener listener;

    public interface MatchListener {
        void onMatchFound(String matchId, String opponentUid, String opponentPseudo);
        void onError(String message);
    }

    public MatchmakingHelper(@NonNull String uid, @NonNull String pseudo) {
        this.uid = uid;
        this.pseudo = pseudo;
        this.db = FirebaseDatabase.getInstance().getReference();
    }

    public void setMatchListener(@Nullable MatchListener l) {
        this.listener = l;
    }

    /**
     * Inscrit le joueur dans /waiting et écoute les autres joueurs pour créer un match 1v1.
     */
    public void findMatch() {
        if (matched.get()) return;

        waitingRef = db.child("waiting");

        // crée une entrée push (waitingKey)
        waitingKey = waitingRef.push().getKey();
        if (waitingKey == null) {
            if (listener != null) listener.onError("Impossible d'obtenir waitingKey");
            return;
        }

        long ts = System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", uid);
        payload.put("pseudo", pseudo);
        payload.put("ts", ts);

        waitingRef.child(waitingKey).setValue(payload)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (listener != null) listener.onError("Erreur ajout à la file");
                        return;
                    }
                    // on écoute les nouveaux enfants pour détecter un adversaire
                    attachWaitingListener();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError("Erreur ajout: " + e.getMessage());
                });
    }

    private void attachWaitingListener() {
        if (waitingRef == null) waitingRef = db.child("waiting");

        // Defensive: si listener déjà attaché, ne pas ré-attacher
        if (waitingListener != null) return;

        waitingListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (matched.get()) return;

                // Récupère les données
                String otherKey = snapshot.getKey();
                if (otherKey == null) return;

                // Si c'est nous, on ignore
                if (otherKey.equals(waitingKey)) return;

                Object oUid = snapshot.child("uid").getValue();
                Object oPseudo = snapshot.child("pseudo").getValue();

                if (oUid == null || oPseudo == null) return;

                String otherUid = oUid.toString();
                String otherPseudo = oPseudo.toString();

                // Safety: ne pas matcher contre soi
                if (otherUid.equals(uid)) return;

                // marque comme matched (atomic) pour éviter double création
                if (!matched.compareAndSet(false, true)) return;

                // retirer les deux entrées waiting (la nôtre et celle de l'autre)
                // et créer le match
                createMatchWithOpponent(otherUid, otherPseudo, otherKey);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Waiting listener cancelled: " + error.getMessage());
                if (listener != null) listener.onError(error.getMessage());
            }
        };

        waitingRef.addChildEventListener(waitingListener);
    }

    private void createMatchWithOpponent(String opponentUid, String opponentPseudo, String opponentWaitingKey) {
        // remove waiting entries (not strictly necessary before creating match but good hygiene)
        if (waitingRef != null) {
            // remove opponent entry and our own entry
            waitingRef.child(opponentWaitingKey).removeValue();
            if (waitingKey != null) waitingRef.child(waitingKey).removeValue();
        }

        // create match node
        String matchId = db.child("matches").push().getKey();
        if (matchId == null) {
            if (listener != null) listener.onError("Impossible de créer matchId");
            return;
        }

        Map<String, Object> match = new HashMap<>();
        match.put("p1_uid", uid);
        match.put("p1_pseudo", pseudo);
        match.put("p2_uid", opponentUid);
        match.put("p2_pseudo", opponentPseudo);
        match.put("state", "playing");
        match.put("p1_score", 0);
        match.put("p2_score", 0);
        match.put("ts", System.currentTimeMillis());

        db.child("matches").child(matchId).setValue(match)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // notify listener
                        if (listener != null) listener.onMatchFound(matchId, opponentUid, opponentPseudo);
                        // cleanup local waiting entry and listeners
                        cleanupWaiting();
                    } else {
                        if (listener != null) listener.onError("Erreur création match");
                        matched.set(false); // rollback possible
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError("Erreur creation match: " + e.getMessage());
                    matched.set(false);
                });
    }

    /**
     * Annule la recherche de match : supprime l'entrée waiting et détache les listeners.
     * Appeler depuis UI quand user annule / back / onDestroyView.
     */
    public void cancel() {
        // marque matched pour éviter race
        matched.set(true);
        cleanupWaiting();
    }

    private void cleanupWaiting() {
        try {
            if (waitingRef != null && waitingKey != null) {
                waitingRef.child(waitingKey).removeValue();
            }
            if (waitingRef != null && waitingListener != null) {
                waitingRef.removeEventListener(waitingListener);
            }
        } catch (Exception e) {
            Log.w(TAG, "cleanupWaiting failed: " + e.getMessage());
        } finally {
            waitingKey = null;
            waitingListener = null;
            waitingRef = null;
        }
    }
}
