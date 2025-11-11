package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private DBHandler dbHandler;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // ---------------- Toolbar ----------------
        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("EduNotes");

        // ---------------- RecyclerView ----------------
        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        // ---------------- DBHandler ----------------
        dbHandler = new OnlineDBHandler(this);

        // ---------------- User Email ----------------
        userEmail = getIntent().getStringExtra("email");

        // ---------------- Load Notes (default newest first) ----------------
        loadNotes("DESC");

        // ---------------- Bottom Navigation ----------------
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id == R.id.nav_upload) {
                intent = new Intent(this, UploadNotesActivity.class);
            } else if (id == R.id.nav_saved) {
                intent = new Intent(this, SavedNotesActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            } else return true;

            intent.putExtra("email", userEmail);
            startActivity(intent);
            return true;
        });

        // ---------------- Edge-to-edge padding ----------------
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ---------------- Load Notes ----------------
    private void loadNotes(String sortOrder) {
        List<Note> noteList = dbHandler.getAllNotes(sortOrder);

        View emptyText = findViewById(R.id.emptyText);
        emptyText.setVisibility(noteList.isEmpty() ? View.VISIBLE : View.GONE);

        noteAdapter = new NoteAdapter(this, noteList, userEmail);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView.setVisibility(View.VISIBLE);
        loadNotes("DESC"); // default refresh
    }

    // ---------------- Toolbar Menu ----------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new SearchFragment())
                    .addToBackStack(null)
                    .commit();
            recyclerView.setVisibility(View.GONE);
            return true;

        } else if (id == R.id.action_sort) {
            // ---------------- PopupMenu for sorting ----------------
            View menuItemView = findViewById(R.id.action_sort);
            PopupMenu popup = new PopupMenu(this, menuItemView);
            popup.getMenu().add("Newest → Oldest");
            popup.getMenu().add("Oldest → Newest");

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getTitle().equals("Newest → Oldest")) {
                    loadNotes("DESC");
                } else {
                    loadNotes("ASC");
                }
                return true;
            });

            popup.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
