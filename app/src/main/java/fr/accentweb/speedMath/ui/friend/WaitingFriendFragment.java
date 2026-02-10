package fr.accentweb.speedMath.ui.friend;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;

import com.google.firebase.database.FirebaseDatabase;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FriendManager;
import fr.accentweb.speedMath.core.PlayerManager;

public class WaitingFriendFragment extends BaseGameFragment {

    private FriendManager friendManager;
    private PlayerManager playerManager;
    private String roomCode;
    private FriendManager.RoomListener roomListener;
    private boolean matchStarted = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_waiting_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        friendManager = FriendManager.getInstance();
        playerManager = PlayerManager.getInstance(requireContext());

        TextView txtRoomCode = view.findViewById(R.id.txtRoomCode);
        TextView txtStatus = view.findViewById(R.id.txtStatus);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnCopy = view.findViewById(R.id.btnCopy);
        Button btnShare = view.findViewById(R.id.btnShare);

        roomCode = friendManager.createRoom(
                playerManager.getOnlineUid(),
                playerManager.getOnlinePseudo(),
                playerManager.getRank()
        );
        txtRoomCode.setText(roomCode);
        txtStatus.setText("Waiting for your friend...");

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        friendManager.cleanupRoom(roomCode);
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
        );

        roomListener = new FriendManager.RoomListener() {
            @Override
            public void onStatusChanged(String status) {
                if (!isAdded() || getView() == null) return;

                if ("ready".equals(status)) {
                    matchStarted = true;
                    FirebaseDatabase.getInstance().getReference("friend_rooms")
                            .child(roomCode)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (!isAdded() || getView() == null) return;
                                String opponentPseudo = snapshot.child("guest_pseudo").getValue(String.class);
                                Long opponentRank = snapshot.child("guest_rank").getValue(Long.class);

                                Bundle args = new Bundle();
                                args.putString("roomId", roomCode);
                                args.putString("player", "P1");
                                args.putString("myPseudo", playerManager.getOnlinePseudo());
                                args.putLong("myRank", playerManager.getRank());
                                args.putString("opponentPseudo", opponentPseudo != null ? opponentPseudo : "opponent");
                                args.putLong("opponentRank", opponentRank != null ? opponentRank : 999999);
                                Navigation.findNavController(requireView())
                                        .navigate(R.id.action_waitingFriendFragment_to_friendFragment, args);
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded() || getView() == null) return;
                                Bundle args = new Bundle();
                                args.putString("roomId", roomCode);
                                args.putString("player", "P1");
                                args.putString("myPseudo", playerManager.getOnlinePseudo());
                                args.putLong("myRank", playerManager.getRank());
                                args.putString("opponentPseudo", "opponent");
                                args.putLong("opponentRank", 999999);

                                Navigation.findNavController(requireView())
                                        .navigate(R.id.action_waitingFriendFragment_to_friendFragment, args);
                            });
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded() || getView() == null) return;
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        };

        friendManager.listenRoom(roomCode, roomListener);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard =
                    (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText("Room Code", roomCode);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getContext(), "Code copied 📋", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Join me on Speed Math!\nRoom code: " + roomCode
            );

            startActivity(Intent.createChooser(shareIntent, "Share room code"));
        });

        btnCancel.setOnClickListener(v -> {
            friendManager.cleanupRoom(roomCode);
            Navigation.findNavController(v).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (roomCode != null && !matchStarted) {
            friendManager.cleanupRoom(roomCode);
        }

        if (roomListener != null && !matchStarted) {
            friendManager.listenRoom(roomCode, null);
        }
    }
}
