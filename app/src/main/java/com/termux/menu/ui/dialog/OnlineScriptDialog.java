package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.termux.TermuxCommandHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 底部抽屉式在线脚本弹窗 — 1:1 对齐 ZeroTermux OnLineShDialog。
 * 纯 Java，无 Lambda，AIDE 兼容。
 */
public class OnlineScriptDialog extends Dialog {

    private static final String SCRIPT_URL = "https://od.ixcmstudio.cn/on_line_sh/main.json";
    private static final String HOME_DIR = "/data/data/com.termux/files/home";

    private Context mContext;
    private TermuxCommandHelper mCmdHelper;
    private TextView mTitleText;
    private ProgressBar mProgressBar;
    private TextView mEmptyText;
    private ScriptAdapter mAdapter;
    private final List<ScriptItem> mItems = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public OnlineScriptDialog(Context context) {
        super(context);
        mContext = context;
        mCmdHelper = TermuxCommandHelper.getInstance(context);
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setMinimumHeight(dp(400));

        // 标题
        LinearLayout header = new LinearLayout(mContext);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setBackgroundColor(0xFF1E2330);

        mTitleText = new TextView(mContext);
        mTitleText.setText("在线脚本");
        mTitleText.setTextColor(0xFFFFFFFF);
        mTitleText.setTextSize(16);
        mTitleText.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView closeBtn = new TextView(mContext);
        closeBtn.setText("✕");
        closeBtn.setTextColor(0xFF888888);
        closeBtn.setTextSize(18);
        closeBtn.setPadding(dp(12), dp(4), 0, dp(4));
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { dismiss(); }
        });

        header.addView(mTitleText);
        header.addView(closeBtn);
        root.addView(header);

        // 进度条
        mProgressBar = new ProgressBar(mContext);
        mProgressBar.setPadding(dp(16), dp(20), dp(16), dp(20));
        root.addView(mProgressBar);

        // 空状态
        mEmptyText = new TextView(mContext);
        mEmptyText.setText("暂无在线脚本");
        mEmptyText.setTextColor(0xFF888888);
        mEmptyText.setTextSize(14);
        mEmptyText.setGravity(Gravity.CENTER);
        mEmptyText.setPadding(0, dp(30), 0, dp(30));
        mEmptyText.setVisibility(View.GONE);
        root.addView(mEmptyText);

        // 列表
        RecyclerView recyclerView = new RecyclerView(mContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setPadding(0, 0, 0, dp(16));
        root.addView(recyclerView);

        setContentView(root);

        // 底部弹出
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.7));
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        mAdapter = new ScriptAdapter();
        recyclerView.setAdapter(mAdapter);

        fetchData();
    }

    private void fetchData() {
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(SCRIPT_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "GavinFloat/1.0");

                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                        is.close();
                        String json = bos.toString("UTF-8");

                        JSONObject obj = new JSONObject(json);
                        final String serviceName = obj.optString("serviceName", "在线脚本");
                        final String ip = obj.optString("ip", "");
                        JSONArray arr = obj.optJSONArray("data");

                        if (arr != null) {
                            mItems.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject item = arr.getJSONObject(i);
                                ScriptItem si = new ScriptItem();
                                si.name = item.optString("name", "");
                                si.note = item.optString("note", "");
                                si.download = item.optString("download", "");
                                si.isDisabled = "1".equals(item.optString("isDownload", "0"));
                                si.fullUrl = ip + si.download;
                                mItems.add(si);
                            }
                        }

                        mHandler.post(new Runnable() {
                            public void run() {
                                mTitleText.setText(serviceName);
                                mProgressBar.setVisibility(View.GONE);
                                if (mItems.isEmpty()) {
                                    mEmptyText.setVisibility(View.VISIBLE);
                                } else {
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    } else {
                        final int code = conn.getResponseCode();
                        mHandler.post(new Runnable() {
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                mEmptyText.setText("加载失败: HTTP " + code);
                                mEmptyText.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            mEmptyText.setText("加载失败: " + e.getMessage());
                            mEmptyText.setVisibility(View.VISIBLE);
                        }
                    });
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }).start();
    }

    private void downloadAndRun(final ScriptItem item) {
        if (item.isDisabled) {
            Toast.makeText(mContext, "该脚本暂不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(mContext, "正在下载: " + item.name, Toast.LENGTH_SHORT).show();
        dismiss();

        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(item.fullUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setRequestProperty("User-Agent", "GavinFloat/1.0");

                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                        is.close();

                        String rawName = item.download;
                        if (rawName.contains("/")) {
                            rawName = rawName.substring(rawName.lastIndexOf('/') + 1);
                        }
                        if (rawName.isEmpty()) rawName = "online_script.sh";
                        final String scriptName = rawName;

                        File outFile = new File(HOME_DIR, scriptName);
                        FileWriter fw = new FileWriter(outFile);
                        fw.write(bos.toString("UTF-8"));
                        fw.close();
                        try {
                            Runtime.getRuntime().exec("chmod 777 " + outFile.getAbsolutePath());
                        } catch (java.io.IOException ignored) {}

                        mHandler.post(new Runnable() {
                            public void run() {
                                mCmdHelper.sendCommandToTerminal(
                                    "cd ~ && chmod 777 " + scriptName + " && ./" + scriptName);
                                Toast.makeText(mContext,
                                    "脚本已下载并执行: " + scriptName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        final int code = conn.getResponseCode();
                        mHandler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(mContext,
                                    "下载失败: HTTP " + code, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(mContext,
                                "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }).start();
    }

    private class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout item = new LinearLayout(mContext);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(16), dp(10), dp(16), dp(10));

            TextView nameTv = new TextView(mContext);
            nameTv.setTextColor(0xFFFFFFFF);
            nameTv.setTextSize(14);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            nameTv.setLayoutParams(np);

            TextView runBtn = new TextView(mContext);
            runBtn.setText("运行");
            runBtn.setTextColor(0xFF4CAF50);
            runBtn.setTextSize(13);
            runBtn.setPadding(dp(16), dp(6), 0, dp(6));

            item.addView(nameTv);
            item.addView(runBtn);
            return new VH(item, nameTv, runBtn);
        }

        @Override
        public void onBindViewHolder(VH holder, int pos) {
            final ScriptItem si = mItems.get(pos);
            String text = si.name;
            if (si.note != null && !si.note.isEmpty()) text += "\n" + si.note;
            holder.nameText.setText(text);
            holder.runBtn.setText(si.isDisabled ? "禁用" : "运行");
            holder.runBtn.setAlpha(si.isDisabled ? 0.4f : 1.0f);
            holder.runBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { downloadAndRun(si); }
            });
        }

        @Override
        public int getItemCount() { return mItems.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView nameText, runBtn;
            VH(View v, TextView n, TextView r) { super(v); nameText = n; runBtn = r; }
        }
    }

    static class ScriptItem {
        String name, note, download, fullUrl;
        boolean isDisabled;
    }

    private int dp(int val) {
        return (int) (val * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }
}
