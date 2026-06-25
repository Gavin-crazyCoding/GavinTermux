package com.termux.menu.termux;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;

public class TermuxCommandHelper {
    private static final String TAG = "TermuxCmdHelper";

    private static final String TERMUX_PKG = "com.termux";
    private static final String TERMUX_SERVICE_CLASS = "com.termux.app.TermuxService";

    private static final String ACTION_SERVICE_EXECUTE = "com.termux.service_execute";
    private static final String EXTRA_ARGUMENTS = "com.termux.execute.arguments";
    private static final String EXTRA_WORKDIR = "com.termux.execute.cwd";
    private static final String EXTRA_BACKGROUND = "com.termux.execute.background";
    private static final String EXTRA_RUNNER = "com.termux.execute.runner";
    private static final String EXTRA_SESSION_ACTION = "com.termux.execute.session_action";
    private static final String EXTRA_STDIN = "com.termux.execute.stdin";

    private static final String BASH_PATH = "/data/data/com.termux/files/usr/bin/bash";
    private static final String HOME_DIR = "/data/data/com.termux/files/home";
    private static final String RUNNER_APP_SHELL = "app-shell";
    private static final String RUNNER_TERMINAL_SESSION = "terminal-session";

    private static final int SESSION_ACTION_DONT_OPEN_ACTIVITY = 3;

    private static TermuxCommandHelper sInstance;
    private Context mContext;

    public static TermuxCommandHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TermuxCommandHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private TermuxCommandHelper(Context context) {
        mContext = context;
    }

    public void sendCommandToTerminal(String command) {
        if (command == null || command.isEmpty()) return;
        String trimmed = command.endsWith("\n") ? command.substring(0, command.length() - 1) : command;
        executeInTerminal(trimmed);
    }

    public void executeInBackground(String command) {
        if (command == null || command.isEmpty()) return;
        try {
            Intent intent = buildServiceIntent();
            intent.setAction(ACTION_SERVICE_EXECUTE);
            intent.setData(Uri.parse("com.termux.file" + ":" + BASH_PATH));
            intent.putExtra(EXTRA_ARGUMENTS, new String[]{"-c", command});
            intent.putExtra(EXTRA_WORKDIR, HOME_DIR);
            intent.putExtra(EXTRA_BACKGROUND, true);
            intent.putExtra(EXTRA_RUNNER, RUNNER_APP_SHELL);
            mContext.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "executeInBackground failed, trying direct shell: " + e.getMessage());
            directShellExec(command);
        }
    }

    public void executeInTerminal(String command) {
        if (command == null || command.isEmpty()) return;
        try {
            File scriptFile = new File(HOME_DIR + "/.termux/.gavinfloat_cmd.sh");
            File parentDir = scriptFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            FileWriter writer = new FileWriter(scriptFile);
            writer.write("#!/data/data/com.termux/files/usr/bin/bash\n");
            writer.write(command + "\n");
            writer.close();
            Runtime.getRuntime().exec("chmod 700 " + scriptFile.getAbsolutePath());

            Intent intent = buildServiceIntent();
            intent.setAction(ACTION_SERVICE_EXECUTE);
            intent.setData(Uri.parse("com.termux.file" + ":" + scriptFile.getAbsolutePath()));
            intent.putExtra(EXTRA_WORKDIR, HOME_DIR);
            intent.putExtra(EXTRA_RUNNER, RUNNER_TERMINAL_SESSION);
            intent.putExtra(EXTRA_SESSION_ACTION, String.valueOf(SESSION_ACTION_DONT_OPEN_ACTIVITY));
            mContext.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "executeInTerminal failed, trying direct shell: " + e.getMessage());
            directShellExec(command);
        }
    }

    public void executeAndCapture(String command, OutputCallback callback) {
        new Thread(() -> {
            String result;
            try {
                Process process = Runtime.getRuntime().exec(new String[]{BASH_PATH, "-c", command});
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                java.io.BufferedReader errReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                while ((line = errReader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                process.waitFor();
                result = sb.toString();
                if (result.isEmpty()) result = "(no output)";
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
            if (callback != null) {
                String finalResult = result;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(
                    () -> callback.onOutput(finalResult));
            }
        }).start();
    }

    public interface OutputCallback {
        void onOutput(String output);
    }

    public boolean isTermuxInstalled() {
        try {
            mContext.getPackageManager().getPackageInfo(TERMUX_PKG, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Intent buildServiceIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(TERMUX_PKG, TERMUX_SERVICE_CLASS));
        return intent;
    }

    private void directShellExec(String command) {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec(BASH_PATH);
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(command + "\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
            } catch (Exception e) {
                Log.e(TAG, "Direct shell exec failed: " + e.getMessage());
            }
        }).start();
    }
}
