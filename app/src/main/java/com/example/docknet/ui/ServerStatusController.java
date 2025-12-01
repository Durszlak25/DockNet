package com.example.docknet.ui;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.docknet.R;
import com.example.docknet.network.ServerStatusManager;

import java.util.Locale;
import android.graphics.Color;

final class ServerColors {
    static final int SUCCESS = Color.parseColor("#20EE20");
    static final int WARNING = Color.parseColor("#FF9800");
    static final int DANGER = Color.parseColor("#FF0000");
    static final int DEFAULT = Color.parseColor("#000000");
    static final int UNAVAILABLE = Color.parseColor("#666666");
}

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
                 final int color = getColorForState(st);
                 activity.runOnUiThread(() -> {
                     if (lastUpdate != null) lastUpdate.setText(lastUpdateTime);
                     if (statusView != null) statusView.setBackgroundColor(color);
                 });
             }

             @Override
             public void onError(Exception e) {
                 activity.runOnUiThread(() -> {
                     if (lastUpdate != null) lastUpdate.setText(activity.getString(R.string.server_status_unavailable));
                     if (statusView != null) statusView.setBackgroundColor(ServerColors.UNAVAILABLE);
                 });
             }
         });
     }

     private int getColorForState(String st) {
         switch (st) {
             case "success": return ServerColors.SUCCESS;
             case "warning": return ServerColors.WARNING;
             case "danger": return ServerColors.DANGER;
             default: return ServerColors.DEFAULT;
         }
     }
 }
