package com.example.speedMath.ui.league;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;
import com.example.speedMath.core.LevelGenerator;
import com.example.speedMath.core.PlayerManager;
import java.util.List;

public class LeagueFragment extends Fragment {

    private RecyclerView recyclerView;
    private LevelAdapter adapter;
    private List<LevelItem> levels;
    private PlayerManager playerManager;
    private TextView textScoreRight, textLevelNumber;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_league, container, false);

        recyclerView = root.findViewById(R.id.recyclerLevels);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        textLevelNumber = root.findViewById(R.id.textLevelNumber);
        textScoreRight = root.findViewById(R.id.textScoreRight);

        playerManager = PlayerManager.getInstance(requireContext());

        int currentLevel = playerManager.getCurrentLevel();
        int score = playerManager.getTotalScore();
        textLevelNumber.setText(currentLevel + " ⭐");
        textScoreRight.setText(score + " pts");

        adapter = new LevelAdapter(LevelGenerator.generateLevels(playerManager.getCurrentLevel()), v -> {
            LevelItem item = (LevelItem) v.getTag();
            Bundle args = new Bundle();
            args.putInt("LEVEL", item.levelNumber);
            args.putString("MODE", item.mode);
            args.putLong("TARGET_SCORE", item.targetScore);
            args.putInt("DIFFICULTY", item.difficulty);
            args.putInt("STATUS", 0);
            if(playerManager.isHapticEnabled()) v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Navigation.findNavController(v)
                    .navigate(R.id.action_navigation_dashboard_to_levelFragment, args);
        });

        recyclerView.setAdapter(adapter);

        return root;
    }

}
