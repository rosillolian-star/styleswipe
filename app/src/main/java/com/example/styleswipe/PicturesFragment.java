package com.example.styleswipe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * PICTURES FRAGMENT
 * Manages the "Pictures" tab view.
 * It fetches outfits from the DB, groups them by date, and displays them in a grid.
 */
public class PicturesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<GalleryItem> galleryItems = new ArrayList<>(); // The current visible list
    private List<GalleryItem> fullList = new ArrayList<>();     // Master list for reset/search

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        recyclerView = view.findViewById(R.id.picturesRecyclerView);
        
        // 1. GRID LAYOUT WITH SPAN LOOKUP
        // We use a 3-column grid. Headers take up all 3 columns, while photos take only 1.
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == GalleryAdapter.TYPE_HEADER ? 3 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        
        // 2. DATA LOADING
        loadPictures();
        adapter = new GalleryAdapter(galleryItems);
        recyclerView.setAdapter(adapter);
        
        return view;
    }

    /**
     * FETCH & GROUP OUTFITS
     * Loads outfits from database and organizes them by their capture date.
     */
    private void loadPictures() {
        galleryItems.clear();
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Retrieve all outfits, most recent first
        Cursor cursor = db.query("outfits",
                null, null, null, null, null, "date_taken DESC");

        // Temporary map to group items by date string
        Map<String, List<model>> grouped = new LinkedHashMap<>();

        while (cursor.moveToNext()) {
            // Extract all database fields
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String event = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imageBitmap"));
            String dateTaken = cursor.getString(cursor.getColumnIndexOrThrow("date_taken"));
            int albumId = cursor.getInt(cursor.getColumnIndexOrThrow("album_id"));

            // Decode image if it's stored as bytes
            Bitmap bitmap = null;
            if (imageBytes != null) {
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }

            model outfit = new model(id, event, location, tags, notes, imageUri, bitmap, dateTaken, albumId);
            
            // Format the raw DB timestamp into a readable grouping key (e.g. "Today")
            String dateKey = formatHeaderDate(dateTaken);
            if (!grouped.containsKey(dateKey)) {
                grouped.put(dateKey, new ArrayList<>());
            }
            grouped.get(dateKey).add(outfit);
        }
        cursor.close();
        db.close();

        // 3. FLATTEN DATA FOR ADAPTER
        // Convert the group map into a simple list containing both Headers and Pictures.
        for (String date : grouped.keySet()) {
            galleryItems.add(new GalleryItem(date)); // Insert Date Header
            for (model outfit : grouped.get(date)) {
                galleryItems.add(new GalleryItem(outfit)); // Insert Outfit Photo
            }
        }
        // Cache the master list for searching
        fullList = new ArrayList<>(galleryItems);
    }

    /**
     * SEARCH FILTER
     * Filters the gallery based on the search query.
     */
    public void filter(String query) {
        if (query.isEmpty()) {
            // Reset to full list if search is cleared
            galleryItems.clear();
            galleryItems.addAll(fullList);
        } else {
            List<GalleryItem> filtered = new ArrayList<>();
            for (GalleryItem item : fullList) {
                if (item.isHeader) continue; // Ignore headers in search results
                
                // Match against event or location
                if ((item.outfit.getEvent() != null && item.outfit.getEvent().toLowerCase().contains(query.toLowerCase())) ||
                    (item.outfit.getLocation() != null && item.outfit.getLocation().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(item);
                }
            }
            galleryItems.clear();
            galleryItems.addAll(filtered);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * DATE FORMATTER
     * Converts a database timestamp into a user-friendly relative string.
     */
    private String formatHeaderDate(String rawDate) {
        if (rawDate == null) return "Unknown Date";
        try {
            // SQLite default format is yyyy-MM-dd HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(rawDate);
            
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            Date yesterday = cal.getTime();

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            if (fmt.format(date).equals(fmt.format(today))) return "Today";
            if (fmt.format(date).equals(fmt.format(yesterday))) return "Yesterday";

            // Otherwise show a standard date format
            return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return rawDate; // Fallback to raw text on error
        }
    }
}
