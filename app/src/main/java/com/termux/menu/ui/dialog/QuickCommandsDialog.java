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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.termux.menu.termux.TermuxCommandHelper;
import java.util.ArrayList;
import java.util.List;

/** 快捷命令 — 保存/运行常用命令 */
public class QuickCommandsDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd; private LinearLayout mList; private EditText mName, mCmdEdit;
    private SharedPreferences mPrefs; private List<String[]> mCmds = new ArrayList<>();

    public QuickCommandsDialog(Context c){super(c);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);mPrefs=mCtx.getSharedPreferences("gavinfloat_cmds",Context.MODE_PRIVATE);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        TextView t=new TextView(mCtx);t.setText("快捷命令");t.setTextColor(0xFFFFFFFF);t.setTextSize(18);t.setPadding(0,0,0,dp(8));r.addView(t);

        // 添加区域
        mName=editText("");mName.setHint("命令名称");
        LinearLayout row1=new LinearLayout(mCtx);row1.addView(mName);r.addView(row1);
        mCmdEdit=editText("");mCmdEdit.setHint("Shell命令");
        LinearLayout row2=new LinearLayout(mCtx);row2.addView(mCmdEdit);
        Button add=new Button(mCtx);add.setText("添加");add.setTextColor(0xFF4CAF50);add.setBackgroundColor(0xFF37474F);add.setTextSize(12);
        add.setOnClickListener(new View.OnClickListener(){public void onClick(View v){saveCmd();}});row2.addView(add);r.addView(row2);

        mList=new LinearLayout(mCtx);mList.setOrientation(LinearLayout.VERTICAL);r.addView(mList);
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.6));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
        loadCmds();
    }
    private void loadCmds(){
        mCmds.clear();mList.removeAllViews();
        int count=mPrefs.getInt("count",0);
        for(int i=0;i<count;i++){final String name=mPrefs.getString("name_"+i,"");final String cmd=mPrefs.getString("cmd_"+i,"");if(name.isEmpty())continue;
            mCmds.add(new String[]{name,cmd});
            LinearLayout row=new LinearLayout(mCtx);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(6),dp(4),dp(6));
            TextView nt=new TextView(mCtx);nt.setText(name);nt.setTextColor(0xFFFFFFFF);nt.setTextSize(13);nt.setMaxLines(1);
            LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);nt.setLayoutParams(np);row.addView(nt);
            Button run=new Button(mCtx);run.setText("运行");run.setTextColor(0xFF4CAF50);run.setBackgroundColor(0x00000000);run.setTextSize(11);run.setPadding(dp(8),dp(2),dp(8),dp(2));
            final int idx=i;run.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,"已运行: "+name,Toast.LENGTH_SHORT).show();dismiss();
            }});row.addView(run);
            Button del=new Button(mCtx);del.setText("删");del.setTextColor(0xFFF44336);del.setBackgroundColor(0x00000000);del.setTextSize(11);del.setPadding(dp(8),dp(2),dp(8),dp(2));
            del.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                mPrefs.edit().remove("name_"+idx).remove("cmd_"+idx).putInt("count",count).apply();loadCmds();
            }});row.addView(del);
            mList.addView(row);
        }
        if(mCmds.isEmpty()){TextView e=new TextView(mCtx);e.setText("暂无快捷命令，添加一个吧");e.setTextColor(0xFF888888);e.setTextSize(13);e.setPadding(0,dp(12),0,0);mList.addView(e);}
    }
    private void saveCmd(){
        String name=mName.getText().toString().trim();String cmd=mCmdEdit.getText().toString().trim();
        if(name.isEmpty()||cmd.isEmpty()){Toast.makeText(mCtx,"名称和命令不能为空",Toast.LENGTH_SHORT).show();return;}
        int count=mPrefs.getInt("count",0);mPrefs.edit().putString("name_"+count,name).putString("cmd_"+count,cmd).putInt("count",count+1).apply();
        mName.setText("");mCmdEdit.setText("");loadCmds();
    }
    private EditText editText(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);et.setLayoutParams(lp);return et;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
