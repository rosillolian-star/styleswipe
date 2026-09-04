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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CAPTURE ACTIVITY
 * Includes Offline AI Clothes Detection to ensure only outfits are saved.
 */
public class CaptureActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView outfitImage;
    private Button captureBtn, uploadBtn, saveBtn;
    private EditText eventField, locationField, tagsField, notesField;

    private Uri imageUri;
    private Bitmap imageBitmap;
    private boolean clothesDetected = false;

    // AI Keywords to look for
    private final List<String> CLOTHING_KEYWORDS = Arrays.asList(
            "Clothing", "Apparel", "Fashion", "Outerwear", "Shirt", "Dress", 
            "Trousers", "Suit", "Activewear", "Sportswear", "Streetwear", 
            "Uniform", "Top", "Footwear", "Jeans", "Coat", "Jacket"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        outfitImage = findViewById(R.id.outfitImage);
        captureBtn = findViewById(R.id.captureBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        saveBtn = findViewById(R.id.saveBtn);
        eventField = findViewById(R.id.eventField);
        locationField = findViewById(R.id.locationField);
        tagsField = findViewById(R.id.tagsField);
        notesField = findViewById(R.id.notesField);

        // Initially disable save until AI verifies the image
        saveBtn.setEnabled(false);
        saveBtn.setAlpha(0.5f);

        captureBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });

        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        saveBtn.setOnClickListener(v -> {
            if (!clothesDetected) {
                Toast.makeText(this, "No clothing detected yet. Please select an outfit photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveOutfitToDatabase();
        });

        setupBottomNavigation();
    }

    private void runClothesDetection(InputImage image) {
        // Show "Analyzing" feedback
        Toast.makeText(this, "Analyzing image for clothing...", Toast.LENGTH_SHORT).show();
        
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    boolean found = false;
                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        
                        // If AI sees a clothing keyword with > 50% confidence
                        if (isClothingKeyword(text) && confidence > 0.5f) {
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        clothesDetected = true;
                        saveBtn.setEnabled(true);
                        saveBtn.setAlpha(1.0f);
                        Toast.makeText(this, "Outfit detected! You can now save your style.", Toast.LENGTH_SHORT).show();
                    } else {
                        clothesDetected = false;
                        saveBtn.setEnabled(false);
                        saveBtn.setAlpha(0.5f);
                        Toast.makeText(this, "No clothing detected. Please capture or upload a photo of an outfit.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Analysis failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isClothingKeyword(String text) {
        for (String keyword : CLOTHING_KEYWORDS) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) return true;
        }
        return false;
    }

    private void saveOutfitToDatabase() {
        String event = eventField.getText().toString();
        String location = locationField.getText().toString();
        String tags = tagsField.getText().toString();
        String notes = notesField.getText().toString();

        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("event", event);
        values.put("location", location);
        values.put("tags", tags);
        values.put("notes", notes);
        values.put("album_id", 1); 

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

        Toast.makeText(this, "Style saved to your history!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, OutfitListActivity.class));
            finish();
        });
        findViewById(R.id.nav_calendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, OutfitListActivity.class);
            intent.putExtra("OPEN_PLANNER", true);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.nav_history).setOnClickListener(v -> {
            Intent intent = new Intent(this, OutfitListActivity.class);
            intent.putExtra("OPEN_ALBUMS", true);
            startActivity(intent);
            finish();
        });
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
                    runClothesDetection(InputImage.fromBitmap(imageBitmap, 0));
                }
            } 
            else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri, 
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        outfitImage.setImageURI(imageUri);
                        imageBitmap = null;
                        runClothesDetection(InputImage.fromFilePath(this, imageUri));
                    } catch (SecurityException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
