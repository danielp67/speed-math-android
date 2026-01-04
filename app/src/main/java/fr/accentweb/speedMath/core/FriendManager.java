package fr.accentweb.speedMath.core;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FriendManager {
    private static FriendManager instance;
    private final DatabaseReference roomsRef;
    private ValueEventListener roomListener;



    private FriendManager() {
        roomsRef = FirebaseDatabase.getInstance().getReference("friend_rooms");
    }

    public static FriendManager getInstance() {
        if (instance == null) {
            instance = new FriendManager();
        }
        return instance;
    }

    public String createRoom(String uid, String pseudo) {
        String roomCode = generateRoomCode();

        Map<String, Object> room = new HashMap<>();
        room.put("host_uid", uid);
        room.put("host_pseudo", pseudo);
        room.put("guest_uid", null);
        room.put("guest_pseudo", null);
        room.put("status", "waiting");
        room.put("timestamp", System.currentTimeMillis());

        roomsRef.child(roomCode).setValue(room);
        return roomCode;
    }

    public void joinRoom(String roomCode, String uid, String pseudo, RoomCallback callback) {
        roomsRef.child(roomCode).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("Connection error");
                return;
            }

            DataSnapshot snapshot = task.getResult();
            if (!snapshot.exists()) {
                callback.onError("Room not found");
                return;
            }

            String status = snapshot.child("status").getValue(String.class);
            if (!"waiting".equals(status)) {
                callback.onError("Room already completed");
                return;
            }

            String hostUid = snapshot.child("host_uid").getValue(String.class);
            if (uid.equals(hostUid)) {
                callback.onError("You cannot join your own room");
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("guest_uid", uid);
            updates.put("guest_pseudo", pseudo);
            updates.put("status", "ready");

            roomsRef.child(roomCode).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError("Error during connexion"));
        });
    }

    public void listenRoom(String roomCode, RoomListener listener) {
        if (listener == null) {
            roomsRef.child(roomCode).removeEventListener(roomListener);
            return;
        }

        roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onError("Room deleted");
                    return;
                }

                String status = snapshot.child("status").getValue(String.class);
                if (status != null) {
                    listener.onStatusChanged(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        };

        roomsRef.child(roomCode).addValueEventListener(roomListener);
    }


    public void startGame(String roomCode) {
        roomsRef.child(roomCode).child("status").setValue("playing");
    }

    public void cleanupRoom(String roomCode) {
        roomsRef.child(roomCode).removeValue();
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public interface RoomCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface RoomListener {
        void onStatusChanged(String status);
        void onError(String error);
    }
}
