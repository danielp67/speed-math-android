package fr.accentweb.speedMath.ui.online;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.MatchmakingHelper;
import fr.accentweb.speedMath.core.PlayerManager;
import com.google.firebase.auth.FirebaseAuth;
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

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        pseudo = playerManager.getOnlinePseudo();

        if(!playerManager.getTodayDate().equals(playerManager.getLastConnection()))
        {
            DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("players").child(uid);
            playerManager.syncOnlineData(playerRef);
        }
      /*  DailyMatchManager.getInstance(requireContext()).checkDailyMatchLimit((canPlay, currentCount, limit) -> {
            if (!canPlay) {
                showDailyLimitReachedToast(currentCount, limit);
                return;
            }
        });*/

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

            matchmakingHelper = new MatchmakingHelper(uid, pseudo, points, rank);

            matchmakingHelper.setMatchListener(new MatchmakingHelper.MatchListener() {
                @Override
                public void onMatchFound(String matchId, String uid, String pseudo, long points, long rank, String opponentUid, String opponentPseudo, long opponentPoints, long opponentRank) {
                    if (matchStarted) return;
                    matchStarted = true;

                    playerManager.incrementDailyMatchPlayed();

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
                    textStatus.setText(String.format("%s%s", getString(R.string.error_message), message));
                    progressBar.setVisibility(View.GONE);
                }
            });

            matchmakingHelper.findMatch();

        }).addOnFailureListener(e -> {

            matchmakingHelper = new MatchmakingHelper(uid, pseudo, 0, 999999);
            matchmakingHelper.findMatch();
        });



        textStatus.setText(R.string.searching_for_a_match);
        progressBar.setVisibility(View.VISIBLE);

       // matchmakingHelper.findMatch();

        btnCancel.setOnClickListener(v -> {
            textStatus.setText(R.string.match_cancelled);
            progressBar.setVisibility(View.GONE);
            if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.navigation_home);
        });

        // backpress
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
                // navigate to fragment online (QCM)
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

}
