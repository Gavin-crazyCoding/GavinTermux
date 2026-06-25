package com.termux.menu.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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

import com.termux.menu.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码编辑器弹窗 — 支持语法高亮、保存、多语言。
 * 纯 Java，AIDE 兼容。
 */
public class CodeEditDialog extends Dialog {

    // 语法高亮正则模式
    private static final Pattern[] HIGHLIGHT_PATTERNS;
    private static final int[] HIGHLIGHT_COLORS;
    static {
        HIGHLIGHT_PATTERNS = new Pattern[] {
            // 关键字 (蓝色)
            Pattern.compile("\\b(public|class|static|void|int|String|boolean|float|double|" +
                "if|else|for|while|return|new|try|catch|throw|import|package|" +
                "def|print|lambda|None|True|False|and|or|not|in|is|" +
                "function|var|let|const|console|require|module|exports|" +
                "echo|exit|source|alias|export|local|read|set|grep|awk|sed|" +
                "def|end|begin|rescue|ensure|module)\\b"),
            // 字符串 (绿色)
            Pattern.compile("\"[^\"]*\"|'[^']*'"),
            // 注释 (灰色)
            Pattern.compile("//[^\n]*|#[^\n]*"),
            // 数字 (橙色)
            Pattern.compile("\\b\\d+\\.?\\d*\\b"),
            // 注解/装饰器 (紫色)
            Pattern.compile("@\\w+"),
        };
        HIGHLIGHT_COLORS = new int[] {
            0xFF569CD6, // 关键字 蓝
            0xFF6A9955, // 字符串 绿
            0xFF808080, // 注释 灰
            0xFFCE9178, // 数字 橙
            0xFFC586C0, // 注解 紫
        };
    }

    private Context mContext;
    private EditText mEditor;
    private TextView mTitleText;
    private String mFilePath;
    private String mOriginalContent;
    private boolean mDirty = false;

    public CodeEditDialog(Context context, String filePath) {
        super(context);
        mContext = context;
        mFilePath = filePath;
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);

