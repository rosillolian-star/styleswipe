package com.example.styleswipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitViewHolder> {
    private List<model> outfitList;
    private int expandedPosition = -1;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(model outfit, int position);
    }

    public OutfitAdapter(List<model> outfitList, OnDeleteClickListener deleteListener) {
        this.outfitList = outfitList != null ? outfitList : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @Override
    public OutfitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OutfitViewHolder holder, int position) {
        model outfit = outfitList.get(position);

        final boolean isExpanded = position == expandedPosition;
        holder.bind(outfit, isExpanded);

        // Expand/collapse toggle
        View.OnClickListener toggleClick = v -> {
            int prevExpanded = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;

            if (prevExpanded != -1) notifyItemChanged(prevExpanded);
            if (expandedPosition != -1) notifyItemChanged(expandedPosition);
        };

        if (holder.expandIcon != null) {
            holder.expandIcon.setOnClickListener(toggleClick);
        }
        holder.itemView.setOnClickListener(toggleClick);

        // Delete logic
        if (holder.deleteBtn != null) {
            holder.deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(outfit, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    public void updateList(List<model> newList) {
        this.outfitList = newList != null ? newList : new ArrayList<>();
        expandedPosition = -1;
        notifyDataSetChanged();
    }
}
