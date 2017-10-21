package com.sheeply.onkyovolumetoast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sheeply.onkyovolumetoast.eiscp.EiscpConnection;
import com.sheeply.onkyovolumetoast.eiscp.EiscpCommandListener;

import java.util.Timer;
import java.util.TimerTask;

public class MonitorService extends Service implements EiscpCommandListener {
    private final String TAG = MonitorService.class.getSimpleName();
    private final int RECONNECT_INTERVAL_MS = 1000 * 30;

    private Timer reconnectTimer;
    private TimerTask reconnectTimerTask;

    private EiscpConnection eiscpConnection;
    private Toast lastToast;

    public MonitorService(Context applicationContext) {
        super();

        Log.i(TAG, "Started!");
    }

    public MonitorService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        setupConnection();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroyed!");

        // Restart the monitor to run forever.
        Intent broadcastIntent = new Intent("com.sheeply.onkyovolumetoast.RestartMonitor");
        sendBroadcast(broadcastIntent);

        cancelReconnectTimer();
        closeConnection();
    }

    private void setupConnection() {
        closeConnection();

        MonitorPreferences prefs = new MonitorPreferences(this);
        String ipAddress = prefs.getIpAddress();
        int port = prefs.getPort();

        eiscpConnection = new EiscpConnection(ipAddress, port);
        eiscpConnection.setListener(MonitorService.this);
        eiscpConnection.open();
    }

    private void closeConnection() {
        if (eiscpConnection != null) {
            eiscpConnection.close();
            eiscpConnection = null;
        }
    }

    private void startReconnectTimer() {
        cancelReconnectTimer();

        // Create new reconnect timer.
        reconnectTimer = new Timer();
        reconnectTimerTask = new TimerTask() {
            public void run() {
                cancelReconnectTimer();
                setupConnection();
            }
        };

        // Try to reconnect in 10 seconds.
        reconnectTimer.schedule(reconnectTimerTask, RECONNECT_INTERVAL_MS);
    }

    private void cancelReconnectTimer() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onVolumeChanged(int volume) {
        final int volumeParam = volume;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (lastToast != null) {
                    lastToast.cancel();
                }

                lastToast = Toast.makeText(MonitorService.this, "Volume: " + volumeParam, Toast.LENGTH_LONG);
                lastToast.show();
            }
        });
    }

    @Override
    public void onMutedChanged(boolean muted) {
        final String message = muted ? "Muted" : "Unmuted";
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (lastToast != null) {
                    lastToast.cancel();
                }

                lastToast = Toast.makeText(MonitorService.this, message, Toast.LENGTH_LONG);
                lastToast.show();
            }
        });
    }

    @Override
    public void onDisconnected() {
        closeConnection();
        startReconnectTimer();
    }
}