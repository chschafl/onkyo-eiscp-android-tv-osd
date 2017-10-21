package com.sheeply.onkyovolumetoast;

import android.content.Context;
import android.content.SharedPreferences;

public class MonitorPreferences {
    SharedPreferences preferences;

    private static final String SERVICE_ON = "serviceOn";
    private static final String IP_ADDRESS = "ipAddress";
    private static final String PORT = "port";

    public MonitorPreferences(Context context) {
        preferences = context.getSharedPreferences("MonitorPreferences", Context.MODE_PRIVATE);
    }

    public String getIpAddress() {
        return preferences.getString(IP_ADDRESS, "192.168.1.180");
    }

    public void setIpAddress(String value) {
        set(IP_ADDRESS, value);
    }

    public int getPort() {
        return preferences.getInt(PORT, 60128);
    }

    public void setPort(int value) {
        set(PORT, value);
    }

    public boolean getIsServiceOn() {
        return preferences.getBoolean(SERVICE_ON, false);
    }

    public void setIsServiceOn(boolean value) {
        set(SERVICE_ON, value);
    }

    private void set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void set(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private void set(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
}
