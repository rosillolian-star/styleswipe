package com.example.styleswipe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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
 * Displays the style timeline and dynamic album categories.
 */
public class PicturesFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<GalleryItem> galleryItems = new ArrayList<>();
    private List<GalleryItem> fullList = new ArrayList<>();
    private LinearLayout albumButtonContainer;
    private View emptyView;
    private boolean favoritesOnly = false;

    public static PicturesFragment newInstance(boolean favoritesOnly) {
        PicturesFragment fragment = new PicturesFragment();
        android.os.Bundle args = new android.os.Bundle();
        args.putBoolean("favoritesOnly", favoritesOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            favoritesOnly = getArguments().getBoolean("favoritesOnly");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        
        recyclerView = view.findViewById(R.id.picturesRecyclerView);
        albumButtonContainer = view.findViewById(R.id.albumButtonContainer);
        emptyView = view.findViewById(R.id.emptyGalleryText);
        
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == GalleryAdapter.TYPE_HEADER ? 3 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        
        refreshData();
        
        return view;
    }

    /**
     * REFRESH UI
     * Reloads both the album category buttons and the photo timeline.
     */
    public void refreshData() {
        loadAlbums();
        loadPictures();
        
        if (emptyView != null) {
            emptyView.setVisibility(galleryItems.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (adapter == null) {
            adapter = new GalleryAdapter(galleryItems);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadAlbums() {
        if (albumButtonContainer == null) return;
        albumButtonContainer.removeAllViews();

        // 1. Add "All" Button
        addAlbumButton("All", -1);

        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("albums", null, null, null, null, null, "name ASC");

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            addAlbumButton(name, id);
        }
        cursor.close();
        db.close();
    }

    private void addAlbumButton(String title, int albumId) {
        Button btn = new Button(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, (int) (40 * getResources().getDisplayMetrics().density));
        params.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
        btn.setLayoutParams(params);
        
        btn.setText(title);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.rounded_button);
        btn.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.white));
        
        btn.setOnClickListener(v -> {
            if (albumId == -1) {
                filter(""); // Show all
            } else {
                filterByAlbum(albumId);
            }
        });
        
        albumButtonContainer.addView(btn);
    }

    private void loadPictures() {
        galleryItems.clear();
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = favoritesOnly ? "is_favorite = 1" : null;

        Cursor cursor = db.query("outfits", null, selection, null, null, null, "date_taken DESC");

        Map<String, List<model>> grouped = new LinkedHashMap<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String event = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imageBitmap"));
            String dateTaken = cursor.getString(cursor.getColumnIndexOrThrow("date_taken"));
            int albumId = cursor.getInt(cursor.getColumnIndexOrThrow("album_id"));
            int isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));

            Bitmap bitmap = null;
            if (imageBytes != null) {
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }

            model outfit = new model(id, event, location, tags, notes, imageUri, bitmap, dateTaken, albumId, isFav == 1);
            
            String dateKey = formatHeaderDate(dateTaken);
            if (!grouped.containsKey(dateKey)) {
                grouped.put(dateKey, new ArrayList<>());
            }
            grouped.get(dateKey).add(outfit);
        }
        cursor.close();
        db.close();

        for (String date : grouped.keySet()) {
            galleryItems.add(new GalleryItem(date));
            for (model outfit : grouped.get(date)) {
                galleryItems.add(new GalleryItem(outfit));
            }
        }
        fullList = new ArrayList<>(galleryItems);
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            galleryItems.clear();
            galleryItems.addAll(fullList);
        } else {
            List<GalleryItem> filtered = new ArrayList<>();
            for (GalleryItem item : fullList) {
                if (item.isHeader) continue;
                String content = (item.outfit.getEvent() + " " + item.outfit.getLocation() + " " + item.outfit.getTags()).toLowerCase();
                if (content.contains(query.toLowerCase())) {
                    filtered.add(item);
                }
            }
            galleryItems.clear();
            galleryItems.addAll(filtered);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void filterByAlbum(int albumId) {
        List<GalleryItem> filtered = new ArrayList<>();
        for (GalleryItem item : fullList) {
            if (item.isHeader) continue;
            if (item.outfit.getAlbumId() == albumId) {
                filtered.add(item);
            }
        }
        galleryItems.clear();
        galleryItems.addAll(filtered);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private String formatHeaderDate(String rawDate) {
        if (rawDate == null) return "Unknown Date";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(rawDate);
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            Date yesterday = cal.getTime();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            if (fmt.format(date).equals(fmt.format(today))) return "Today";
            if (fmt.format(date).equals(fmt.format(yesterday))) return "Yesterday";
            return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return rawDate;
        }
    }
}
