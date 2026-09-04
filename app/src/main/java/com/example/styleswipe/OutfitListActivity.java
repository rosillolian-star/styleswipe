package com.example.styleswipe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * OUTFIT LIST ACTIVITY
 * Immersive style hub with tab-specific actions.
 */
public class OutfitListActivity extends AppCompatActivity {

    private View navAdd, addAlbumBtn, searchBarCard;
    private EditText gallerySearchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_list);

        // UI INITIALIZATION
        navAdd = findViewById(R.id.nav_add);
        addAlbumBtn = findViewById(R.id.addAlbumBtn);
        searchBarCard = findViewById(R.id.searchBarCard);
        gallerySearchInput = findViewById(R.id.gallerySearchInput);
        
        // 1. NAVIGATION ACTIONS
        
        // GALLERY -> Full style timeline
        findViewById(R.id.nav_home).setOnClickListener(v -> 
            switchFragment(new PicturesFragment(), true, true, true));
        
        // FAVORITES -> Only favorited items
        findViewById(R.id.nav_fav).setOnClickListener(v -> 
            switchFragment(PicturesFragment.newInstance(true), true, false, true));
        
        // ADD OUTFIT -> Open Capture
        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, CaptureActivity.class));
        });

        // OUTFIT PLANNER -> Calendar view
        findViewById(R.id.nav_calendar).setOnClickListener(v -> 
            switchFragment(new PlannerFragment(), true, false, false));
        
        // HISTORY (Albums)
        findViewById(R.id.nav_history).setOnClickListener(v -> 
            switchFragment(new AlbumsFragment(), true, false, false));

        addAlbumBtn.setOnClickListener(v -> showAddAlbumDialog());

        // 2. SEARCH LOGIC
        gallerySearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGallery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 3. DEFAULT VIEW
        if (savedInstanceState == null) {
            switchFragment(new PicturesFragment(), true, true, true);
        }
    }

    /**
     * FRAGMENT NAVIGATION
     */
    private void switchFragment(Fragment fragment, boolean showAddOutfit, boolean showAddAlbum, boolean showSearch) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        
        navAdd.setVisibility(showAddOutfit ? View.VISIBLE : View.GONE);
        addAlbumBtn.setVisibility(showAddAlbum ? View.VISIBLE : View.GONE);
        searchBarCard.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        
        if (gallerySearchInput != null) {
            gallerySearchInput.setText("");
        }
    }

    private void filterGallery(String query) {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof PicturesFragment) {
            ((PicturesFragment) current).filter(query);
        }
    }

    private void showAddAlbumDialog() {
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Album Name");
        input.setTextColor(getResources().getColor(R.color.style_dark_brown));
        
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(64, 16, 64, 16);
        input.setLayoutParams(params);
        container.addView(input);

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle("New Album")
                .setMessage("Enter collection name:")
                .setView(container)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (!name.isEmpty()) createAlbum(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createAlbum(String name) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("name", name);
        db.insert("albums", null, cv);
        db.close();
        
        // REFRESH CURRENT FRAGMENT
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current instanceof AlbumsFragment) {
            switchFragment(new AlbumsFragment(), true, false, false);
        } else if (current instanceof PicturesFragment) {
            ((PicturesFragment) current).refreshData();
        }

        Toast.makeText(this, "Album created: " + name, Toast.LENGTH_SHORT).show();
    }
}
