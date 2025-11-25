package com.example.docknet.ui;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.docknet.R;
import com.example.docknet.network.ServerStatusManager;

import java.util.Locale;

public class ServerStatusController {
    private final AppCompatActivity activity;
    private final ServerStatusManager manager;

    public ServerStatusController(AppCompatActivity activity, ServerStatusManager manager) {
        this.activity = activity;
        this.manager = manager;
    }

    public void setup() {
        TextView lastUpdate = activity.findViewById(R.id.server_status_text);
        View statusView = activity.findViewById(R.id.server_status_image);

        manager.fetchStatus(new ServerStatusManager.StatusCallback() {
            @Override
            public void onStatus(String lastUpdateTime, String state) {
                final String st = state != null ? state.toLowerCase(Locale.ROOT) : "";
                final int color;
                switch (st) {
                    case "success": color = android.graphics.Color.parseColor("#20EE20"); break;
                    case "warning": color = android.graphics.Color.parseColor("#FF9800"); break;
                    case "danger": color = android.graphics.Color.parseColor("#FF0000"); break;
                    default: color = android.graphics.Color.parseColor("#000000"); break;
                }
                activity.runOnUiThread(() -> {
                    if (lastUpdate != null) lastUpdate.setText(lastUpdateTime);
                    if (statusView != null) statusView.setBackgroundColor(color);
                });
            }

            @Override
            public void onError(Exception e) {
                activity.runOnUiThread(() -> {
                    if (lastUpdate != null) lastUpdate.setText(activity.getString(R.string.server_status_unavailable));
                    if (statusView != null) statusView.setBackgroundColor(android.graphics.Color.parseColor("#666666"));
                });
            }
        });
    }

    public void teardown() {
        // no-op: manager lifecycle managed by caller
    }
}

