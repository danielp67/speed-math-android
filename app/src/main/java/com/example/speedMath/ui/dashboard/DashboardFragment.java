package com.example.speedMath.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private LevelAdapter adapter;
    private List<LevelItem> levels;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = root.findViewById(R.id.recyclerLevels);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 colonnes

        // Créer les niveaux
        levels = new ArrayList<>();
        levels.add(new LevelItem(1, "ADD", 5, 1));
        levels.add(new LevelItem(2, "SUB", 5, 1));
        levels.add(new LevelItem(3, "MUL", 5, 1));
        levels.add(new LevelItem(4, "DIV", 5, 1));
        levels.add(new LevelItem(5, "ALL", 5, 1));
        levels.add(new LevelItem(6, "ADD", 5, 2));
        levels.add(new LevelItem(7, "SUB", 5, 2));
        levels.add(new LevelItem(8, "MUL", 5, 2));
        levels.add(new LevelItem(9, "DIV", 5, 2));
        levels.add(new LevelItem(10, "ALL", 10, 2));
        // ajouter plus de niveaux si nécessaire

        adapter = new LevelAdapter(levels, v -> {
            LevelItem item = (LevelItem) v.getTag();
            Bundle args = new Bundle();
            args.putInt("LEVEL", item.levelNumber);
            args.putString("MODE", item.mode);
            args.putInt("REQUIRED_CORRECT", item.requiredCorrect);
            args.putInt("DIFFICULTY", item.difficulty);

            Navigation.findNavController(v)
                    .navigate(R.id.action_navigation_dashboard_to_levelFragment, args);
        });

        recyclerView.setAdapter(adapter);

        return root;
    }
}
