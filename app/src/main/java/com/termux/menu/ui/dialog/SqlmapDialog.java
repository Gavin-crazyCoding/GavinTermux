package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

/**
 * Sqlmap 图形化参数表单 — 对齐 ZeroTermux-main SqlmapDialog。
 */
public class SqlmapDialog extends Dialog {

    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private EditText mUrlEdit, mPostEdit, mCookieEdit, mCustomEdit, mLevelEdit, mRiskEdit;
    private CheckBox mBatchCb, mRandomAgentCb, mDbsCb, mTablesCb, mColumnsCb, mDumpCb;
    private LinearLayout mAdvancedLayout;

    public SqlmapDialog(Context context) {
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
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView title = new TextView(mContext);
        title.setText("Sqlmap 注入测试");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        // Target URL
        root.addView(label("目标URL:"));
        mUrlEdit = editText("http://");
        root.addView(mUrlEdit);

        // Wizard mode toggle
        final CheckBox wizardCb = checkBox("向导模式 (--wizard, 自动选择参数)");
        root.addView(wizardCb);

        // Advanced panel
        mAdvancedLayout = new LinearLayout(mContext);
        mAdvancedLayout.setOrientation(LinearLayout.VERTICAL);

        root.addView(label("POST数据 (--data):"));
        mPostEdit = editText("");
        mAdvancedLayout.addView(mPostEdit);

        root.addView(label("Cookie (--cookie):"));
        mCookieEdit = editText("");
        mAdvancedLayout.addView(mCookieEdit);

        mBatchCb = checkBox("批处理模式 (--batch)");
        mRandomAgentCb = checkBox("随机UserAgent (--random-agent)");
        mDbsCb = checkBox("枚举数据库 (--dbs)");
        mTablesCb = checkBox("枚举表 (--tables)");
        mColumnsCb = checkBox("枚举列 (--columns)");
        mDumpCb = checkBox("导出数据 (--dump)");
        mAdvancedLayout.addView(mBatchCb);
        mAdvancedLayout.addView(mRandomAgentCb);
        mAdvancedLayout.addView(mDbsCb);
        mAdvancedLayout.addView(mTablesCb);
        mAdvancedLayout.addView(mColumnsCb);
        mAdvancedLayout.addView(mDumpCb);

        root.addView(label("自定义参数:"));
        mCustomEdit = editText("");
        mAdvancedLayout.addView(mCustomEdit);

        LinearLayout numRow = new LinearLayout(mContext);
        numRow.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(label("Level (--level):"));
        mLevelEdit = editText("1");
        numRow.addView(mLevelEdit);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(12);
        mLevelEdit.setLayoutParams(lp);

        root.addView(label("Risk (--risk):"));
        mRiskEdit = editText("1");
        numRow.addView(mRiskEdit);
        root.addView(numRow);

        root.addView(mAdvancedLayout);

        wizardCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                mAdvancedLayout.setVisibility(checked ? View.GONE : View.VISIBLE);
            }
        });

        // Buttons
        LinearLayout btnRow = new LinearLayout(mContext);
        btnRow.setPadding(0, dp(16), 0, 0);

        Button scanBtn = btn("开始扫描", 0xFF4CAF50);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { start(); }
        });
        btnRow.addView(scanBtn);

        Button stopBtn = btn("停止", 0xFFF44336);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCmdHelper.sendCommandToTerminal("pkill -f sqlmap");
                Toast.makeText(mContext, "已停止", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(stopBtn);

        root.addView(btnRow);
        scroll.addView(root);
        setContentView(scroll);

        Window w = getWindow();
        if (w != null) {
            w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(mContext.getResources().getDisplayMetrics().heightPixels * 0.8));
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }

    private void start() {
        String url = mUrlEdit.getText().toString().trim();
        if (url.isEmpty() || url.equals("http://")) {
            Toast.makeText(mContext, "请输入目标URL", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder cmd = new StringBuilder();
        cmd.append("if ! command -v sqlmap > /dev/null 2>&1; then apt install sqlmap -y; fi && ");
        cmd.append("sqlmap -u \"").append(url).append("\"");

        if (mAdvancedLayout.getVisibility() == View.VISIBLE) {
            appendOpt(cmd, "--data", mPostEdit);
            appendOpt(cmd, "--cookie", mCookieEdit);
            if (mBatchCb.isChecked()) cmd.append(" --batch");
            if (mRandomAgentCb.isChecked()) cmd.append(" --random-agent");
            if (mDbsCb.isChecked()) cmd.append(" --dbs");
            if (mTablesCb.isChecked()) cmd.append(" --tables");
            if (mColumnsCb.isChecked()) cmd.append(" --columns");
            if (mDumpCb.isChecked()) cmd.append(" --dump");
            appendOpt(cmd, "--level", mLevelEdit);
            appendOpt(cmd, "--risk", mRiskEdit);
            String custom = mCustomEdit.getText().toString().trim();
            if (!custom.isEmpty()) cmd.append(" ").append(custom);
        } else {
            cmd.append(" --wizard");
        }

        mCmdHelper.sendCommandToTerminal(cmd.toString());
        Toast.makeText(mContext, "Sqlmap命令已发送", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void appendOpt(StringBuilder sb, String flag, EditText et) {
        String v = et.getText().toString().trim();
        if (!v.isEmpty()) sb.append(" ").append(flag).append(" \"").append(v).append("\"");
    }

    private TextView label(String t) {
        TextView tv = new TextView(mContext);
        tv.setText(t); tv.setTextColor(0xFFBBBBBB); tv.setTextSize(13);
        tv.setPadding(0, dp(10), 0, dp(4)); return tv;
    }
    private EditText editText(String def) {
        EditText et = new EditText(mContext);
        et.setText(def); et.setTextColor(0xFFFFFFFF); et.setHintTextColor(0xFF888888);
        et.setBackgroundColor(0x22FFFFFF); et.setPadding(dp(12),dp(8),dp(12),dp(8));
        et.setSingleLine(true); et.setTextSize(14); return et;
    }
    private CheckBox checkBox(String t) {
        CheckBox cb = new CheckBox(mContext);
        cb.setText(t); cb.setTextColor(0xFFFFFFFF); cb.setTextSize(13);
        cb.setPadding(0, dp(4), 0, dp(4)); return cb;
    }
    private Button btn(String text, int color) {
        Button b = new Button(mContext); b.setText(text); b.setTextColor(0xFFFFFFFF);
        b.setBackgroundColor(color);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        p.rightMargin = dp(6); b.setLayoutParams(p); return b;
    }
    private int dp(int v) { return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f); }
}
