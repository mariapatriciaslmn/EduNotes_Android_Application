package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private DBHandler dbHandler; // connect to local DB


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // --- Toolbar setup ---
        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("EduNotes");
        }

        // --- Initialize database handler ---
        dbHandler = new DBHandler(this);

        // --- RecyclerView setup ---
        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // --- Get data from database ---
        noteList = dbHandler.getAllNotes(); // use your DBHandler method

        noteAdapter = new NoteAdapter(this, noteList);
        recyclerView.setAdapter(noteAdapter);

        // --- Show empty text if no notes ---
        if (noteList == null || noteList.isEmpty()) {
            findViewById(R.id.emptyText).setVisibility(android.view.View.VISIBLE);
        } else {
            findViewById(R.id.emptyText).setVisibility(android.view.View.GONE);
        }

        // --- Bottom navigation ---
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_upload) {
                startActivity(new Intent(this, UploadNotesActivity.class));
                return true;
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedNotesActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                intent.putExtra("email", getIntent().getStringExtra("email"));
                return true;
            }
            return false;
        });

        // --- Edge to edge layout ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data every time you return to Home
        noteList = dbHandler.getAllNotes();
        noteAdapter.updateData(noteList);
    }
}
