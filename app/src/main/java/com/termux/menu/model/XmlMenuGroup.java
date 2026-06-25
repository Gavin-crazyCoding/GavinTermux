package com.termux.menu.model;

import java.util.ArrayList;
import java.util.List;

public class XmlMenuGroup {
    private String groupName;
    private int id;
    private List<XmlMenuItem> items;

    public XmlMenuGroup(String groupName, int id) {
        this.groupName = groupName;
        this.id = id;
        this.items = new ArrayList<>();
    }

    public String getGroupName() { return groupName; }
    public int getId() { return id; }
    public List<XmlMenuItem> getItems() { return items; }

    public void addItem(XmlMenuItem item) {
        items.add(item);
    }
}
