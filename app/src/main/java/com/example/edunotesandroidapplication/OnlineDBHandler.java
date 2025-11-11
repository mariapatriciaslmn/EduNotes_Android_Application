package com.example.edunotesandroidapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.edunotesandroidapplication.models.Comment;
import com.example.edunotesandroidapplication.models.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OnlineDBHandler {

    private static final String BASE_URL = "http://192.168.254.190/edunotes/";
    private Context context;
    private RequestQueue queue;

    public OnlineDBHandler(Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }

    // ---------------- Users ----------------

    public void addUser(String name, String email, String password, Response.Listener<String> listener) {
        String url = BASE_URL + "add_user.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "addUser error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        queue.add(request);
    }

    public void checkUser(String email, String password, Response.Listener<String> listener) {
        String url = BASE_URL + "check_user.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "checkUser error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        queue.add(request);
    }

    public void getUser(String email, Response.Listener<String> listener) {
        String url = BASE_URL + "get_user.php?email=" + email;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                listener,
                error -> Log.e("OnlineDB", "getUser error: " + error));
        queue.add(request);
    }

    public void updateProfilePicture(String email, String uri, Response.Listener<String> listener) {
        String url = BASE_URL + "update_profile_picture.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "updateProfilePicture error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("email", email);
                params.put("uri", uri);
                return params;
            }
        };
        queue.add(request);
    }

    // ---------------- Notes ----------------

    public void addNote(String title, String description, String imageUrl, String uploaderEmail, Response.Listener<String> listener) {
        String url = BASE_URL + "add_note.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "addNote error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("title", title);
                params.put("description", description);
                params.put("image_url", imageUrl);
                params.put("uploader_email", uploaderEmail);
                return params;
            }
        };
        queue.add(request);
    }

    public void fetchNotes(String order, Response.Listener<String> listener) {
        String url = BASE_URL + "fetch_notes.php?order=" + order;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                listener,
                error -> Log.e("OnlineDB", "fetchNotes error: " + error));
        queue.add(request);
    }

    public void fetchNotesByUser(String email, String order, Response.Listener<String> listener) {
        String url = BASE_URL + "fetch_notes_by_user.php?email=" + email + "&order=" + order;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                listener,
                error -> Log.e("OnlineDB", "fetchNotesByUser error: " + error));
        queue.add(request);
    }

    public void getNoteById(int noteId, Response.Listener<String> listener) {
        String url = BASE_URL + "get_note.php?note_id=" + noteId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                listener,
                error -> Log.e("OnlineDB", "getNoteById error: " + error));
        queue.add(request);
    }

    // ---------------- Saved Notes ----------------

    public void saveNote(int noteId, String userEmail, Response.Listener<String> listener) {
        String url = BASE_URL + "save_note.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "saveNote error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("note_id", String.valueOf(noteId));
                params.put("user_email", userEmail);
                return params;
            }
        };
        queue.add(request);
    }

    public void removeSavedNote(int noteId, String userEmail, Response.Listener<String> listener) {
        String url = BASE_URL + "remove_saved_note.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "removeSavedNote error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("note_id", String.valueOf(noteId));
                params.put("user_email", userEmail);
                return params;
            }
        };
        queue.add(request);
    }

    public void fetchSavedNotes(String userEmail, Response.Listener<String> listener) {
        String url = BASE_URL + "fetch_saved_notes.php?email=" + userEmail;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                listener,
                error -> Log.e("OnlineDB", "fetchSavedNotes error: " + error));
        queue.add(request);
    }

    // ---------------- Comments ----------------

    public void addComment(int noteId, String userName, String content, Response.Listener<String> listener) {
        String url = BASE_URL + "add_comment.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                listener,
                error -> Log.e("OnlineDB", "addComment error: " + error)) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("note_id", String.valueOf(noteId));
                params.put("user_name", userName);
                params.put("content", content);
                return params;
            }
        };
        queue.add(request);
    }

    public void fetchComments(int noteId, Response.Listener<String> listener) {
        String url = BASE_URL + "fetch_comments.php?note_id=" + noteId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                listener,
                error -> Log.e("OnlineDB", "fetchComments error: " + error));
        queue.add(request);
    }

}
