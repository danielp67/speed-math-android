package com.example.speedMath.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;
import com.example.speedMath.model.Level;

import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    private List<Level> levels;
    private View.OnClickListener listener;

    public LevelAdapter(List<Level> levels, View.OnClickListener listener) {
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
        Level level = levels.get(position);
        holder.button.setText("Niveau " + level.levelNumber);
        holder.button.setEnabled(level.unlocked);
        holder.button.setTag(level.levelNumber);
        holder.button.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        Button button;
        public LevelViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.buttonLevel);
        }
    }
}
