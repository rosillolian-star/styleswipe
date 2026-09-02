package com.example.styleswipe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LOGIN ACTIVITY
 * Handles the authentication process using the local SQLite database.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText emailField, passwordField;
    private Button loginBtn;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI INITIALIZATION
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginBtn = findViewById(R.id.loginBtn);
        backBtn = findViewById(R.id.backBtn);

        // 1. LOGIN TRIGGER
        loginBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // 2. CREDENTIAL VERIFICATION
            int userId = checkUser(email, password);
            if (userId != -1) {
                // SUCCESS: Save user session so they don't have to re-login next time
                SharedPreferences prefs = getSharedPreferences("StyleSwipePrefs", Context.MODE_PRIVATE);
                prefs.edit().putInt("USER_ID", userId).apply();

                // Android Toast for login success
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                
                // Clear activity history and go to the main screen
                Intent intent = new Intent(this, OutfitListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // FAILURE: Show error message
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Simple back navigation
        backBtn.setOnClickListener(v -> finish());
    }

    /**
     * DATABASE LOOKUP
     * Checks if the email/password combination exists in the local 'users' table.
     * Returns the user's ID if found, otherwise -1.
     */
    private int checkUser(String email, String password) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Search for a matching row
        Cursor cursor = db.query("users", new String[]{"id"}, 
                "email = ? AND password = ?", new String[]{email, password}, 
                null, null, null);

        int userIdFound = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userIdFound = cursor.getInt(0); // Get the ID from the first column
            cursor.close();
        }
        db.close();
        return userIdFound;
    }
}
