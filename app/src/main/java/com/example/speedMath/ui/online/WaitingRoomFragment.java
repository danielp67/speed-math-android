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

public class WaitingRoomFragment extends Fragment {

    private TextView textStatus;
    private ProgressBar progressBar;
    private Button btnCancel;
    private MatchmakingHelper matchmakingHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textStatus = view.findViewById(R.id.textStatus);
        progressBar = view.findViewById(R.id.progressBar);
        btnCancel = view.findViewById(R.id.btnCancel);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pseudo = getArguments() != null ? getArguments().getString("pseudo") : "Player";

        matchmakingHelper = new MatchmakingHelper(uid, pseudo);
        matchmakingHelper.setMatchListener(new MatchmakingHelper.MatchListener() {
            @Override
            public void onMatchFound(String matchId, String opponentUid, String opponentPseudo) {
                Bundle bundle = new Bundle();
                bundle.putString("matchId", matchId);
                bundle.putString("myUid", uid);
                bundle.putString("opponentUid", opponentUid);
                bundle.putString("opponentPseudo", opponentPseudo);

                Navigation.findNavController(view).navigate(R.id.action_waitingRoomFragment_to_onlineQCMFragment, bundle);
            }

            @Override
            public void onError(String message) {
                textStatus.setText("Erreur: " + message);
            }
        });

        matchmakingHelper.findMatch();

        btnCancel.setOnClickListener(v -> {
            textStatus.setText("Recherche annulée");
            progressBar.setVisibility(View.GONE);
            getActivity().onBackPressed();
        });
    }
}
