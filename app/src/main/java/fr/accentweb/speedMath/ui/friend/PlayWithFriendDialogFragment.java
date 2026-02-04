package fr.accentweb.speedMath.ui.friend;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;

public class PlayWithFriendDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = requireActivity().getLayoutInflater().inflate(R.layout.dialog_play_with_friend, null);

        AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.SpeedMath_Dialog)
                .setView(v)
                .create();

        NavController navController = Navigation.findNavController(
                requireActivity(),
                R.id.nav_host_fragment
        );

        v.findViewById(R.id.btnCreateRoom).setOnClickListener(view -> {
            dialog.dismiss();
            navController.navigate(R.id.waitingFriendFragment);
        });

        v.findViewById(R.id.btnJoinRoom).setOnClickListener(view -> {
            dialog.dismiss();
            navController.navigate(R.id.joinFriendFragment);
        });

        return dialog;
    }
}
