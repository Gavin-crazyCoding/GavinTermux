package com.termux.menu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.model.MenuCategoryData;
import com.termux.menu.model.MenuEntryData;
import com.termux.menu.model.XmlMenuGroup;
import com.termux.menu.model.XmlMenuItem;
import com.termux.menu.termux.TermuxCommandHelper;
import com.termux.menu.ui.EditorController;
import com.termux.menu.ui.FileBrowserController;
import com.termux.menu.ui.FluidRippleDrawable;
import com.termux.menu.ui.IconProvider;
import com.termux.menu.ui.MenuCategoryAdapter;
import com.termux.menu.ui.PageController;
import com.termux.menu.ui.SettingsController;
import com.termux.menu.ui.TerminalOutputController;
import com.termux.menu.ui.dialog.ApiConfigDialog;
import com.termux.menu.ui.dialog.CamphishDialog;
import com.termux.menu.ui.dialog.CodeEditDialog;
import com.termux.menu.ui.dialog.DirbDialog;
import com.termux.menu.ui.dialog.DownloadCenterDialog;
import com.termux.menu.ui.dialog.KaliToolsDialog;
import com.termux.menu.ui.dialog.MetasploitDialog;
import com.termux.menu.ui.dialog.NmapDialog;
import com.termux.menu.ui.dialog.OnlineScriptDialog;
import com.termux.menu.ui.dialog.AdbManagerDialog;
import com.termux.menu.ui.dialog.AIChatDialog;
import com.termux.menu.ui.dialog.GitManagerDialog;
import com.termux.menu.ui.dialog.NetworkToolsDialog;
import com.termux.menu.ui.dialog.SystemDashboardDialog;
import com.termux.menu.ui.dialog.PackageManagerDialog;
import com.termux.menu.ui.dialog.ProcessManagerDialog;
import com.termux.menu.ui.dialog.QuickCommandsDialog;
import com.termux.menu.ui.dialog.SeekerDialog;
import com.termux.menu.ui.dialog.SqlmapDialog;
import com.termux.menu.ui.dialog.WiFiToolsDialog;
import com.termux.menu.utils.FileUtils;
import com.termux.menu.utils.PrefsManager;
import com.termux.menu.xml.XmlMenuParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FloatingBallService extends Service {
    private static final String TAG = "GavinFloat";
    private static final String CHANNEL_ID = "gavinfloat_channel";
    private static final String EXTERNAL_MENU_PATH =
        FileUtils.TERMUX_HOME + "/ZtInfo/main_menu_path.xml";

    private WindowManager mWindowManager;
    private View mBallView;
    private View mMenuView;
    private WindowManager.LayoutParams mBallParams;
    private WindowManager.LayoutParams mMenuParams;
    private boolean mMenuShowing = false;

    private TermuxCommandHelper mCmdHelper;
    private PrefsManager mPrefs;
    private MenuCategoryAdapter mCategoryAdapter;
    private PageController mPageController;
    private EditorController mEditorController;
    private FileBrowserController mFileBrowserController;
    private TerminalOutputController mTerminalController;
    private SettingsController mSettingsController;

    // ---- 卡片视图引用 ----
    private TextView mServiceStatus;
    private View mInfoCard;
    private ImageView mIpExpandIcon;
    private TextView mIpStatus;
    private View mMenuPackageCard;
    private TextView mMenuPackageCurrent;
    private ImageView mMenuPackageExpand;
    private RecyclerView mMenuPackageList;
    private View mDataInfoCard;
    private ImageView mDataExpandIcon;
    private TextView mDataInfoContent;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mCmdHelper = TermuxCommandHelper.getInstance(this);
        mPrefs = new PrefsManager(this);

        createNotificationChannel();
        startForeground(1, buildNotification());
        createBall();
        createMenuPanel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideMenu();
        if (mBallView != null && mBallView.isAttachedToWindow()) {
            mWindowManager.removeView(mBallView);
        }
        if (mCategoryAdapter != null) mCategoryAdapter.release();
    }

    // ======== 通知 ========

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                "GavinFloat", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("悬浮菜单服务");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        Intent ni = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, ni,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent exitIntent = new Intent(this, ExitReceiver.class);
        exitIntent.setAction(ExitReceiver.ACTION_EXIT);
        PendingIntent exitPi = PendingIntent.getBroadcast(this, 1, exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GavinFloat")
            .setContentText("悬浮菜单运行中")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "退出", exitPi)
            .setOngoing(true)
            .build();
    }

    // ======== WindowManager 悬浮层 ========

    private int getOverlayType() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void createBall() {
        mBallView = new View(this);
        int size = dp(mPrefs.getBallSize());
        GradientDrawable ballBg = new GradientDrawable();
        ballBg.setShape(GradientDrawable.OVAL);
        // 奢华金黑渐变球
        ballBg.setColors(new int[]{0xFF2C1810, 0xFF0D0221});
        ballBg.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        ballBg.setGradientRadius(size / 2f);
        ballBg.setStroke(dp(1), 0x80D4AF37);
        mBallView.setBackground(ballBg);
        mBallView.setElevation(dp(4));
        // 流体波纹叠加（API 23+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBallView.setForeground(new FluidRippleDrawable(0xFFD4AF37));
        }

        mBallParams = new WindowManager.LayoutParams(
            size, size, getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        mBallParams.gravity = Gravity.TOP | Gravity.START;
        mBallParams.x = mPrefs.getBallX();
        mBallParams.y = dp(mPrefs.getBallY());

        mBallView.setOnTouchListener(new BallTouchListener());
        mWindowManager.addView(mBallView, mBallParams);
    }

    private void createMenuPanel() {
        mMenuView = LayoutInflater.from(this).inflate(R.layout.panel_floating, null);

        // 应用已保存的主题色到面板
        applySavedTheme(mMenuView);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int menuWidth = (int) (screenWidth * (mPrefs.getMenuWidth() / 100f));

        mMenuParams = new WindowManager.LayoutParams(
            menuWidth, WindowManager.LayoutParams.MATCH_PARENT,
            getOverlayType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
        mMenuParams.gravity = Gravity.TOP | Gravity.START;
        mMenuParams.x = 0;
        mMenuParams.y = 0;

        initControllers();
        findCardViews();
        setupCardListeners();
        loadAndShowMenu();
        updateStatusIndicators();

    }

    private void initControllers() {
        mPageController = new PageController(mMenuView, page -> {
            if (page == PageController.PAGE_MENU) {
                updateStatusIndicators();
            }
        });
        mPageController.setDefaultTitle("GavinFloat");

        mEditorController = new EditorController(mMenuView, mPageController);
        mFileBrowserController = new FileBrowserController(mMenuView, mPageController, mEditorController);
        mTerminalController = new TerminalOutputController(mMenuView, mCmdHelper, mPageController);
        mSettingsController = new SettingsController(mMenuView, this, mPrefs,
            mPageController, () -> {
                hideMenu();
                recreateBallAndMenu();
            });
    }

    /**
     * 查找所有卡片视图引用，对齐 ZeroTermux 左侧栏结构。
     */
    private void findCardViews() {
        // 品牌卡片
        mServiceStatus = mMenuView.findViewById(R.id.service_status);

        // IP 卡片
        mIpStatus = mMenuView.findViewById(R.id.ip_status);
        mIpExpandIcon = mMenuView.findViewById(R.id.ip_expand_icon);
        mInfoCard = mMenuView.findViewById(R.id.info_card);

        // 菜单包卡片
        mMenuPackageCard = mMenuView.findViewById(R.id.menu_package_card);
        mMenuPackageCurrent = mMenuView.findViewById(R.id.menu_package_current);
        mMenuPackageExpand = mMenuView.findViewById(R.id.menu_package_expand);
        mMenuPackageList = mMenuView.findViewById(R.id.menu_package_list);

        // 数据卡片
        mDataInfoCard = mMenuView.findViewById(R.id.data_info_card);
        mDataExpandIcon = mMenuView.findViewById(R.id.data_expand_icon);
        mDataInfoContent = mMenuView.findViewById(R.id.data_info_content);
    }

    /**
     * 绑定所有卡片交互事件。
     */
    private void setupCardListeners() {
        // 设置按钮 → 打开设置页
        View settingsBtn = mMenuView.findViewById(R.id.open_settings_btn);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> mSettingsController.show());
        }

        // IP 卡片 → 展开/收起 IP 地址 + INFO 卡片
        View ipCard = mMenuView.findViewById(R.id.ip_card);
        if (ipCard != null) {
            ipCard.setOnClickListener(v -> {
                boolean showing = (mIpStatus != null && mIpStatus.getVisibility() == View.VISIBLE);
                // 切换 IP 状态文字
                if (mIpStatus != null) {
                    mIpStatus.setVisibility(showing ? View.GONE : View.VISIBLE);
                }
                // 切换 INFO 卡片
                if (mInfoCard != null) {
                    mInfoCard.setVisibility(showing ? View.GONE : View.VISIBLE);
                }
                // 旋转箭头
                if (mIpExpandIcon != null) {
                    mIpExpandIcon.setRotation(showing ? 0 : 180);
                }
                // 首次展开时加载 IP
                if (!showing) {
                    loadIpAddress();
                }
            });
        }

        // INFO 卡片内 QQ群/TG群 点击
        View qqGroup = mMenuView.findViewById(R.id.qq_group_tv);
        if (qqGroup != null) {
            qqGroup.setOnClickListener(v -> Toast.makeText(this,
                "QQ群功能暂未开放", Toast.LENGTH_SHORT).show());
        }
        View tgGroup = mMenuView.findViewById(R.id.telegram_group_tv);
        if (tgGroup != null) {
            tgGroup.setOnClickListener(v -> Toast.makeText(this,
                "Telegram群功能暂未开放", Toast.LENGTH_SHORT).show());
        }

        // 菜单包卡片 → 展开/收起菜单包列表
        if (mMenuPackageCard != null) {
            mMenuPackageCard.setOnClickListener(v -> {
                boolean showing = mMenuPackageList.getVisibility() == View.VISIBLE;
                mMenuPackageList.setVisibility(showing ? View.GONE : View.VISIBLE);
                if (mMenuPackageExpand != null) {
                    mMenuPackageExpand.setRotation(showing ? 0 : 180);
                }
                if (!showing) {
                    loadMenuPackageList();
                }
            });
        }

        // 数据信息卡片 → 展开/收起详情
        View dataCard = mMenuView.findViewById(R.id.data_card);
        if (dataCard != null) {
            dataCard.setOnClickListener(v -> {
                boolean showing = mDataInfoCard.getVisibility() == View.VISIBLE;
                mDataInfoCard.setVisibility(showing ? View.GONE : View.VISIBLE);
                if (mDataExpandIcon != null) {
                    mDataExpandIcon.setRotation(showing ? 0 : 180);
                }
                if (!showing) {
                    loadDataInfo();
                }
            });
        }
    }

    private void recreateBallAndMenu() {
        if (mBallView != null && mBallView.isAttachedToWindow()) {
            mWindowManager.removeView(mBallView);
        }
        if (mMenuShowing) {
            try { mWindowManager.removeView(mMenuView); } catch (Exception ignored) {}
            mMenuShowing = false;
        }
        createBall();
        createMenuPanel();
    }

    // ======== 菜单加载 ========

    private void loadAndShowMenu() {
        RecyclerView menuList = mMenuView.findViewById(R.id.menu_list);
        if (menuList == null) return;
        menuList.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<MenuCategoryData> categories = loadMenuData();
        if (mCategoryAdapter != null) mCategoryAdapter.release();
        mCategoryAdapter = new MenuCategoryAdapter(this, categories);
        menuList.setAdapter(mCategoryAdapter);

        TextView errorMsg = mMenuView.findViewById(R.id.menu_error);
        if (errorMsg != null) {
            if (categories.isEmpty()) {
                errorMsg.setVisibility(View.VISIBLE);
                errorMsg.setText("菜单为空 — 检查 default_menu.xml 或外部配置");
            } else {
                errorMsg.setVisibility(View.GONE);
            }
        }
    }

    private void updateStatusIndicators() {
        boolean installed = mCmdHelper.isTermuxInstalled();
        if (mServiceStatus != null) {
            mServiceStatus.setText(installed ? "已连接" : "未连接");
            mServiceStatus.setTextColor(installed ? 0xFF4CAF50 : 0xFFF44336);
        }
    }

    // ======== 菜单数据源 ========

    private ArrayList<MenuCategoryData> loadMenuData() {
        List<XmlMenuGroup> groups = null;

        // 1) assets 内置菜单（优先，确保新版菜单总是加载）
        try {
            InputStream is = getAssets().open("default_menu.xml");
            groups = XmlMenuParser.parseFromStream(is);
            is.close();
            Log.i(TAG, "Loaded assets default menu: " + (groups != null ? groups.size() : 0) + " groups");
            if (mMenuPackageCurrent != null) {
                mMenuPackageCurrent.setText("当前: 内置菜单");
            }
        } catch (Exception e) {
            Log.e(TAG, "assets menu error: " + e.getMessage());
        }

        // 2) 外部 XML 配置覆盖（仅当用户手动安装时）
        File extFile = new File(EXTERNAL_MENU_PATH);
        if (extFile.exists() && extFile.length() > 0) {
            List<XmlMenuGroup> extGroups = XmlMenuParser.parseFromFile(extFile);
            if (extGroups != null && !extGroups.isEmpty()) {
                groups = extGroups;
                Log.i(TAG, "External XML overrides: " + extGroups.size() + " groups");
                if (mMenuPackageCurrent != null) {
                    mMenuPackageCurrent.setText("当前: 外部配置菜单");
                }
            }
        }

        // 3) 硬编码兜底
        if (groups == null || groups.isEmpty()) {
            groups = buildFallbackMenu();
            Log.i(TAG, "Using hardcoded fallback");
            if (mMenuPackageCurrent != null) {
                mMenuPackageCurrent.setText("当前: 兜底菜单");
            }
        }

        ArrayList<MenuCategoryData> categories = new ArrayList<>();
        for (XmlMenuGroup group : groups) {
            ArrayList<MenuEntryData> entries = new ArrayList<>();
            for (XmlMenuItem item : group.getItems()) {
                entries.add(buildEntry(item));
            }
            if (!entries.isEmpty()) {
                categories.add(new MenuCategoryData(group.getGroupName(), group.getId(), entries));
            }
        }
        return categories;
    }

    private MenuEntryData buildEntry(XmlMenuItem item) {
        return new MenuEntryData(item.getName(),
            resolveIcon(item.getName()),
            item,
            ctx -> handleClick(item));
    }

    /**
     * 图标解析：优先使用 ZeroTermux 原版 mipmap 图标，找不到再用 IconProvider 文字图标。
     * 1:1 对齐 ZeroTermux 菜单的图标风格。
     */
    private android.graphics.drawable.Drawable resolveIcon(String name) {
        if (name == null) return IconProvider.getIcon(name);
        int resId = mapIconResource(name);
        if (resId != 0) {
            try {
                return getResources().getDrawable(resId);
            } catch (Throwable ignored) {
                Log.w(TAG, "Icon not found for: " + name + " id=" + resId);
            }
        }
        return IconProvider.getIcon(name);
    }

    /**
     * 菜单项名称 → mipmap/drawable 资源 ID 映射。
     * 对齐 ZeroTermux 各 ClickConfig.getIcon() 的返回值。
     */
    private int mapIconResource(String name) {
        if (name == null) return 0;
        // 常用功能
        if (name.equals("切换源")) return R.mipmap.qinghua_ico;
        if (name.equals("容器切换")) return R.mipmap.rongqi_ico;
        if (name.contains("备份")) return R.mipmap.beifen_ico;
        if (name.contains("MOE")) return R.mipmap.moe_ico;
        if (name.contains("发行版") || name.contains("Linux")) return R.mipmap.linux_ico;
        if (name.equals("QEMU")) return R.mipmap.qemu_ico;
        if (name.equals("定时任务")) return R.mipmap.timer;
        if (name.equals("Gavin设置") || name.equals("设置")) return R.mipmap.settings;
        // 创建项目
        if (name.contains("C项目")) return R.drawable.ic_project_c;
        if (name.contains("Java项目")) return R.drawable.ic_project_java;
        if (name.contains("Python项目")) return R.drawable.ic_project_python;
        if (name.contains("PHP项目")) return R.drawable.ic_project_php;
        if (name.contains("NPM项目")) return R.drawable.ic_project_npm;
        if (name.contains("打开项目")) return R.drawable.ic_project_open;
        // X11功能
        if (name.equals("X11设置")) return R.mipmap.termux_x11;
        if (name.equals("显示终端")) return R.mipmap.show_command;
        if (name.equals("隐藏终端")) return R.mipmap.hide_command;
        if (name.contains("X11环境")) return R.mipmap.install_eg;
        if (name.contains("修复")) return R.mipmap.xiugai_ddd;
        if (name.contains("安装X11")) return R.mipmap.x11_so_install;
        if (name.contains("显示键盘")) return R.mipmap.x11_keyboard_visible;
        if (name.contains("隐藏键盘")) return R.mipmap.x11_keyboard_gone;
        if (name.equals("VNC")) return R.mipmap.vnc_ico;
        // 美化功能
        if (name.contains("悬浮")) return R.mipmap.xuanfu_window;
        if (name.contains("美化")) return R.mipmap.meihua_all;
        if (name.contains("字体")) return R.mipmap.ziti_font_ico;
        if (name.contains("全屏")) return R.mipmap.quanping_ico;
        if (name.contains("雪花")) return R.mipmap.xuehua_ico;
        if (name.contains("粒子")) return R.mipmap.particle;
        if (name.contains("清除")) return R.mipmap.clear_style;
        if (name.contains("网页数据") || name.contains("网页")) return R.mipmap.web_start_ico;
        // 引擎
        if (name.contains("密钥")) return R.mipmap.keyboard_img_menu;
        if (name.contains("文件浏览") || name.equals("文件浏览")) return R.mipmap.filebrowser_ico;
        if (name.contains("X86") || name.contains("Alpine")) return R.mipmap.alpine_run;
        // ROOT功能
        if (name.contains("开启")) return R.mipmap.adb_root;
        if (name.contains("关闭")) return R.mipmap.adb_no_root;
        if (name.contains("ADB连接") || name.contains("adb connect")) return R.mipmap.yc_connect;
        if (name.contains("ADB断开")) return R.mipmap.close_window;
        if (name.contains("ADB设备") || name.contains("adb device")) return R.mipmap.adb_shell;
        if (name.contains("ADB Shell")) return R.mipmap.adb_shell;
        if (name.contains("Docker")) return R.mipmap.docker;
        // Kali Linux
        if (name.equals("安装/重装")) return R.mipmap.install_module;
        if (name.contains("安装状态")) return R.mipmap.data_msg;
        if (name.equals("Kali换源")) return R.mipmap.code_view;
        if (name.equals("启动终端")) return R.mipmap.rongqi_item;
        if (name.equals("Root终端")) return R.mipmap.rongqi_ico;
        if (name.equals("启动桌面")) return R.mipmap.vnc_ico;
        if (name.equals("停止桌面")) return R.mipmap.waring;
        if (name.equals("设置密码")) return R.mipmap.close_window;
        if (name.equals("启动SSH")) return R.mipmap.yc_connect;
        if (name.equals("停止SSH")) return R.mipmap.close_window;
        if (name.equals("安装工具集")) return R.mipmap.install_module;
        if (name.equals("常用Kali工具")) return R.mipmap.install_module;
        if (name.equals("Kali工具快捷集")) return R.mipmap.install_module;
        if (name.equals("Nmap") || name.contains("Nmap扫描")) return R.mipmap.install_apk;
        if (name.equals("Dirb")) return R.mipmap.filebrowser_ico;
        if (name.equals("Metasploit")) return R.mipmap.mingl_ico;
        if (name.equals("Sqlmap")) return R.mipmap.waring;
        if (name.equals("Seeker")) return R.mipmap.online_sh;
        if (name.equals("CamPhish")) return R.mipmap.duanxin_ico;
        if (name.contains("一键安装") || name.contains("Kali NetHunter")) return R.mipmap.linux_new_ico;
        if (name.contains("Kali Shell") || name.contains("Kali终端")) return R.mipmap.linux_ico;
        // 线上功能
        if (name.contains("在线脚本") || name.contains("在线命令")) return R.mipmap.online_sh;
        if (name.contains("论坛") || name.contains("Zero")) return R.mipmap.bbs_zero;
        if (name.contains("下载")) return R.mipmap.download;
        if (name.contains("仓库")) return R.mipmap.gongongcangku;
        // 配置终端
        if (name.contains("Shell") || name.contains("shell")) return R.mipmap.adb_shell;
        if (name.contains("键盘") || name.contains("按键")) return R.mipmap.zt_command_key;
        if (name.contains("默认") || name.contains("bashrc默认")) return R.mipmap.def_bash;
        if (name.contains("编辑") && name.contains("bash")) return R.mipmap.bash_change;
        if (name.contains("欢迎") || name.contains("消息")) return R.mipmap.start_msg_ico;
        if (name.contains("命令定义") || name.contains("自定义")) return R.mipmap.zidingyi_cmd;
        if (name.contains("开机")) return R.mipmap.qidong_new_old;
        // ZT功能
        if (name.contains("安装模块")) return R.mipmap.install_module;
        if (name.contains("FTP")) return R.mipmap.ftp_web;
        if (name.contains("软链")) return R.mipmap.link_ico;
        if (name.contains("卸载")) return R.mipmap.uninstall;
        if (name.contains("远程")) return R.mipmap.yc_connect;
        if (name.contains("短信")) return R.mipmap.duanxin_ico;
        if (name.contains("打开路径")) return R.mipmap.file_launcher_icon;
        if (name.contains("数据信息") || name.contains("数据包")) return R.mipmap.data_msg;
        if (name.contains("语言")) return R.mipmap.yuyan_ico;
        if (name.contains("开源") || name.contains("GitHub")) return R.mipmap.github;
        // 系统工具
        if (name.contains("更新")) return R.mipmap.install_eg;
        if (name.contains("升级")) return R.mipmap.jixu_download;
        if (name.contains("清理")) return R.mipmap.chongzhi_ico;
        if (name.contains("进程")) return R.mipmap.run_ico;
        if (name.contains("磁盘")) return R.mipmap.dsk;
        if (name.contains("内存")) return R.mipmap.run_icooo;
        if (name.contains("系统信息") || name.contains("系统")) return R.mipmap.caozuo_lll;
        if (name.contains("IP")) return R.mipmap.http;
        if (name.contains("已安装")) return R.mipmap.apk_img;
        if (name.contains("网络测试") || name.contains("ping")) return R.mipmap.http;
        if (name.contains("环境变量")) return R.mipmap.def_bash;
        if (name.contains("CPU")) return R.mipmap.run_ico;
        return 0;
    }

    /**
     * 丰富的兜底菜单，对齐 ZeroTermux 的完整分组结构。
     * 当 default_menu.xml 不可用时使用。
     */
    private List<XmlMenuGroup> buildFallbackMenu() {
        List<XmlMenuGroup> groups = new ArrayList<>();
        String HOME = FileUtils.TERMUX_HOME;

        // 1. 常用功能 (id=0)
        XmlMenuGroup common = new XmlMenuGroup("常用功能", 0);
        common.addItem(menuItem("切换源", "ztShell:termux-change-repo", true));
        common.addItem(menuItem("容器切换", "shell_output:ls ~/.termux/container 2>/dev/null || echo '无容器'", true));
        common.addItem(menuItem("备份恢复", "commands:备份全部@@shell_output:tar czf ~/backup_$(date +%Y%m%d).tar.gz -C /data/data/com.termux/files . 2>&1,恢复@@shell_output:echo '恢复功能需手动操作',查看备份@@shell_output:ls -lh ~/backup_*.tar.gz 2>/dev/null", false));
        common.addItem(menuItem("发行版Linux", "shell_output:ls ~/termux-install-linux-master/ 2>/dev/null || echo '未安装'", true));
        common.addItem(menuItem("QEMU", "shell_output:which qemu-system-x86_64 2>/dev/null || echo 'QEMU未安装'", true));
        common.addItem(menuItem("定时任务", "editor:" + HOME + "/.termux/crontab", false));
        common.addItem(menuItem("Gavin设置", "internal:settings", false));
        groups.add(common);

        // 2. 创建项目 (id=8)
        XmlMenuGroup projects = new XmlMenuGroup("创建项目", 8);
        projects.addItem(menuItem("新建C项目", "ztShell:mkdir -p " + HOME + "/projects/c_project", true));
        projects.addItem(menuItem("新建Java项目", "ztShell:mkdir -p " + HOME + "/projects/java_project/src", true));
        projects.addItem(menuItem("新建Python项目", "ztShell:mkdir -p " + HOME + "/projects/py_project", true));
        projects.addItem(menuItem("新建PHP项目", "ztShell:mkdir -p " + HOME + "/projects/php_project", true));
        projects.addItem(menuItem("新建NPM项目", "ztShell:mkdir -p " + HOME + "/projects/npm_project", true));
        projects.addItem(menuItem("打开项目", "filebrowser:" + HOME + "/projects", false));
        groups.add(projects);

        // 3. X11功能 (id=1)
        XmlMenuGroup x11 = new XmlMenuGroup("X11功能", 1);
        x11.addItem(menuItem("X11设置", "editor:" + HOME + "/.termux/x11.properties", false));
        x11.addItem(menuItem("显示终端", "ztShell:echo '请在Termux中操作'", true));
        x11.addItem(menuItem("隐藏终端", "ztShell:pkill Xvnc 2>/dev/null; pkill Xwayland 2>/dev/null", true));
        x11.addItem(menuItem("X11环境", "shell_output:pkg list-installed 2>/dev/null | grep x11", true));
        x11.addItem(menuItem("修复环境", "ztShell:apt --fix-broken install -y", false));
        x11.addItem(menuItem("安装X11", "ztShell:pkg install -y x11-repo xterm", false));
        x11.addItem(menuItem("显示键盘", "ztShell:echo 'show-extra-keys' > ~/.termux/termux.properties", true));
        x11.addItem(menuItem("隐藏键盘", "ztShell:echo 'hide-extra-keys' > ~/.termux/termux.properties", true));
        x11.addItem(menuItem("VNC", "ztShell:vncserver -geometry 1280x720 :1", false));
        groups.add(x11);

        // 4. 美化功能 (id=2)
        XmlMenuGroup beauty = new XmlMenuGroup("美化功能", 2);
        beauty.addItem(menuItem("悬浮窗口", "internal:settings", false));
        beauty.addItem(menuItem("美化设置", "internal:settings", false));
        beauty.addItem(menuItem("字体设置", "internal:settings", false));
        beauty.addItem(menuItem("全屏模式", "internal:settings", false));
        beauty.addItem(menuItem("雪花特效", "internal:settings", false));
        beauty.addItem(menuItem("网页数据", "jumpUrl:https://wiki.zerotermux.dev", false));
        beauty.addItem(menuItem("粒子特效", "internal:settings", false));
        beauty.addItem(menuItem("清除样式", "internal:settings", false));
        groups.add(beauty);

        // 5. 需要插件/引擎 (id=6)
        XmlMenuGroup engine = new XmlMenuGroup("引擎", 6);
        engine.addItem(menuItem("密钥数据", "editor:" + HOME + "/.termux/keys", false));
        engine.addItem(menuItem("文件浏览", "filebrowser:" + HOME, false));
        engine.addItem(menuItem("X86 Alpine", "shell_output:echo '需要安装 proot-distro'", true));
        groups.add(engine);

        // 6. ROOT功能 (id=5)
        XmlMenuGroup root = new XmlMenuGroup("ROOT/ADB", 5);
        root.addItem(menuItem("开启网络ADB", "ztShell:su -c 'setprop service.adb.tcp.port 5555; stop adbd; start adbd'", false));
        root.addItem(menuItem("关闭网络ADB", "ztShell:su -c 'setprop service.adb.tcp.port -1; stop adbd; start adbd'", false));
        root.addItem(menuItem("Docker检查", "shell_output:su -c 'docker ps -a 2>/dev/null' || echo 'Docker未安装'", true));
        groups.add(root);

        // 7. Kali Linux (id=10)
        XmlMenuGroup kali = new XmlMenuGroup("Kali Linux", 10);
        kali.addItem(menuItem("一键安装Kali", "ztShell:cd ~ && bash kali.sh 2>/dev/null || echo '请先将kali.sh放入Termux目录'", true));
        kali.addItem(menuItem("Kali安装状态", "shell_output:if [ -d ~/kali-arm64 ]; then echo '已安装'; du -sh ~/kali-arm64; else echo '未安装'; fi", true));
        kali.addItem(menuItem("Kali换源", "commands:清华源@@ztShell:sed -i 's@http.kali.org@mirrors.tuna.tsinghua.edu.cn/kali@g' ~/kali-arm64/etc/apt/sources.list,阿里源@@ztShell:sed -i 's@http.kali.org@mirrors.aliyun.com/kali@g' ~/kali-arm64/etc/apt/sources.list", false));
        kali.addItem(menuItem("Kali终端", "commands:普通终端@@ztShell:nethunter,Root终端@@ztShell:nethunter -r", false));
        kali.addItem(menuItem("Kali桌面GUI", "commands:启动KeX@@ztShell:nethunter -r kex start,停止KeX@@ztShell:nethunter -r kex stop", false));
        kali.addItem(menuItem("Kali SSH", "commands:启动SSH@@ztShell:nethunter -r 'service ssh start',停止SSH@@ztShell:nethunter -r 'service ssh stop'", false));
        kali.addItem(menuItem("Kali更新", "ztShell:nethunter -r 'apt update && apt upgrade -y'", true));
        kali.addItem(menuItem("Kali工具安装", "commands:完整工具包@@ztShell:nethunter -r 'apt install kali-linux-default -y',Metasploit@@ztShell:nethunter -r 'apt install metasploit-framework -y',Nmap@@ztShell:apt install nmap -y", false));
        kali.addItem(menuItem("Nmap扫描", "internal:nmap", false));
        kali.addItem(menuItem("Kali工具集", "commands:信息收集@@internal:nmap,漏洞扫描@@ztShell:nethunter -r nikto,SQL注入@@ztShell:nethunter -r sqlmap,密码破解@@ztShell:nethunter -r hydra,WiFi安全@@ztShell:nethunter -r aircrack-ng,渗透测试@@ztShell:nethunter -r msfconsole", false));
        kali.addItem(menuItem("Kali密码设置", "ztShell:nethunter -r 'passwd root'", true));
        groups.add(kali);

        // 8. 线上功能 (id=3)
        XmlMenuGroup online = new XmlMenuGroup("线上功能", 3);
        online.addItem(menuItem("在线脚本", "internal:online_scripts", false));
        online.addItem(menuItem("Zero论坛", "jumpUrl:https://github.com/ZeroTermux-China/ZeroTermux/discussions", false));
        online.addItem(menuItem("下载中心", "internal:download_center", false));
        online.addItem(menuItem("公共仓库", "jumpUrl:https://github.com/Gavin-CrazyCoding?tab=repositories", false));
        groups.add(online);

        // 9. 配置终端 (id=7)
        XmlMenuGroup config = new XmlMenuGroup("配置终端", 7);
        config.addItem(menuItem("Shell运行", "shell_output:bash --version 2>/dev/null", true));
        config.addItem(menuItem("底部键盘", "editor:" + HOME + "/.termux/termux.properties", false));
        config.addItem(menuItem("默认bashrc", "editor:/data/data/com.termux/files/usr/etc/bash.bashrc", false));
        config.addItem(menuItem("编辑bashrc", "editor:" + HOME + "/.bashrc", false));
        config.addItem(menuItem("欢迎信息", "editor:/data/data/com.termux/files/usr/etc/motd", false));
        config.addItem(menuItem("命令定义", "editor:" + HOME + "/.bash_aliases", false));
        config.addItem(menuItem("开机命令", "editor:" + HOME + "/.termux/boot/start.sh", false));
        groups.add(config);

        // 10. ZT功能 (id=4)
        XmlMenuGroup zt = new XmlMenuGroup("ZT功能", 4);
        zt.addItem(menuItem("安装模块", "commands:curl@@ztShell:pkg install -y curl,git@@ztShell:pkg install -y git,vim@@ztShell:pkg install -y vim,python@@ztShell:pkg install -y python,nodejs@@ztShell:pkg install -y nodejs,clang@@ztShell:pkg install -y clang", false));
        zt.addItem(menuItem("FTP服务", "ztShell:pkg install -y pure-ftpd && pure-ftpd -B", false));
        zt.addItem(menuItem("常用软链", "shell_output:ls -la " + HOME + "/storage/ 2>/dev/null || termux-setup-storage", true));
        zt.addItem(menuItem("我的软链", "shell_output:find " + HOME + " -maxdepth 2 -type l 2>/dev/null", true));
        zt.addItem(menuItem("卸载模块", "commands:curl@@ztShell:pkg uninstall -y curl,git@@ztShell:pkg uninstall -y git,vim@@ztShell:pkg uninstall -y vim", false));
        zt.addItem(menuItem("远程连接", "ztShell:sshd 2>/dev/null && echo 'SSH已启动:8022' || pkg install -y openssh", true));
        zt.addItem(menuItem("网页数据", "jumpUrl:https://wiki.zerotermux.dev", false));
        zt.addItem(menuItem("短信功能", "shell_output:echo '需要 Termux:API 支持'", true));
        zt.addItem(menuItem("打开路径", "filebrowser:" + HOME, false));
        zt.addItem(menuItem("数据信息", "shell_output:uname -a; df -h /data; free -h 2>/dev/null", true));
        zt.addItem(menuItem("语言切换", "internal:language", false));
        zt.addItem(menuItem("开源地址", "jumpUrl:https://github.com/Gavin-CrazyCoding/", false));
        groups.add(zt);

        return groups;
    }

    private static XmlMenuItem menuItem(String name, String action, boolean autoRun) {
        return new XmlMenuItem(name, action, "", autoRun, "", false, "", "", "", "", "");
    }

    // ======== 点击事件派发 ========

    private void handleClick(XmlMenuItem item) {
        String action = item.getClickAction();
        if (TextUtils.isEmpty(action)) return;

        if (action.startsWith("ztShell:")) {
            handleShell(action.substring("ztShell:".length()).trim(), item);
        } else if (action.startsWith("shell_output:")) {
            handleShellOutput(action.substring("shell_output:".length()).trim(), item);
        } else if (action.startsWith("editor:")) {
            handleEditor(action.substring("editor:".length()).trim());
        } else if (action.startsWith("filebrowser:")) {
            handleFileBrowser(action.substring("filebrowser:".length()).trim());
        } else if (action.startsWith("internal:")) {
            handleInternal(action.substring("internal:".length()).trim());
        } else if (action.startsWith("jumpUrl:")) {
            handleUrl(action.substring("jumpUrl:".length()).trim());
        } else if (action.startsWith("commands:")) {
            handleCommands(action.substring("commands:".length()).trim(), item);
        } else if (action.startsWith("shellUrl:")) {
            mTerminalController.executeAndShow("curl -s '" + action.substring("shellUrl:".length()).trim() + "' | bash");
            hideMenu();
        } else if (action.startsWith("downloadUrl:")) {
            handleUrl(action.substring("downloadUrl:".length()).trim());
        } else if (action.startsWith("appWebUrl:")) {
            handleUrl(action.substring("appWebUrl:".length()).trim());
        } else if (action.startsWith("startActivity:") || action.startsWith("actionActivity:")) {
            Toast.makeText(this, "暂不支持启动Activity: " + action, Toast.LENGTH_SHORT).show();
        } else if (action.startsWith("input:")) {
            handleInput(action.substring("input:".length()).trim(), item);
        }
    }

    /**
     * 输入弹窗处理，对齐 ZeroTermux 的输入交互（如卸载模块、执行命令等）。
     * 格式: input:标题@@提示文字@@shell命令
     */
    private void handleInput(String spec, XmlMenuItem item) {
        String[] parts = spec.split("@@");
        String title = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "输入";
        String hint = parts.length > 1 ? parts[1] : "请输入";
        String shellTemplate = parts.length > 2 ? parts[2] : "";

        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(hint);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFF888888);
        input.setBackgroundColor(0x22FFFFFF);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));

        hideMenu();
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("确定", (d, w) -> {
                String val = input.getText().toString().trim();
                if (val.isEmpty()) {
                    Toast.makeText(FloatingBallService.this, "输入为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 安全检查：防止注入危险字符
                if (!val.matches("[a-zA-Z0-9._+\\- @/:=\\n]+")) {
                    Toast.makeText(FloatingBallService.this,
                        "输入包含非法字符", Toast.LENGTH_SHORT).show();
                    return;
                }
                String cmd = shellTemplate.replace("{}", val);
                if (item.isDialogConfirm()) {
                    showConfirmDialog("确认执行", "即将执行: " + cmd, () ->
                        mCmdHelper.sendCommandToTerminal(cmd));
                } else {
                    mCmdHelper.sendCommandToTerminal(item.isAutoRunShell() ? cmd + "\n" : cmd);
                    Toast.makeText(FloatingBallService.this, "已发送", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("取消", (d, w) -> d.dismiss())
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    private void handleShell(String shell, XmlMenuItem item) {
        if (shell.isEmpty()) return;
        if (item.isDialogConfirm()) {
            hideMenu();
            String title = !TextUtils.isEmpty(item.getDialogTitle()) ? item.getDialogTitle() : "确认";
            String msg = !TextUtils.isEmpty(item.getDialogMessage()) ? item.getDialogMessage() : "确认执行此命令？";
            showConfirmDialog(title, msg, new Runnable() {
                public void run() {
                    mCmdHelper.sendCommandToTerminal(shell);
                    Toast.makeText(FloatingBallService.this, "命令已发送到Termux", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mCmdHelper.sendCommandToTerminal(item.isAutoRunShell() ? shell + "\n" : shell);
            Toast.makeText(this, "命令已发送", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShellOutput(String command, XmlMenuItem item) {
        if (command.isEmpty()) return;
        if (item.isDialogConfirm()) {
            String title = !TextUtils.isEmpty(item.getDialogTitle()) ? item.getDialogTitle() : "确认";
            String msg = !TextUtils.isEmpty(item.getDialogMessage()) ? item.getDialogMessage() : "确认执行？";
            showConfirmDialog(title, msg, () -> mTerminalController.executeAndShow(command));
        } else {
            mTerminalController.executeAndShow(command);
        }
    }

    private void handleEditor(String path) {
        if (path.isEmpty()) path = FileUtils.TERMUX_HOME;
        hideMenu();
        new CodeEditDialog(FloatingBallService.this, path).show();
    }

    private void handleFileBrowser(String path) {
        if (path.isEmpty()) path = FileUtils.TERMUX_HOME;
        mFileBrowserController.openDirectory(path);
    }

    private void handleInternal(String action) {
        switch (action) {
            case "settings":
                mSettingsController.show();
                break;
            case "editor":
                mEditorController.openNewFile(FileUtils.TERMUX_HOME + "/untitled.txt", "");
                break;
            case "filebrowser":
                mFileBrowserController.openDirectory(FileUtils.TERMUX_HOME);
                break;
            case "language":
                Toast.makeText(this, "语言切换功能暂未实现", Toast.LENGTH_SHORT).show();
                break;
            case "download_center":
                hideMenu();
                new DownloadCenterDialog(FloatingBallService.this).show();
                break;
            case "online_scripts":
                hideMenu();
                new OnlineScriptDialog(FloatingBallService.this).show();
                break;
            case "nmap":
                hideMenu();
                new NmapDialog(FloatingBallService.this).show();
                break;
            case "kali_tools":
                hideMenu();
                new KaliToolsDialog(FloatingBallService.this).show();
                break;
            case "sqlmap":
                hideMenu();
                new SqlmapDialog(FloatingBallService.this).show();
                break;
            case "metasploit":
                hideMenu();
                new MetasploitDialog(FloatingBallService.this).show();
                break;
            case "dirb":
                hideMenu();
                new DirbDialog(FloatingBallService.this).show();
                break;
            case "seeker":
                hideMenu();
                new SeekerDialog(FloatingBallService.this).show();
                break;
            case "camphish":
                hideMenu();
                new CamphishDialog(FloatingBallService.this).show();
                break;
            case "create_c_project":
                createAndOpenProject("c_project", "main.c",
                    "/* C Project - GavinFloat */\n" +
                    "#include <stdio.h>\n" +
                    "#include <stdlib.h>\n\n" +
                    "int main(int argc, char** argv) {\n" +
                    "    printf(\"Hello, Termux!\\n\");\n" +
                    "    return 0;\n" +
                    "}\n");
                break;
            case "create_java_project":
                createAndOpenProject("java_project", "Main.java",
                    "/* Java Project - GavinFloat */\n" +
                    "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(\"Hello, Termux!\");\n" +
                    "    }\n" +
                    "}\n");
                break;
            case "create_python_project":
                createAndOpenProject("py_project", "main.py",
                    "#!/usr/bin/env python\n" +
                    "# Python Project - GavinFloat\n\n" +
                    "def main():\n" +
                    "    print(\"Hello, Termux!\")\n\n" +
                    "if __name__ == \"__main__\":\n" +
                    "    main()\n");
                break;
            case "create_php_project":
                createAndOpenProject("php_project", "index.php",
                    "<?php\n/* PHP Project - GavinFloat */\n" +
                    "echo \"Hello, Termux!\\n\";\n");
                break;
            case "create_npm_project":
                createAndOpenProject("npm_project", "index.js",
                    "// Node.js Project - GavinFloat\n" +
                    "console.log(\"Hello, Termux!\");\n");
                break;
            case "api_config":
                hideMenu();
                new ApiConfigDialog(FloatingBallService.this).show();
                break;
            case "pkg_manager":
                hideMenu();
                new PackageManagerDialog(FloatingBallService.this).show();
                break;
            case "wifi_tools":
                hideMenu();
                new WiFiToolsDialog(FloatingBallService.this).show();
                break;
            case "process_manager":
                hideMenu();
                new ProcessManagerDialog(FloatingBallService.this).show();
                break;
            case "network_tools":
                hideMenu();
                new NetworkToolsDialog(FloatingBallService.this).show();
                break;
            case "quick_commands":
                hideMenu();
                new QuickCommandsDialog(FloatingBallService.this).show();
                break;
            case "adb_manager":
                hideMenu();
                new AdbManagerDialog(FloatingBallService.this).show();
                break;
            case "ai_chat":
                hideMenu();
                new AIChatDialog(FloatingBallService.this).show();
                break;
            case "git_manager":
                hideMenu();
                new GitManagerDialog(FloatingBallService.this).show();
                break;
            case "sys_dashboard":
                hideMenu();
                new SystemDashboardDialog(FloatingBallService.this).show();
                break;
            default:
                Toast.makeText(this, "未知操作: " + action, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleUrl(String url) {
        hideMenu();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCommands(String commands, XmlMenuItem item) {
        try {
            // @@ 分隔名称和值，逗号分隔多个选项
            // 注意：值中可能含 @@（如 sed 分隔符），用 split("@@", 2) 限制只分裂第一个
            String[] parts = commands.split(",");
            String[] titles = new String[parts.length];
            String[] values = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String[] pair = parts[i].split("@@", 2);
                titles[i] = pair[0].trim();
                values[i] = pair.length > 1 ? pair[1].trim() : "";
            }
            hideMenu();
            String title = !TextUtils.isEmpty(item.getListTitle()) ? item.getListTitle() : "选择";
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(titles, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) {
                        String val = values[w];
                        if (val.startsWith("shell_output:")) {
                            mTerminalController.executeAndShow(val.substring("shell_output:".length()));
                        } else if (val.startsWith("ztShell:")) {
                            mCmdHelper.sendCommandToTerminal(val.substring("ztShell:".length()));
                        } else if (val.startsWith("internal:")) {
                            handleInternal(val.substring("internal:".length()));
                        } else if (val.startsWith("editor:")) {
                            handleEditor(val.substring("editor:".length()));
                        } else if (val.startsWith("jumpUrl:")) {
                            handleUrl(val.substring("jumpUrl:".length()));
                        } else if (val.startsWith("filebrowser:")) {
                            handleFileBrowser(val.substring("filebrowser:".length()));
                        } else {
                            mCmdHelper.sendCommandToTerminal(val);
                        }
                    }
                })
                .setNegativeButton("取消", (d, w) -> d.dismiss())
                .create();
            applyDialogType(dialog);
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "handleCommands error: " + e.getMessage(), e);
            Toast.makeText(this, "命令解析失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showConfirmDialog(String title, String msg, final Runnable onOk) {
        try {
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) { onOk.run(); }
                })
                .setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int w) { d.dismiss(); }
                })
                .setCancelable(true)
                .create();
            if (dialog.getWindow() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                } else {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                }
            }
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Dialog error: " + e.getMessage());
            Toast.makeText(this, title + ": " + msg, Toast.LENGTH_LONG).show();
            onOk.run();
        }
    }

    private void applyDialogType(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
    }

    // ======== 卡片数据加载 ========

    private void applySavedTheme(View panel) {
        android.content.SharedPreferences p = getSharedPreferences("gavinfloat_theme", MODE_PRIVATE);
        int accent = p.getInt("accent_color", 0xFFD4AF37);
        // 应用到标题文字
        TextView title = panel.findViewById(R.id.header_title);
        if (title != null) title.setTextColor(accent);
    }

    private void createAndOpenProject(String subdir, String mainFile, String content) {
        String base = FileUtils.TERMUX_HOME + "/projects/" + subdir;
        java.io.File mainFileObj = new java.io.File(base, mainFile);
        String mainPath = mainFileObj.getAbsolutePath();

        // 确保父目录存在
        java.io.File parentDir = mainFileObj.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        boolean ok = FileUtils.writeFile(mainPath, content);
        if (ok) {
            Toast.makeText(this, "项目已创建，打开编辑器...", Toast.LENGTH_SHORT).show();
            hideMenu();
            new CodeEditDialog(FloatingBallService.this, mainPath).show();
        } else {
            Toast.makeText(this, "创建失败，请检查权限: " + mainPath, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadIpAddress() {
        if (mIpStatus == null) return;
        mIpStatus.setText("获取中...");
        // 简单快速获取IP: ifconfig → ip addr → getprop
        mCmdHelper.executeAndCapture(
            "(ifconfig 2>/dev/null | grep 'inet ' | grep -v 127.0.0.1 | awk '{print $2}' | head -3; " +
            "ip addr show 2>/dev/null | grep 'inet ' | grep -v 127.0.0.1 | awk '{print $2}' | head -3; " +
            "getprop dhcp.wlan0.ipaddress 2>/dev/null) 2>/dev/null | head -3",
            new TermuxCommandHelper.OutputCallback() {
                public void onOutput(String output) {
                    final String s = output.trim();
                    mIpStatus.post(new Runnable() {
                        public void run() {
                            mIpStatus.setText(s.isEmpty() ? "无网络连接" : s);
                        }
                    });
                }
            });
    }

    private void loadDataInfo() {
        if (mDataInfoContent == null) return;
        mDataInfoContent.setText("加载中...");
        mCmdHelper.executeAndCapture(
            "echo '=== Termux路径 ===' && echo '/data/data/com.termux/files' && " +
            "echo '' && echo '=== Home目录 ===' && du -sh ~/ 2>/dev/null && " +
            "echo '' && echo '=== 磁盘使用 ===' && df -h /data 2>/dev/null | tail -1 && " +
            "echo '' && echo '=== 内核版本 ===' && uname -r 2>/dev/null",
            output -> mDataInfoContent.post(() -> mDataInfoContent.setText(output.trim())));
    }

    /**
     * 加载菜单包列表。
     * 弹出对话框提供三个选项：刷新当前、网络更新、重置默认。
     */
    private void loadMenuPackageList() {
        String[] options = {"刷新当前菜单", "网络更新菜单", "重置默认菜单"};
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("菜单管理")
            .setItems(options, (d, w) -> {
                switch (w) {
                    case 0: // 刷新
                        loadAndShowMenu();
                        Toast.makeText(FloatingBallService.this, "菜单已刷新", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: // 网络更新
                        downloadMenuFromNetwork();
                        break;
                    case 2: // 重置默认
                        resetDefaultMenu();
                        break;
                }
                if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
            })
            .setNegativeButton("取消", (d, w) -> {
                if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
                d.dismiss();
            })
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    /**
     * 从网络下载最新菜单配置。
     * 使用 HttpURLConnection（无额外依赖，AIDE 兼容）。
     */
    private void downloadMenuFromNetwork() {
        Toast.makeText(this, "正在下载菜单...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            java.net.HttpURLConnection conn = null;
            try {
                java.net.URL url = new java.net.URL(
                    "https://od.ixcmstudio.cn/repository/main/menu/zt_menu_config.xml");
                conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("User-Agent", "GavinFloat/1.0");
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    java.io.InputStream is = conn.getInputStream();
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                    is.close();
                    String xml = bos.toString("UTF-8");

                    // 保存到外部菜单路径
                    new java.io.File(EXTERNAL_MENU_PATH).getParentFile().mkdirs();
                    java.io.FileWriter fw = new java.io.FileWriter(EXTERNAL_MENU_PATH);
                    fw.write(xml);
                    fw.close();

                    new android.os.Handler(getMainLooper()).post(() -> {
                        Toast.makeText(FloatingBallService.this,
                            "菜单下载成功！正在刷新...", Toast.LENGTH_SHORT).show();
                        mMenuPackageCurrent.setText("当前: 网络更新菜单");
                        loadAndShowMenu();
                        if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
                    });
                } else {
                    showToastOnMain("下载失败: HTTP " + conn.getResponseCode());
                }
            } catch (Exception e) {
                showToastOnMain("下载失败: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    /**
     * 重置为内置默认菜单。
     */
    private void resetDefaultMenu() {
        try {
            // 删除外部配置文件
            new java.io.File(EXTERNAL_MENU_PATH).delete();

            // 从 assets 复制到外部路径（可选，方便用户编辑）
            java.io.InputStream is = getAssets().open("default_menu.xml");
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            is.close();

            new java.io.File(EXTERNAL_MENU_PATH).getParentFile().mkdirs();
            java.io.FileWriter fw = new java.io.FileWriter(EXTERNAL_MENU_PATH);
            fw.write(bos.toString("UTF-8"));
            fw.close();

            mMenuPackageCurrent.setText("当前: 内置菜单");
            loadAndShowMenu();
            if (mMenuPackageList != null) mMenuPackageList.setVisibility(View.GONE);
            Toast.makeText(this, "已重置为默认菜单", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "重置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showToastOnMain(String msg) {
        new android.os.Handler(getMainLooper()).post(() ->
            Toast.makeText(FloatingBallService.this, msg, Toast.LENGTH_SHORT).show());
    }

    // ======== 悬浮球触摸 ========

    private void toggleMenu() {
        if (mMenuShowing) hideMenu(); else showMenu();
    }

    private void showMenu() {
        if (mMenuShowing) return;
        updateStatusIndicators();
        mPageController.showPage(PageController.PAGE_MENU);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int ballCenterX = mBallParams.x + dp(mPrefs.getBallSize() / 2);
        int menuWidth = (int) (screenWidth * (mPrefs.getMenuWidth() / 100f));
        if (ballCenterX < screenWidth / 2) {
            mMenuParams.x = dp(mPrefs.getBallSize() + 5);
        } else {
            mMenuParams.x = screenWidth - menuWidth - dp(mPrefs.getBallSize() + 5);
        }
        mMenuParams.y = 0;

        mWindowManager.addView(mMenuView, mMenuParams);
        mMenuShowing = true;
    }

    private void hideMenu() {
        if (!mMenuShowing) return;
        try { mWindowManager.removeView(mMenuView); } catch (Exception ignored) {}
        mMenuShowing = false;
        mPrefs.setBallX(mBallParams.x);
        mPrefs.setBallY(mBallParams.y);
    }

    private class BallTouchListener implements View.OnTouchListener {
        private float startX, startY;
        private float initialX, initialY;
        private boolean moved = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    initialX = mBallParams.x;
                    initialY = mBallParams.y;
                    moved = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - startX;
                    float dy = event.getRawY() - startY;
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        moved = true;
                        mBallParams.x = (int) (initialX + dx);
                        mBallParams.y = (int) (initialY + dy);
                        mWindowManager.updateViewLayout(mBallView, mBallParams);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved) toggleMenu();
                    return true;
            }
            return false;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
