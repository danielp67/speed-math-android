package com.example.speedMath.ui.league;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;

import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private List<LevelItem> levels;
    private View.OnClickListener listener;

    public LevelAdapter(List<LevelItem> levels, View.OnClickListener listener) {
        this.levels = levels;
        this.listener = listener;
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

        holder.levelNumber.setText(String.valueOf(item.levelNumber));

        switch (item.status) {
            case LOCKED:
                holder.levelStatus.setText("\uD83D\uDD12"); // 🔒
                holder.levelNumber.setTextColor(
                        holder.itemView.getResources().getColor(R.color.grey_light)
                );

                // FIX : désactiver les clics
                holder.itemView.setOnClickListener(null);
                holder.itemView.setClickable(false);
                break;

            case UNLOCKED:
                holder.levelStatus.setText("✩");
                holder.levelNumber.setTextColor(
                        holder.itemView.getResources().getColor(R.color.black)
                );

                // autoriser clic
                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(listener);
                holder.itemView.setTag(item);
                break;

            case COMPLETED:
                holder.levelStatus.setText("⭐");
                holder.levelNumber.setTextColor(
                        holder.itemView.getResources().getColor(R.color.black)
                );

                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(listener);
                holder.itemView.setTag(item);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView levelNumber, levelStatus;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            levelNumber = itemView.findViewById(R.id.textLevelNumber);
            levelStatus = itemView.findViewById(R.id.textLevelStatus);
        }
    }
}
