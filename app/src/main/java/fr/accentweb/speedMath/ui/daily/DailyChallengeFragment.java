package fr.accentweb.speedMath.ui.daily;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.BaseGameFragment;
import fr.accentweb.speedMath.core.FeedbackManager;
import fr.accentweb.speedMath.core.PlayerManager;
import fr.accentweb.speedMath.utils.AnimUtils;

public class DailyChallengeFragment extends BaseGameFragment {
    private PlayerManager playerManager;
    private TextView txtStreak, txtReward, txtStreakSteps, txtTicketAction;
    private View ticketFront, ticketBack;
    private MaterialCardView cardTicket;
    private Button btnPlayOnline;
    private FeedbackManager feedbackManager;
    private boolean pendingReward = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ecouter le résultat du défi
        getParentFragmentManager().setFragmentResultListener("daily_result", this, (requestKey, bundle) -> {
            boolean success = bundle.getBoolean("SUCCESS");
            if (success) {
                pendingReward = true;
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
        txtTicketAction = view.findViewById(R.id.txtTicketAction); // Assure-toi que cet ID existe dans ton XML pour le texte "Click to Play"
        cardTicket = view.findViewById(R.id.cardTicket);
        ticketFront = view.findViewById(R.id.ticketFront);
        ticketBack = view.findViewById(R.id.ticketBack);
        btnPlayOnline = view.findViewById(R.id.btnPlayOnline);

        refreshUI();

        cardTicket.setOnClickListener(v -> {
            if (playerManager.isDailyChallengeDoneToday()) {
                Toast.makeText(getContext(), "Challenge already done, come back tomorrow !", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pendingReward) {
                // Donner la récompense après le jeu
                claimReward();
            } else {
                // Lancer le jeu
                Navigation.findNavController(v).navigate(R.id.action_dailyChallengeFragment_to_dailyGameFragment);
            }
        });

        if (pendingReward) {
            txtTicketAction.setText("You win ! Clic to reveal your gift");
            txtTicketAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.correct));
        }

        btnPlayOnline.setOnClickListener(v -> {
            NavController nav = Navigation.findNavController(requireView());
            nav.navigate(R.id.navigation_home);
        });

        return view;
    }

    private void claimReward() {
        pendingReward = false;
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
        txtStreak.setText("🔥 Série : " + streak + " jours");

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
