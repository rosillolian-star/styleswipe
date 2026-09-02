package com.example.styleswipe;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SIGNUP ACTIVITY
 * Handles the creation of new local user accounts.
 */
public class SignupActivity extends AppCompatActivity {
    private EditText emailField, passwordField, confirmPasswordField;
    private Button signupBtn;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI INITIALIZATION
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        signupBtn = findViewById(R.id.signupBtn);
        backBtn = findViewById(R.id.backBtn);

        // 1. REGISTRATION TRIGGER
        signupBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirm = confirmPasswordField.getText().toString().trim();

            // 2. INPUT VALIDATION
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. DATABASE REGISTRATION
            if (registerUser(email, password)) {
                // Android Toast for signup success
                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                
                // Go to Login to let them sign in
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                // Android Toast for signup failure (likely email already exists)
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            }
        });

        // Simple back navigation
        backBtn.setOnClickListener(v -> finish());
    }

    /**
     * DATABASE INSERTION
     * Saves the new user credentials into the 'users' table.
     * Returns true if insertion succeeded, false otherwise.
     */
    private boolean registerUser(String email, String password) {
        OutfitDatabaseHelper dbHelper = new OutfitDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);

        // Try to insert the new row
        long result = db.insert("users", null, values);
        db.close();
        
        // If result is -1, it means the UNIQUE constraint on email failed
        return result != -1;
    }
}
