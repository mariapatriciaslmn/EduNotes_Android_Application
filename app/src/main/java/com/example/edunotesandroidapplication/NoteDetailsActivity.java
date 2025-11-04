package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoteDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_details);

        // --- Initialize UI elements ---
        TextView title = findViewById(R.id.noteTitle);
        TextView description = findViewById(R.id.noteDescription);
        TextView uploader = findViewById(R.id.uploaderName);
        ImageView image = findViewById(R.id.noteImage);
        Toolbar noteDetailsToolbar = findViewById(R.id.noteDetailsToolbar);

        // --- Setup Toolbar ---
        setSupportActionBar(noteDetailsToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Note Details");
        }

        // Add back arrow and listener
        noteDetailsToolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        noteDetailsToolbar.setNavigationOnClickListener(v -> {
            Intent backIntent = new Intent(NoteDetailsActivity.this, HomeActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(backIntent);
            finish();
        });

        // --- Receive data from Intent ---
        Intent intent = getIntent();
        if (intent != null) {
            String noteTitle = intent.getStringExtra("title");
            String noteDescription = intent.getStringExtra("description");
            String noteUploader = intent.getStringExtra("uploaderName");
            String imageUrl = intent.getStringExtra("imageUrl");

            // --- Display data ---
            title.setText(noteTitle != null ? noteTitle : "Untitled Note");
            description.setText(noteDescription != null ? noteDescription : "No description available.");
            uploader.setText(noteUploader != null ? noteUploader : "Unknown uploader");

            // (Optional) If you want to display the image later, use Glide or Picasso here
            // Glide.with(this).load(imageUrl).into(image);
        }

        // --- Edge-to-edge padding ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
