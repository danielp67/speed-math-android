package com.example.speedMath.ui.online;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.MatchmakingHelper;
import com.example.speedMath.core.PlayerManager;
import com.google.firebase.auth.FirebaseAuth;

public class WaitingRoomFragment extends Fragment {

    private TextView textStatus;
    private ProgressBar progressBar;
    private Button btnCancel;
    private MatchmakingHelper matchmakingHelper;
    private boolean matchStarted = false;
    private PlayerManager playerManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textStatus = view.findViewById(R.id.textStatus);
        progressBar = view.findViewById(R.id.progressBar);
        btnCancel = view.findViewById(R.id.btnCancel);

        playerManager = PlayerManager.getInstance(requireContext());
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pseudo = playerManager.getOnlinePseudo();

        // create helper and listener
        matchmakingHelper = new MatchmakingHelper(uid, pseudo);
        matchmakingHelper.setMatchListener(new MatchmakingHelper.MatchListener() {
            @Override
            public void onMatchFound(String matchId, String opponentUid, String opponentPseudo) {
                if (matchStarted) return;
                matchStarted = true;

                // Optionnel : cancel pour être sûr que waiting est propre
                matchmakingHelper.cancelMatchmaking();

                Bundle bundle = new Bundle();
                bundle.putString("matchId", matchId);
                bundle.putString("myUid", uid);
                bundle.putString("opponentUid", opponentUid);
                bundle.putString("opponentPseudo", opponentPseudo);
                // need to know if we are player 1 or 2
                if (uid.compareTo(opponentUid) < 0) {
                    bundle.putString("player", "P1");
                } else {
                    bundle.putString("player", "P2");
                }


                // navigation vers le fragment online (QCM)
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_waitingRoomFragment_to_onlineQCMFragment, bundle);
            }

            @Override
            public void onError(String message) {
                textStatus.setText("Error : " + message);
                progressBar.setVisibility(View.GONE);
            }
        });

        textStatus.setText("Searching for a match…");
        progressBar.setVisibility(View.VISIBLE);

        matchmakingHelper.findMatch();

        // bouton annuler
        btnCancel.setOnClickListener(v -> {
            textStatus.setText("Match cancelled");
            progressBar.setVisibility(View.GONE);
            // supprime entry waiting
            if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
            // revient en arrière
            Navigation.findNavController(requireView()).navigateUp();
        });

        // backpress : on annule proprement
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
                        setEnabled(false);
                        requireActivity().onBackPressed();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // assure cleanup si le fragment est détruit
        if (matchmakingHelper != null) {
            matchmakingHelper.cancelMatchmaking();
        }
    }
}
