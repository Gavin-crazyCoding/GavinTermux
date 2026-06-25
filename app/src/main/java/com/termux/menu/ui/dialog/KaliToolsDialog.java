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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

/**
 * Kali 工具分类浏览器 — 13 类 50+ 工具，对齐 ZeroTermux-main 的 Kali 工具分类。
 * 纯 Java，AIDE 兼容。
 */
public class KaliToolsDialog extends Dialog {

    private static final String[][] CATEGORIES = {
        {"信息收集", "nmap", "theharvester", "recon-ng", "dnsenum", "dnsrecon", "amass", "sublist3r"},
        {"漏洞扫描", "nikto", "openvas", "nessus", "lynis"},
        {"Web应用", "sqlmap", "burpsuite", "zaproxy", "dirb", "gobuster", "wpscan", "joomscan"},
        {"密码破解", "john", "hashcat", "hydra", "medusa", "crunch", "cewl"},
        {"WiFi安全", "aircrack-ng", "kismet", "reaver", "bully", "wifite", "fern-wifi-cracker"},
        {"流量分析", "wireshark", "tshark", "ettercap", "dsniff", "netsniff-ng"},
        {"渗透测试", "metasploit-framework", "searchsploit", "exploitdb", "beef-xss"},
        {"后渗透", "empire", "powersploit", "mimikatz", "responder", "evil-winrm"},
        {"取证", "autopsy", "foremost", "binwalk", "volatility"},
        {"逆向工程", "radare2", "ghidra", "ollydbg", "jadx"},
        {"匿名工具", "tor", "proxychains", "macchanger", "anonsurf"},
        {"移动安全", "apktool", "jadx-gui", "drozer", "frida"},
        {"社工工程", "setoolkit", "phishing-framework"},
    };

    private Context mContext;
    private TermuxCommandHelper mCmdHelper;

    public KaliToolsDialog(Context context) {
        super(context);
        mContext = context;
        mCmdHelper = TermuxCommandHelper.getInstance(context);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);

        ScrollView scroll = new ScrollView(mContext);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));

        // 标题
        TextView title = new TextView(mContext);
        title.setText("Kali 工具分类 (Kali-Root环境)");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        // 每个分类
        for (int i = 0; i < CATEGORIES.length; i++) {
            final String catName = CATEGORIES[i][0];

            // 分类标题
            TextView catTitle = new TextView(mContext);
            catTitle.setText(catName);
            catTitle.setTextColor(0xFF48BAF3);
            catTitle.setTextSize(14);
            catTitle.setPadding(0, dp(10), 0, dp(4));
            root.addView(catTitle);

            // 工具流式布局
            LinearLayout toolRow = new LinearLayout(mContext);
            toolRow.setOrientation(LinearLayout.HORIZONTAL);
            toolRow.setPadding(0, 0, 0, dp(4));
            int rowWidth = 0;
            int maxWidth = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.65);

            for (int j = 1; j < CATEGORIES[i].length; j++) {
                final String toolName = CATEGORIES[i][j];
                final String installCmd = "nethunter -r 'apt install " + toolName + " -y'";
                final String runCmd = "nethunter -r " + toolName;

                TextView btn = new TextView(mContext);
                btn.setText(toolName);
                btn.setTextColor(0xFFFFFFFF);
                btn.setTextSize(11);
                btn.setBackgroundColor(0x33FFFFFF);
                btn.setPadding(dp(8), dp(4), dp(8), dp(4));
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mCmdHelper.sendCommandToTerminal(runCmd);
                        Toast.makeText(mContext,
                            "已发送: " + toolName, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
                btn.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        mCmdHelper.sendCommandToTerminal(installCmd);
                        Toast.makeText(mContext,
                            "正在安装: " + toolName, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.rightMargin = dp(6);
                lp.bottomMargin = dp(4);
                btn.setLayoutParams(lp);
                toolRow.addView(btn);
            }
            root.addView(toolRow);
        }

        // 底部说明
        TextView note = new TextView(mContext);
        note.setText("点击=运行工具 | 长按=安装工具");
        note.setTextColor(0xFF888888);
        note.setTextSize(11);
        note.setPadding(0, dp(16), 0, 0);
        root.addView(note);

        scroll.addView(root);
        setContentView(scroll);

        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.85));
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
    }

    private int dp(int val) {
        return (int) (val * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }
}
