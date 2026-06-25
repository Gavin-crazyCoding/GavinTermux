# GavinFloat

> 悬浮球侧边栏 APK — 独立于 ZeroTermux 运行的高级图形化管理工具

[![Android](https://img.shields.io/badge/Android-6.0%2B-green)](https://developer.android.com)
[![AIDE](https://img.shields.io/badge/AIDE-Compatible-blue)](https://aide-ide.com)
[![Java](https://img.shields.io/badge/Java-100%25-orange)](https://www.java.com)

## 简介

GavinFloat 是一个运行在 WindowManager 叠加层的悬浮球应用。点击悬浮球弹出侧边栏菜单，提供 **21 个图形化工具弹窗**，覆盖 Kali Nethunter 渗透测试、系统管理、网络工具、AI 对话、ADB 控制等场景。

与 ZeroTermux 共享 `com.termux` 用户 ID，可直接向 Termux 终端发送命令。

## 截图

```
┌──────────────────────────┐
│  GavinFloat        ● 已连接│
├──────────────────────────┤
│  G avin F loat     ⚙    │
│  v1.0 / 已连接          │
├──────────────────────────┤
│  IP                     │
│  192.168.1.100          │
├──────────────────────────┤
│  ▸ 常用功能              │
│  ┌─────┬─────┬─────┐    │
│  │切换源│容器  │备份 │    │
│  ├─────┼─────┼─────┤    │
│  │MOE  │发行版│QEMU │    │
│  └─────┴─────┴─────┘    │
│  ▸ Kali Nethunter       │
│  ▸ 创建项目              │
│  ▸ 系统工具              │
└──────────────────────────┘
      ○ (悬浮球)
```

## 功能特性

### 21 个图形化工具弹窗

| 工具 | 功能 |
|------|------|
| **Nmap** | 端口扫描参数表单（扫描类型/端口/时序） |
| **Metasploit** | msfvenom Payload 生成器（6平台×载荷×回调） |
| **Sqlmap** | SQL 注入表单（URL/向导/高级选项） |
| **Dirb** | Web 目录扫描（字典/代理/Cookie） |
| **Seeker** | 钓鱼页面生成（8种模板/端口/隧道） |
| **CamPhish** | 摄像头钓鱼（Ngrok/拍照/录像） |
| **WiFi 工具集** | 8个 WiFi 安全工具（扫描/破解/Deauth/WPA/WPS/WEP） |
| **Kali 工具浏览器** | 13类 50+ 工具分类（点击运行/长按安装） |
| **ADB 管理器** | 设备列表/APK安装/Shell/端口转发/截图录屏 |
| **AI 助手** | 接入 DeepSeek/OpenAI/Claude/Ollama 的聊天对话 |
| **包管理器** | 搜索/安装/卸载 pkg 软件包 |
| **进程管理器** | ps 进程列表 + KILL 按钮 |
| **网络工具** | Ping/Traceroute/NSLookup/Curl/Whois/DNS |
| **Git 管理器** | 克隆/拉取/提交/推送/状态/日志 |
| **系统仪表盘** | CPU/内存/磁盘/电池实时数据 |
| **代码编辑器** | 语法高亮 + 多语言检测 + 保存 |
| **下载中心** | 底部抽屉式下载列表 |
| **在线脚本** | 脚本市场（下载+执行） |
| **快捷命令** | 保存/运行常用命令（持久化存储） |
| **API/MCP 配置** | DeepSeek/OpenAI/Claude/Ollama 配置 |
| **主题配色** | 6种预设 + 8色自定义 |

### Kali Nethunter 集成
- 一键安装 Kali（kali.sh 脚本）
- 终端（普通/Root）
- 桌面 GUI（KeX VNC）
- SSH 管理（启/停）
- 换源（4个镜像源）
- 工具安装（完整包/MSF/Nmap/Wireshark/sqlmap）
- 密码设置

### 菜单系统
- 11 个分组，90+ 个菜单项
- 3 列网格布局
- 展开/折叠动画
- 支持外部 XML 配置覆盖
- 支持网络更新菜单

## 编译

### 环境要求
- **AIDE** (Android IDE) 或 Android Studio
- Android SDK 23+
- Java 8+

### AIDE 编译（推荐）
1. 用 AIDE 打开项目根目录
2. 等待索引完成
3. 点击「运行」编译安装

### Gradle 编译
```bash
./gradlew assembleDebug
```

## 安装

1. 安装 APK
2. 授予「悬浮窗」权限（系统设置 → 应用 → GavinFloat → 悬浮窗权限）
3. 打开应用，悬浮球出现在屏幕边缘
4. 点击悬浮球打开菜单，拖动悬浮球改变位置

## 依赖

- `androidx.appcompat:appcompat:1.6.1`
- `androidx.recyclerview:recyclerview:1.3.2`
- `androidx.cardview:cardview:1.0.0`
- `com.google.android.material:material:1.12.0`

## 项目结构

```
com.termux.menu/
├── FloatingBallService.java    # 核心服务：悬浮球 + 菜单面板
├── MainActivity.java           # 启动 Activity
├── ExitReceiver.java           # 通知栏退出
├── model/                      # 数据模型
├── termux/TermuxCommandHelper.java  # Termux 通信
├── ui/
│   ├── dialog/                 # 21 个图形化弹窗
│   ├── IconProvider.java       # 文字图标生成
│   ├── TouchRippleView.java    # 触摸水波纹
│   ├── RippleFrameLayout.java  # 自定义触摸框架
│   └── ...                     # 适配器/控制器
├── utils/                      # 工具类
└── xml/XmlMenuParser.java      # XML 菜单解析
```

## 菜单配置

默认菜单在 `assets/default_menu.xml`。支持外部配置：
```
/data/data/com.termux/files/home/ZtInfo/main_menu_path.xml
```

### 支持的点击类型

| 类型 | 格式 | 说明 |
|------|------|------|
| Shell 发送 | `ztShell:command` | 发送命令到 Termux 终端 |
| Shell 输出 | `shell_output:command` | 执行并显示输出 |
| 编辑器 | `editor:path` | 打开代码编辑器 |
| 文件浏览 | `filebrowser:path` | 打开文件浏览器 |
| 内部操作 | `internal:name` | 调用内部功能 |
| URL 跳转 | `jumpUrl:https://...` | 浏览器打开 |
| 命令列表 | `commands:名@@命令,名@@命令` | 列表选择 |
| 在线脚本 | `shellUrl:https://...` | 下载并执行脚本 |
| 输入弹窗 | `input:标题@@提示@@模板` | 带输入框的弹窗 |

## 许可证

MIT License

## 致谢

- [ZeroTermux](https://github.com/hanxinhao000/ZeroTermux) — 菜单设计参考
- [Termux](https://github.com/termux/termux-app) — Android 终端模拟器

---

🤖 Built with [Claude Code](https://claude.ai/code)
