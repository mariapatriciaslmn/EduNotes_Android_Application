package com.example.edunotesandroidapplication.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Comment {

    private int id;
    private int noteId;
    private String userName;
    private String content;
    private String dateCreated;

    public Comment(int id, int noteId, String userName, String content, String dateCreated) {
        this.id = id;
        this.noteId = noteId;
        this.userName = userName;
        this.content = content;
        this.dateCreated = dateCreated;
    }

    public int getId() { return id; }
    public int getNoteId() { return noteId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public String getDateCreated() { return dateCreated; }

    // JSON parsing
    public static Comment fromJson(JSONObject obj) throws JSONException {
        return new Comment(
                obj.getInt("id"),
                obj.getInt("note_id"),
                obj.optString("user_name", ""),
                obj.optString("content", ""),
                obj.optString("date_created", "")
        );
    }
}