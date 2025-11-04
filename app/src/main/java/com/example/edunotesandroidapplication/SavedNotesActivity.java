package com.example.edunotesandroidapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textNoSavedNotes;
    private NoteAdapter noteAdapter;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notes);

        // ---------------- Toolbar setup ----------------
        Toolbar toolbar = findViewById(R.id.savedNotesToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Notes");
        }
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ---------------- Initialize views ----------------
        recyclerView = findViewById(R.id.recyclerViewSavedNotes);
        textNoSavedNotes = findViewById(R.id.textNoSavedNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ---------------- Initialize DBHandler ----------------
        dbHandler = new DBHandler(this);

        // Get user email from intent
        String userEmail = getIntent().getStringExtra("email");

        // ---------------- Fetch saved notes ----------------
        List<Note> savedNotes = dbHandler.getAllSavedNotes(userEmail);

        // ---------------- Display data ----------------
        if (savedNotes == null || savedNotes.isEmpty()) {
            textNoSavedNotes.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textNoSavedNotes.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Use the adapter in "saved notes mode"
            noteAdapter = new NoteAdapter(this, savedNotes, true);
            recyclerView.setAdapter(noteAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh saved notes when coming back
        String userEmail = getIntent().getStringExtra("email");
        List<Note> savedNotes = dbHandler.getAllSavedNotes(userEmail);

        if (savedNotes == null || savedNotes.isEmpty()) {
            textNoSavedNotes.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textNoSavedNotes.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            noteAdapter.updateData(savedNotes);
        }
    }
}
