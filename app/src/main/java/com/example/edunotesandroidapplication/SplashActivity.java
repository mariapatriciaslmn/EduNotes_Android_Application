package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    // Declaration of views
    ProgressBar starting;
    TextView edutext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        starting = findViewById(R.id.starting);
        edutext = findViewById(R.id.eduText);

        new Handler().postDelayed(() -> {
            // Example: move to LoginActivity after 3 seconds
            Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        }, 3000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}