package com.termux.menu.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;

public class FileViewHolder extends RecyclerView.ViewHolder {
    public ImageView mIcon;
    public TextView mName;
    public TextView mSize;
    public LinearLayout mLayout;

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.file_icon);
        mName = itemView.findViewById(R.id.file_name);
        mSize = itemView.findViewById(R.id.file_size);
        mLayout = itemView.findViewById(R.id.file_item_layout);
    }
}
