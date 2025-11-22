package com.example.edunotesandroidapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edunotesandroidapplication.R;
import com.example.edunotesandroidapplication.models.Files;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private final Context context;
    private final List<Files> items;

    public FilesAdapter(Context context, List<Files> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Files item = items.get(position);
        holder.fileName.setText(item.getName());

        String ext = (item.getExtension() == null) ? "" : item.getExtension().toLowerCase();

        if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("webp")) {
            try {
                Glide.with(context)
                        .load(item.getUri())
                        .centerCrop()
                        .placeholder(R.drawable.ic_note_placeholder)
                        .into(holder.fileThumb);
            } catch (Exception e) {
                holder.fileThumb.setImageResource(R.drawable.ic_note_placeholder);
            }
        } else {
            int iconRes = getIconForExtension(ext);
            holder.fileThumb.setImageResource(iconRes);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = item.getUri();
                String mime = guessMimeType(uri, ext);
                if (mime == null) mime = "*/*";

                intent.setDataAndType(uri, mime);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String guessMimeType(Uri uri, String ext) {
        if (ext != null && !ext.isEmpty()) {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (mime != null) return mime;
        }
        String scheme = uri.getScheme();
        if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            String path = uri.toString();
            int dot = path.lastIndexOf('.');
            if (dot != -1 && dot + 1 < path.length()) {
                String ext2 = path.substring(dot + 1).toLowerCase();
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext2);
            }
        }
        return null;
    }

    private int getIconForExtension(String ext) {
        switch (ext) {
            case "pdf": return R.drawable.ic_pdf;
            case "doc":
            case "docx": return R.drawable.ic_word;
            case "ppt":
            case "pptx": return R.drawable.ic_ppt;
            case "xls":
            case "xlsx": return R.drawable.ic_excel;
            case "txt": return R.drawable.ic_text;
            case "zip":
            case "rar": return R.drawable.ic_zip;
            default: return R.drawable.ic_file;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        final ImageView fileThumb;
        final TextView fileName;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileThumb = itemView.findViewById(R.id.fileThumb);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }
}