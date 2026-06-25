package com.termux.menu.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;

public class MenuEntryViewHolder extends RecyclerView.ViewHolder {
    public ImageView mIcon;
    public TextView mTitle;
    public LinearLayout mLayout;

    public MenuEntryViewHolder(@NonNull View itemView) {
        super(itemView);
        mIcon = itemView.findViewById(R.id.entry_icon);
        mTitle = itemView.findViewById(R.id.entry_title);
        mLayout = itemView.findViewById(R.id.entry_layout);
    }
}
