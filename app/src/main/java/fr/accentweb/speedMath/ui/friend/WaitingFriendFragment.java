package fr.accentweb.speedMath.ui.friend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.FriendManager;
import fr.accentweb.speedMath.core.PlayerManager;

public class WaitingFriendFragment extends Fragment {

    private FriendManager friendManager;
    private PlayerManager playerManager;

    private String roomCode;

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

        // Création de la room
        roomCode = friendManager.createRoom(playerManager.getOnlineUid(), playerManager.getOnlinePseudo());
        txtRoomCode.setText(roomCode);
        txtStatus.setText("Waiting for your friend…");

        // Écoute de la room
        friendManager.listenRoom(roomCode, status -> {
            if ("ready".equals(status)) {
                txtStatus.setText("Friend joined!");

                Bundle args = new Bundle();
                args.putString("ROOM_CODE", roomCode);

                Navigation.findNavController(requireView())
                        .navigate(R.id.action_waitingFriendFragment_to_friendFragment, args);
            }
        });

        btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }
}
