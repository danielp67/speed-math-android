package com.example.speedMath.core;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
public class DailyMatchManager {
    private static final String TAG = "DailyMatchManager";
    private static DailyMatchManager instance;
    private final DatabaseReference systemRef;
    private final DatabaseReference playerRef;
    private int dailyMatchLimit = 5;  // Valeur par défaut
    private String uid;

    private DailyMatchManager(Context context) {
        this.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.systemRef = FirebaseDatabase.getInstance().getReference("system");
        this.playerRef = FirebaseDatabase.getInstance().getReference("players").child(uid);

        // Charge la limite depuis Firebase
        loadDailyMatchLimit();
    }

    public static synchronized DailyMatchManager getInstance(Context context) {
        if (instance == null) {
            instance = new DailyMatchManager(context);
        }
        return instance;
    }

    // Charge la limite quotidienne depuis Firebase
    private void loadDailyMatchLimit() {
        systemRef.child("dailyMatchLimit").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    dailyMatchLimit = snapshot.getValue(Integer.class);
                    Log.d(TAG, "Limite quotidienne chargée: " + dailyMatchLimit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erreur de chargement de la limite: " + error.getMessage());
            }
        });
    }

    // Vérifie si le joueur peut jouer une nouvelle partie
    public void checkDailyMatchLimit(DailyMatchLimitCallback callback) {
        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onResult(false, 0, dailyMatchLimit);
                    return;
                }

                String lastConnection = snapshot.child("lastConnection").getValue(String.class);
                int dailyMatchPlayed = snapshot.child("dailyMatchPlayed").getValue(Integer.class) != null ?
                        snapshot.child("dailyMatchPlayed").getValue(Integer.class) : 0;

                String today = getTodayDate();

                // Réinitialise si lastConnection est antérieur à aujourd'hui
                if (lastConnection == null || !lastConnection.equals(today)) {
                    resetDailyMatchCount(today);
                    callback.onResult(true, 0, dailyMatchLimit);  // Peut jouer (compteur réinitialisé)
                } else {
                    callback.onResult(dailyMatchPlayed < dailyMatchLimit, dailyMatchPlayed, dailyMatchLimit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false, 0, dailyMatchLimit);
            }
        });
    }

    // Incrémente le compteur de parties quotidiennes
    public void incrementDailyMatchCount() {
        String today = getTodayDate();
        playerRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                String lastConnection = currentData.child("lastConnection").getValue(String.class);
                int dailyMatchPlayed = currentData.child("dailyMatchPlayed").getValue(Integer.class) != null ?
                        currentData.child("dailyMatchPlayed").getValue(Integer.class) : 0;

                // Réinitialise si lastConnection est antérieur à aujourd'hui
                if (lastConnection == null || !lastConnection.equals(today)) {
                    currentData.child("lastConnection").setValue(today);
                    currentData.child("dailyMatchPlayed").setValue(1);
                } else {
                    currentData.child("dailyMatchPlayed").setValue(dailyMatchPlayed + 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Erreur de transaction: " + error.getMessage());
                }
            }
        });
    }

    // Réinitialise le compteur quotidien
    private void resetDailyMatchCount(String today) {
        playerRef.child("lastConnection").setValue(today);
        playerRef.child("dailyMatchPlayed").setValue(0);
    }

    // Récupère la date du jour (format yyyy-MM-dd)
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    public interface DailyMatchLimitCallback {
        void onResult(boolean canPlay, int currentCount, int limit);
    }
}
