package fr.accentweb.speedMath.ui.friend;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.FirebaseDatabase;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.FriendManager;
import fr.accentweb.speedMath.core.PlayerManager;

public class JoinFriendFragment extends Fragment {

    private FriendManager friendManager;
    private PlayerManager playerManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_join_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        friendManager = FriendManager.getInstance();
        playerManager = PlayerManager.getInstance(requireContext());

        EditText inputRoomCode = view.findViewById(R.id.inputRoomCode);
        Button btnJoin = view.findViewById(R.id.btnJoin);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnJoin.setOnClickListener(v -> {
            String code = inputRoomCode.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(code)) {
                inputRoomCode.setError("Enter a room code");
                return;
            }

            btnJoin.setEnabled(false);
            btnJoin.setText("Connection...");

            friendManager.joinRoom(code, playerManager.getOnlineUid(), playerManager.getOnlinePseudo(), playerManager.getRank(),
                    new FriendManager.RoomCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded() || getView() == null) return;

                            Bundle args = new Bundle();
                            args.putString("roomId", code);
                            args.putString("player", "P2");  // player always P2
                            args.putString("myPseudo", playerManager.getOnlinePseudo());
                            args.putLong("myRank", playerManager.getRank());

                            // Fetch data from Firebase
                            FirebaseDatabase.getInstance().getReference("friend_rooms")
                                    .child(code)
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        if (!isAdded() || getView() == null) return;
                                        String hostPseudo = snapshot.child("host_pseudo").getValue(String.class);
                                        Long hostRank = snapshot.child("host_rank").getValue(Long.class);

                                        args.putString("opponentPseudo", hostPseudo != null ? hostPseudo : "opponent");
                                        args.putLong("opponentRank", hostRank != null ? hostRank : 999999);

                                        Navigation.findNavController(requireView())
                                                .navigate(R.id.action_joinFriendFragment_to_friendFragment, args);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!isAdded() || getView() == null) return;
                                        args.putString("opponentPseudo", "opponent");
                                        args.putLong("opponentRank", 999999);

                                        Navigation.findNavController(requireView())
                                                .navigate(R.id.action_joinFriendFragment_to_friendFragment, args);
                                    });
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded() || getView() == null) return;
                            btnJoin.setEnabled(true);
                            btnJoin.setText("Join");
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

}
