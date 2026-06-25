package com.termux.menu.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IconProvider {

    public static Drawable getIcon(String name) {
        if (name == null) return createTextIcon("?", 0xFF888888);

        if (contains(name, "切换源", "change", "repo")) return createTextIcon("源", 0xFF4CAF50);
        if (contains(name, "容器", "container")) return createTextIcon("容", 0xFF9C27B0);
        if (contains(name, "备份", "恢复", "backup")) return createTextIcon("备", 0xFFFF9800);
        if (contains(name, "MOE", "moe")) return createTextIcon("M", 0xFFE91E63);
        if (contains(name, "发行版", "linux", "distro")) return createTextIcon("L", 0xFF795548);
        if (contains(name, "QEMU", "qemu")) return createTextIcon("Q", 0xFF607D8B);
        if (contains(name, "设置", "settings")) return createTextIcon("⚙", 0xFF48BAF3);
        if (contains(name, "编辑器", "editor", "编辑")) return createTextIcon("✎", 0xFFFF5722);
        if (contains(name, "C项目")) return createTextIcon("C", 0xFF2196F3);
        if (contains(name, "Java")) return createTextIcon("J", 0xFFFF5722);
        if (contains(name, "Python")) return createTextIcon("Py", 0xFF4CAF50);
        if (contains(name, "PHP")) return createTextIcon("PHP", 0xFF9C27B0);
        if (contains(name, "NPM", "npm", "node")) return createTextIcon("N", 0xFF4CAF50);
        if (contains(name, "Shell", "shell", "脚本")) return createTextIcon("Sh", 0xFF795548);
        if (contains(name, "编译", "compile", "build")) return createTextIcon("⚒", 0xFFFF9800);
        if (contains(name, "运行", "run")) return createTextIcon("▶", 0xFF4CAF50);
        if (contains(name, "项目", "project")) return createTextIcon("📁", 0xFF2196F3);
        if (contains(name, "X11")) return createTextIcon("X", 0xFFFF5722);
        if (contains(name, "终端", "terminal", "显示终端")) return createTextIcon("▶", 0xFF4CAF50);
        if (contains(name, "隐藏")) return createTextIcon("◼", 0xFF757575);
        if (contains(name, "环境", "env")) return createTextIcon("环", 0xFFFF9800);
        if (contains(name, "修复", "fix")) return createTextIcon("🔧", 0xFFFF5722);
        if (contains(name, "安装", "install")) return createTextIcon("⬇", 0xFF4CAF50);
        if (contains(name, "键盘", "keyboard")) return createTextIcon("⌨", 0xFF607D8B);
        if (contains(name, "VNC", "vnc")) return createTextIcon("V", 0xFF9C27B0);
        if (contains(name, "悬浮", "float")) return createTextIcon("浮", 0xFF48BAF3);
        if (contains(name, "美化", "beauty", "theme")) return createTextIcon("🎨", 0xFFE91E63);
        if (contains(name, "字体", "font")) return createTextIcon("A", 0xFFFF9800);
        if (contains(name, "全屏", "fullscreen")) return createTextIcon("⛶", 0xFF2196F3);
        if (contains(name, "雪花", "snow")) return createTextIcon("❄", 0xFF48BAF3);
        if (contains(name, "粒子", "particle")) return createTextIcon("✦", 0xFFFF9800);
        if (contains(name, "清除", "clear")) return createTextIcon("✕", 0xFFF44336);
        if (contains(name, "网页", "web", "wiki")) return createTextIcon("🌐", 0xFF2196F3);
        if (contains(name, "密钥", "key", "ssh")) return createTextIcon("🔑", 0xFFFF9800);
        if (contains(name, "文件", "file", "浏览")) return createTextIcon("📂", 0xFF4CAF50);
        if (contains(name, "X86", "alpine")) return createTextIcon("x86", 0xFF607D8B);
        if (contains(name, "网络ADB", "adb")) return createTextIcon("ADB", 0xFF4CAF50);
        if (contains(name, "关闭", "close")) return createTextIcon("✕", 0xFFF44336);
        if (contains(name, "Docker", "docker")) return createTextIcon("D", 0xFF2196F3);
        if (contains(name, "在线", "online")) return createTextIcon("☁", 0xFF48BAF3);
        if (contains(name, "论坛", "bbs", "forum")) return createTextIcon("论", 0xFF9C27B0);
        if (contains(name, "下载", "download")) return createTextIcon("⬇", 0xFF4CAF50);
        if (contains(name, "仓库", "repo", "release")) return createTextIcon("仓", 0xFF795548);
        if (contains(name, "模块", "module")) return createTextIcon("模", 0xFF48BAF3);
        if (contains(name, "FTP", "ftp")) return createTextIcon("FTP", 0xFFFF9800);
        if (contains(name, "软链", "link")) return createTextIcon("🔗", 0xFF2196F3);
        if (contains(name, "卸载", "uninstall")) return createTextIcon("✕", 0xFFF44336);
        if (contains(name, "远程", "remote", "ssh")) return createTextIcon("远", 0xFF4CAF50);
        if (contains(name, "短信", "sms")) return createTextIcon("✉", 0xFFFF5722);
        if (contains(name, "定时", "cron", "task")) return createTextIcon("⏰", 0xFFFF9800);
        if (contains(name, "路径", "path", "打开路径")) return createTextIcon("📍", 0xFF795548);
        if (contains(name, "数据", "data", "信息")) return createTextIcon("📊", 0xFF2196F3);
        if (contains(name, "语言", "language")) return createTextIcon("语", 0xFF9C27B0);
        if (contains(name, "开源", "github", "GitHub")) return createTextIcon("G", 0xFF24292E);
        if (contains(name, "更新", "update", "upgrade")) return createTextIcon("↑", 0xFF4CAF50);
        if (contains(name, "升级", "upgrade")) return createTextIcon("⬆", 0xFF4CAF50);
        if (contains(name, "清理", "clean")) return createTextIcon("🗑", 0xFFFF5722);
        if (contains(name, "进程", "process")) return createTextIcon("⚡", 0xFFFF9800);
        if (contains(name, "磁盘", "disk")) return createTextIcon("💾", 0xFF607D8B);
        if (contains(name, "内存", "memory", "mem")) return createTextIcon("M", 0xFF2196F3);
        if (contains(name, "系统", "system", "sys")) return createTextIcon("⚙", 0xFF607D8B);
        if (contains(name, "IP", "网络", "network", "ping")) return createTextIcon("🌐", 0xFF48BAF3);
        if (contains(name, "已安装", "installed", "包")) return createTextIcon("📦", 0xFF4CAF50);
        if (contains(name, "CPU", "cpu")) return createTextIcon("⚡", 0xFFFF5722);
        if (contains(name, "变量", "env", "环境变量")) return createTextIcon("$", 0xFF795548);
        if (contains(name, "Bash", "bash")) return createTextIcon("$", 0xFF4CAF50);
        if (contains(name, "命令", "command")) return createTextIcon(">", 0xFFFF9800);
        if (contains(name, "启动", "boot", "开机")) return createTextIcon("⏻", 0xFF4CAF50);

        return createTextIcon(name.length() > 0 ? name.substring(0, 1) : "?", 0xFF888888);
    }

    private static boolean contains(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    public static Drawable createTextIcon(String text, int bgColor) {
        return new TextIconDrawable(text, bgColor);
    }

    private static class TextIconDrawable extends Drawable {
        private final String mText;
        private final Paint mBgPaint;
        private final Paint mTextPaint;

        TextIconDrawable(String text, int bgColor) {
            mText = text;
            mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBgPaint.setColor(bgColor);
            mBgPaint.setStyle(Paint.Style.FILL);
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setColor(0xFFFFFFFF);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            float cx = bounds.exactCenterX();
            float cy = bounds.exactCenterY();
            float radius = Math.min(bounds.width(), bounds.height()) / 2f - 2;
            canvas.drawCircle(cx, cy, radius, mBgPaint);
            mTextPaint.setTextSize(radius * (mText.length() > 2 ? 0.55f : mText.length() > 1 ? 0.65f : 0.85f));
            Paint.FontMetrics fm = mTextPaint.getFontMetrics();
            float textY = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(mText, cx, textY, mTextPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mBgPaint.setAlpha(alpha);
            mTextPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {}

        @Override
        public int getOpacity() { return PixelFormat.TRANSLUCENT; }

        @Override
        public int getIntrinsicWidth() { return 80; }

        @Override
        public int getIntrinsicHeight() { return 80; }
    }
}
