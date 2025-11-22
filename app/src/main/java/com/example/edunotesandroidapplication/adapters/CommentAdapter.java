package com.example.edunotesandroidapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.R;
import com.example.edunotesandroidapplication.models.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.userName.setText(comment.getUserName());
        holder.content.setText(comment.getContent());
        holder.date.setText(formatDate(comment.getDateCreated()));
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.commentList = newComments;
        notifyDataSetChanged();
    }

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = input.parse(rawDate);
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return output.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return rawDate;
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, content, date;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.commentUsername);
            content = itemView.findViewById(R.id.commentText);
            date = itemView.findViewById(R.id.commentDate);
        }
    }
}
