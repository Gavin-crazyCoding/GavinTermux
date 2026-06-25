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

/** Git 可视化管理器 */
public class GitManagerDialog extends Dialog {
    private Context mCtx; private TermuxCommandHelper mCmd;
    private EditText mRepoUrl, mBranch, mCommitMsg, mDirPath;
    public GitManagerDialog(Context c){super(c);mCtx=c;mCmd=TermuxCommandHelper.getInstance(c);init();}
    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);setCancelable(true);
        ScrollView sv=new ScrollView(mCtx);LinearLayout r=new LinearLayout(mCtx);
        r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(0xFF161823);r.setPadding(dp(16),dp(16),dp(16),dp(16));
        addTitle(r,"Git 管理器");

        // 克隆仓库
        r.addView(label("仓库URL:"));
        mRepoUrl=editText("");mRepoUrl.setHint("https://github.com/user/repo.git");r.addView(mRepoUrl);
        r.addView(label("目标目录(可选):"));
        mDirPath=editText("");mDirPath.setHint("留空=自动使用仓库名");r.addView(mDirPath);
        Button clone=btn("克隆仓库",0xFF4CAF50);clone.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
            String url=mRepoUrl.getText().toString().trim(),dir=mDirPath.getText().toString().trim();
            if(url.isEmpty()){Toast.makeText(mCtx,"输入仓库URL",Toast.LENGTH_SHORT).show();return;}
            String cmd="pkg install git -y 2>/dev/null; git clone "+url+(dir.isEmpty()?"":" "+dir);
            mCmd.sendCommandToTerminal(cmd);Toast.makeText(mCtx,"正在克隆...",Toast.LENGTH_SHORT).show();dismiss();
        }});r.addView(clone);

        // 常用操作
        r.addView(label("分支:"));
        mBranch=editText("main");r.addView(mBranch);
        r.addView(label("提交信息:"));
        mCommitMsg=editText("update");r.addView(mCommitMsg);

        String[][] ops={{"git status","状态"},{"git pull","拉取"},{"git add .","暂存全部"},
            {"git add . && git commit -m ","提交"},{"git push","推送"},
            {"git log --oneline -10","日志"},{"git branch -a","分支列表"},
            {"git diff","差异"},{"git stash","暂存修改"},{"git stash pop","恢复暂存"}};

        LinearLayout grid=new LinearLayout(mCtx);grid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<ops.length;i+=2){LinearLayout row=new LinearLayout(mCtx);
            for(int j=0;j<2&&(i+j)<ops.length;j++){final String cmd=ops[i+j][0];final String label=ops[i+j][1];
                Button b=btn(label,0xFF37474F);b.setTextSize(11);b.setPadding(dp(6),dp(6),dp(6),dp(6));
                b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){
                    String fullCmd="cd ~/storage/shared 2>/dev/null; "+cmd;
                    if(cmd.endsWith("-m "))fullCmd+=mCommitMsg.getText().toString().trim();
                    mCmd.sendCommandToTerminal(fullCmd);Toast.makeText(mCtx,label+" 已发送",Toast.LENGTH_SHORT).show();
                }});
                LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);bp.rightMargin=j==0?dp(4):0;bp.bottomMargin=dp(4);b.setLayoutParams(bp);row.addView(b);
            }grid.addView(row);
        }r.addView(grid);
        sv.addView(r);setContentView(sv);
        Window w=getWindow();if(w!=null){w.setGravity(Gravity.BOTTOM);w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(mCtx.getResources().getDisplayMetrics().heightPixels*0.7));w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);else w.setType(WindowManager.LayoutParams.TYPE_PHONE);}
    }
    private void addTitle(LinearLayout r,String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFFFFFFF);tv.setTextSize(18);tv.setPadding(0,0,0,dp(12));r.addView(tv);}
    private TextView label(String t){TextView tv=new TextView(mCtx);tv.setText(t);tv.setTextColor(0xFFBBBBBB);tv.setTextSize(13);tv.setPadding(0,dp(10),0,dp(4));return tv;}
    private EditText editText(String d){EditText et=new EditText(mCtx);et.setText(d);et.setTextColor(0xFFFFFFFF);et.setHintTextColor(0xFF888888);et.setBackgroundColor(0x22FFFFFF);et.setPadding(dp(12),dp(8),dp(12),dp(8));et.setSingleLine(true);et.setTextSize(14);return et;}
    private Button btn(String t,int c){Button b=new Button(mCtx);b.setText(t);b.setTextColor(0xFFFFFFFF);b.setBackgroundColor(c);b.setTextSize(13);b.setPadding(dp(10),dp(6),dp(10),dp(6));return b;}
    private int dp(int v){return (int)(v*mCtx.getResources().getDisplayMetrics().density+0.5f);}
}
