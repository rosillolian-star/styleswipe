package com.example.styleswipe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * PLANNER FRAGMENT
 * Features a literal calendar to plan outfits for specific days.
 */
public class PlannerFragment extends Fragment {

    private CalendarView calendarView;
    private ImageView outfitImg;
    private TextView eventText, dateLabel;
    private View emptyStateView;
    private LinearLayout detailsLayout;
    private FloatingActionButton pickBtn, clearBtn;
    private String selectedDate; // YYYY-MM-DD

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        outfitImg = view.findViewById(R.id.plannedOutfitImg);
        eventText = view.findViewById(R.id.plannedEvent);
        dateLabel = view.findViewById(R.id.selectedDateLabel);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        detailsLayout = view.findViewById(R.id.plannedDetails);
        pickBtn = view.findViewById(R.id.pickOutfitBtn);
        clearBtn = view.findViewById(R.id.clearPlanBtn);

        // Set default selected date to today
        Calendar cal = Calendar.getInstance();
        updateSelectedDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {
            updateSelectedDate(year, month, dayOfMonth);
            loadPlannedOutfit();
        });

        pickBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OutfitSelectionActivity.class);
            intent.putExtra("TARGET_DATE", selectedDate);
            startActivityForResult(intent, 100);
        });

        clearBtn.setOnClickListener(v -> clearPlannedOutfit());

        loadPlannedOutfit();
        return view;
    }

    private void updateSelectedDate(int year, int month, int day) {
        selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        
        // Show readable date label
        try {
            SimpleDateFormat dbFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = dbFmt.parse(selectedDate);
            SimpleDateFormat displayFmt = new SimpleDateFormat("MMMM d", Locale.getDefault());
            
            // Special cases for Today/Tomorrow
            Calendar now = Calendar.getInstance();
            String todayStr = dbFmt.format(now.getTime());
            now.add(Calendar.DAY_OF_YEAR, 1);
            String tomorrowStr = dbFmt.format(now.getTime());
            
            if (selectedDate.equals(todayStr)) {
                dateLabel.setText("Today's Plan");
            } else if (selectedDate.equals(tomorrowStr)) {
                dateLabel.setText("Tomorrow's Plan");
            } else {
                dateLabel.setText(displayFmt.format(date) + " Plan");
            }
        } catch (Exception e) {
            dateLabel.setText("Selected Plan");
        }
    }

    private void loadPlannedOutfit() {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT o.* FROM outfits o " +
                "JOIN planned_outfits p ON o.id = p.outfit_id " +
                "WHERE p.date = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{selectedDate});

        if (cursor.moveToFirst()) {
            String event = cursor.getString(cursor.getColumnIndexOrThrow("event"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("imageBitmap"));

            eventText.setText(event != null && !event.isEmpty() ? event : "Outfit Planned");
            detailsLayout.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            clearBtn.setVisibility(View.VISIBLE);
            outfitImg.setAlpha(1.0f);

            if (imageUri != null && !imageUri.isEmpty()) {
                outfitImg.setImageURI(Uri.parse(imageUri));
            } else if (imageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                outfitImg.setImageBitmap(bitmap);
            }
            
            // Animation for polish
            View card = getView().findViewById(R.id.plannedOutfitCard);
            card.setAlpha(0f);
            card.animate().alpha(1f).setDuration(300).start();
        } else {
            outfitImg.setImageDrawable(null);
            outfitImg.setAlpha(0.3f);
            detailsLayout.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
            clearBtn.setVisibility(View.GONE);
        }
        cursor.close();
        db.close();
    }

    private void clearPlannedOutfit() {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("planned_outfits", "date = ?", new String[]{selectedDate});
        db.close();
        
        Toast.makeText(getContext(), "Plan cleared", Toast.LENGTH_SHORT).show();
        loadPlannedOutfit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == android.app.Activity.RESULT_OK) {
            loadPlannedOutfit();
        }
    }
}
