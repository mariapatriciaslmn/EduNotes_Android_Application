package com.example.edunotesandroidapplication.models;

public class Comment {
    private int id;
    private int noteId;
    private String userName;
    private String content;
    private String dateCreated; // timestamp

    public Comment(int id, int noteId, String userName, String content, String dateCreated) {
        this.id = id;
        this.noteId = noteId;
        this.userName = userName;
        this.content = content;
        this.dateCreated = dateCreated;
    }

    // Getters
    public int getId() { return id; }
    public int getNoteId() { return noteId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public String getDateCreated() { return dateCreated; }
}
