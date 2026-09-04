package com.example.styleswipe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
 * ALBUM DETAIL ACTIVITY
 */
public class AlbumDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<GalleryItem> galleryItems = new ArrayList<>();
    private int albumId;
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        albumId = getIntent().getIntExtra("ALBUM_ID", -1);
        albumName = getIntent().getStringExtra("ALBUM_NAME");

        if (albumId == -1) {
            finish();
            return;
        }

        TextView title = findViewById(R.id.albumTitle);
        title.setText(albumName);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.albumPicturesRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == GalleryAdapter.TYPE_HEADER ? 3 : 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);

        loadAlbumPictures();
        adapter = new GalleryAdapter(galleryItems);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.addPictureToAlbumBtn).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, OutfitPickerActivity.class);
            intent.putExtra("ALBUM_ID", albumId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlbumPictures();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadAlbumPictures() {
        galleryItems.clear();
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "album_id = ?";
        String[] selectionArgs = new String[]{String.valueOf(albumId)};
        
        if (albumName != null && albumName.equalsIgnoreCase("Recent")) {
             selection = "album_id = ? OR album_id = -1";
        }

        Cursor cursor = db.query("outfits", null, selection, selectionArgs, null, null, "date_taken DESC");

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
            int aId = cursor.getInt(cursor.getColumnIndexOrThrow("album_id"));
            int isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));

            Bitmap bitmap = null;
            if (imageBytes != null) {
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }

            model outfit = new model(id, event, location, tags, notes, imageUri, bitmap, dateTaken, aId, isFav == 1);
            
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
