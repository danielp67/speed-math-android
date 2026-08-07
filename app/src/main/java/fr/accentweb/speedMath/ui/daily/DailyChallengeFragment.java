package fr.accentweb.speedMath.ui.daily;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.card.MaterialCardView;

import java.util.Random;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.utils.AnimUtils;

public class DailyChallengeFragment extends BaseGameFragment {
    private PlayerManager playerManager;
    private TextView txtStreak, txtReward, txtStreakSteps, txtTicketAction;
    private View ticketFront, ticketBack, wheelContainer;
    private MaterialCardView cardTicket;
    private Button btnPlayOnline, btnSpin;
    private MagicWheelView magicWheel;
    private FeedbackManager feedbackManager;
    private boolean pendingReward = false;
    private boolean isSpinning = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("daily_result", this, (requestKey, bundle) -> {
            boolean success = bundle.getBoolean("SUCCESS");
            if (success) {
                pendingReward = true;
                playerManager.setDailyChallengeWaitingClaim(true);
                if (isAdded()) refreshUI();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_challenge, container, false);
        playerManager = PlayerManager.getInstance(requireContext());
        feedbackManager = new FeedbackManager(requireContext());
        feedbackManager.loadSounds(R.raw.correct, R.raw.wrong, R.raw.levelup);
        
        txtStreak = view.findViewById(R.id.txtStreak);
        txtReward = view.findViewById(R.id.txtReward);
        txtStreakSteps = view.findViewById(R.id.txtStreakSteps);
        txtTicketAction = view.findViewById(R.id.txtTicketAction);
        cardTicket = view.findViewById(R.id.cardTicket);
        ticketFront = view.findViewById(R.id.ticketFront);
        ticketBack = view.findViewById(R.id.ticketBack);
        btnPlayOnline = view.findViewById(R.id.btnPlayOnline);
        btnSpin = view.findViewById(R.id.btnSpin);
        magicWheel = view.findViewById(R.id.magicWheel);
        wheelContainer = view.findViewById(R.id.wheelContainer);

        pendingReward = playerManager.isDailyChallengeWaitingClaim();

        refreshUI();

        btnSpin.setOnClickListener(v -> {
            if (playerManager.isDailyChallengeDoneToday()) {
                Toast.makeText(getContext(), "Challenge already done today !", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSpinning) return;
            startSpin();
        });

        cardTicket.setOnClickListener(v -> {
            if (pendingReward) {
                claimReward();
            }
        });

        btnPlayOnline.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
        });

        return view;
    }

    private void startSpin() {
        isSpinning = true;
        feedbackManager.playCorrectSound();
        
        Random r = new Random();
        int degrees = 1800 + r.nextInt(360); // Minimum 5 full rotations
        
        magicWheel.animate()
            .rotationBy(degrees)
            .setDuration(4000)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> {
                isSpinning = false;
                float rotation = magicWheel.getRotation() % 360;
                if (rotation < 0) rotation += 360;
                
                // Pointer at top (270 degrees in coordinate system where 0 is 3 o'clock)
                // But rotationBy adds to current rotation. 
                // MagicWheelView draws 0 at 0 degrees.
                // Each sector is 60 degrees.
                // 0-60: Kart, 60-120: Space, 120-180: Tetris, ...
                // The arrow is at top (270 degrees relative to wheel center).
                // So we need to find which sector is at 270 degrees.
                int sector = (int) ((270 - rotation + 360) % 360) / 60;
                String mode;
                switch (sector % 3) {
                    case 0: mode = "KART"; break;
                    case 1: mode = "SPACE"; break;
                    default: mode = "TETRIS"; break;
                }
                
                launchGame(mode);
            })
            .start();
    }

    private void launchGame(String mode) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            Bundle args = new Bundle();
            args.putBoolean("IS_DAILY", true);
            
            NavController nav = Navigation.findNavController(requireView());
            switch (mode) {
                case "KART":
                    nav.navigate(R.id.action_dailyChallengeFragment_to_mathKartFragment, args);
                    break;
                case "SPACE":
                    nav.navigate(R.id.action_dailyChallengeFragment_to_mathInvadersFragment, args);
                    break;
                case "TETRIS":
                    nav.navigate(R.id.action_dailyChallengeFragment_to_mathTetrisFragment, args);
                    break;
            }
        }, 1000);
    }

    private void claimReward() {
        pendingReward = false;
        playerManager.setDailyChallengeWaitingClaim(false);
        int reward = playerManager.completeDailyChallengeAndGetReward();
        txtReward.setText("🎟 +" + reward + " Matchs Online !");

        AnimUtils.flipTicketWithTurns(cardTicket, ticketFront, ticketBack, 2, 1000);
        btnPlayOnline.setVisibility(View.VISIBLE);
        feedbackManager.playLevelUpSound();
        refreshUI();
        playerManager.addDailyRewardMatches(reward);
    }

    private void refreshUI() {
        int streak = playerManager.getDailyStreak();
        txtStreak.setText("🔥 Streak : " + streak + " day(s)");

        if (pendingReward) {
            cardTicket.setVisibility(View.VISIBLE);
            btnSpin.setVisibility(View.GONE);
            wheelContainer.setVisibility(View.GONE);
            txtTicketAction.setText("You win! Tap to reveal your gift");
        } else if (playerManager.isDailyChallengeDoneToday()) {
            cardTicket.setVisibility(View.GONE);
            btnSpin.setVisibility(View.VISIBLE);
            btnSpin.setText("ALREADY DONE TODAY");
            btnSpin.setEnabled(false);
            btnPlayOnline.setVisibility(View.VISIBLE);
            wheelContainer.setVisibility(View.VISIBLE);
        } else {
            cardTicket.setVisibility(View.GONE);
            btnSpin.setVisibility(View.VISIBLE);
            btnSpin.setEnabled(true);
            wheelContainer.setVisibility(View.VISIBLE);
        }

        SpannableString streakSteps = new SpannableString("✔ 1   ✔ 3   ✔ 5   ✔ 7   ✔ 14   ✔ 30");
        for (int day : new int[]{1, 3, 5, 7, 14, 30}) {
            if (streak >= day) {
                int start = streakSteps.toString().indexOf("✔ " + day);
                if (start != -1) {
                    streakSteps.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.correct)),
                            start, start + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    streakSteps.setSpan(new StyleSpan(Typeface.BOLD), start, start + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        txtStreakSteps.setText(streakSteps);
    }
}
