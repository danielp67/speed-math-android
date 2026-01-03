package fr.accentweb.speedMath.ui.friend;

import android.os.Bundle;
import android.text.TextUtils;
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
                btnJoin.setText("Connexion...");

                friendManager.joinRoom(code, playerManager.getOnlineUid(),
                        new FriendManager.RoomCallback() {
                            @Override
                            public void onSuccess() {
                                Bundle args = new Bundle();
                                args.putString("ROOM_CODE", code);

                                Navigation.findNavController(requireView())
                                        .navigate(R.id.action_joinFriendFragment_to_friendFragment, args);
                            }

                            @Override
                            public void onError(String error) {
                                btnJoin.setEnabled(true);
                                btnJoin.setText("Join");
                                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            btnCancel.setOnClickListener(v ->
                    Navigation.findNavController(v).popBackStack()
            );
        }

}
