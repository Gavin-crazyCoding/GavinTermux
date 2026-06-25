package com.termux.menu.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.R;
import com.termux.menu.ui.dialog.ApiConfigDialog;
import com.termux.menu.ui.dialog.ThemeConfigDialog;
import com.termux.menu.utils.PrefsManager;

public class SettingsController {
    private Context mContext;
    private View mRootView;
    private PrefsManager mPrefs;
    private SeekBar mBallSizeSeek;
    private SeekBar mMenuWidthSeek;
    private TextView mInfoText;
    private PageController mPageController;

    public interface SettingsChangeListener {
        void onSettingsChanged();
    }

    private SettingsChangeListener mListener;

    public SettingsController(View rootView, Context context, PrefsManager prefs,
                              PageController pageController, SettingsChangeListener listener) {
        mContext = context;
        mRootView = rootView;
        mPrefs = prefs;
        mPageController = pageController;
        mListener = listener;

        mBallSizeSeek = rootView.findViewById(R.id.setting_ball_size);
        mMenuWidthSeek = rootView.findViewById(R.id.setting_menu_width);
        mInfoText = rootView.findViewById(R.id.setting_info);

        mBallSizeSeek.setProgress(prefs.getBallSize() - 20);
        mMenuWidthSeek.setProgress(prefs.getMenuWidth() - 50);

        mBallSizeSeek.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPrefs.setBallSize(seekBar.getProgress() + 20);
                if (mListener != null) mListener.onSettingsChanged();
            }
        });

        mMenuWidthSeek.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPrefs.setMenuWidth(seekBar.getProgress() + 50);
                if (mListener != null) mListener.onSettingsChanged();
            }
        });

        // API/MCP配置按钮
        addApiConfigButton(rootView);

        updateInfo();
    }

    private void addApiConfigButton(View root) {
        ScrollView sv = root.findViewById(R.id.page_settings);
        if (sv == null) return;
        View content = sv.getChildAt(0);
        if (!(content instanceof LinearLayout)) return;
        LinearLayout ll = (LinearLayout) content;

        Button apiBtn = new Button(mContext);
        apiBtn.setText("API/MCP 配置");
        apiBtn.setTextColor(0xFFFFFFFF);
        apiBtn.setBackgroundColor(0xFF2196F3);
        apiBtn.setPadding(0, dp(12), 0, dp(12));
        apiBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new ApiConfigDialog(mContext).show();
            }
        });
        ll.addView(apiBtn, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // 主题配色按钮
        Button themeBtn = new Button(mContext);
        themeBtn.setText("主题配色");
        themeBtn.setTextColor(0xFFFFFFFF);
        themeBtn.setBackgroundColor(0xFFD4AF37);
        themeBtn.setPadding(0, dp(12), 0, dp(12));
        themeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new ThemeConfigDialog(mContext).show();
            }
        });
        ll.addView(themeBtn, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private int dp(int v) {
        return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }

    public void show() {
        mPageController.setTitle("设置");
        mPageController.showPage(PageController.PAGE_SETTINGS);
        updateInfo();
    }

    private void updateInfo() {
        String info = "GavinFloat v1.0\n独立侧边栏APK\n兼容任意Termux\n\n"
            + "悬浮球大小: " + mPrefs.getBallSize() + "dp\n"
            + "菜单宽度: " + mPrefs.getMenuWidth() + "%\n"
            + "字体大小: " + mPrefs.getFontSize() + "sp";
        mInfoText.setText(info);
    }

    private static abstract class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    }
}
