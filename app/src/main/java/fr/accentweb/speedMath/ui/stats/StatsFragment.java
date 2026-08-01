package fr.accentweb.speedMath.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.PlayerManager;

public class StatsFragment extends Fragment {

    private PlayerManager playerManager;
    private TextView tvRank, tvWinRate, tvTotalMatches, tvWins, tvLosses, tvDraws, tvTotalPoints;
    private TextView tvStreakAdd, tvStreakSub, tvStreakMul, tvStreakDiv, tvStreakAll, tvStreakInvaders, tvStreakTetris;
    private TextView tvLevelTitle, tvLevelProgressText;
    private ProgressBar pbLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        playerManager = PlayerManager.getInstance(requireContext());

        // Online Stats
        tvRank = root.findViewById(R.id.tvRank);
        tvWins = root.findViewById(R.id.tvWins);
        tvLosses = root.findViewById(R.id.tvLosses);
        tvDraws = root.findViewById(R.id.tvDraws);
        tvTotalMatches = root.findViewById(R.id.tvTotalMatches);
        tvTotalPoints = root.findViewById(R.id.tvTotalPoints);
        tvWinRate = root.findViewById(R.id.tvWinRate);

        // Best Streaks
        tvStreakAdd = root.findViewById(R.id.tvStreakAdd);
        tvStreakSub = root.findViewById(R.id.tvStreakSub);
        tvStreakMul = root.findViewById(R.id.tvStreakMul);
        tvStreakDiv = root.findViewById(R.id.tvStreakDiv);
        tvStreakAll = root.findViewById(R.id.tvStreakAll);
        tvStreakInvaders = root.findViewById(R.id.tvStreakInvaders);
        tvStreakTetris = root.findViewById(R.id.tvStreakTetris);

        // League
        tvLevelTitle = root.findViewById(R.id.tvLevelTitle);
        pbLevel = root.findViewById(R.id.pbLevel);
        tvLevelProgressText = root.findViewById(R.id.tvLevelProgressText);

        displayStats();

        return root;
    }

    private void displayStats() {
        // Online Data
        int wins = playerManager.getOnlineWins();
        int losses = playerManager.getOnlineLosses();
        int draws = playerManager.getOnlineDraws();
        int matches = playerManager.getOnlinePlayedMatches();
        int totalPoints = playerManager.getOnlineScore();
        int rank = playerManager.getRank();

        int winRate = (matches > 0) ? (wins * 100) / matches : 0;

        if (rank == 999999) {
            tvRank.setText(R.string.stats_rank_none);
        } else {
            tvRank.setText(getString(R.string.stats_rank_format, rank));
        }
        
        tvWins.setText(String.valueOf(wins));
        tvLosses.setText(String.valueOf(losses));
        tvDraws.setText(String.valueOf(draws));
        tvTotalMatches.setText(String.valueOf(matches));
        tvTotalPoints.setText(String.valueOf(totalPoints));
        tvWinRate.setText(getString(R.string.stats_win_rate_format, winRate));

        // Streaks Data
        tvStreakAdd.setText(String.valueOf(playerManager.getCorrectAnswersStreak("ADD", playerManager.getAddSuiteDifficulty())));
        tvStreakSub.setText(String.valueOf(playerManager.getCorrectAnswersStreak("SUB", playerManager.getSubSuiteDifficulty())));
        tvStreakMul.setText(String.valueOf(playerManager.getCorrectAnswersStreak("MUL", playerManager.getMulSuiteDifficulty())));
        tvStreakDiv.setText(String.valueOf(playerManager.getCorrectAnswersStreak("DIV", playerManager.getDivSuiteDifficulty())));
        tvStreakAll.setText(String.valueOf(playerManager.getCorrectAnswersStreak("ALL", playerManager.getAllSuiteDifficulty())));
        
        tvStreakInvaders.setText(String.valueOf(playerManager.getCorrectAnswersStreak("INVADERS", 0)));
        tvStreakTetris.setText(String.valueOf(playerManager.getCorrectAnswersStreak("TETRIS", 0)));

        // League Data
        int currentLevel = playerManager.getCurrentLevel();
        tvLevelTitle.setText(getString(R.string.stats_league_level, currentLevel));
        
        // Progress bar (Milestone every 10 levels)
        int progress = (currentLevel % 10) * 10;
        if (progress == 0 && currentLevel > 0) progress = 100;
        
        pbLevel.setProgress(progress);
        tvLevelProgressText.setText(getString(R.string.stats_milestone_progress, progress));
    }
}
