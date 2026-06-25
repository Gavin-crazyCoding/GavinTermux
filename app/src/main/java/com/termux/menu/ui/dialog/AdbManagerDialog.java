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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.menu.termux.TermuxCommandHelper;

/** ADB 可视化管理器 — 设备列表/安装APK/Shell/截图/重启 */
public class AdbManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private LinearLayout mDevList; private TextView mStatus;
    private Handler mH = new Handler(Looper.getMainLooper());
    private EditText mShellInput, mApkPath, mLocalPort, mRemotePort;

    public AdbManagerDialog(Context c) { super(c); mCtx=c; mCmd=TermuxCommandHelper.getInstance(c); init(); }
    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE); setCancelable(true);
        ScrollView sv = new ScrollView(mCtx); LinearLayout r = new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL); r.setBackgroundColor(0xFF161823); r.setPadding(dp(16),dp(16),dp(16),dp(16));

        addTitle(r, "ADB 管理器");
        mStatus = addText(r, "状态: 检测中...", 0xFF888888, 12);

        // 设备列表
        Button refreshBtn = btn("刷新设备", 0xFF2196F3);
        refreshBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){refreshDevices();}});
        r.addView(refreshBtn);
        mDevList = new LinearLayout(mCtx); mDevList.setOrientation(LinearLayout.VERTICAL);
        r.addView(mDevList);

        // ADB Shell
        addText(r, "ADB Shell:", 0xFFBBBBBB, 13); addSpace(r);
        mShellInput = editText(""); mShellInput.setHint("输入shell命令...");
        LinearLayout shRow = new LinearLayout(mCtx);
        shRow.addView(mShellInput);
        Button shBtn = btn("执行", 0xFF4CAF50); shBtn.setTextSize(12);
        shBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String cmd = mShellInput.getText().toString().trim();
            if(cmd.isEmpty()){Toast.makeText(mCtx,"输入命令",Toast.LENGTH_SHORT).show();return;}
            mCmd.sendCommandToTerminal("adb shell "+cmd); Toast.makeText(mCtx,"已发送",Toast.LENGTH_SHORT).show();
            mShellInput.setText("");
        }}); shRow.addView(shBtn); r.addView(shRow);

        // 安装APK
        addText(r, "安装APK:", 0xFFBBBBBB, 13); addSpace(r);
        mApkPath = editText(""); mApkPath.setHint("/sdcard/Download/app.apk");
        LinearLayout apkRow = new LinearLayout(mCtx);
        apkRow.addView(mApkPath);
        Button instBtn = btn("安装", 0xFFFF9800); instBtn.setTextSize(12);
        instBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String p = mApkPath.getText().toString().trim();
            if(p.isEmpty()){Toast.makeText(mCtx,"输入APK路径",Toast.LENGTH_SHORT).show();return;}
            mCmd.sendCommandToTerminal("adb install -r "+p); Toast.makeText(mCtx,"正在安装...",Toast.LENGTH_SHORT).show();
        }}); apkRow.addView(instBtn); r.addView(apkRow);

        // 端口转发
        addText(r, "端口转发:", 0xFFBBBBBB, 13); addSpace(r);
        LinearLayout pfRow = new LinearLayout(mCtx);
        mLocalPort = editText(""); mLocalPort.setHint("本地端口"); pfRow.addView(mLocalPort);
        mRemotePort = editText(""); mRemotePort.setHint("远程端口"); pfRow.addView(mRemotePort);
        Button pfBtn = btn("转发", 0xFF9C27B0); pfBtn.setTextSize(12);
        pfBtn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String l=mLocalPort.getText().toString().trim(),r2=mRemotePort.getText().toString().trim();
            if(l.isEmpty()||r2.isEmpty()){Toast.makeText(mCtx,"输入端口",Toast.LENGTH_SHORT).show();return;}
            mCmd.sendCommandToTerminal("adb forward tcp:"+l+" tcp:"+r2); Toast.makeText(mCtx,"端口转发已设置",Toast.LENGTH_SHORT).show();
        }}); pfRow.addView(pfBtn); r.addView(pfRow);

        // 快捷操作网格
        addSpace(r);
        String[][] actions = {{"截图","adb exec-out screencap -p > /sdcard/screen.png"},{"录屏","adb shell screenrecord /sdcard/video.mp4"},
            {"重启设备","adb reboot"},{"重启Recovery","adb reboot recovery"},{"重启Bootloader","adb reboot bootloader"},
            {"停止ADB","adb kill-server"},{"启动ADB","adb start-server"},{"列出包","adb shell pm list packages"}};
        LinearLayout grid = new LinearLayout(mCtx); grid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<actions.length;i+=2){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<2&&(i+j)<actions.length;j++){final String label=actions[i+j][0];final String cmd=actions[i+j][1];
                Button b=btn(label,0xFF37474F);b.setTextSize(11);b.setPadding(dp(6),dp(6),dp(6),dp(6));
                b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                    mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,label+" 已发送",Toast.LENGTH_SHORT).show();
                }});
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);bp.rightMargin=j==0?dp(4):0;bp.bottomMargin=dp(4);b.setLayoutParams(bp);row.addView(b);
            }grid.addView(row);
        }r.addView(grid);

        sv.addView(r); setContentView(sv);
        Window w=getWindow(); if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.85));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
        refreshDevices();
    }

    private void refreshDevices() {
        mStatus.setText("状态: 检测中...");
        mCmd.executeAndCapture("adb devices 2>/dev/null | tail -n +2", new TermuxCommandHelper.OutputCallback(){public void onOutput(String o){
            mH.post(new Runnable(){public void run(){
                mDevList.removeAllViews(); String[] lines=o.split("\n"); int cnt=0;
                for(final String line:lines){String l=line.trim();if(l.isEmpty()||l.startsWith("*"))continue;
                    String[] parts=l.split("\t");if(parts.length<1)continue;final String serial=parts[0];String state=parts.length>1?parts[1]:"unknown";cnt++;
                    LinearLayout row=new LinearLayout(mCtx);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(2),dp(4),dp(2));
                    TextView ts=new TextView(mCtx);ts.setText(serial+" ["+state+"]");ts.setTextColor(state.contains("device")?0xFF4CAF50:0xFFFF9800);ts.setTextSize(12);
                    LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);ts.setLayoutParams(tp);row.addView(ts);
                    if(state.contains("device")){
                        Button conn=quickBtn("连接",0xFF2196F3);conn.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                            mCmd.sendCommandToTerminal("adb -s "+serial+" shell");Toast.makeText(mCtx,"已连接: "+serial,Toast.LENGTH_SHORT).show();
                        }});row.addView(conn);
                        Button dis=quickBtn("断开",0xFFF44336);dis.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                            mCmd.sendCommandToTerminal("adb disconnect "+serial);Toast.makeText(mCtx,"已断开: "+serial,Toast.LENGTH_SHORT).show();refreshDevices();
                        }});row.addView(dis);
                    }mDevList.addView(row);
                }mStatus.setText("状态: "+cnt+" 个设备");
            }});
        }});
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private TextView addText(LinearLayout r,String t,int c,int s){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(c);tv.setTextSize(s);tv.setPadding(0,dp(4),0,dp(4));r.addView(tv);return tv;}
    private void addSpace(LinearLayout r){View v=new View(mCtx);v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(4)));r.addView(v);}
    private EditText editText(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(13);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);et.setLayoutParams(lp);return et;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(13);b.setPadding(dp(10),dp(6),dp(10),dp(6));return b;}
    private Button quickBtn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(c);b.setBackgroundColor(0x00000000);b.setTextSize(10);b.setPadding(dp(6),dp(2),dp(6),dp(2));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
