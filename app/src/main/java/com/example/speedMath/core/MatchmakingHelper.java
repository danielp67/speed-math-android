package com.example.speedMath.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MatchmakingHelper {

    public interface MatchCallback {
        void onMatchFound(String matchId, String opponentUid, String opponentPseudo);
    }

    private final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private final String uid;
    private final String pseudo;
    private String waitingKey = null;
    private boolean cancelled = false;

    public MatchmakingHelper(String uid, String pseudo) {
        this.uid = uid;
        this.pseudo = pseudo;
    }

    public void cancel() {
        cancelled = true;
        if (waitingKey != null) {
            db.child("waiting").child(waitingKey).removeValue();
        }
    }

    public void findMatch(MatchCallback callback) {

        // 1) Chercher un adversaire
        db.child("waiting").limitToFirst(1).get().addOnCompleteListener(task -> {

            if (!task.isSuccessful() || cancelled) return;

            DataSnapshot snap = task.getResult();

            // 1A) SI IL Y A UN JOUEUR EN ATTENTE
            if (snap.exists()) {
                DataSnapshot first = snap.getChildren().iterator().next();

                String opponentUid = first.child("uid").getValue(String.class);
                String opponentPseudo = first.child("pseudo").getValue(String.class);

                // On ne se matche pas soi-même
                if (opponentUid != null && opponentUid.equals(uid)) return;

                // Créer match
                String matchId = db.child("matches").push().getKey();

                HashMap<String, Object> p1 = new HashMap<>();
                p1.put("uid", uid);
                p1.put("pseudo", pseudo);

                HashMap<String, Object> p2 = new HashMap<>();
                p2.put("uid", opponentUid);
                p2.put("pseudo", opponentPseudo);

                HashMap<String, Object> create = new HashMap<>();
                create.put("p1", p1);
                create.put("p2", p2);
                create.put("state", "playing");


                db.child("matches").child(matchId).setValue(create);

                db.child("waiting").child(first.getKey()).removeValue();

                if (cancelled) return;

                callback.onMatchFound(matchId, opponentUid, opponentPseudo);
            }

            // 1B) Sinon : on s'ajoute à la liste d'attente
            else {
                waitingKey = db.child("waiting").push().getKey();

                HashMap<String, Object> data = new HashMap<>();
                data.put("uid", uid);
                data.put("pseudo", pseudo);
                data.put("ts", System.currentTimeMillis());

                db.child("waiting").child(waitingKey).setValue(data);

                // on écoute pour voir si quelqu'un nous matche
                waitForMatch(callback);
            }

        });
    }

    private void waitForMatch(MatchCallback callback) {

        db.child("matches").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (cancelled) return;

                for (DataSnapshot m : snapshot.getChildren()) {
                    String p1 = m.child("p1").child("uid").getValue(String.class);
                    String p2 = m.child("p2").child("uid").getValue(String.class);

                    if (uid.equals(p1) || uid.equals(p2)) {

                        String opponentUid = uid.equals(p1) ? p2 : p1;
                        String opponentPseudo = uid.equals(p1)
                                ? m.child("p2").child("pseudo").getValue(String.class)
                                : m.child("p1").child("pseudo").getValue(String.class);

                        callback.onMatchFound(m.getKey(), opponentUid, opponentPseudo);
                        return;
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
