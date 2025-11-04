package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private DBHandler dbHandler;
    private String userEmail; // to track logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // ---------------- Toolbar setup ----------------
        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EduNotes");
        }

        // ---------------- Initialize DBHandler ----------------
        dbHandler = new DBHandler(this);

        // ---------------- Initialize views ----------------
        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // ---------------- Get user email ----------------
        userEmail = getIntent().getStringExtra("email");

        // ---------------- Load notes ----------------
        loadNotes();

        // ---------------- Bottom Navigation ----------------
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;

            } else if (id == R.id.nav_upload) {
                startActivity(new Intent(this, UploadNotesActivity.class));
                Intent intent = new Intent (this, UploadNotesActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
                return true;

            } else if (id == R.id.nav_saved) {
                Intent intent = new Intent(this, SavedNotesActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
                return true;

            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
                return true;
            }

            return false;
        });

        // ---------------- Edge-to-Edge Padding ----------------
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ---------------- Load Notes from Database ----------------
    private void loadNotes() {
        noteList = dbHandler.getAllNotes();

        if (noteList == null || noteList.isEmpty()) {
            findViewById(R.id.emptyText).setVisibility(android.view.View.VISIBLE);
        } else {
            findViewById(R.id.emptyText).setVisibility(android.view.View.GONE);
        }

        // Get ALL notes from database
        List<Note> noteList = dbHandler.getAllNotes();

        noteAdapter = new NoteAdapter(this, noteList);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // refresh notes every time you return
    }
}
