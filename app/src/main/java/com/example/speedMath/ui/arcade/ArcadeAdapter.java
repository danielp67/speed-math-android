package com.example.speedMath.ui.arcade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.speedMath.R;

import java.util.List;

public class ArcadeAdapter extends RecyclerView.Adapter<ArcadeAdapter.ViewHolder> {

    private final List<ArcadeItem> items;
    private final View.OnClickListener clickListener;

    public ArcadeAdapter(List<ArcadeItem> items, View.OnClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iconCard);
            title = itemView.findViewById(R.id.titleCard);
            description = itemView.findViewById(R.id.descriptionCard);
        }
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_arcade, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        ArcadeItem item = items.get(pos);

        h.icon.setText(item.icon);
        h.icon.setTextSize(item.iconSize);
        h.title.setText(item.title);
        h.description.setText(item.description);

        h.itemView.setTag(item.mode);
        h.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() { return items.size(); }
}

