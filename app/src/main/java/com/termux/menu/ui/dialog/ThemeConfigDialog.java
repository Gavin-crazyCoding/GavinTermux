package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** 主题配色配置 — 预设主题 + 自定义颜色 */
public class ThemeConfigDialog extends Dialog {

    private static final String PREFS = "gavinfloat_theme";
    // bg, accent (paired arrays)
    private static final int[][] PRESET_COLORS = {
        {color(0x0D,0x02,0x21), color(0xD4,0xAF,0x37)},
        {color(0x00,0x00,0x00), color(0x00,0xFF,0x88)},
        {color(0x0A,0x0A,0x2E), color(0xFF,0x6B,0x6B)},
        {color(0x1A,0x00,0x33), color(0x00,0xCC,0xFF)},
        {color(0x0D,0x11,0x17), color(0x58,0xA6,0xFF)},
        {color(0xFF,0xFF,0xFF), color(0x33,0x33,0x33)},
    };
    private static final String[] PRESET_NAMES = {
        "午夜金","黑客绿","赛博红","极光蓝","GitHub暗","极简白"
    };

    private static int color(int r, int g, int b) { return 0xFF000000 | (r << 16) | (g << 8) | b; }
    private static int alphaColor(int a, int r, int g, int b) { return (a << 24) | (r << 16) | (g << 8) | b; }

    private Context mContext;
    private int mBgColor, mAccentColor, mCardColor;

    public ThemeConfigDialog(Context context) {
        super(context);
        mContext = context;
        loadCurrent();
        init();
    }

    private void loadCurrent() {
        SharedPreferences p = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        mBgColor = p.getInt("bg_color", PRESET_COLORS[0][0]);
        mAccentColor = p.getInt("accent_color", PRESET_COLORS[0][1]);
        mCardColor = p.getInt("card_color", alphaColor(0x18, 0xFF, 0xFF, 0xFF));
    }

    private void save() {
        mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("bg_color", mBgColor)
            .putInt("accent_color", mAccentColor)
            .putInt("card_color", mCardColor)
            .apply();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        ScrollView scroll = new ScrollView(mContext);
        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView title = new TextView(mContext);
        title.setText("主题配色"); title.setTextColor(0xFFFFFFFF); title.setTextSize(18);
        title.setPadding(0,0,0,dp(12)); root.addView(title);

        // 预设主题
        root.addView(label("预设主题:"));
        LinearLayout presetGrid = new LinearLayout(mContext);
        presetGrid.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < PRESET_COLORS.length; i += 2) {
            LinearLayout row = new LinearLayout(mContext);
            for (int j = 0; j < 2 && (i+j) < PRESET_COLORS.length; j++) {
                final int idx = i + j;
                LinearLayout card = new LinearLayout(mContext);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(12), dp(10), dp(12), dp(10));
                card.setBackgroundColor(0xFF2A2A3A);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                cp.rightMargin = j == 0 ? dp(4) : 0; cp.bottomMargin = dp(4);
                card.setLayoutParams(cp);

                // 色块预览
                View swatch = new View(mContext);
                swatch.setBackgroundColor(PRESET_COLORS[idx][1]);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(40), dp(40));
                card.addView(swatch);

                TextView name = new TextView(mContext);
                name.setText(PRESET_NAMES[idx]); name.setTextColor(0xFFFFFFFF); name.setTextSize(12);
                name.setPadding(0, dp(4), 0, 0);
                card.addView(name);

                card.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                    mBgColor = PRESET_COLORS[idx][0]; mAccentColor = PRESET_COLORS[idx][1]; mCardColor = alphaColor(0x18, 0xFF, 0xFF, 0xFF);
                    save();
                    Toast.makeText(mContext, "已应用: " + PRESET_NAMES[idx], Toast.LENGTH_SHORT).show();
                    dismiss();
                }});
                row.addView(card);
            }
            presetGrid.addView(row);
        }
        root.addView(presetGrid);

        // 自定义颜色选择（简化版：预设16色）
        root.addView(label("自定义强调色:"));
        LinearLayout colorRow = new LinearLayout(mContext);
        int[] colors = {
            color(0xD4,0xAF,0x37), color(0x00,0xFF,0x88), color(0xFF,0x6B,0x6B), color(0x00,0xCC,0xFF),
            color(0x58,0xA6,0xFF), color(0xFF,0x98,0x00), color(0xE9,0x1E,0x63), color(0x9C,0x27,0xB0)
        };
        for (final int c : colors) {
            View dot = new View(mContext);
            dot.setBackgroundColor(c);
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(dp(28), dp(28));
            dp.rightMargin = dp(6); dot.setLayoutParams(dp);
            dot.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                mAccentColor = c; save();
                Toast.makeText(mContext, "强调色已更新", Toast.LENGTH_SHORT).show();
            }});
            colorRow.addView(dot);
        }
        root.addView(colorRow);

        scroll.addView(root); setContentView(scroll);
        Window w = getWindow();
        if (w != null) { w.setGravity(Gravity.BOTTOM); w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }

    private TextView label(String t) { TextView tv = new TextView(mContext); tv.setText(t); tv.setTextColor(0xFFBBBBBB); tv.setTextSize(13); tv.setPadding(0,dp(10),0,dp(4)); return tv; }
    private int dp(int v) { return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f); }
}
