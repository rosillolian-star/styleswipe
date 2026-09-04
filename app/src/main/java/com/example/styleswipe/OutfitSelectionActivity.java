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
 * OUTFIT SELECTION ACTIVITY
 * Used by the Calendar/Planner to pick an outfit for a specific date.
 */
public class OutfitSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<model> outfitList = new ArrayList<>();
    private String targetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_selection);

        targetDate = getIntent().getStringExtra("TARGET_DATE");
        if (targetDate == null) {
            finish();
            return;
        }

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.selectionRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        loadAllOutfits();
        recyclerView.setAdapter(new SelectionAdapter(outfitList));
    }

    private void loadAllOutfits() {
        outfitList.clear();
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("outfits", null, null, null, null, null, "date_taken DESC");

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

            outfitList.add(new model(id, event, location, tags, notes, imageUri, bitmap, dateTaken, albumId, isFav == 1));
        }
        cursor.close();
        db.close();
    }

    private void planOutfit(model outfit) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("outfit_id", outfit.getId());
        cv.put("date", targetDate);

        // Use CONFLICT_REPLACE to overwrite existing plan for the same date
        db.insertWithOnConflict("planned_outfits", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        setResult(RESULT_OK);
        finish();
    }

    private class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ViewHolder> {
        private List<model> items;
        public SelectionAdapter(List<model> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_picture, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            model o = items.get(position);
            if (o.getImageUri() != null && !o.getImageUri().isEmpty()) {
                holder.img.setImageURI(Uri.parse(o.getImageUri()));
            } else if (o.getImageBitmap() != null) {
                holder.img.setImageBitmap(o.getImageBitmap());
            }
            holder.itemView.setOnClickListener(v -> planOutfit(o));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            ViewHolder(View v) { super(v); img = v.findViewById(R.id.pictureThumb); }
        }
    }
}
