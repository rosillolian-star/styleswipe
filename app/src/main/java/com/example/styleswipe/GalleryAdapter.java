package com.example.styleswipe;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * GALLERY ADAPTER
 * This is a multi-type adapter that handles both Date Headers and Outfit Pictures.
 * It is used by both the Pictures tab and the Album Detail screen.
 */
public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // A. View Types
    public static final int TYPE_HEADER = 0;  // For date labels (Today, Oct 12...)
    public static final int TYPE_PICTURE = 1; // For actual outfit photo tiles
    
    private List<GalleryItem> items;

    public GalleryAdapter(List<GalleryItem> items) { 
        this.items = items; 
    }

    /**
     * VIEW TYPE LOGIC
     * Determines whether a position should show a header or a picture.
     */
    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader ? TYPE_HEADER : TYPE_PICTURE;
    }

    /**
     * VIEWHOLDER CREATION
     * Inflates the correct layout file (Header vs Picture) based on viewType.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_picture, parent, false);
            return new PictureViewHolder(v);
        }
    }

    /**
     * DATA BINDING
     * populates the views with data from the GalleryItem list.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GalleryItem item = items.get(position);
        
        // 1. Handle Header Binding
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).dateText.setText(item.date);
        } 
        // 2. Handle Picture Binding
        else if (holder instanceof PictureViewHolder) {
            PictureViewHolder pvh = (PictureViewHolder) holder;
            model o = item.outfit;
            
            // Set image from path or direct data
            if (o.getImageUri() != null && !o.getImageUri().isEmpty()) {
                pvh.img.setImageURI(Uri.parse(o.getImageUri()));
            } else if (o.getImageBitmap() != null) {
                pvh.img.setImageBitmap(o.getImageBitmap());
            }

            // 3. GALLERY ITEM INTERACTION
            // When a photo tile is clicked, open the detail view for that specific outfit
            pvh.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), OutfitDetailActivity.class);
                intent.putExtra("OUTFIT_ID", o.getId()); // Pass the unique DB ID
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() { 
        return items.size(); 
    }

    // SPECIALIZED VIEWHOLDERS
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        HeaderViewHolder(View v) { 
            super(v); 
            dateText = v.findViewById(R.id.dateHeader); 
        }
    }

    static class PictureViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        PictureViewHolder(View v) { 
            super(v); 
            img = v.findViewById(R.id.pictureThumb); 
        }
    }
}
