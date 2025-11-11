package com.example.edunotesandroidapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;

import java.util.List;

public class SavedNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textNoSavedNotes;
    private NoteAdapter noteAdapter;
    private DBHandler dbHandler;
    private String userEmail;

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
        userEmail = getIntent().getStringExtra("email");

        // Load saved notes
        loadSavedNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh saved notes
        loadSavedNotes();
    }

    private void loadSavedNotes() {
        if (userEmail == null) {
            textNoSavedNotes.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        List<Note> savedNotes = dbHandler.getAllSavedNotes(userEmail);

        if (savedNotes == null || savedNotes.isEmpty()) {
            textNoSavedNotes.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textNoSavedNotes.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (noteAdapter == null) {
                noteAdapter = new NoteAdapter(this, savedNotes, userEmail);
                recyclerView.setAdapter(noteAdapter);
            } else {
                noteAdapter.updateData(savedNotes);
            }
        }
    }
}
