package com.example.edunotesandroidapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchRecyclerView;
    private NoteAdapter noteAdapter;
    private DBHandler dbHandler;
    private List<Note> noteList;
    private String userEmail; // currently logged-in user

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchEditText = view.findViewById(R.id.searchInput);
        searchRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);

        dbHandler = new DBHandler(getContext());

        // Get logged-in user email from arguments or shared preferences
        if (getArguments() != null) {
            userEmail = getArguments().getString("email", "");
        } else {
            userEmail = ""; // fallback
        }

        // Fetch all notes (sorted DESC by default)
        noteList = dbHandler.getAllNotes("DESC");

        // Setup RecyclerView
        noteAdapter = new NoteAdapter(getContext(), noteList, userEmail);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(noteAdapter);

        // Search logic
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        return view;
    }

    private void filterNotes(String query) {
        List<Note> filteredList = new ArrayList<>();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    note.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }

        noteAdapter.updateData(filteredList);
    }
}
