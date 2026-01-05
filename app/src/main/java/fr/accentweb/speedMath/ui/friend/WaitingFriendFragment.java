package fr.accentweb.speedMath.ui.friend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.FirebaseDatabase;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.FriendManager;
import fr.accentweb.speedMath.core.PlayerManager;

public class WaitingFriendFragment extends Fragment {

    private FriendManager friendManager;
    private PlayerManager playerManager;
    private String roomCode;
    private FriendManager.RoomListener roomListener;

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

        roomCode = friendManager.createRoom(
                playerManager.getOnlineUid(),
                playerManager.getOnlinePseudo()
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
                    FirebaseDatabase.getInstance().getReference("friend_rooms")
                            .child(roomCode)
                            .child("guest_pseudo")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (!isAdded() || getView() == null) return;
                                String opponentPseudo = snapshot.getValue(String.class);

                                Bundle args = new Bundle();
                                args.putString("roomId", roomCode);
                                args.putString("player", "P1");
                                args.putString("myPseudo", playerManager.getOnlinePseudo());
                                args.putString("opponentPseudo", opponentPseudo != null ? opponentPseudo : "opponent");

                                Navigation.findNavController(requireView())
                                        .navigate(R.id.action_waitingFriendFragment_to_friendFragment, args);
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded() || getView() == null) return;
                                Bundle args = new Bundle();
                                args.putString("roomId", roomCode);
                                args.putString("player", "P1");
                                args.putString("myPseudo", playerManager.getOnlinePseudo());
                                args.putString("opponentPseudo", "opponent");

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

        btnCancel.setOnClickListener(v -> {
            friendManager.cleanupRoom(roomCode);
            Navigation.findNavController(v).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (roomListener != null) {
            friendManager.listenRoom(roomCode, null);
        }
    }
}
