package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
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
    private List<Note> userNotesList;
    private DBHandler dbHandler;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        Toolbar profileToolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(profileToolbar);

        profileToolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // ✅ Initialize
        dbHandler = new DBHandler(this);
        profilePicture = findViewById(R.id.profilePicture);
        usernameText = findViewById(R.id.usernameText);
        uploadStats = findViewById(R.id.uploadStats);
        emptyUserNotesText = findViewById(R.id.emptyUserNotesText);
        userNotesRecyclerView = findViewById(R.id.userNotesRecyclerView);

        // Get logged-in user's email
        userEmail = getIntent().getStringExtra("email");

        // Get user's name from DB
        String userName = dbHandler.getUserNameByEmail(userEmail);
        usernameText.setText(userName != null ? userName : "User");

        // Load notes by user
        userNotesList = dbHandler.getNotesByUser(userEmail);

        // Setup RecyclerView
        userNotesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (userNotesList != null && !userNotesList.isEmpty()) {
            noteAdapter = new NoteAdapter(this, userNotesList);
            userNotesRecyclerView.setAdapter(noteAdapter);
            uploadStats.setText("Uploads: " + userNotesList.size());
            emptyUserNotesText.setVisibility(TextView.GONE);
        } else {
            uploadStats.setText("Uploads: 0");
            emptyUserNotesText.setVisibility(TextView.VISIBLE);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
