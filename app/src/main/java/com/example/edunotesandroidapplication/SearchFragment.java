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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchRecyclerView;
    private NoteAdapter noteAdapter;
    private OnlineDBHandler dbHandler;
    private List<Note> noteList = new ArrayList<>();
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchEditText = view.findViewById(R.id.searchInput);
        searchRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);

        // Initialize DB handler
        dbHandler = new OnlineDBHandler(getContext());

        // Get user email from arguments
        if (getArguments() != null) {
            userEmail = getArguments().getString("email", "");
        } else {
            userEmail = "";
        }

        // Setup RecyclerView
        noteAdapter = new NoteAdapter(getContext(), noteList, userEmail);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(noteAdapter);

        // Fetch all notes
        fetchAllNotes();

        // Live search
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

    private void fetchAllNotes() {
        dbHandler.fetchNotes("DESC", response -> {
            noteList.clear();
            if (response == null || response.isEmpty()) response = "[]";

            try {
                JSONArray arr = new JSONArray(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    // Parse files JSON array
                    ArrayList<String> filesList = new ArrayList<>();
                    String filesJson = obj.optString("files_json", "[]");
                    try {
                        JSONArray filesArray = new JSONArray(filesJson);
                        for (int j = 0; j < filesArray.length(); j++) {
                            filesList.add(filesArray.getString(j));
                        }
                    } catch (JSONException e) {
                        if (!filesJson.isEmpty() && !filesJson.equals("[]")) {
                            filesList.add(filesJson);
                        }
                    }

                    // Optional: use first file as preview
                    String firstFile = filesList.isEmpty() ? "" : filesList.get(0);

                    // Create Note object
                    Note note = new Note(
                            obj.getInt("note_id"),
                            obj.getString("title"),
                            obj.getString("description"),
                            firstFile,
                            obj.getString("uploader_email"),
                            obj.optString("date_created", "")
                    );
                    note.setFiles(filesList); // save all files in Note object
                    noteList.add(note);
                }

                noteAdapter.updateData(noteList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void filterNotes(String query) {
        String lowerQuery = query.toLowerCase().trim();
        List<Note> filteredList = new ArrayList<>();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(lowerQuery) ||
                    note.getDescription().toLowerCase().contains(lowerQuery)) {
                filteredList.add(note);
            }
        }
        noteAdapter.updateData(filteredList);
    }
}