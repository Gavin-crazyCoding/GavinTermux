package com.termux.menu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    public static final String TERMUX_HOME = "/data/data/com.termux/files/home";
    public static final String TERMUX_USR = "/data/data/com.termux/files/usr";
    public static final String BASH_PATH = TERMUX_USR + "/bin/bash";

    public static String readFile(String path) {
        File f = new File(path);
        if (!f.exists() || !f.isFile()) return null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean writeFile(String path, String content) {
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            FileWriter writer = new FileWriter(f);
            writer.write(content);
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{BASH_PATH, "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            while ((line = errReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            process.waitFor();
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static File[] listFilesSorted(String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) return new File[0];
        File[] files = dir.listFiles();
        if (files == null) return new File[0];
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
        return files;
    }

    public static String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format(Locale.US, "%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024));
        return String.format(Locale.US, "%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
        return sdf.format(new Date(timestamp));
    }

    public static String getFileExtension(String name) {
        int idx = name.lastIndexOf('.');
        if (idx >= 0 && idx < name.length() - 1) return name.substring(idx + 1).toLowerCase();
        return "";
    }
}
