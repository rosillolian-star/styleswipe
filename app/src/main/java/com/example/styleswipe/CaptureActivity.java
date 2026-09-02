package com.example.styleswipe;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;

/**
 * CAPTURE ACTIVITY
 * The main interface for adding new outfits. 
 * Allows users to take photos, upload from gallery, and save style metadata.
 */
public class CaptureActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView outfitImage;
    private Button captureBtn, uploadBtn, saveBtn;
    private EditText eventField, locationField, tagsField, notesField;

    private Uri imageUri;   // stores the path for gallery images
    private Bitmap imageBitmap; // stores the pixel data for camera photos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        // UI INITIALIZATION
        outfitImage = findViewById(R.id.outfitImage);
        captureBtn = findViewById(R.id.captureBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        saveBtn = findViewById(R.id.saveBtn);
        eventField = findViewById(R.id.eventField);
        locationField = findViewById(R.id.locationField);
        tagsField = findViewById(R.id.tagsField);
        notesField = findViewById(R.id.notesField);

        // 1. CAMERA CAPTURE TRIGGER
        captureBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });

        // 2. GALLERY UPLOAD TRIGGER
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        // 3. SAVE OUTFIT LOGIC
        saveBtn.setOnClickListener(v -> {
            String event = eventField.getText().toString();
            String location = locationField.getText().toString();
            String tags = tagsField.getText().toString();
            String notes = notesField.getText().toString();

            if (imageUri != null || imageBitmap != null) {
                OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("event", event);
                values.put("location", location);
                values.put("tags", tags);
                values.put("notes", notes);
                
                int targetAlbumId = getIntent().getIntExtra("ALBUM_ID", 1);
                values.put("album_id", targetAlbumId);

                if (imageUri != null) {
                    values.put("imageUri", imageUri.toString());
                } else if (imageBitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageBytes = stream.toByteArray();
                    values.put("imageBitmap", imageBytes);
                }

                db.insert("outfits", null, values);
                db.close();

                Toast.makeText(this, "Outfit saved locally!", Toast.LENGTH_SHORT).show();
                
                // Return to previous screen after save
                finish();
            } else {
                Toast.makeText(this, "Please capture or upload an image first.", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. BOTTOM NAVIGATION SETUP (Matching OutfitListActivity)
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, OutfitListActivity.class));
            finish();
        });

        findViewById(R.id.nav_calendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, OutfitListActivity.class);
            intent.putExtra("OPEN_ALBUMS", true);
            startActivity(intent);
            finish();
        });

        // Other icons are placeholders for now
        findViewById(R.id.nav_fav).setOnClickListener(v -> {});
        findViewById(R.id.nav_history).setOnClickListener(v -> {});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    outfitImage.setImageBitmap(imageBitmap);
                    imageUri = null;
                }
            } 
            else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri, 
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    outfitImage.setImageURI(imageUri);
                    imageBitmap = null;
                }
            }
        }
    }
}
