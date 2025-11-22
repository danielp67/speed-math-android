package com.example.speedMath.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerLevels;
    private LevelsAdapter adapter;
    private List<LevelItem> levels;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerLevels = root.findViewById(R.id.recyclerLevels);

        // Création de la liste des niveaux
        levels = new ArrayList<>();
        for (int i = 1; i <= 20; i++) { // 20 niveaux pour exemple
            boolean unlocked = i <= 5; // par ex. seuls les 5 premiers sont débloqués
            levels.add(new LevelItem(i, unlocked));
        }

        // Adapter
        adapter = new LevelsAdapter(levels);
        recyclerLevels.setAdapter(adapter);

        // Grid responsive
        int spanCount = getResources().getDisplayMetrics().widthPixels / 300;
        if(spanCount < 2) spanCount = 2;
        recyclerLevels.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        return root;
    }

    // Classe pour représenter un niveau
    public static class LevelItem {
        public int levelNumber;
        public boolean unlocked;

        public LevelItem(int levelNumber, boolean unlocked) {
            this.levelNumber = levelNumber;
            this.unlocked = unlocked;
        }
    }

    // Adapter RecyclerView
    public class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.LevelViewHolder> {

        private List<LevelItem> levels;

        public LevelsAdapter(List<LevelItem> levels) {
            this.levels = levels;
        }

        @NonNull
        @Override
        public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_level, parent, false);
            return new LevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
            LevelItem item = levels.get(position);
            holder.buttonLevel.setText("Level " + item.levelNumber);

            // Débloqué ou non
            holder.buttonLevel.setEnabled(item.unlocked);
            holder.buttonLevel.setAlpha(item.unlocked ? 1f : 0.5f);

            holder.buttonLevel.setOnClickListener(v -> {
                // Passer le niveau choisi au GameFragment
                Bundle args = new Bundle();
                args.putInt("LEVEL", item.levelNumber);
                Navigation.findNavController(v)
                        .navigate(R.id.action_navigation_dashboard_to_gameFragment, args);
            });
        }

        @Override
        public int getItemCount() {
            return levels.size();
        }

        class LevelViewHolder extends RecyclerView.ViewHolder {
            Button buttonLevel;
            LevelViewHolder(@NonNull View itemView) {
                super(itemView);
                buttonLevel = itemView.findViewById(R.id.buttonLevel);
            }
        }
    }
}
