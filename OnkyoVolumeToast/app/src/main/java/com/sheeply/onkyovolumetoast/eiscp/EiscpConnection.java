package com.sheeply.onkyovolumetoast.eiscp;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class EiscpConnection {
    final String TAG = EiscpConnection.class.getSimpleName();
    final int HEARTBEAT_INTERVAL_MS = 1000 * 60; // 1 minute interval

    private String ipAddress;
    private int port;
    private boolean isRunning;
    private EiscpCommandListener listener;
    private Thread listenerThread;
    private Socket socket;
    private EiscpSerializer serializer;
    private DataOutputStream outputStream;
    private Timer heartbeatTimer;

    public EiscpConnection(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;

        this.serializer = new EiscpSerializer();
    }

    public void setListener(EiscpCommandListener listener) {
        this.listener = listener;
    }

    public void open() {
        if (isRunning) {
            Log.e(TAG, "Connection has already been opened.");
            return;
        }

        isRunning = true;
        Log.i(TAG, "Connecting to " + ipAddress + ":" + port + "...");

        listenerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    SocketAddress address = new InetSocketAddress(ipAddress, port);
                    socket = new Socket();
                    socket.connect(address, 1000);
                    outputStream = new DataOutputStream(socket.getOutputStream());

                    Log.i(TAG, "Connected to " + ipAddress + ":" + port + ".");
                    startHeartbeat();
                    listen();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error opening socket: ", e);
                    notifyListenerOnDisconnect();
                }
            }
        });

        listenerThread.start();
    }

    private void startHeartbeat() {
        TimerTask heartbeatTask = new TimerTask() {
            public void run() { send("AMTQSTN"); }
        };

        heartbeatTimer = new Timer(true);
        heartbeatTimer.scheduleAtFixedRate(heartbeatTask, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS);
    }

    public void send(String command) {
        try {
            serializer.write(command, outputStream);
            Log.i(TAG, "Command '" + command + "' sent.");
        } catch (IOException e) {
            Log.e(TAG, "Can't send command: ", e);
            notifyListenerOnDisconnect();
        }
    }

    public void close() {
        if (this.heartbeatTimer != null) {
            this.heartbeatTimer.cancel();
            this.heartbeatTimer = null;
        }

        this.listener = null;
        this.isRunning = false;
        this.listenerThread = null;

        try {
            if (this.outputStream != null) {
                this.outputStream.close();
            }
        }
        catch (IOException e) {}
        finally {
            this.outputStream = null;
        }

        try {
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch (IOException e) {}
        finally {
            this.socket = null;
        }
    }

    public void finalize() {
        close();
    }

    private void listen() {
        DataInputStream inputStream = null;

        try {
            inputStream = new DataInputStream(socket.getInputStream());

            while (isRunning) {
                String command = serializer.read(inputStream);
                this.processCommand(command);
            }

            inputStream.close();
            inputStream = null;
        }
        catch (IOException e) {
            Log.e(TAG, "Error listening: ", e);
            notifyListenerOnDisconnect();

            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e1) {}
                finally {
                    inputStream = null;
                }
            }
        }
    }

    private void processCommand(String command) {
        if (command.startsWith("MVL")) {
            String volumeHex = command.substring(3, 5);
            int volume = Integer.parseInt(volumeHex, 16);

            Log.i(TAG, "Volume: " + volume);
            notifyListenerOnVolumeChange(volume);
        }
        else if (command.startsWith("AMT")) {
            String stateHex = command.substring(3, 5);
            boolean muted = Integer.parseInt(stateHex, 16) == 1;

            Log.i(TAG, "Muted: " + muted);
            notifyListenerOnMutedChange(muted);
        }
        else {
            Log.i(TAG, "Unhandled command: " + command);
        }
    }

    private void notifyListenerOnDisconnect() {
        EiscpCommandListener listener = this.listener;
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    private void notifyListenerOnVolumeChange(int volume) {
        EiscpCommandListener listener = this.listener;
        if (listener != null) {
            listener.onVolumeChanged(volume);
        }
    }

    private void notifyListenerOnMutedChange(boolean muted) {
        EiscpCommandListener listener = this.listener;
        if (listener != null) {
            listener.onMutedChanged(muted);
        }
    }
}
