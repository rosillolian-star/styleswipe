package com.example.styleswipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * OUTFIT LIST ACTIVITY
 * Revamped activity matching the modern design.
 * Features a custom header, horizontal categories, and an arched bottom nav.
 */
public class OutfitListActivity extends AppCompatActivity {

    private TextView emptyPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_list);

        // Update greeting based on login status
        TextView helloText = findViewById(R.id.helloText);
        android.content.SharedPreferences prefs = getSharedPreferences("StyleSwipePrefs", android.content.Context.MODE_PRIVATE);
        if (prefs.getInt("USER_ID", -1) == -1) {
            helloText.setText("Hello, Guest");
        }

        // 1. UI INITIALIZATION
        emptyPlaceholder = findViewById(R.id.emptyPlaceholder);
        
        // 2. CUSTOM BOTTOM NAV CLICKS
        findViewById(R.id.nav_home).setOnClickListener(v -> switchFragment(new PicturesFragment()));
        findViewById(R.id.nav_fav).setOnClickListener(v -> {
            // Placeholder for favorites
        });
        
        // MAIN ADD BUTTON (+)
        FloatingActionButton addBtn = findViewById(R.id.nav_add);
        addBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CaptureActivity.class));
        });

        findViewById(R.id.nav_calendar).setOnClickListener(v -> switchFragment(new AlbumsFragment()));
        findViewById(R.id.nav_history).setOnClickListener(v -> {
            // Placeholder for history
        });

        // 3. TOP SECTION INTERACTION
        findViewById(R.id.searchSection).setOnClickListener(v -> showSearchDialog());
        findViewById(R.id.filterBtn).setOnClickListener(v -> {
            // Placeholder for filters
        });

        // 4. DEFAULT VIEW
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("OPEN_ALBUMS", false)) {
                switchFragment(new AlbumsFragment());
            } else {
                switchFragment(new PicturesFragment());
            }
        }
        
        // Check if there's any data to show
        checkEmptyState();
    }

    /**
     * FRAGMENT SWITCHER
     */
    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        
        // Hide placeholder if a fragment is manually switched
        emptyPlaceholder.setVisibility(View.GONE);
    }

    /**
     * EMPTY STATE LOGIC
     * Displays a message if no outfits are found in the database.
     */
    private void checkEmptyState() {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        android.database.sqlite.SQLiteDatabase db = dbHelper.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM outfits", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();

        if (count == 0) {
            emptyPlaceholder.setVisibility(View.VISIBLE);
        } else {
            emptyPlaceholder.setVisibility(View.GONE);
        }
    }

    /**
     * SEARCH INTERFACE
     * Opens a themed pop-up dialog for filtering outfits.
     */
    private void showSearchDialog() {
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Search by event or location");
        
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(64, 16, 64, 16);
        input.setLayoutParams(params);
        container.addView(input);
        
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle("Search Outfits")
                .setView(container)
                .setPositiveButton("Search", (dialog, which) -> {
                    String query = input.getText().toString();
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (current instanceof PicturesFragment) {
                        ((PicturesFragment) current).filter(query);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
