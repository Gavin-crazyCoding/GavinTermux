package com.termux.menu.xml;

import android.text.TextUtils;
import android.util.Log;

import com.termux.menu.model.XmlMenuGroup;
import com.termux.menu.model.XmlMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlMenuParser {
    private static final String TAG = "XmlMenuParser";

    public static List<XmlMenuGroup> parseFromFile(File xmlFile) {
        List<XmlMenuGroup> result = new ArrayList<>();
        if (xmlFile == null || !xmlFile.exists()) return result;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();
            return parseDocument(document);
        } catch (Exception e) {
            Log.e(TAG, "parseFromFile error: " + e.getMessage());
        }
        return result;
    }

    public static List<XmlMenuGroup> parseFromStream(InputStream is) {
        List<XmlMenuGroup> result = new ArrayList<>();
        if (is == null) return result;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            return parseDocument(document);
        } catch (Exception e) {
            Log.e(TAG, "parseFromStream error: " + e.getMessage());
        }
        return result;
    }

    private static List<XmlMenuGroup> parseDocument(Document doc) {
        List<XmlMenuGroup> result = new ArrayList<>();
        NodeList groupList = doc.getElementsByTagName("group");
        for (int i = 0; i < groupList.getLength(); i++) {
            Element groupEl = (Element) groupList.item(i);
            String groupName = groupEl.getAttribute("name");
            int groupId = parseInt(groupEl.getAttribute("id"));
            XmlMenuGroup group = new XmlMenuGroup(groupName, groupId);
            NodeList itemList = groupEl.getElementsByTagName("item");
            for (int j = 0; j < itemList.getLength(); j++) {
                Element itemEl = (Element) itemList.item(j);
                group.addItem(parseItem(itemEl));
            }
            result.add(group);
        }
        return result;
    }

    private static XmlMenuItem parseItem(Element el) {
        return new XmlMenuItem(
            el.getAttribute("name"),
            el.getAttribute("click"),
            el.getAttribute("icon"),
            parseBool(el.getAttribute("autoRunShell")),
            el.getAttribute("packageName"),
            parseBool(el.getAttribute("dialogConfirm")),
            el.getAttribute("dialogTitle"),
            el.getAttribute("dialogMessage"),
            el.getAttribute("intentData"),
            el.getAttribute("listTitle"),
            el.getAttribute("activityTitle")
        );
    }

    private static boolean parseBool(String val) {
        return !TextUtils.isEmpty(val) && "true".equals(val.trim());
    }

    private static int parseInt(String val) {
        if (val == null || val.trim().isEmpty()) return 0;
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return 0; }
    }
}
