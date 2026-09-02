package com.example.styleswipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * HOME ACTIVITY
 * The first screen the user sees (Splash/Welcome).
 * It manages session persistence and primary navigation paths.
 */
public class HomeActivity extends AppCompatActivity {
    private Button loginBtn, signupBtn, skipBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI INITIALIZATION
        loginBtn = findViewById(R.id.loginBtn);
        signupBtn = findViewById(R.id.signupBtn);
        skipBtn = findViewById(R.id.skipBtn);

        // 2. NAVIGATION TO LOGIN
        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // 3. NAVIGATION TO SIGNUP
        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // 4. GUEST ACCESS (SKIP): 
        // Directly enters the app without creating a local account.
        skipBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OutfitListActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
