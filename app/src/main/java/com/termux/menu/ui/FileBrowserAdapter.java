package com.termux.menu.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;
import com.termux.menu.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class FileBrowserAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private Context mContext;
    private ArrayList<File> mFiles;
    private FileActionListener mListener;

    public interface FileActionListener {
        void onFileClick(File file);
        void onFolderClick(File folder);
        void onFileLongClick(File file);
    }

    public FileBrowserAdapter(Context context, FileActionListener listener) {
        mContext = context;
        mFiles = new ArrayList<>();
        mListener = listener;
    }

    public void setFiles(File[] files) {
        mFiles.clear();
        if (files != null) {
            for (File f : files) mFiles.add(f);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_file_entry, parent, false);
        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = mFiles.get(position);
        holder.mName.setText(file.getName());

        if (file.isDirectory()) {
            holder.mIcon.setImageResource(android.R.drawable.ic_menu_agenda);
            int count = file.listFiles() != null ? file.listFiles().length : 0;
            holder.mSize.setText(count + " 项");
        } else {
            holder.mIcon.setImageResource(android.R.drawable.ic_menu_edit);
            holder.mSize.setText(FileUtils.formatSize(file.length()));
        }

        holder.mLayout.setOnClickListener(v -> {
            if (file.isDirectory() && mListener != null) {
                mListener.onFolderClick(file);
            } else if (mListener != null) {
                mListener.onFileClick(file);
            }
        });

        holder.mLayout.setOnLongClickListener(v -> {
            if (mListener != null) mListener.onFileLongClick(file);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }
}
