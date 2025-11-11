package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;

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
        setContentView(R.layout.activity_profile);

        // Toolbar elements
        ImageView backButton = findViewById(R.id.backButton);
        ImageView logoutButton = findViewById(R.id.logoutButton);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        // Back button click
        backButton.setOnClickListener(v -> finish());

        // Logout button click
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Initialize views
        profilePicture = findViewById(R.id.profilePicture);
        usernameText = findViewById(R.id.usernameText);
        uploadStats = findViewById(R.id.uploadStats);
        emptyUserNotesText = findViewById(R.id.emptyUserNotesText);
        userNotesRecyclerView = findViewById(R.id.userNotesRecyclerView);

        // Initialize DBHandler
        dbHandler = new DBHandler(this);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("email");

        // Get user name from DB
        String userName = dbHandler.getUserNameByEmail(userEmail);
        usernameText.setText(userName != null ? userName : "User");

        // Load user's uploaded notes
        refreshUserNotes();

        // Apply edge-to-edge window insets
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

    private void refreshUserNotes() {
        // Fetch notes for this user with DESC order (latest first)
        List<Note> userNotesList = dbHandler.getNotesByUser(userEmail, "DESC");

        if (userNotesList != null && !userNotesList.isEmpty()) {
            if (noteAdapter == null) {
                noteAdapter = new NoteAdapter(this, userNotesList, userEmail);
                userNotesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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
