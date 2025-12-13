package com.example.speedMath.ui.online;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.speedMath.R;
import com.example.speedMath.core.DailyMatchManager;
import com.example.speedMath.core.MatchmakingHelper;
import com.example.speedMath.core.PlayerManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WaitingRoomFragment extends Fragment {

    private String uid;
    private String pseudo;
    private TextView textStatus;
    private ProgressBar progressBar;
    private Button btnCancel;
    private MatchmakingHelper matchmakingHelper;
    private boolean matchStarted = false;
    private PlayerManager playerManager;
    private FrameLayout overlayContainer;
    private OnBackPressedCallback backPressedCallback;
    private long points;
    private long rank;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_waiting_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textStatus = view.findViewById(R.id.textStatus);
        progressBar = view.findViewById(R.id.progressBar);
        btnCancel = view.findViewById(R.id.btnCancel);
        overlayContainer = view.findViewById(R.id.overlayContainer);

        playerManager = PlayerManager.getInstance(requireContext());
        DailyMatchManager.getInstance(requireContext()).checkDailyMatchLimit((canPlay, currentCount, limit) -> {
            if (!canPlay) {
                showDailyLimitReachedToast(currentCount, limit);
                return;
            }
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pseudo = playerManager.getOnlinePseudo();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("players")
                .child(uid);

        ref.get().addOnSuccessListener(snapshot -> {

            long points = snapshot.child("points").getValue(Long.class) != null
                    ? snapshot.child("points").getValue(Long.class)
                    : 0;

            long rank = snapshot.child("rank").getValue(Long.class) != null
                    ? snapshot.child("rank").getValue(Long.class)
                    : 999999;

            // ✅ CRÉATION
            matchmakingHelper = new MatchmakingHelper(uid, pseudo, points, rank);

            // ✅ LISTENER
            matchmakingHelper.setMatchListener(new MatchmakingHelper.MatchListener() {
                @Override
                public void onMatchFound(String matchId, String uid, String pseudo, long points, long rank, String opponentUid, String opponentPseudo, long opponentPoints, long opponentRank) {
                    if (matchStarted) return;
                    matchStarted = true;

                    DailyMatchManager.getInstance(requireContext())
                            .incrementDailyMatchCount();

                    backPressedCallback = new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            handleBackOrUpNavigation(matchId, uid, opponentUid);
                        }
                    };

                    matchmakingHelper.cancelMatchmaking();

                    Bundle bundle = new Bundle();
                    bundle.putString("matchId", matchId);
                    bundle.putString("myUid", uid);
                    bundle.putString("myPseudo", pseudo);
                    bundle.putLong("myPoints", points);
                    bundle.putLong("myRank", rank);
                    bundle.putString("opponentUid", opponentUid);
                    bundle.putString("opponentPseudo", opponentPseudo);
                    bundle.putLong("opponentPoints", opponentPoints);
                    bundle.putLong("opponentRank", opponentRank);
                    bundle.putString(
                            "player",
                            uid.compareTo(opponentUid) < 0 ? "P1" : "P2"
                    );

                    showCountdownOverlay(bundle);
                }

                @Override
                public void onError(String message) {
                    textStatus.setText("Error : " + message);
                    progressBar.setVisibility(View.GONE);
                }
            });

            // ✅ ICI ET SEULEMENT ICI
            matchmakingHelper.findMatch();

        }).addOnFailureListener(e -> {

            // fallback sécurité
            matchmakingHelper = new MatchmakingHelper(uid, pseudo, 0, 999999);
            matchmakingHelper.findMatch();
        });



        textStatus.setText("Searching for a match…");
        progressBar.setVisibility(View.VISIBLE);

       // matchmakingHelper.findMatch();

        // bouton annuler
        btnCancel.setOnClickListener(v -> {
            textStatus.setText("Match cancelled");
            progressBar.setVisibility(View.GONE);
            // supprime entry waiting
            if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
            // revient en arrière
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.navigation_home);
        });

        // backpress : on annule proprement
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private void showCountdownOverlay(Bundle bundle) {
        overlayContainer.setVisibility(View.VISIBLE);
        View overlayView = getLayoutInflater().inflate(R.layout.countdown_overlay, overlayContainer, true);
        TextView textCountdown = overlayView.findViewById(R.id.textCountdown);
        btnCancel.setVisibility(View.GONE);

        new CountDownTimer(3500, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / 1000) % 60;
                if(seconds == 0) return;
                textCountdown.setText(String.valueOf(seconds));
            }

            public void onFinish() {
                overlayContainer.setVisibility(View.GONE);
                // navigation vers le fragment online (QCM)
                if (getView() != null) {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_waitingRoomFragment_to_onlineQCMFragment, bundle);
                }
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // assure cleanup si le fragment est détruit
        if (matchmakingHelper != null) {
            matchmakingHelper.cancelMatchmaking();
        }
    }

    private void declareForfeitLoss(String matchId, String uid, String opponentUid) {
        if (!matchStarted) return;
        int nbQuestions = 5;
        String player = uid.compareTo(opponentUid) < 0 ? "P1" : "P2";
        Log.w(TAG, "Player quit the match → declaring forfeit loss.");

        String winnerField = player.equals("P1") ? "p2" : "p1";
        String opponentScoreField = player.equals("P1") ? "p2_score" : "p1_score";
        DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        // give the opponent a point
        matchRef.child(opponentScoreField).setValue(nbQuestions);

        matchRef.child("winner").setValue(winnerField);
        matchRef.child("state").setValue("finished");

    }

    private void handleBackOrUpNavigation(String matchId, String uid, String opponentUid) {
        declareForfeitLoss(matchId, uid, opponentUid);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                NavController nav = Navigation.findNavController(requireView());
                nav.navigate(R.id.navigation_home);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating", e);
                requireActivity().finish();
            }
        }, 500);
    }

    private void showDailyLimitReachedToast(int currentCount, int limit) {
        NavController nav = Navigation.findNavController(requireView());
        nav.navigate(R.id.navigation_home);

        Toast.makeText(requireContext(), "Daily Limit Reached : " + limit + " matches played.", Toast.LENGTH_SHORT).show();
    }
}
