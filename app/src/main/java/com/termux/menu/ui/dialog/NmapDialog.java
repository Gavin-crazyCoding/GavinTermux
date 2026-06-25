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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.termux.TermuxCommandHelper;

/**
 * Nmap 图形化参数表单 — 1:1 对齐 ZeroTermux-main NmapDialog。
 * 纯 Java，程序化布局，AIDE 兼容。
 */
public class NmapDialog extends Dialog {

    private Context mContext;
    private TermuxCommandHelper mCmdHelper;

    private EditText mTargetEdit;
    private CheckBox mCheckAllAdvanced;    // -sV -sC
    private CheckBox mCheckPingScan;       // -sn
    private CheckBox mCheckServiceVersion;  // -sV
    private CheckBox mCheckSkipDiscovery;   // -Pn
    private CheckBox mCheckNoDNS;           // -n
    private CheckBox mCheckIPv6;            // -6
    private CheckBox mCheckTopPorts;        // --top-ports 20
    private CheckBox mCheckFastMode;        // -F
    private CheckBox mCheckNoRandomize;     // -r
    private Spinner mTimingSpinner;
    private LinearLayout mAdvancedLayout;

    public NmapDialog(Context context) {
        super(context);
        mContext = context;
        mCmdHelper = TermuxCommandHelper.getInstance(context);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        ScrollView scroll = new ScrollView(mContext);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        // 标题
        TextView title = new TextView(mContext);
        title.setText("Nmap 扫描");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        // 目标输入
        TextView targetLabel = label("目标 IP/域名:");
        root.addView(targetLabel);

        mTargetEdit = new EditText(mContext);
        mTargetEdit.setHint("例如: 192.168.1.1 或 scanme.nmap.org");
        mTargetEdit.setTextColor(0xFFFFFFFF);
        mTargetEdit.setHintTextColor(0xFF888888);
        mTargetEdit.setBackgroundColor(0x22FFFFFF);
        mTargetEdit.setPadding(dp(12), dp(8), dp(12), dp(8));
        mTargetEdit.setSingleLine(true);
        mTargetEdit.setTextSize(14);
        root.addView(mTargetEdit);

        // 高级选项开关
        LinearLayout advSwitch = row();
        TextView advLabel = new TextView(mContext);
        advLabel.setText("高级选项");
        advLabel.setTextColor(0xFFFFFFFF);
        advLabel.setTextSize(14);
        advSwitch.addView(advLabel);

        final CheckBox advToggle = new CheckBox(mContext);
        advToggle.setText("展开");
        advToggle.setTextColor(0xFF48BAF3);
        advToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                mAdvancedLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
            }
        });
        advSwitch.addView(advToggle);
        root.addView(advSwitch);

        // 高级选项面板
        mAdvancedLayout = new LinearLayout(mContext);
        mAdvancedLayout.setOrientation(LinearLayout.VERTICAL);
        mAdvancedLayout.setVisibility(View.GONE);
        mAdvancedLayout.setPadding(dp(8), dp(4), 0, dp(4));

        // 扫描类型
        mCheckAllAdvanced = checkBox("-sV -sC (版本检测+默认脚本)");
        mCheckPingScan = checkBox("-sn (仅Ping扫描)");
        mCheckServiceVersion = checkBox("-sV (版本检测)");
        mCheckSkipDiscovery = checkBox("-Pn (跳过主机发现)");
        mCheckNoDNS = checkBox("-n (禁用DNS解析)");
        mCheckIPv6 = checkBox("-6 (IPv6扫描)");

        mAdvancedLayout.addView(mCheckAllAdvanced);
        mAdvancedLayout.addView(mCheckPingScan);
        mAdvancedLayout.addView(mCheckServiceVersion);
        mAdvancedLayout.addView(mCheckSkipDiscovery);
        mAdvancedLayout.addView(mCheckNoDNS);
        mAdvancedLayout.addView(mCheckIPv6);

        // 端口选项
        mCheckTopPorts = checkBox("--top-ports 20 (常用端口)");
        mCheckFastMode = checkBox("-F (快速扫描)");
        mCheckNoRandomize = checkBox("-r (顺序扫描)");
        mAdvancedLayout.addView(mCheckTopPorts);
        mAdvancedLayout.addView(mCheckFastMode);
        mAdvancedLayout.addView(mCheckNoRandomize);

        // 时序模板
        LinearLayout timingRow = row();
        TextView timingLabel = new TextView(mContext);
        timingLabel.setText("时序模板:");
        timingLabel.setTextColor(0xFFFFFFFF);
        timingLabel.setTextSize(13);
        timingRow.addView(timingLabel);

        mTimingSpinner = new Spinner(mContext);
        String[] timings = {"默认 (T3)", "偏执 T0", "偷偷摸摸 T1", "有礼貌 T2", "野蛮 T4", "疯狂 T5"};
        ArrayAdapter<String> ta = new ArrayAdapter<>(mContext,
            android.R.layout.simple_spinner_item, timings);
        ta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimingSpinner.setAdapter(ta);
        timingRow.addView(mTimingSpinner);
        mAdvancedLayout.addView(timingRow);

        root.addView(mAdvancedLayout);

        // 按钮行
        LinearLayout btnRow = new LinearLayout(mContext);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, dp(16), 0, 0);

        Button scanBtn = new Button(mContext);
        scanBtn.setText("开始扫描");
        scanBtn.setTextColor(0xFFFFFFFF);
        scanBtn.setBackgroundColor(0xFF4CAF50);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { startScan(); }
        });
        btnRow.addView(scanBtn);

        Button stopBtn = new Button(mContext);
        stopBtn.setText("停止");
        stopBtn.setTextColor(0xFFFFFFFF);
        stopBtn.setBackgroundColor(0xFFF44336);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCmdHelper.sendCommandToTerminal("pkill -f nmap");
                Toast.makeText(mContext, "已发送停止信号", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        sp.leftMargin = dp(12);
        stopBtn.setLayoutParams(sp);
        btnRow.addView(stopBtn);

        root.addView(btnRow);
        scroll.addView(root);
        setContentView(scroll);

        // 底部弹出
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.75));
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
    }

    private void startScan() {
        String target = mTargetEdit.getText().toString().trim();
        if (target.isEmpty()) {
            Toast.makeText(mContext, "请输入目标IP或域名", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("if ! command -v nmap > /dev/null 2>&1; then pkg install nmap -y; fi && nmap");

        if (mAdvancedLayout.getVisibility() == View.VISIBLE) {
            if (mCheckAllAdvanced.isChecked()) cmd.append(" -sV -sC");
            if (mCheckPingScan.isChecked()) cmd.append(" -sn");
            if (mCheckServiceVersion.isChecked()) cmd.append(" -sV");
            if (mCheckSkipDiscovery.isChecked()) cmd.append(" -Pn");
            if (mCheckNoDNS.isChecked()) cmd.append(" -n");
            if (mCheckIPv6.isChecked()) cmd.append(" -6");
            if (mCheckTopPorts.isChecked()) cmd.append(" --top-ports 20");
            if (mCheckFastMode.isChecked()) cmd.append(" -F");
            if (mCheckNoRandomize.isChecked()) cmd.append(" -r");

            int[] timingMap = {3, 0, 1, 2, 4, 5};
            int ti = mTimingSpinner.getSelectedItemPosition();
            if (ti > 0) cmd.append(" -T").append(timingMap[ti]);
        }

        cmd.append(" ").append(target);
        mCmdHelper.sendCommandToTerminal(cmd.toString());
        Toast.makeText(mContext, "Nmap命令已发送到Termux", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private TextView label(String text) {
        TextView tv = new TextView(mContext);
        tv.setText(text);
        tv.setTextColor(0xFFBBBBBB);
        tv.setTextSize(13);
        tv.setPadding(0, dp(10), 0, dp(4));
        return tv;
    }

    private CheckBox checkBox(String text) {
        CheckBox cb = new CheckBox(mContext);
        cb.setText(text);
        cb.setTextColor(0xFFFFFFFF);
        cb.setTextSize(13);
        cb.setPadding(0, dp(4), 0, dp(4));
        return cb;
    }

    private LinearLayout row() {
        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.setPadding(0, dp(6), 0, dp(6));
        return ll;
    }

    private int dp(int val) {
        return (int) (val * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }
}
