package com.example.networktrans;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Autostart", "started");
        if (intent.getAction().equals(ACTION_BOOT)) { //开机启动完成后，要做的事情
            Intent intent1 = new Intent(context, MyService.class);
            context.startForegroundService(intent1);
        }
    }
}