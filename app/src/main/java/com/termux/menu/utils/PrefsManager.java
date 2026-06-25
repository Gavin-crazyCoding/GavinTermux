package com.termux.menu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS_NAME = "gavinfloat_prefs";
    private static final String KEY_BALL_SIZE = "ball_size";
    private static final String KEY_MENU_WIDTH = "menu_width";
    private static final String KEY_BG_COLOR = "bg_color";
    private static final String KEY_CARD_COLOR = "card_color";
    private static final String KEY_TEXT_COLOR = "text_color";
    private static final String KEY_ACCENT_COLOR = "accent_color";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_BALL_X = "ball_x";
    private static final String KEY_BALL_Y = "ball_y";

    private SharedPreferences mPrefs;

    public PrefsManager(Context context) {
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getBallSize() { return mPrefs.getInt(KEY_BALL_SIZE, 50); }
    public void setBallSize(int size) { mPrefs.edit().putInt(KEY_BALL_SIZE, size).apply(); }

    public int getMenuWidth() { return mPrefs.getInt(KEY_MENU_WIDTH, 85); }
    public void setMenuWidth(int width) { mPrefs.edit().putInt(KEY_MENU_WIDTH, width).apply(); }

    public int getBgColor() { return mPrefs.getInt(KEY_BG_COLOR, 0xFF2B2B2B); }
    public void setBgColor(int color) { mPrefs.edit().putInt(KEY_BG_COLOR, color).apply(); }

    public int getCardColor() { return mPrefs.getInt(KEY_CARD_COLOR, 0x55000000); }
    public void setCardColor(int color) { mPrefs.edit().putInt(KEY_CARD_COLOR, color).apply(); }

    public int getTextColor() { return mPrefs.getInt(KEY_TEXT_COLOR, 0xFFFFFFFF); }
    public void setTextColor(int color) { mPrefs.edit().putInt(KEY_TEXT_COLOR, color).apply(); }

    public int getAccentColor() { return mPrefs.getInt(KEY_ACCENT_COLOR, 0xFF48BAF3); }
    public void setAccentColor(int color) { mPrefs.edit().putInt(KEY_ACCENT_COLOR, color).apply(); }

    public int getFontSize() { return mPrefs.getInt(KEY_FONT_SIZE, 13); }
    public void setFontSize(int size) { mPrefs.edit().putInt(KEY_FONT_SIZE, size).apply(); }

    public int getBallX() { return mPrefs.getInt(KEY_BALL_X, 0); }
    public void setBallX(int x) { mPrefs.edit().putInt(KEY_BALL_X, x).apply(); }

    public int getBallY() { return mPrefs.getInt(KEY_BALL_Y, 200); }
    public void setBallY(int y) { mPrefs.edit().putInt(KEY_BALL_Y, y).apply(); }
}
