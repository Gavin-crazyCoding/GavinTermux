package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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

/** API/MCP配置弹窗 — 支持多种AI后端 */
public class ApiConfigDialog extends Dialog {
    private static final String PREFS = "gavinfloat_api";
    private Context mContext;
    private Spinner mProviderSpinner;
    private EditText mKeyEdit, mEndpointEdit, mModelEdit;

    private static final String[][] PROVIDERS = {
        {"DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"},
        {"OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"},
        {"Claude", "https://api.anthropic.com/v1", "claude-sonnet-4-6"},
        {"Ollama(本地)", "http://127.0.0.1:11434/v1", "llama3"},
        {"自定义", "", ""},
    };

    public ApiConfigDialog(Context context) {
        super(context);
        mContext = context;
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
        title.setText("API/MCP 配置");
        title.setTextColor(0xFFFFFFFF); title.setTextSize(18);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        root.addView(label("AI提供商:"));
        String[] names = new String[PROVIDERS.length];
        for (int i = 0; i < PROVIDERS.length; i++) names[i] = PROVIDERS[i][0];
        mProviderSpinner = new Spinner(mContext);
        mProviderSpinner.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, names));
        root.addView(mProviderSpinner);

        root.addView(label("API Key:"));
        mKeyEdit = editText("");
        mKeyEdit.setHint("sk-...");
        root.addView(mKeyEdit);

        root.addView(label("Endpoint URL:"));
        mEndpointEdit = editText(PROVIDERS[0][1]);
        root.addView(mEndpointEdit);

        root.addView(label("模型名称:"));
        mModelEdit = editText(PROVIDERS[0][2]);
        root.addView(mModelEdit);

        mProviderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                mEndpointEdit.setText(PROVIDERS[pos][1]);
                mModelEdit.setText(PROVIDERS[pos][2]);
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        // 加载已有配置
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        mKeyEdit.setText(prefs.getString("api_key", ""));
        mEndpointEdit.setText(prefs.getString("api_endpoint", PROVIDERS[0][1]));
        mModelEdit.setText(prefs.getString("api_model", PROVIDERS[0][2]));

        TextView note = new TextView(mContext);
        note.setText("配置后可用于代码补全和AI辅助。\n本地Ollama无需API Key。MCP工具可通过端点接入。");
        note.setTextColor(0xFF888888); note.setTextSize(11);
        note.setPadding(0, dp(12), 0, dp(8));
        root.addView(note);

        LinearLayout btnRow = new LinearLayout(mContext);
        btnRow.setPadding(0, dp(12), 0, 0);
        Button saveBtn = btn("保存", 0xFF4CAF50);
        saveBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { save(); }});
        btnRow.addView(saveBtn);
        Button testBtn = btn("测试连接", 0xFF2196F3);
        testBtn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { test(); }});
        btnRow.addView(testBtn);
        root.addView(btnRow);

        scroll.addView(root);
        setContentView(scroll);
        Window w = getWindow();
        if (w != null) { w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }

    private void save() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
            .putString("api_key", mKeyEdit.getText().toString().trim())
            .putString("api_endpoint", mEndpointEdit.getText().toString().trim())
            .putString("api_model", mModelEdit.getText().toString().trim())
            .putString("api_provider", PROVIDERS[mProviderSpinner.getSelectedItemPosition()][0])
            .apply();
        Toast.makeText(mContext, "API配置已保存", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void test() {
        String endpoint = mEndpointEdit.getText().toString().trim();
        String key = mKeyEdit.getText().toString().trim();
        if (endpoint.isEmpty()) { Toast.makeText(mContext, "请输入Endpoint", Toast.LENGTH_SHORT).show(); return; }
        Toast.makeText(mContext, "正在测试连接 " + endpoint + " ...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() { public void run() {
            try {
                java.net.HttpURLConnection c = (java.net.HttpURLConnection)
                    new java.net.URL(endpoint + "/models").openConnection();
                c.setConnectTimeout(8000); c.setReadTimeout(8000);
                if (!key.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + key);
                final int code = c.getResponseCode();
                c.disconnect();
                new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() { public void run() {
                    Toast.makeText(mContext, code == 200 ? "连接成功!" : "HTTP " + code, Toast.LENGTH_SHORT).show();
                }});
            } catch (final Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() { public void run() {
                    Toast.makeText(mContext, "连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }});
            }
        }}).start();
    }

    private TextView label(String t) { TextView tv = new TextView(mContext);
        tv.setText(t); tv.setTextColor(0xFFBBBBBB); tv.setTextSize(13);
        tv.setPadding(0, dp(10), 0, dp(4)); return tv; }
    private EditText editText(String d) { EditText et = new EditText(mContext);
        et.setText(d); et.setTextColor(0xFFFFFFFF); et.setHintTextColor(0xFF888888);
        et.setBackgroundColor(0x22FFFFFF); et.setPadding(dp(12),dp(8),dp(12),dp(8));
        et.setSingleLine(true); et.setTextSize(14); return et; }
    private Button btn(String t, int c) { Button b = new Button(mContext);
        b.setText(t); b.setTextColor(0xFFFFFFFF); b.setBackgroundColor(c);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        p.rightMargin = dp(6); b.setLayoutParams(p); return b; }
    private int dp(int v) { return (int)(v*mContext.getResources().getDisplayMetrics().density+0.5f); }
}
