package com.termux.menu.model;

import java.util.ArrayList;

public class MenuCategoryData {
    public String title;
    public int id;
    public boolean isExpanded;
    public ArrayList<MenuEntryData> entries;

    public MenuCategoryData(String title, int id, ArrayList<MenuEntryData> entries) {
        this.title = title;
        this.id = id;
        this.isExpanded = true;
        this.entries = entries;
    }
}
