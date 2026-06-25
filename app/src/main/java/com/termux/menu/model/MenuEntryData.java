package com.termux.menu.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class MenuEntryData {
    private String name;
    private Drawable icon;
    private XmlMenuItem xmlItem;
    private ClickHandler handler;

    public interface ClickHandler {
        void onClick(Context context);
    }

    public MenuEntryData(String name, Drawable icon, XmlMenuItem xmlItem, ClickHandler handler) {
        this.name = name;
        this.icon = icon;
        this.xmlItem = xmlItem;
        this.handler = handler;
    }

    public String getName() { return name; }
    public Drawable getIcon() { return icon; }
    public XmlMenuItem getXmlItem() { return xmlItem; }
    public ClickHandler getHandler() { return handler; }
}
