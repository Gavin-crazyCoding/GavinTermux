package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** AI 对话 — 使用配置的API进行聊天/代码辅助 */
public class AIChatDialog extends Dialog {
    private Context mCtx; private LinearLayout mChatList; private EditText mInput;
    private ScrollView mScroll; private Handler mH = new Handler(Looper.getMainLooper());
    private String mEndpoint, mKey, mModel;

    public AIChatDialog(Context c) { super(c); mCtx=c; loadConfig(); init(); }
    private void loadConfig() {
        SharedPreferences p = mCtx.getSharedPreferences("gavinfloat_api", Context.MODE_PRIVATE);
        mEndpoint = p.getString("api_endpoint", "https://api.deepseek.com/v1");
        mKey = p.getString("api_key", "");
        mModel = p.getString("api_model", "deepseek-chat");
    }
    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE); setCancelable(true);
        LinearLayout r = new LinearLayout(mCtx); r.setOrientation(LinearLayout.VERTICAL); r.setBackgroundColor(0xFF161823);

        // 标题
        LinearLayout hdr = new LinearLayout(mCtx); hdr.setPadding(dp(16),dp(12),dp(16),dp(12)); hdr.setBackgroundColor(0xFF1E2330);
        TextView title = new TextView(mCtx); title.setText("AI 助手 ("+mModel+")"); title.setTextColor(0xFFFFFFFF); title.setTextSize(16);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1)); hdr.addView(title);
        TextView close = new TextView(mCtx); close.setText("✕"); close.setTextColor(0xFF888888); close.setTextSize(18); close.setPadding(dp(12),0,0,0);
        close.setOnClickListener(new View.OnClickListener(){public void onClick(View v){dismiss();}}); hdr.addView(close);
        r.addView(hdr);

        // API状态提示
        if(mKey.isEmpty()){TextView warn=new TextView(mCtx);warn.setText("⚠ 未配置API Key，请在设置中配置");warn.setTextColor(0xFFFF9800);warn.setTextSize(12);warn.setPadding(dp(16),dp(4),dp(16),dp(4));r.addView(warn);}

        // 聊天列表
        mScroll = new ScrollView(mCtx); mScroll.setFillViewport(true);
        mChatList = new LinearLayout(mCtx); mChatList.setOrientation(LinearLayout.VERTICAL); mChatList.setPadding(dp(12),dp(8),dp(12),dp(8));
        mScroll.addView(mChatList); r.addView(mScroll);

        // 快捷提示
        LinearLayout hints = new LinearLayout(mCtx); hints.setPadding(dp(8),dp(4),dp(8),dp(4));
        String[] htexts = {"写Python脚本","解释命令","修复错误","优化代码","写Shell"};
        for(final String ht:htexts){Button hb=new Button(mCtx);hb.setText(ht);hb.setTextColor(0xFFFFFFFF);hb.setBackgroundColor(0xFF37474F);hb.setTextSize(11);hb.setPadding(dp(8),dp(4),dp(8),dp(4));
            hb.setOnClickListener(new View.OnClickListener(){public void onClick(View v){mInput.setText(ht);}});
            LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);hp.rightMargin=dp(4);hb.setLayoutParams(hp);hints.addView(hb);
        }r.addView(hints);

        // 输入行
        LinearLayout inputRow = new LinearLayout(mCtx); inputRow.setPadding(dp(8),dp(8),dp(8),dp(12));
        mInput = new EditText(mCtx); mInput.setHint("输入消息..."); mInput.setTextColor(0xFFFFFFFF); mInput.setHintTextColor(0xFF888888);
        mInput.setBackgroundColor(0x22FFFFFF); mInput.setPadding(dp(12),dp(8),dp(12),dp(8)); mInput.setTextSize(14); mInput.setSingleLine(false); mInput.setMaxLines(3);
        mInput.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1)); inputRow.addView(mInput);
        Button send = new Button(mCtx); send.setText("发送"); send.setTextColor(0xFFFFFFFF); send.setBackgroundColor(0xFFD4AF37); send.setTextSize(13); send.setPadding(dp(12),dp(8),dp(12),dp(8));
        send.setOnClickListener(new View.OnClickListener(){public void onClick(View v){sendMessage();}}); inputRow.addView(send);
        r.addView(inputRow);

        setContentView(r);
        Window w=getWindow(); if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.85));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}

        addBotMsg("你好！我是AI助手。可以帮你写代码、解释命令、调试错误。\n请在设置中配置API Key后使用。");
    }

    private void sendMessage() {
        final String msg = mInput.getText().toString().trim();
        if(msg.isEmpty()) return;
        if(mKey.isEmpty()){Toast.makeText(mCtx,"请先在设置中配置API Key",Toast.LENGTH_SHORT).show();return;}
        addUserMsg(msg); mInput.setText("");
        addBotMsg("思考中...");
        new Thread(new Runnable(){public void run(){
            try {
                URL url = new URL(mEndpoint+"/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST"); conn.setConnectTimeout(30000); conn.setReadTimeout(60000);
                conn.setRequestProperty("Content-Type","application/json");
                conn.setRequestProperty("Authorization","Bearer "+mKey);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("model", mModel);
                JSONArray msgs = new JSONArray();
                JSONObject um = new JSONObject(); um.put("role","user"); um.put("content",msg); msgs.put(um);
                body.put("messages", msgs);
                body.put("max_tokens", 1024);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8")); os.close();

                if(conn.getResponseCode()==200){
                    java.io.InputStream is = conn.getInputStream();
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    byte[] buf=new byte[4096]; int n;
                    while((n=is.read(buf))!=-1)bos.write(buf,0,n); is.close();
                    JSONObject resp = new JSONObject(bos.toString("UTF-8"));
                    final String reply = resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    mH.post(new Runnable(){public void run(){
                        updateLastBotMsg(reply);
                    }});
                } else {
                    final int code = conn.getResponseCode();
                    mH.post(new Runnable(){public void run(){
                        updateLastBotMsg("API错误: HTTP "+code+"\n请检查API Key和Endpoint配置");
                    }});
                }
                conn.disconnect();
            } catch(final Exception e){
                mH.post(new Runnable(){public void run(){
                    updateLastBotMsg("连接失败: "+e.getMessage());
                }});
            }
        }}).start();
    }

    private void addUserMsg(String msg) {
        TextView tv = new TextView(mCtx); tv.setText(msg); tv.setTextColor(0xFFFFFFFF); tv.setTextSize(13);
        tv.setBackgroundColor(0xFF1E3A5F); tv.setPadding(dp(12),dp(8),dp(12),dp(8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity=Gravity.END; lp.bottomMargin=dp(8); lp.leftMargin=dp(40); tv.setLayoutParams(lp);
        mChatList.addView(tv); mScroll.post(new Runnable(){public void run(){mScroll.fullScroll(View.FOCUS_DOWN);}});
    }

    private void addBotMsg(String msg) {
        TextView tv = new TextView(mCtx); tv.setText(msg); tv.setTextColor(0xFFD4D4D4); tv.setTextSize(13);
        tv.setBackgroundColor(0xFF2A2A3A); tv.setPadding(dp(12),dp(8),dp(12),dp(8));
        tv.setTag("bot");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity=Gravity.START; lp.bottomMargin=dp(8); lp.rightMargin=dp(40); tv.setLayoutParams(lp);
        mChatList.addView(tv); mScroll.post(new Runnable(){public void run(){mScroll.fullScroll(View.FOCUS_DOWN);}});
    }

    private void updateLastBotMsg(String msg) {
        for(int i=mChatList.getChildCount()-1;i>=0;i--){View v=mChatList.getChildAt(i);if("bot".equals(v.getTag())){((TextView)v).setText(msg);return;}}
        addBotMsg(msg);
    }

    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
