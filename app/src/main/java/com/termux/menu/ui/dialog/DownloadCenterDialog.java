package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 底部抽屉式下载中心弹窗 — 1:1 对齐 ZeroTermux DownLoadDialogBoom。
 * 纯 Java，无 Lambda，AIDE 兼容。
 */
public class DownloadCenterDialog extends Dialog {

    private static final String REPO_URL = "https://od.ixcmstudio.cn/repository/main/";

    private Context mContext;
    private TextView mTitleText;
    private ProgressBar mProgressBar;
    private TextView mEmptyText;
    private DownloadAdapter mAdapter;
    private final List<DownloadItem> mItems = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public DownloadCenterDialog(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF161823);
        root.setMinimumHeight(dp(400));

        // 标题栏
        LinearLayout header = new LinearLayout(mContext);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setBackgroundColor(0xFF1E2330);

        mTitleText = new TextView(mContext);
        mTitleText.setText("下载中心");
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
        mEmptyText.setText("暂无数据");
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
        recyclerView.setClipToPadding(false);
        root.addView(recyclerView);

        setContentView(root);

        // 底部弹出样式
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.7));
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setWindowAnimations(android.R.style.Animation_Dialog);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                window.setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        mAdapter = new DownloadAdapter();
        recyclerView.setAdapter(mAdapter);

        fetchData();
    }

    private void fetchData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);
        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(REPO_URL);
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
                        final String serviceName = obj.optString("serviceName", "下载中心");
                        final String ip = obj.optString("ip", REPO_URL);
                        JSONArray arr = obj.optJSONArray("data");

                        if (arr != null) {
                            mItems.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject item = arr.getJSONObject(i);
                                DownloadItem di = new DownloadItem();
                                di.name = item.optString("name", "");
                                di.note = item.optString("note", "");
                                di.size = item.optString("size", "");
                                di.download = item.optString("download", "");
                                di.fileName = item.optString("fileName", "");
                                di.type = item.optString("type", "");
                                di.isDisabled = "1".equals(item.optString("isDownload", "0"));
                                di.fullUrl = ip + di.download;
                                mItems.add(di);
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

    private class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout item = new LinearLayout(mContext);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(16), dp(10), dp(16), dp(10));

            TextView name = new TextView(mContext);
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(14);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            name.setLayoutParams(nameParams);

            TextView btn = new TextView(mContext);
            btn.setText("下载");
            btn.setTextColor(0xFF48BAF3);
            btn.setTextSize(13);
            btn.setPadding(dp(16), dp(6), 0, dp(6));

            item.addView(name);
            item.addView(btn);
            return new VH(item, name, btn);
        }

        @Override
        public void onBindViewHolder(VH holder, int pos) {
            final DownloadItem di = mItems.get(pos);
            String text = di.name;
            if (!TextUtils.isEmpty(di.size)) text += "  [" + di.size + "]";
            if (!TextUtils.isEmpty(di.note)) text += "\n" + di.note;
            holder.nameText.setText(text);

            holder.downBtn.setText(di.isDisabled ? "禁用" : "下载");
            holder.downBtn.setAlpha(di.isDisabled ? 0.4f : 1.0f);

            holder.downBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (di.isDisabled) {
                        Toast.makeText(mContext, "该文件暂不可下载", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(di.fullUrl));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        Toast.makeText(mContext, "正在打开: " + di.name, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(mContext, "无法打开: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() { return mItems.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView nameText, downBtn;
            VH(View v, TextView n, TextView b) { super(v); nameText = n; downBtn = b; }
        }
    }

    static class DownloadItem {
        String name, note, size, download, fileName, type, fullUrl;
        boolean isDisabled;
    }

    private int dp(int val) {
        return (int) (val * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }
}
