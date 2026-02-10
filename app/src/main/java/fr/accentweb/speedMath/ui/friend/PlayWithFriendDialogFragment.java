package fr.accentweb.speedMath.ui.friend;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.PlayerManager;

public class PlayWithFriendDialogFragment extends DialogFragment {

    private PlayerManager playerManager;
    private String uid, pseudo;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = requireActivity().getLayoutInflater().inflate(R.layout.dialog_play_with_friend, null);

        playerManager = PlayerManager.getInstance(requireContext());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pseudo = playerManager.getOnlinePseudo();

        if(!playerManager.getTodayDate().equals(playerManager.getLastConnection()))
        {
            DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("players").child(uid);
            playerManager.syncOnlineData(playerRef);
        }
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
