package com.example.edunotesandroidapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.edunotesandroidapplication.models.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NoteEventBus {

    private static final NoteEventBus instance = new NoteEventBus();

    // LiveData for new note IDs
    private final MutableLiveData<Integer> newNoteLiveData = new MutableLiveData<>();

    // Listeners for uploaded files
    private final Set<FilesListener> filesListeners = new HashSet<>();

    private NoteEventBus() {}

    public static NoteEventBus getInstance() {
        return instance;
    }

    // ------------------ New Note LiveData ------------------
    public LiveData<Integer> getNewNoteLiveData() {
        return newNoteLiveData;
    }

    public void postNewNote(int noteId) {
        newNoteLiveData.postValue(noteId);
    }

    // ------------------ Uploaded Files Listeners ------------------
    public interface FilesListener {
        void onUploadedFiles(ArrayList<Files> files);
    }

    public void registerFilesListener(FilesListener listener) {
        filesListeners.add(listener);
    }

    public void unregisterFilesListener(FilesListener listener) {
        filesListeners.remove(listener);
    }

    public void postUploadedFiles(ArrayList<Files> files) {
        for (FilesListener listener : filesListeners) {
            listener.onUploadedFiles(files);
        }
    }
}