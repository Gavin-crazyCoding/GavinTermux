package com.termux.menu.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;
import com.termux.menu.model.MenuCategoryData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuCategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private Context mContext;
    private ArrayList<MenuCategoryData> mCategories;
    private HashMap<Integer, MenuEntryAdapter> mAdapters;

    public MenuCategoryAdapter(Context context, ArrayList<MenuCategoryData> categories) {
        mContext = context;
        mCategories = categories;
        mAdapters = new HashMap<>();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_menu_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        MenuCategoryData cat = mCategories.get(position);
        holder.mTitle.setText(cat.title);

        holder.mItemsRec.setLayoutManager(new GridLayoutManager(mContext, 3));
        MenuEntryAdapter adapter = new MenuEntryAdapter(mContext, cat.entries);
        mAdapters.put(position, adapter);
        holder.mItemsRec.setAdapter(adapter);

        applyExpandState(holder, cat.isExpanded);

        holder.itemView.setOnClickListener(v -> {
            cat.isExpanded = !cat.isExpanded;
            applyExpandState(holder, cat.isExpanded);
        });
    }

    private void applyExpandState(CategoryViewHolder holder, boolean expanded) {
        if (expanded) {
            holder.mExpandIcon.setRotation(180);
            holder.mItemsRec.setVisibility(View.VISIBLE);
        } else {
            holder.mExpandIcon.setRotation(0);
            holder.mItemsRec.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public void release() {
        mContext = null;
        for (Map.Entry<Integer, MenuEntryAdapter> e : mAdapters.entrySet()) {
            e.getValue().release();
        }
        mAdapters.clear();
    }
}
