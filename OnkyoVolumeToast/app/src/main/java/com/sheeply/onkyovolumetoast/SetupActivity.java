package com.sheeply.onkyovolumetoast;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SetupActivity extends AppCompatActivity {
    MonitorPreferences preferences;

    Intent serviceIntent;
    MonitorService monitorService;

    EditText ipAddressText;
    EditText portText;

    Button saveButton;
    ToggleButton serviceToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        preferences = new MonitorPreferences(this);

        ipAddressText = (EditText) findViewById(R.id.ipAddressText);
        ipAddressText.setText(preferences.getIpAddress());

        portText = (EditText) findViewById(R.id.portText);
        portText.setText(String.valueOf(preferences.getPort()));

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setIpAddress(ipAddressText.getText().toString());
                preferences.setPort(Integer.parseInt(portText.getText().toString()));
                restartService();
            }
        });

        serviceToggleButton = (ToggleButton) findViewById(R.id.serviceToggleButton);
        serviceToggleButton.setChecked(preferences.getIsServiceOn());
        serviceToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setIsServiceOn(serviceToggleButton.isChecked());
                updateServiceState();
            }
        });

        monitorService = new MonitorService(this.getApplicationContext());
        serviceIntent = new Intent(this.getApplicationContext(), monitorService.getClass());

        updateServiceState();
    }

    private void updateServiceState() {
        if (isServiceRunning(monitorService.getClass()) != preferences.getIsServiceOn()) {
            if (preferences.getIsServiceOn()) {
                this.startService(serviceIntent);
            }
            else {
                this.stopService(serviceIntent);
            }
        }
    }

    private void restartService() {
        if (isServiceRunning(monitorService.getClass())) {
            this.stopService(serviceIntent);
            this.startService(serviceIntent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        this.stopService(serviceIntent);
        super.onDestroy();
    }
}
