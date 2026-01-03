package fr.accentweb.speedMath.ui.friend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import fr.accentweb.speedMath.R;

public class PlayWithFriendDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.dialog_play_with_friend, container, false);

        v.findViewById(R.id.btnCreateRoom).setOnClickListener(view -> {
            dismiss();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_home_to_waitingFriendFragment);
        });

        v.findViewById(R.id.btnJoinRoom).setOnClickListener(view -> {
            dismiss();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_home_to_joinFriendFragment);
        });

        return v;
    }
}
