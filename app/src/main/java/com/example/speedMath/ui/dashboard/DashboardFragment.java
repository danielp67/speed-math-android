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
import com.example.speedMath.core.LevelGenerator;

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
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new LevelAdapter(LevelGenerator.generateLevels(), v -> {
            LevelItem item = (LevelItem) v.getTag();
            Bundle args = new Bundle();
            args.putInt("LEVEL", item.levelNumber);
            args.putString("MODE", item.mode);
            args.putInt("REQUIRED_CORRECT", item.requiredCorrect);
            args.putInt("DIFFICULTY", item.difficulty);
            args.putInt("STATUS", 0);

            Navigation.findNavController(v)
                    .navigate(R.id.action_navigation_dashboard_to_levelFragment, args);
        });

        recyclerView.setAdapter(adapter);

        return root;
    }
}
