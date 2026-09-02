package com.example.styleswipe;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class OutfitViewHolder extends RecyclerView.ViewHolder {
    public ImageView outfitThumb;
    public View expandIcon, deleteBtn;
    public TextView eventText, locationText, tagsText, notesText;
    public LinearLayout detailsLayout;

    public OutfitViewHolder(View itemView) {
        super(itemView);
        outfitThumb = itemView.findViewById(R.id.outfitThumb);
        expandIcon = itemView.findViewById(R.id.expandIcon);
        deleteBtn = itemView.findViewById(R.id.deleteBtn);
        eventText = itemView.findViewById(R.id.eventText);
        locationText = itemView.findViewById(R.id.locationText);
        tagsText = itemView.findViewById(R.id.tagsText);
        notesText = itemView.findViewById(R.id.notesText);
        detailsLayout = itemView.findViewById(R.id.detailsLayout);
    }

    public void bind(model outfit, boolean isExpanded) {
        if (eventText != null) eventText.setText(outfit.getEvent());
        if (locationText != null) locationText.setText(outfit.getLocation());
        if (tagsText != null) tagsText.setText(outfit.getTags());
        if (notesText != null) notesText.setText(outfit.getNotes());

        if (detailsLayout != null) {
            detailsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        // Handle Image
        if (outfit.getImageUri() != null && !outfit.getImageUri().isEmpty()) {
            try {
                outfitThumb.setImageURI(Uri.parse(outfit.getImageUri()));
            } catch (Exception e) {
                // If URI access fails (e.g. permission lost), show fallback
                outfitThumb.setImageResource(R.drawable.rounded_button);
            }
        } else if (outfit.getImageBitmap() != null) {
            outfitThumb.setImageBitmap(outfit.getImageBitmap());
        } else {
            // Fallback
            outfitThumb.setImageResource(R.drawable.rounded_button);
        }
    }
}