        LinearLayout root = new LinearLayout(mContext);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1E1E1E);

        // 标题栏
        LinearLayout header = new LinearLayout(mContext);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(0xFF2D2D30);
        header.setPadding(dp(12), dp(8), dp(12), dp(8));

        mTitleText = new TextView(mContext);
        mTitleText.setText(new File(mFilePath).getName());
        mTitleText.setTextColor(0xFFFFFFFF);
        mTitleText.setTextSize(14);
        mTitleText.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        header.addView(mTitleText);

        // 关闭按钮
        TextView closeBtn = new TextView(mContext);
        closeBtn.setText("✕");
        closeBtn.setTextColor(0xFF888888);
        closeBtn.setTextSize(18);
        closeBtn.setPadding(dp(16), 0, 0, 0);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { confirmClose(); }
        });
        header.addView(closeBtn);
        root.addView(header);

        // 信息行
        TextView info = new TextView(mContext);
        info.setText(mFilePath);
        info.setTextColor(0xFF888888);
        info.setTextSize(10);
        info.setPadding(dp(12), dp(4), dp(12), dp(4));
        root.addView(info);

        // 编辑器
        ScrollView scroll = new ScrollView(mContext);
        scroll.setFillViewport(true);

        mEditor = new EditText(mContext);
        mEditor.setBackgroundColor(0xFF1E1E1E);
        mEditor.setTextColor(0xFFD4D4D4);
        mEditor.setHintTextColor(0xFF555555);
        mEditor.setTypeface(Typeface.MONOSPACE);
        mEditor.setTextSize(13);
        mEditor.setPadding(dp(12), dp(8), dp(12), dp(8));
        mEditor.setGravity(Gravity.TOP | Gravity.START);
        mEditor.setHorizontallyScrolling(true);
        mEditor.setVerticalScrollBarEnabled(true);

        // 语法高亮 TextWatcher（延迟250ms批量着色，避免卡顿）
        mEditor.addTextChangedListener(new TextWatcher() {
            private android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
            private Runnable highlightTask;
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {}
            public void afterTextChanged(Editable s) {
                if (highlightTask != null) h.removeCallbacks(highlightTask);
                highlightTask = new Runnable() { public void run() {
                    applyHighlighting(s);
                }};
                h.postDelayed(highlightTask, 250);
            }
        });
        scroll.addView(mEditor);
        root.addView(scroll);

        // 底部按钮栏
        LinearLayout footer = new LinearLayout(mContext);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setBackgroundColor(0xFF2D2D30);
        footer.setPadding(dp(8), dp(6), dp(8), dp(6));
        footer.setGravity(Gravity.CENTER_VERTICAL);

        // 语言标签
        TextView lang = new TextView(mContext);
        lang.setText(detectLanguage(mFilePath));
        lang.setTextColor(0xFF569CD6);
        lang.setTextSize(11);
        lang.setPadding(0, 0, dp(12), 0);
        footer.addView(lang);

        Button saveBtn = new Button(mContext);
        saveBtn.setText("保存");
        saveBtn.setTextColor(0xFFFFFFFF);
        saveBtn.setBackgroundColor(0xFF0E639C);
        saveBtn.setTextSize(12);
        saveBtn.setPadding(dp(16), dp(4), dp(16), dp(4));
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { saveFile(); }
        });
        footer.addView(saveBtn);
        root.addView(footer);

        setContentView(root);
        setupWindow();
        loadFile();
    }

    private void loadFile() {
        try {
            String content = FileUtils.readFile(mFilePath);
            if (content == null) content = "";
            mOriginalContent = content;
            mEditor.setText(content);
            mDirty = false;
            updateTitle();
        } catch (Exception e) {
            mEditor.setText("无法读取文件: " + e.getMessage());
        }
    }

    private void saveFile() {
        String content = mEditor.getText().toString();
        try {
            File f = new File(mFilePath);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            FileWriter fw = new FileWriter(f);
            fw.write(content);
            fw.close();
            mOriginalContent = content;
            mDirty = false;
            updateTitle();
            Toast.makeText(mContext, "已保存: " + f.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmClose() {
        if (mDirty) {
            Toast.makeText(mContext, "有未保存的更改，请先保存", Toast.LENGTH_SHORT).show();
        } else {
            dismiss();
        }
    }

    private void updateTitle() {
        String name = new File(mFilePath).getName();
        mTitleText.setText((mDirty ? "● " : "") + name);
    }

    private String detectLanguage(String path) {
        if (path == null) return "text";
        String p = path.toLowerCase();
        if (p.endsWith(".java")) return "Java";
        if (p.endsWith(".kt")) return "Kotlin";
        if (p.endsWith(".py")) return "Python";
        if (p.endsWith(".js")) return "JavaScript";
        if (p.endsWith(".ts")) return "TypeScript";
        if (p.endsWith(".c") || p.endsWith(".h")) return "C";
        if (p.endsWith(".cpp") || p.endsWith(".hpp")) return "C++";
        if (p.endsWith(".sh") || p.endsWith(".bash")) return "Shell";
        if (p.endsWith(".xml")) return "XML";
        if (p.endsWith(".html")) return "HTML";
        if (p.endsWith(".css")) return "CSS";
        if (p.endsWith(".json")) return "JSON";
        if (p.endsWith(".md")) return "Markdown";
        if (p.endsWith(".php")) return "PHP";
        if (p.endsWith(".rb")) return "Ruby";
        if (p.endsWith(".go")) return "Go";
        if (p.endsWith(".rs")) return "Rust";
        if (p.endsWith(".yaml") || p.endsWith(".yml")) return "YAML";
        if (p.endsWith(".sql")) return "SQL";
        if (p.endsWith(".lua")) return "Lua";
        return "text";
    }

    private void applyHighlighting(Editable editable) {
        // 只在文本不太长时做高亮（>10000字符跳过，避免卡顿）
        if (editable.length() > 10000) return;
        // 清除旧的颜色span
        ForegroundColorSpan[] old = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan s : old) editable.removeSpan(s);

        String text = editable.toString();
        for (int p = 0; p < HIGHLIGHT_PATTERNS.length; p++) {
            Matcher m = HIGHLIGHT_PATTERNS[p].matcher(text);
            while (m.find()) {
                editable.setSpan(new ForegroundColorSpan(HIGHLIGHT_COLORS[p]),
                    m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void setupWindow() {
        Window w = getWindow();
        if (w != null) {
            w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                w.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else w.setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
    }

    private int dp(int v) { return (int)(v * mContext.getResources().getDisplayMetrics().density + 0.5f); }
}
