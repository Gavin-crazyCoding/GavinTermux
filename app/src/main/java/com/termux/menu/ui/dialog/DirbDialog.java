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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

/** Dirb Web目录扫描 — 对齐 ZeroTermux-main DirbDialog */
public class DirbDialog extends Dialog {
    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private EditText mUrlEdit, mWordlistEdit, mExtEdit, mIgnoreEdit, mShowEdit;
    private EditText mDelayEdit, mProxyEdit, mCookieEdit;
    private CheckBox mNotRecursive, mIgnoreRedirects, mCaseInsensitive;

    public DirbDialog(Context context) {
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

        addTitle(root, "Dirb 目录扫描");

        root.addView(label("目标URL:"));
        mUrlEdit = editText("http://");
        root.addView(mUrlEdit);

        root.addView(label("字典路径 (默认/usr/share/dirb/wordlists/common.txt):"));
        mWordlistEdit = editText("");
        root.addView(mWordlistEdit);

        root.addView(label("文件扩展名 (-X, 逗号分隔):"));
        mExtEdit = editText("");
        root.addView(mExtEdit);

        root.addView(label("忽略状态码 (-N):"));
        mIgnoreEdit = editText("");
        root.addView(mIgnoreEdit);

        root.addView(label("仅显示状态码 (-S):"));
        mShowEdit = editText("");
        root.addView(mShowEdit);

        root.addView(label("请求延迟毫秒 (-z):"));
        mDelayEdit = editText("");
        root.addView(mDelayEdit);

        root.addView(label("代理 (-p):"));
        mProxyEdit = editText("");
        root.addView(mProxyEdit);

        root.addView(label("Cookie (-c):"));
        mCookieEdit = editText("");
        root.addView(mCookieEdit);

        mNotRecursive = checkBox("非递归模式 (-r)");
        mIgnoreRedirects = checkBox("忽略重定向 (-w)");
        mCaseInsensitive = checkBox("大小写不敏感 (-i)");
        root.addView(mNotRecursive);
        root.addView(mIgnoreRedirects);
        root.addView(mCaseInsensitive);

        LinearLayout btnRow = new LinearLayout(mContext);
        btnRow.setPadding(0, dp(16), 0, 0);
        Button scanBtn = btn("扫描", 0xFF4CAF50);
        scanBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { startScan(); }});
        btnRow.addView(scanBtn);
        Button stopBtn = btn("停止", 0xFFF44336);
        stopBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            mCmdHelper.sendCommandToTerminal("pkill -f dirb");
            Toast.makeText(mContext, "已停止", Toast.LENGTH_SHORT).show();
        }});
        btnRow.addView(stopBtn);
        root.addView(btnRow);

        scroll.addView(root);
        setContentView(scroll);
        setupWindow();
    }

    private void startScan() {
        String url = mUrlEdit.getText().toString().trim();
        if (url.isEmpty() || url.equals("http://")) {
            Toast.makeText(mContext, "请输入目标URL", Toast.LENGTH_SHORT).show(); return;
        }
        StringBuilder cmd = new StringBuilder();
        cmd.append("if ! command -v dirb > /dev/null 2>&1; then apt install dirb -y; fi && dirb ").append(url);
        append(cmd, mWordlistEdit, "");
        appendOpt(cmd, "-X", mExtEdit);
        appendOpt(cmd, "-N", mIgnoreEdit);
        appendOpt(cmd, "-S", mShowEdit);
        appendOpt(cmd, "-z", mDelayEdit);
        appendOpt(cmd, "-p", mProxyEdit);
        appendOpt(cmd, "-c", mCookieEdit);
        if (mNotRecursive.isChecked()) cmd.append(" -r");
        if (mIgnoreRedirects.isChecked()) cmd.append(" -w");
        if (mCaseInsensitive.isChecked()) cmd.append(" -i");
        mCmdHelper.sendCommandToTerminal(cmd.toString());
        Toast.makeText(mContext, "Dirb命令已发送", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void appendOpt(StringBuilder sb, String flag, EditText et) {
        String v = et.getText().toString().trim();
        if (!v.isEmpty()) sb.append(" ").append(flag).append(" ").append(v);
    }
    private void append(StringBuilder sb, EditText et, String def) {
        String v = et.getText().toString().trim();
        if (!v.isEmpty()) sb.append(" ").append(v); else if (!def.isEmpty()) sb.append(" ").append(def);
    }
    private void addTitle(LinearLayout root, String t) {
        TextView tv = new TextView(mContext);
        tv.setText(t); tv.setTextColor(0xFFFFFFFF); tv.setTextSize(18);
        tv.setPadding(0,0,0,dp(12)); root.addView(tv);
    }
    private void setupWindow() {
        Window w = getWindow();
        if (w != null) { w.setGravity(Gravity.BOTTOM); w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            (int)(mContext.getResources().getDisplayMetrics().heightPixels*0.8));
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE); }
    }
    private TextView label(String t) { TextView tv=new TextView(mContext); tv.setText(t); tv.setTextColor(0xFFBBBBBB); tv.setTextSize(13); tv.setPadding(0,dp(10),0,dp(4)); return tv; }
    private EditText editText(String def) { EditText et=new EditText(mContext); et.setText(def); et.setTextColor(0xFFFFFFFF); et.setHintTextColor(0xFF888888); et.setBackgroundColor(0x22FFFFFF); et.setPadding(dp(12),dp(8),dp(12),dp(8)); et.setSingleLine(true); et.setTextSize(14); return et; }
    private CheckBox checkBox(String t) { CheckBox cb=new CheckBox(mContext); cb.setText(t); cb.setTextColor(0xFFFFFFFF); cb.setTextSize(13); cb.setPadding(0,dp(4),0,dp(4)); return cb; }
    private Button btn(String text, int color) { Button b=new Button(mContext); b.setText(text); b.setTextColor(0xFFFFFFFF); b.setBackgroundColor(color); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1); p.rightMargin=dp(6); b.setLayoutParams(p); return b; }
    private int dp(int v) { return (int)(v*mContext.getResources().getDisplayMetrics().density+0.5f); }
}
