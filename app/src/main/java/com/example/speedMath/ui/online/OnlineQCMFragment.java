package com.example.speedMath.ui.online;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.MatchmakingHelper;
import com.google.firebase.auth.FirebaseAuth;

public class OnlineQCMFragment extends Fragment {

    private TextView txtStatus, txtOpponent;
    private View overlayWaiting;
    private ProgressBar progressWaiting;
    private Button btnCancel;

    private String myUid;
    private String myPseudo;

    private MatchmakingHelper matchmaking;

    public OnlineQCMFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online_qcm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myPseudo = requireActivity()
                .getSharedPreferences("prefs", 0)
                .getString("pseudo", "Player");

        overlayWaiting = view.findViewById(R.id.overlayWaiting);
        txtStatus = view.findViewById(R.id.txtStatus);
        progressWaiting = view.findViewById(R.id.progressWaiting);
        btnCancel = view.findViewById(R.id.btnCancel);
        txtOpponent = view.findViewById(R.id.txtOpponent);

        btnCancel.setOnClickListener(v -> cancelMatchmaking());

        matchmaking = new MatchmakingHelper(myUid, myPseudo);

        startMatchmaking();
    }

    private void startMatchmaking() {
        showWaitingUI(true);

        matchmaking.findMatch((matchId, opponentUid, opponentPseudo) -> {

            showWaitingUI(false);
            txtOpponent.setText("Adversaire : " + opponentPseudo);

            // Lancer la partie
            Bundle args = new Bundle();
            args.putString("matchId", matchId);
            args.putString("uid", myUid);
            args.putString("opponentUid", opponentUid);

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_onlineQCMFragment_to_onlineQCMGameFragment, args);
        });
    }

    private void cancelMatchmaking() {
        matchmaking.cancel();
        showWaitingUI(false);
    }

    private void showWaitingUI(boolean show) {
        overlayWaiting.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}
