package com.example.styleswipe;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * OUTFIT PICKER ACTIVITY
 * Allows users to browse their existing outfits and "move" them into a specific album.
 */
public class OutfitPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PickerAdapter adapter;
    private List<model> outfitList = new ArrayList<>();
    private int targetAlbumId; // The ID of the album we are adding photos TO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_picker);

        // 1. DATA RECEIVAL: Get target album ID from the intent
        targetAlbumId = getIntent().getIntExtra("ALBUM_ID", -1);
        if (targetAlbumId == -1) {
            finish(); // Safety exit if album ID is invalid
            return;
        }

        // Standard back button setup
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        // 2. GRID RECYCLERVIEW SETUP (3 columns)
        recyclerView = findViewById(R.id.pickerRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // 3. LOAD DATA & BIND ADAPTER
        loadAllAvailableOutfits();
        adapter = new PickerAdapter(outfitList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * DATABASE LOADING
     * Fetches outfits from the database that are NOT already in the target album.
     */
    private void loadAllAvailableOutfits() {
        outfitList.clear();
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // QUERY CRITERIA: Select all outfits where album_id is different from the current album
        Cursor cursor = db.query("outfits", null, "album_id != ?", 
                new String[]{String.valueOf(targetAlbumId)}, null, null, "date_taken DESC");

        while (cursor.moveToNext()) {
            // Data extraction logic
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String event = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imageBitmap"));
            String dateTaken = cursor.getString(cursor.getColumnIndexOrThrow("date_taken"));
            int albumId = cursor.getInt(cursor.getColumnIndexOrThrow("album_id"));

            Bitmap bitmap = null;
            if (imageBytes != null) {
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }

            // Create a model object for the picker list
            outfitList.add(new model(id, event, location, tags, notes, imageUri, bitmap, dateTaken, albumId));
        }
        cursor.close();
        db.close();
    }

    /**
     * DATABASE UPDATE
     * Updates the 'album_id' column of a specific outfit to the new target album.
     */
    private void addOutfitToAlbum(model outfit) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues cv = new ContentValues();
        cv.put("album_id", targetAlbumId); // Set the new album ID
        
        // Update the row matching this outfit's unique ID
        db.update("outfits", cv, "id = ?", new String[]{String.valueOf(outfit.getId())});
        db.close();
        
        finish(); // Close activity and return to the Album screen
    }

    /**
     * PICKER ADAPTER
     * Specialized adapter to show the selection grid and handle item clicks.
     */
    private class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.ViewHolder> {
        private List<model> items;
        public PickerAdapter(List<model> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reusing the same grid item layout as the main gallery
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_picture, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            model o = items.get(position);
            
            // Display image
            if (o.getImageUri() != null && !o.getImageUri().isEmpty()) {
                holder.img.setImageURI(Uri.parse(o.getImageUri()));
            } else if (o.getImageBitmap() != null) {
                holder.img.setImageBitmap(o.getImageBitmap());
            }
            
            // SELECTION LOGIC: Click a photo to "move" it into the album
            holder.itemView.setOnClickListener(v -> addOutfitToAlbum(o));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            ViewHolder(View v) { super(v); img = v.findViewById(R.id.pictureThumb); }
        }
    }
}
