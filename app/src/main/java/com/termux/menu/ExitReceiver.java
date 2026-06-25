package com.termux.menu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 通知栏一键退出广播接收器 */
public class ExitReceiver extends BroadcastReceiver {
    public static final String ACTION_EXIT = "com.termux.menu.EXIT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_EXIT.equals(intent.getAction())) {
            context.stopService(new Intent(context, FloatingBallService.class));
        }
    }
}
