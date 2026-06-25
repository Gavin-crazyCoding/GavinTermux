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
import com.termux.menu.termux.TermuxCommandHelper;

/** 系统仪表盘 — 实时CPU/内存/磁盘/电池 */
public class SystemDashboardDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private TextView mCpu,mMem,mDisk,mBat,mUptime;
    private Handler mH=new Handler(Looper.getMainLooper());
    public SystemDashboardDialog(Context c){super(c);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"系统仪表盘");

        mCpu=addCard(r,"CPU");mMem=addCard(r,"内存");mDisk=addCard(r,"磁盘");mBat=addCard(r,"电池");mUptime=addCard(r,"运行时间");

        Button ref=btn("刷新",0xFF2196F3);ref.setOnClickListener(new View.OnClickListener(){public void onClick(View v){loadAll();}});r.addView(ref);

        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.6));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
        loadAll();
    }
    private TextView addCard(LinearLayout r,String title){
        TextView t=new TextView(mCtx);t.setText(title+": 加载中...");t.setTextColor(0xFFBBBBBB);t.setTextSize(12);
        LinearLayout card=new LinearLayout(mCtx);card.setPadding(dp(12),dp(8),dp(12),dp(8));card.setBackgroundColor(0xFF2A2A3A);
        card.addView(t);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);lp.bottomMargin=dp(6);card.setLayoutParams(lp);r.addView(card);
        return t;
    }
    private void loadAll(){
        exec("cat /proc/cpuinfo | grep 'model name' | head -1 | cut -d: -f2; echo '---'; grep -c processor /proc/cpuinfo",mCpu,"CPU");
        exec("free -h 2>/dev/null | awk 'NR==2{print $3\"/\"$2}' || cat /proc/meminfo | head -3",mMem,"内存");
        exec("df -h /data 2>/dev/null | awk 'NR==2{print $3\"/\"$2\" (\"$5\")\"}'",mDisk,"磁盘");
        exec("echo 电池信息需要API支持; dumpsys battery 2>/dev/null | grep level | head -1 || echo 无数据",mBat,"电池");
        exec("uptime 2>/dev/null || cat /proc/uptime | awk '{printf \"%.0fm\",$1/60}'",mUptime,"运行时间");
    }
    private void exec(String cmd,final TextView tv,final String label){
        tv.setText(label+": 加载中...");
        mCmd.executeAndCapture(cmd+" 2>/dev/null",new TermuxCommandHelper.OutputCallback(){public void onOutput(String o){
            final String s=o.trim();mH.post(new Runnable(){public void run(){tv.setText(label+": "+(s.isEmpty()?"无数据":s));}});
        }});
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(13);b.setPadding(dp(10),dp(6),dp(10),dp(6));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
