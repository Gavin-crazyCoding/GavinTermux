package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** 包管理器GUI — 搜索/安装/卸载软件包 */
public class PackageManagerDialog extends Dialog {
    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private EditText mSearchEdit;
    private LinearLayout mResultList;
    private TextView mStatusText;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private List<String> mAllPkgs = new ArrayList<>();

    public PackageManagerDialog(Context context) {
        super(context);
        mContext = context;
        mCmdHelper = TermuxCommandHelper.getInstance(context);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        ScrollView scroll = new ScrollView(mContext);
        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = new TextView(mContext);
        title.setText("包管理器"); title.setTextColor(0xFFFFFFFF); title.setTextSize(18);
        title.setPadding(0,0,0,dp(12)); root.addView(title);

        // 搜索栏
        LinearLayout searchRow = new LinearLayout(mContext);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        mSearchEdit = new EditText(mContext);
        mSearchEdit.setHint("搜索软件包...");
        mSearchEdit.setTextColor(0xFFFFFFFF); mSearchEdit.setHintTextColor(0xFF888888);
        mSearchEdit.setBackgroundColor(0x22FFFFFF); mSearchEdit.setPadding(dp(12),dp(8),dp(12),dp(8));
        mSearchEdit.setSingleLine(true); mSearchEdit.setTextSize(14);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        mSearchEdit.setLayoutParams(sp);
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {}
            public void afterTextChanged(Editable s) { filterResults(s.toString()); }
        });
        searchRow.addView(mSearchEdit);

        Button refreshBtn = new Button(mContext);
        refreshBtn.setText("刷新"); refreshBtn.setTextColor(0xFFFFFFFF);
        refreshBtn.setBackgroundColor(0xFF2196F3); refreshBtn.setTextSize(12);
        refreshBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { loadPackages(); }});
        searchRow.addView(refreshBtn);
        root.addView(searchRow);

        // 快捷按钮
        LinearLayout quickRow = new LinearLayout(mContext);
        String[] quicks = {"已安装","可升级","Python","Node","GCC","Vim","Git","SSH"};
        for (final String q : quicks) {
            Button qb = new Button(mContext); qb.setText(q); qb.setTextColor(0xFFFFFFFF);
            qb.setBackgroundColor(0xFF37474F); qb.setTextSize(11); qb.setPadding(dp(8),dp(4),dp(8),dp(4));
            qb.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                mSearchEdit.setText(q); filterResults(q);
            }});
            LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            qp.rightMargin = dp(6); qb.setLayoutParams(qp); quickRow.addView(qb);
        }
        quickRow.setPadding(0, dp(8), 0, dp(8));
        root.addView(quickRow);

        mStatusText = new TextView(mContext);
        mStatusText.setText("加载中..."); mStatusText.setTextColor(0xFF888888); mStatusText.setTextSize(12);
        root.addView(mStatusText);

        mResultList = new LinearLayout(mContext);
        mResultList.setOrientation(LinearLayout.VERTICAL);
        root.addView(mResultList);

        scroll.addView(root); setContentView(scroll);
        Window w = getWindow();
        if (w != null) { w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(mContext.getResources().getDisplayMetrics().heightPixels*0.75));
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        loadPackages();
    }

    private void loadPackages() {
        mStatusText.setText("加载中...");
        mCmdHelper.executeAndCapture("pkg list-all 2>/dev/null | awk '{print $1}' | cut -d/ -f2 | head -500",
            new TermuxCommandHelper.OutputCallback() { public void onOutput(String output) {
                String[] lines = output.split("\n");
                mAllPkgs.clear();
                for (String l : lines) { l = l.trim(); if (!l.isEmpty() && !l.startsWith("Listing")) mAllPkgs.add(l); }
                mHandler.post(new Runnable() { public void run() {
                    mStatusText.setText("共 " + mAllPkgs.size() + " 个包");
                    filterResults(mSearchEdit.getText().toString());
                }});
            }});
    }

    private void filterResults(String query) {
        mResultList.removeAllViews();
        int count = 0;
        for (final String pkg : mAllPkgs) {
            if (count >= 30) break;
            if (!query.isEmpty() && !pkg.toLowerCase().contains(query.toLowerCase())) continue;
            count++;

            LinearLayout row = new LinearLayout(mContext);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(6), dp(8), dp(6));
            row.setBackgroundColor(count % 2 == 0 ? 0x11FFFFFF : 0x00000000);

            TextView name = new TextView(mContext);
            name.setText(pkg); name.setTextColor(0xFFFFFFFF); name.setTextSize(13);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            name.setLayoutParams(np);
            row.addView(name);

            Button instBtn = new Button(mContext);
            instBtn.setText("安装"); instBtn.setTextColor(0xFF4CAF50); instBtn.setTextSize(11);
            instBtn.setBackgroundColor(0x00000000); instBtn.setPadding(dp(8),dp(2),dp(8),dp(2));
            instBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                mCmdHelper.sendCommandToTerminal("pkg install -y " + pkg);
                Toast.makeText(mContext, "正在安装: " + pkg, Toast.LENGTH_SHORT).show();
            }});
            row.addView(instBtn);

            Button rmBtn = new Button(mContext);
            rmBtn.setText("卸载"); rmBtn.setTextColor(0xFFF44336); rmBtn.setTextSize(11);
            rmBtn.setBackgroundColor(0x00000000); rmBtn.setPadding(dp(8),dp(2),dp(8),dp(2));
            rmBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                mCmdHelper.sendCommandToTerminal("pkg uninstall -y " + pkg);
                Toast.makeText(mContext, "正在卸载: " + pkg, Toast.LENGTH_SHORT).show();
            }});
            row.addView(rmBtn);

            mResultList.addView(row);
        }
        if (mResultList.getChildCount() == 0) {
            TextView empty = new TextView(mContext);
            empty.setText("未找到匹配的包"); empty.setTextColor(0xFF888888); empty.setTextSize(13);
            empty.setPadding(dp(8), dp(16), dp(8), dp(16));
            mResultList.addView(empty);
        }
    }

    private int dp(int v) { return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f); }
}
