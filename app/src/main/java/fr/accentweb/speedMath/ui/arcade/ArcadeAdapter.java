package fr.accentweb.speedMath.ui.arcade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.PlayerManager;

import java.util.List;

public class ArcadeAdapter extends RecyclerView.Adapter<ArcadeAdapter.ViewHolder> {

    private final List<ArcadeItem> items;
    private final OnItemClickListener listener;
    private final PlayerManager playerManager;

    public interface OnItemClickListener {
        void onPlayClick(View v, String mode);
        void onSettingsClick(View v, String mode);
    }

    public ArcadeAdapter(List<ArcadeItem> items, OnItemClickListener listener, PlayerManager playerManager) {
        this.items = items;
        this.listener = listener;
        this.playerManager = playerManager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconCard;
        TextView titleCard;
        TextView descriptionCard;
        View btnPlay;
        View btnSettings;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCard = itemView.findViewById(R.id.iconCard);
            titleCard = itemView.findViewById(R.id.titleCard);
            descriptionCard = itemView.findViewById(R.id.descriptionCard);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnSettings = itemView.findViewById(R.id.btnSettings);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_arcade, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        ArcadeItem item = items.get(pos);

        h.iconCard.setText(item.icon);
        h.iconCard.setTextSize(item.iconSize);
        h.titleCard.setText(item.title);

        // Mettre à jour le texte de difficulté pour "Memory" et "Memory Duo"
        if (item.mode.equals("MEMORY")) {
            int savedDifficulty = playerManager.getMemoryDifficulty();
            MemoryDifficulty difficulty = MemoryDifficulty.values()[savedDifficulty];
            h.descriptionCard.setText("Difficulty: " + difficulty.label);
        } else if (item.mode.equals("MEMORY_DUO")) {
            int savedDifficulty = playerManager.getMemoryDuoDifficulty();
            MemoryDifficulty difficulty = MemoryDifficulty.values()[savedDifficulty];
            h.descriptionCard.setText("Difficulty: " + difficulty.label);
        } else {
            h.descriptionCard.setText(item.description);
        }

        h.btnPlay.setTag(item.mode);
        h.btnPlay.setOnClickListener(v -> listener.onPlayClick(v, item.mode));
        h.btnSettings.setTag(item.mode);
        h.btnSettings.setOnClickListener(v -> listener.onSettingsClick(v, item.mode));
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}
