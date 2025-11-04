package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView usernameText, uploadStats, emptyUserNotesText;
    private RecyclerView userNotesRecyclerView;

    private NoteAdapter noteAdapter;
    private DBHandler dbHandler;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Toolbar setup
        Toolbar profileToolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(profileToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }

        profileToolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        profileToolbar.setNavigationOnClickListener(v -> {
            // Go back to HomeActivity
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("email", userEmail);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Initialize views
        profilePicture = findViewById(R.id.profilePicture);
        usernameText = findViewById(R.id.usernameText);
        uploadStats = findViewById(R.id.uploadStats);
        emptyUserNotesText = findViewById(R.id.emptyUserNotesText);
        userNotesRecyclerView = findViewById(R.id.userNotesRecyclerView);

        // Initialize DBHandler
        dbHandler = new DBHandler(this);

        // Get user email
        userEmail = getIntent().getStringExtra("email");

        // Get user name
        String userName = dbHandler.getUserNameByEmail(userEmail);
        usernameText.setText(userName != null ? userName : "User");

        // Load user's uploaded notes
        List<Note> userNotesList = dbHandler.getNotesByUser(userEmail);

        // Setup RecyclerView
        userNotesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (userNotesList != null && !userNotesList.isEmpty()) {
            noteAdapter = new NoteAdapter(this, userNotesList);
            userNotesRecyclerView.setAdapter(noteAdapter);
            uploadStats.setText("Uploads: " + userNotesList.size());
            emptyUserNotesText.setVisibility(View.GONE);
        } else {
            uploadStats.setText("Uploads: 0");
            emptyUserNotesText.setVisibility(View.VISIBLE);
        }

        // Adjust window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserNotes();
    }

    // Refresh user notes when returning
    private void refreshUserNotes() {
        List<Note> userNotesList = dbHandler.getNotesByUser(userEmail);

        if (userNotesList != null && !userNotesList.isEmpty()) {
            if (noteAdapter == null) {
                noteAdapter = new NoteAdapter(this, userNotesList);
                userNotesRecyclerView.setAdapter(noteAdapter);
            } else {
                noteAdapter.updateData(userNotesList);
            }

            uploadStats.setText("Uploads: " + userNotesList.size());
            emptyUserNotesText.setVisibility(View.GONE);
        } else {
            uploadStats.setText("Uploads: 0");
            emptyUserNotesText.setVisibility(View.VISIBLE);
        }
    }
}
