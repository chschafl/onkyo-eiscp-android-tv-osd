package com.sheeply.onkyovolumetoast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MonitorBroadcastReceiver extends BroadcastReceiver {
    final String TAG = MonitorBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "android.intent.action.BOOT_COMPLETED":
            case "com.sheeply.onkyovolumetoast.RestartMonitor":
                this.startService(context);
                break;

            default:
                Log.e(TAG, "Unhandled action: " + intent.getAction());
        }
    }

    private void startService(Context context) {
        MonitorPreferences preferences = new MonitorPreferences(context);

        if (preferences.getIsServiceOn()) {
            Log.i(TAG, "Starting service...");
            context.startService(new Intent(context, MonitorService.class));
        }
        else {
            Log.w(TAG, "Service disabled.");
        }
    }
}