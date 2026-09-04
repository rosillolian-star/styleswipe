package com.example.styleswipe;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * OUTFIT DETAIL ACTIVITY
 */
public class OutfitDetailActivity extends AppCompatActivity {

    private int outfitId;
    private boolean isFavorite = false;
    private FloatingActionButton favBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_detail);

        outfitId = getIntent().getIntExtra("OUTFIT_ID", -1);
        if (outfitId == -1) {
            finish();
            return;
        }

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        favBtn = findViewById(R.id.favBtn);
        
        favBtn.setOnClickListener(v -> toggleFavorite());

        loadOutfitDetails(outfitId);
    }

    private void loadOutfitDetails(int id) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("outfits", null, "id = ?", 
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String event = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow("tags"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imageBitmap"));
            String dateTaken = cursor.getString(cursor.getColumnIndexOrThrow("date_taken"));
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1;

            ((TextView) findViewById(R.id.detailEvent)).setText(event);
            ((TextView) findViewById(R.id.detailLocation)).setText(location);
            ((TextView) findViewById(R.id.detailTags)).setText(tags);
            ((TextView) findViewById(R.id.detailNotes)).setText(notes);
            ((TextView) findViewById(R.id.detailDate)).setText("Saved on: " + dateTaken);

            updateFavIcon();

            ImageView img = findViewById(R.id.detailImage);
            if (imageUri != null && !imageUri.isEmpty()) {
                img.setImageURI(Uri.parse(imageUri));
            } else if (imageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                img.setImageBitmap(bitmap);
            }
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_favorite", isFavorite ? 1 : 0);
        
        db.update("outfits", cv, "id = ?", new String[]{String.valueOf(outfitId)});
        db.close();
        
        updateFavIcon();
        Toast.makeText(this, isFavorite ? "Added to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    private void updateFavIcon() {
        if (isFavorite) {
            favBtn.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favBtn.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
}
