package com.termux.menu.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;
import com.termux.menu.model.MenuEntryData;

import java.util.ArrayList;

public class MenuEntryAdapter extends RecyclerView.Adapter<MenuEntryViewHolder> {
    private Context mContext;
    private ArrayList<MenuEntryData> mEntries;

    public MenuEntryAdapter(Context context, ArrayList<MenuEntryData> entries) {
        mContext = context;
        mEntries = entries;
    }

    @NonNull
    @Override
    public MenuEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_menu_entry, parent, false);
        return new MenuEntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuEntryViewHolder holder, int position) {
        MenuEntryData entry = mEntries.get(position);
        holder.mTitle.setText(entry.getName());
        if (entry.getIcon() != null) {
            holder.mIcon.setImageDrawable(entry.getIcon());
        } else {
            holder.mIcon.setImageResource(android.R.drawable.ic_menu_preferences);
        }
        holder.mLayout.setOnClickListener(v -> {
            if (entry.getHandler() != null) {
                entry.getHandler().onClick(mContext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    public void release() {
        mContext = null;
    }
}
