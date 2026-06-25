package com.termux.menu.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitle;
    public ImageView mExpandIcon;
    public RecyclerView mItemsRec;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        mTitle = itemView.findViewById(R.id.category_title);
        mExpandIcon = itemView.findViewById(R.id.category_expand_icon);
        mItemsRec = itemView.findViewById(R.id.category_items_rec);
    }
}
