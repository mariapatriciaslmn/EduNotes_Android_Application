package com.example.edunotesandroidapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    // Database constants
    private static final String DB_NAME = "edunotes_user_db";
    private static final int DB_VERSION = 1;

    // User table
    private static final String USER_TABLE = "user_table";
    private static final String USER_ID_COL = "id";
    private static final String NAME_COL = "name";
    private static final String EMAIL_COL = "email";
    private static final String PASSWORD_COL = "password";

    // Notes table
    private static final String NOTES_TABLE = "notes";
    private static final String NOTE_ID_COL = "note_id";
    private static final String TITLE_COL = "title";
    private static final String DESC_COL = "description";
    private static final String IMAGE_URL_COL = "image_url"; // optional, can be empty

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table
        String createUserTableQuery = "CREATE TABLE " + USER_TABLE + " ("
                + USER_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT, "
                + EMAIL_COL + " TEXT, "
                + PASSWORD_COL + " TEXT)";

        // Create notes table
        String createNotesTableQuery = "CREATE TABLE " + NOTES_TABLE + " ("
                + NOTE_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE_COL + " TEXT, "
                + DESC_COL + " TEXT, "
                + IMAGE_URL_COL + " TEXT)";

        db.execSQL(createUserTableQuery);
        db.execSQL(createNotesTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE);
        onCreate(db);
    }

    // ------------------- USER METHODS -------------------
    public void addNewUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);
        values.put(EMAIL_COL, email);
        values.put(PASSWORD_COL, password);
        db.insert(USER_TABLE, null, values);
        db.close();
    }

    public Boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USER_TABLE + " WHERE " + EMAIL_COL + " = ? AND " + PASSWORD_COL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // ------------------- NOTE METHODS -------------------
    public void addNewNote(String title, String description, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE_COL, title);
        values.put(DESC_COL, description);
        values.put(IMAGE_URL_COL, imageUrl);
        db.insert(NOTES_TABLE, null, values);
        db.close();
    }

    public List<Note> getAllNotes() {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + NOTES_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow(NOTE_ID_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DESC_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_URL_COL))
                );
                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return noteList;
    }

    // Get user name by email
    public String getUserNameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = null;

        Cursor cursor = db.rawQuery("SELECT name FROM user_table WHERE email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        }
        cursor.close();
        db.close();
        return name;
    }

    // Get notes uploaded by a specific user (based on email)
    public List<Note> getNotesByUser(String email) {
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE uploader_email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_url"))
                );
                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return noteList;
    }

}
