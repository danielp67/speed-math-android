package fr.accentweb.speedMath.ui.friend;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import fr.accentweb.speedMath.R;

public class PlayWithFriendDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = requireActivity().getLayoutInflater().inflate(R.layout.dialog_play_with_friend, null);

        AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.SpeedMath_Dialog)
                .setView(v)
                .create();

        v.findViewById(R.id.btnCreateRoom).setOnClickListener(view -> {
            dialog.dismiss();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_home_to_waitingFriendFragment);
        });

        v.findViewById(R.id.btnJoinRoom).setOnClickListener(view -> {
            dialog.dismiss();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigation_home_to_joinFriendFragment);
        });

        return dialog;
    }
}
