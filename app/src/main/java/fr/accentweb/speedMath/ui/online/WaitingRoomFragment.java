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

import java.util.HashMap;
import java.util.Random;

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

    private long points = 0;
    private long rank = 999999;
    private CountDownTimer matchmakingTimer;
    private Random random = new Random();

    private boolean fragmentActive = true; // <-- flag fragment actif

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

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("players")
                .child(uid);

        ref.get().addOnSuccessListener(snapshot -> {

            points = snapshot.child("points").getValue(Long.class) != null
                    ? snapshot.child("points").getValue(Long.class)
                    : 0;

            rank = snapshot.child("rank").getValue(Long.class) != null
                    ? snapshot.child("rank").getValue(Long.class)
                    : 999999;

            matchmakingHelper = new MatchmakingHelper(uid, pseudo, points, rank);

            matchmakingHelper.setMatchListener(new MatchmakingHelper.MatchListener() {
                @Override
                public void onMatchFound(String matchId, String uid, String pseudo, long points, long rank, String opponentUid, String opponentPseudo, long opponentPoints, long opponentRank) {
                    if (!fragmentActive || matchStarted) return; // <-- check fragment actif
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
                    if (!fragmentActive) return;
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

        int randomDelay = 8000 + random.nextInt(4000); // 1-12s
        startMatchmakingTimer(randomDelay);

        btnCancel.setOnClickListener(v -> {
            fragmentActive = false;
            textStatus.setText(R.string.match_cancelled);
            progressBar.setVisibility(View.GONE);
            if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
            if (matchmakingTimer != null) matchmakingTimer.cancel();
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.navigation_home);
        });

        // backpress
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        fragmentActive = false;
                        if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
                        if (matchmakingTimer != null) matchmakingTimer.cancel();
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });
    }

    private void showCountdownOverlay(Bundle bundle) {
        if (!fragmentActive) return;
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
                if (!fragmentActive) return;
                overlayContainer.setVisibility(View.GONE);
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
        fragmentActive = false;
        if (matchmakingHelper != null) matchmakingHelper.cancelMatchmaking();
        if (matchmakingTimer != null) matchmakingTimer.cancel();
    }

    private void declareForfeitLoss(String matchId, String uid, String opponentUid) {
        if (!matchStarted || !fragmentActive) return;
        int nbQuestions = 5;
        String player = uid.compareTo(opponentUid) < 0 ? "P1" : "P2";
        Log.w(TAG, "Player quit the match → declaring forfeit loss.");

        String winnerField = player.equals("P1") ? "p2" : "p1";
        String opponentScoreField = player.equals("P1") ? "p2_score" : "p1_score";
        DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

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

    private void startBotMatch() {
        if (!fragmentActive) return;
        matchStarted = true;
        playerManager.incrementDailyMatchPlayed();

        if (matchmakingHelper != null) {
            matchmakingHelper.cancelMatchmaking();
        }

        String[] botNames = {
                "Alex_657", "Tay", "Jordan9", "Casey", "_riley", "Morgan", "J4mi3", "Avery",
                "Noah_21", "LiamX", "Emma_88", "Olivia7", "Sofia_3", "Leo_404", "Nico_77",
                "MiaX3", "Zara_09", "Kai_1337", "Luna_42", "Max_900", "Eli_29", "Nora_5",
                "Sam_0x", "Izzy_24", "Rex_91", "Nova_17", "Kira_08", "Finn_300"
        };
        String botPseudo = botNames[random.nextInt(botNames.length)];
        String botUid = "bot_" + System.currentTimeMillis();
        long botPoints = 0;
        long botRank = 999999;
        String matchId = "bot_" + System.currentTimeMillis();

        HashMap<String, Object> matchData = new HashMap<>();
        matchData.put("p1_uid", uid);
        matchData.put("p1_pseudo", pseudo);
        matchData.put("p1_points", points);
        matchData.put("p1_ranking", rank);
        matchData.put("p2_uid", botUid);
        matchData.put("p2_pseudo", botPseudo);
        matchData.put("p2_points", botPoints);
        matchData.put("p2_ranking", botRank);
        matchData.put("state", "playing");
        matchData.put("p1_score", 0);
        matchData.put("p2_score", 0);
        matchData.put("timestamp", System.currentTimeMillis());
        matchData.put("is_bot_match", true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!fragmentActive) return;
            FirebaseDatabase.getInstance().getReference("matches").child(matchId)
                    .setValue(matchData)
                    .addOnSuccessListener(aVoid -> {
                        if (!fragmentActive) return;
                        Bundle bundle = new Bundle();
                        bundle.putString("matchId", matchId);
                        bundle.putString("myUid", uid);
                        bundle.putString("myPseudo", pseudo);
                        bundle.putLong("myPoints", points);
                        bundle.putLong("myRank", rank);
                        bundle.putString("opponentUid", botUid);
                        bundle.putString("opponentPseudo", botPseudo);
                        bundle.putLong("opponentPoints", botPoints);
                        bundle.putLong("opponentRank", botRank);
                        bundle.putString("player", "P1");
                        bundle.putBoolean("is_bot_match", true);

                        progressBar.setVisibility(View.GONE);

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showCountdownOverlay(bundle);
                        }, 1500);
                    });
        }, 500 + random.nextInt(1000));
    }

    private void startMatchmakingTimer(int delayMs) {
        if (matchmakingTimer != null) {
            matchmakingTimer.cancel();
        }

        matchmakingTimer = new CountDownTimer(delayMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { }

            @Override
            public void onFinish() {
                if (!matchStarted && fragmentActive) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!matchStarted && fragmentActive) {
                            startBotMatch();
                        }
                    }, 1000 + random.nextInt(2000));
                }
            }
        }.start();
    }

}
