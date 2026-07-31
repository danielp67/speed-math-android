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
    private TextView tvRank, tvWinRate, tvTotalMatches, tvWins, tvLosses, tvDraws, tvLevelTitle, tvTotalPoints, tvAvgScore;
    private ProgressBar pbLevel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        playerManager = PlayerManager.getInstance(requireContext());

        tvRank = root.findViewById(R.id.tvRank);
        tvWinRate = root.findViewById(R.id.tvWinRate);
        tvTotalMatches = root.findViewById(R.id.tvTotalMatches);
        tvWins = root.findViewById(R.id.tvWins);
        tvLosses = root.findViewById(R.id.tvLosses);
        tvDraws = root.findViewById(R.id.tvDraws);
        tvLevelTitle = root.findViewById(R.id.tvLevelTitle);
        pbLevel = root.findViewById(R.id.pbLevel);
        tvTotalPoints = root.findViewById(R.id.tvTotalPoints);
        tvAvgScore = root.findViewById(R.id.tvAvgScore);

        // Data retrieval
        int wins = playerManager.getOnlineWins();
        int losses = playerManager.getOnlineLosses();
        int draws = playerManager.getOnlineDraws();
        int matches = playerManager.getOnlinePlayedMatches();
        int currentLevel = playerManager.getCurrentLevel();
        int totalScore = playerManager.getOnlineScore();
        int rank = playerManager.getRank();

        // Win Rate Calculation
        int winRate = 0;
        if (matches > 0) {
            winRate = (wins * 100) / matches;
        }

        // Avg Score Calculation
        int avgScore = 0;
        if (matches > 0) {
            avgScore = totalScore / matches;
        }

        // Level Progress (Assuming 100 levels for example, or relative to a goal)
        int progress = (currentLevel % 10) * 10;

        tvRank.setText(rank == 999999 ? "#---" : "#" + rank);
        tvWinRate.setText(getString(R.string.stats_win_rate_format, winRate));
        tvTotalMatches.setText(String.valueOf(matches));
        tvWins.setText("W: " + wins);
        tvLosses.setText("L: " + losses);
        tvDraws.setText("D: " + draws);
        tvLevelTitle.setText(getString(R.string.stats_level_progress_format, currentLevel));
        pbLevel.setProgress(progress);
        tvTotalPoints.setText(String.valueOf(totalScore));
        tvAvgScore.setText(String.valueOf(avgScore));

        return root;
    }
}
