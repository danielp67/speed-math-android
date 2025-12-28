package fr.accentweb.speedMath.ui.arcade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.accentweb.speedMath.R;
import fr.accentweb.speedMath.core.GameDifficulty;
import fr.accentweb.speedMath.core.MemoryDifficulty;
import fr.accentweb.speedMath.core.PlayerManager;

public class ArcadeAdapter extends RecyclerView.Adapter<ArcadeAdapter.ViewHolder> {

    private final List<ArcadeItem> items;
    private final OnItemClickListener listener;
    private final PlayerManager playerManager;
    private OnlineStats onlineStats; // <-- rendre modifiable

    public interface OnItemClickListener {
        void onPlayClick(View v, String mode);
        void onSettingsClick(View v, String mode);
    }

    public ArcadeAdapter(List<ArcadeItem> items, OnItemClickListener listener, PlayerManager playerManager, OnlineStats onlineStats) {
        this.items = items;
        this.listener = listener;
        this.playerManager = playerManager;
        this.onlineStats = onlineStats; // peut être null initialement
    }

    public void setOnlineStats(OnlineStats stats) {
        this.onlineStats = stats;
        notifyDataSetChanged(); // Met à jour toutes les cartes
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconCard, titleCard, descriptionCard;
        View btnPlay, btnSettings;
        View layoutOnlineInfo;
        TextView txtPlayersOnline, txtGamesLeft;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCard = itemView.findViewById(R.id.iconCard);
            titleCard = itemView.findViewById(R.id.titleCard);
            descriptionCard = itemView.findViewById(R.id.descriptionCard);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnSettings = itemView.findViewById(R.id.btnSettings);
            layoutOnlineInfo = itemView.findViewById(R.id.layoutOnlineInfo);
            txtPlayersOnline = itemView.findViewById(R.id.txtPlayersOnline);
            txtGamesLeft = itemView.findViewById(R.id.txtGamesLeft);
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

        // ICON & TITLE
        h.iconCard.setText(item.icon);
        h.iconCard.setTextSize(item.iconSize);
        h.titleCard.setText(item.title);

        // DESCRIPTION
        switch (item.mode) {
            case "MEMORY":
                int memDiff = playerManager.getMemoryDifficulty();
                MemoryDifficulty memDifficulty = MemoryDifficulty.values()[memDiff];
                h.descriptionCard.setText(item.description + " - " + memDifficulty.label);
                break;

            case "MEMORY_DUO":
                int memDuoDiff = playerManager.getMemoryDuoDifficulty();
                MemoryDifficulty memDuoDifficulty = MemoryDifficulty.values()[memDuoDiff];
                h.descriptionCard.setText(item.description + " - " + memDuoDifficulty.label);
                break;

            default:
                GameDifficulty gameDiff = getCurrentGameDifficulty(item.mode);
                h.descriptionCard.setText(item.description + " - " + gameDiff.getDisplayName());
        }

        boolean isOnline = "ONLINE".equals(item.mode);

        // SETTINGS / ONLINE INFO
        h.btnSettings.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        h.btnSettings.setClickable(!isOnline);

        if (isOnline) {
            h.layoutOnlineInfo.setVisibility(View.VISIBLE);
            h.descriptionCard.setText(item.description);

            if (onlineStats != null) {
                h.txtPlayersOnline.setText("\uD83D\uDFE2 " + onlineStats.playersOnline);
                h.txtGamesLeft.setText(onlineStats.gamesPlayedToday + " / " + onlineStats.dailyLimit);
            } else {
                h.txtPlayersOnline.setText("\uD83D\uDFE2 0");
                h.txtGamesLeft.setText("0 / 0");
            }
        } else {
            h.layoutOnlineInfo.setVisibility(View.GONE);
        }

        // CLICK LISTENERS
        h.btnPlay.setTag(item.mode);
        h.btnPlay.setOnClickListener(v -> listener.onPlayClick(v, item.mode));

        h.btnSettings.setTag(item.mode);
        h.btnSettings.setOnClickListener(v -> listener.onSettingsClick(v, item.mode));
    }

    private GameDifficulty getCurrentGameDifficulty(String mode) {
        int difficultyValue = 3; // PROGRESSIVE par défaut

        switch (mode) {
            case "QCM": difficultyValue = playerManager.getSoloDifficulty(); break;
            case "DUAL": difficultyValue = playerManager.getBattleDifficulty(); break;
            case "ALL": difficultyValue = playerManager.getAllSuiteDifficulty(); break;
            case "ADD": difficultyValue = playerManager.getAddSuiteDifficulty(); break;
            case "SUB": difficultyValue = playerManager.getSubSuiteDifficulty(); break;
            case "MUL": difficultyValue = playerManager.getMulSuiteDifficulty(); break;
            case "DIV": difficultyValue = playerManager.getDivSuiteDifficulty(); break;
        }

        return GameDifficulty.fromValue(difficultyValue);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
