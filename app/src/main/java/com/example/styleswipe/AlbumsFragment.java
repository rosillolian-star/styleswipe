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
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * ALBUMS FRAGMENT
 * Manages the "Albums" tab view.
 * It displays a grid of collections, each with a cover photo and item count.
 */
public class AlbumsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private List<Album> albumList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        
        // 1. RECYCLERVIEW INITIALIZATION
        recyclerView = view.findViewById(R.id.albumsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns for albums
        
        // 2. CREATE ALBUM TRIGGER
        FloatingActionButton fab = view.findViewById(R.id.addAlbumBtn);
        fab.setOnClickListener(v -> showAddAlbumDialog());
        
        // 3. LOAD & DISPLAY
        loadAlbums();
        adapter = new AlbumAdapter(albumList);
        recyclerView.setAdapter(adapter);
        
        return view;
    }

    /**
     * ALBUM CREATION DIALOG
     * Builds and shows a themed pop-up for naming a new album.
     */
    private void showAddAlbumDialog() {
        // Create a layout container for the EditText to add margins
        android.widget.FrameLayout container = new android.widget.FrameLayout(getContext());
        EditText input = new EditText(getContext());
        input.setHint("Album Name");
        input.setHintTextColor(ContextCompat.getColor(getContext(), R.color.style_tan));
        input.setTextColor(ContextCompat.getColor(getContext(), R.color.style_dark_brown));
        
        // Custom background for the input: White with a peachy border
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(android.graphics.Color.WHITE);
        gd.setCornerRadius(16f);
        gd.setStroke(2, ContextCompat.getColor(getContext(), R.color.style_light_peachy));
        input.setBackground(gd);
        input.setPadding(32, 32, 32, 32);
        
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(64, 16, 64, 16);
        input.setLayoutParams(params);
        container.addView(input);

        // Show the Material Alert Dialog
        new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogTheme)
                .setTitle("New Album")
                .setMessage("Enter the name of your new style collection:")
                .setView(container)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (!name.isEmpty()) {
                        createAlbum(name); // Persist to database
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * SAVE ALBUM TO DB
     * Inserts the new album name into the local 'albums' table.
     */
    private void createAlbum(String name) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db.insert("albums", null, cv);
        db.close();
        
        // Refresh the list to show the new album
        loadAlbums();
        adapter.notifyDataSetChanged();
    }

    /**
     * FETCH ALBUM DATA
     * Retrieves all albums and their associated photo counts and latest covers.
     */
    private void loadAlbums() {
        albumList.clear();
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // A. Get all album records
        Cursor cursor = db.query("albums", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            
            // B. Define selection (handle "Recent" special case)
            String selection = "album_id = ?";
            if (name.equalsIgnoreCase("Recent")) {
                selection = "album_id = ? OR album_id = -1";
            }
            
            // C. Get Count of outfits in this album
            Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM outfits WHERE " + selection, new String[]{String.valueOf(id)});
            int count = 0;
            if (countCursor.moveToFirst()) count = countCursor.getInt(0);
            countCursor.close();

            // D. Get the Latest Image for the album cover
            Cursor imgCursor = db.query("outfits", new String[]{"imageUri", "imageBitmap"}, 
                    selection, new String[]{String.valueOf(id)}, null, null, "date_taken DESC", "1");
            
            String coverUri = null;
            Bitmap coverBitmap = null;
            
            if (imgCursor.moveToFirst()) {
                coverUri = imgCursor.getString(0);
                byte[] imageBytes = imgCursor.getBlob(1);
                if (imageBytes != null) {
                    coverBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                }
            }
            imgCursor.close();
            
            // Add the compiled album data to our list
            albumList.add(new Album(id, name, count, coverUri, coverBitmap));
        }
        cursor.close();
        db.close();
    }

    /**
     * DATA MODEL FOR ALBUMS
     */
    public static class Album {
        public int id;
        public String name;
        public int count;
        public String coverUri;
        public Bitmap coverBitmap;
        
        public Album(int id, String name, int count, String coverUri, Bitmap coverBitmap) { 
            this.id = id; 
            this.name = name; 
            this.count = count; 
            this.coverUri = coverUri;
            this.coverBitmap = coverBitmap;
        }
    }

    /**
     * ADAPTER CLASS
     * Binds the Album data to the Grid items in the RecyclerView.
     */
    private static class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
        private List<Album> items;
        public AlbumAdapter(List<Album> items) { this.items = items; }

        @NonNull
        @Override
        public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
            return new AlbumViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
            Album album = items.get(position);
            holder.name.setText(album.name);
            holder.count.setText(String.valueOf(album.count) + " photos");

            // 1. DYNAMIC COVER LOADING
            if (album.coverUri != null && !album.coverUri.isEmpty()) {
                holder.cover.setImageURI(Uri.parse(album.coverUri));
            } else if (album.coverBitmap != null) {
                holder.cover.setImageBitmap(album.coverBitmap);
            } else {
                // Fallback if album is empty
                holder.cover.setImageResource(R.drawable.rounded_button); 
            }

            // 2. ALBUM CLICK NAVIGATION
            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), AlbumDetailActivity.class);
                intent.putExtra("ALBUM_ID", album.id);
                intent.putExtra("ALBUM_NAME", album.name);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class AlbumViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView name, count;
            ImageView cover;
            AlbumViewHolder(View v) { 
                super(v); 
                name = v.findViewById(R.id.albumName); 
                count = v.findViewById(R.id.albumCount); 
                cover = v.findViewById(R.id.albumCover);
            }
        }
    }
}
