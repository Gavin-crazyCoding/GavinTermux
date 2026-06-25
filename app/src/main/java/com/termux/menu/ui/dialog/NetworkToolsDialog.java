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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.menu.termux.TermuxCommandHelper;

/** 网络工具箱 — ping/traceroute/nslookup/curl */
public class NetworkToolsDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private EditText mTarget;
    public NetworkToolsDialog(Context c){super(c);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(20),dp(16),dp(20),dp(16));
        TextView t=new TextView(mCtx);t.setText("网络工具");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setPadding(0,0,0,dp(12));r.addView(t);
        r.addView(label("目标地址/IP:"));
        mTarget=editText("baidu.com");r.addView(mTarget);

        String[][] tools={{"Ping(4次)","ping -c 4 {}"},{"Ping(持续)","ping {}"},{"Traceroute","traceroute {}"},
            {"NSLookup","nslookup {}"},{"Curl(GET)","curl -sI {}"},{"Curl(完整)","curl -s {}"},
            {"Whois","whois {} 2>/dev/null || pkg install whois -y"},{"DNS查所有","dig {} ANY 2>/dev/null || echo 需安装dnsutils"}};

        LinearLayout grid=new LinearLayout(mCtx);grid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<tools.length;i+=2){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<2&&(i+j)<tools.length;j++){final String label=tools[i+j][0];final String cmd=tools[i+j][1];
                Button b=new Button(mCtx);b.setText(label);b.setTextColor(0xFFFFFFFF);b.setTextSize(12);b.setBackgroundColor(0xFF37474F);b.setPadding(dp(8),dp(8),dp(8),dp(8));
                b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                    String target=mTarget.getText().toString().trim();
                    if(target.isEmpty()){Toast.makeText(mCtx,"请输入目标地址",Toast.LENGTH_SHORT).show();return;}
                    mCmd.sendCommandToTerminal(cmd.replace("{}",target));
                    Toast.makeText(mCtx,"已发送: "+label,Toast.LENGTH_SHORT).show();dismiss();
                }});
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);bp.rightMargin=j==0?dp(4):0;bp.bottomMargin=dp(4);b.setLayoutParams(bp);row.addView(b);
            }grid.addView(row);
        }r.addView(grid);
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.65));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private EditText editText(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
