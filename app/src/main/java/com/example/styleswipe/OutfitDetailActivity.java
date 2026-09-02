package com.example.styleswipe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * OUTFIT DETAIL ACTIVITY
 * This screen opens when a user clicks a photo in the gallery.
 * It displays the full-sized image and all style metadata associated with it.
 */
public class OutfitDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        // 1. DATA RECEIVAL: Get the unique ID of the outfit passed via the Intent
        int outfitId = getIntent().getIntExtra("OUTFIT_ID", -1);
        if (outfitId == -1) {
            finish(); // Safety check: if no ID, close the screen
            return;
        }

        // Close detail view and return to gallery
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        // 2. FETCH DATA FROM DB
        loadOutfitDetails(outfitId);
    }

    /**
     * DATABASE LOADING
     * Queries the single specific outfit by its ID and populates the UI.
     */
    private void loadOutfitDetails(int id) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Search for the specific row matching this ID
        Cursor cursor = db.query("outfits", null, "id = ?", 
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // A. Extract all metadata
            String event = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imageBitmap"));
            String dateTaken = cursor.getString(cursor.getColumnIndexOrThrow("date_taken"));

            // B. Populate Text Views
            ((TextView) findViewById(R.id.detailEvent)).setText(event);
            ((TextView) findViewById(R.id.detailLocation)).setText(location);
            ((TextView) findViewById(R.id.detailTags)).setText(tags);
            ((TextView) findViewById(R.id.detailNotes)).setText(notes);
            ((TextView) findViewById(R.id.detailDate)).setText("Saved on: " + dateTaken);

            // C. Load full image
            ImageView img = findViewById(R.id.detailImage);
            if (imageUri != null && !imageUri.isEmpty()) {
                // If it's a gallery path
                img.setImageURI(Uri.parse(imageUri));
            } else if (imageBytes != null) {
                // If it's a binary photo from camera, decode first
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                img.setImageBitmap(bitmap);
            }
        }

        if (cursor != null) cursor.close();
        db.close();
    }
}
