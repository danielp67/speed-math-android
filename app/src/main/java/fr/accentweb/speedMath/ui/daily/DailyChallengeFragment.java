package fr.accentweb.speedMath.ui.daily;

import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.PlayerManager;

public class DailyChallengeFragment extends Fragment {

    private TextView txtStreak;
    private TextView txtStreakSteps;
    private TextView txtReward;
    private CardView cardTicket;
    private Button btnPlayOnline;

    private PlayerManager playerManager;

    private final int[] STEPS = {1, 3, 5, 7, 14, 30};

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_daily_challenge, container, false);

        playerManager = PlayerManager.getInstance(requireContext());

        txtStreak = view.findViewById(R.id.txtStreak);
        txtStreakSteps = view.findViewById(R.id.txtStreakSteps);
        txtReward = view.findViewById(R.id.txtReward);
        cardTicket = view.findViewById(R.id.cardTicket);
        btnPlayOnline = view.findViewById(R.id.btnPlayOnline);

        refreshUI();

        cardTicket.setOnClickListener(v -> onTicketClicked());

        btnPlayOnline.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Use reward on Online Mode 🚀", Toast.LENGTH_SHORT).show();
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.navigation_home);
        });

        return view;
    }

    private void onTicketClicked() {

        if (playerManager.isDailyChallengeDoneToday()) {
            Toast.makeText(getContext(), "Ticket already used today 🔒", Toast.LENGTH_SHORT).show();
            return;
        }

        int reward = playerManager.completeDailyChallengeAndGetReward();
        playerManager.addDailyRewardMatches(reward);

        animateTicket(cardTicket);

        Toast.makeText(
                getContext(),
                "+" + reward + " matches online 🎟",
                Toast.LENGTH_LONG
        ).show();

        btnPlayOnline.setVisibility(View.VISIBLE);

        refreshUI();
    }

    private void refreshUI() {
        int streak = playerManager.getDailyStreak();
        int reward = playerManager.getRewardForStreak(streak);

        txtStreak.setText("🔥 Streak: " + streak + " days");
        txtReward.setText("🎟 +" + reward + " Online Match");

        txtStreakSteps.setText(buildColoredSteps(streak));
    }

    private SpannableString buildColoredSteps(int streak) {
        StringBuilder raw = new StringBuilder();
        for (int step : STEPS) {
            raw.append(step).append("   ");
        }

        SpannableString span = new SpannableString(raw.toString());
        int index = 0;

        for (int step : STEPS) {
            int start = index;
            int end = start + String.valueOf(step).length();

            if (streak >= step) {
                span.setSpan(
                        new ForegroundColorSpan(
                                ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                        ),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } else if (isNextStep(streak, step)) {
                span.setSpan(
                        new ForegroundColorSpan(
                                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                        ),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            index = end + 3;
        }

        return span;
    }

    private boolean isNextStep(int streak, int step) {
        for (int s : STEPS) {
            if (streak < s) {
                return s == step;
            }
        }
        return false;
    }

    private void animateTicket(View v) {
        v.setScaleX(0.95f);
        v.setScaleY(0.95f);

        v.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}
