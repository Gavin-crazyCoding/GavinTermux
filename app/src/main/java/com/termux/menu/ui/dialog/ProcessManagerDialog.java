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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.menu.termux.TermuxCommandHelper;

/** 进程管理器 — 查看/终止进程 */
public class ProcessManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    private LinearLayout mList; private Handler mH = new Handler(Looper.getMainLooper());
    public ProcessManagerDialog(Context c) { super(c); mCtx=c; mCmd=TermuxCommandHelper.getInstance(c); init(); }
    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE); setCancelable(true);
        ScrollView sv = new ScrollView(mCtx); LinearLayout r = new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL); r.setBackgroundColor(0xFF161823); r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t = new TextView(mCtx); t.setText("进程管理器"); t.setTextColor(0xFFFFFFFF); t.setTextSize(18); t.setPadding(0,0,0,dp(8)); r.addView(t);
        Button ref = new Button(mCtx); ref.setText("刷新"); ref.setTextColor(0xFFFFFFFF); ref.setBackgroundColor(0xFF2196F3);
        ref.setOnClickListener(new View.OnClickListener(){public void onClick(View v){refresh();}}); r.addView(ref);
        mList = new LinearLayout(mCtx); mList.setOrientation(LinearLayout.VERTICAL); r.addView(mList);
        sv.addView(r); setContentView(sv);
        Window w = getWindow(); if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.7));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
        refresh();
    }
    private void refresh() {
        mList.removeAllViews();
        TextView loading = new TextView(mCtx); loading.setText("加载中..."); loading.setTextColor(0xFF888888); loading.setTextSize(13); mList.addView(loading);
        mCmd.executeAndCapture("ps aux --sort=-%mem 2>/dev/null | head -30",new TermuxCommandHelper.OutputCallback(){public void onOutput(String o){
            mH.post(new Runnable(){public void run(){
                mList.removeAllViews();
                final String[] lines = o.split("\n");
                for (int i=1;i<lines.length;i++) {
                    final String line = lines[i].trim(); if(line.isEmpty())continue;
                    String[] cols = line.split("\\s+");
                    if(cols.length<11)continue;
                    final String pid = cols[1], cpu=cols[2], mem=cols[3], cmd=line.substring(line.indexOf(cols[10]));
                    LinearLayout row = new LinearLayout(mCtx); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(4),dp(2),dp(4),dp(2));
                    TextView info = new TextView(mCtx); info.setText(pid+" | CPU:"+cpu+"% MEM:"+mem+"% | "+cmd); info.setTextColor(0xFFFFFFFF); info.setTextSize(11); info.setMaxLines(2);
                    LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1); info.setLayoutParams(ip); row.addView(info);
                    Button kill = new Button(mCtx); kill.setText("KILL"); kill.setTextColor(0xFFF44336); kill.setTextSize(10); kill.setPadding(dp(4),dp(2),dp(4),dp(2)); kill.setBackgroundColor(0x00000000);
                    kill.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                        mCmd.sendCommandToTerminal("kill -9 "+pid+" 2>/dev/null || echo 需要权限");
                        Toast.makeText(mCtx,"已发送kill信号: "+pid,Toast.LENGTH_SHORT).show(); refresh();
                    }}); row.addView(kill);
                    mList.addView(row);
                }
                if(mList.getChildCount()==0){TextView e=new TextView(mCtx);e.setText("无进程数据");e.setTextColor(0xFF888888);e.setTextSize(13);mList.addView(e);}
            }});
        }});
    }
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
