package com.example.docknet.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class ServerStatusManager {
    public interface StatusCallback {
        void onStatus(String lastUpdate, String state);
        void onError(Exception e);
    }

    private final NetworkClient networkClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ServerStatusManager(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public void fetchStatus(StatusCallback callback) {
        executor.submit(() -> {
            try {
                String url = "https://www.edsm.net/api-status-v1/elite-server";
                String json = networkClient.performApiRequest(url);
                JSONObject obj = new JSONObject(json);
                String lastUpdate = obj.optString("lastUpdate", "Unknown");
                String state = obj.optString("type", "");
                callback.onStatus(lastUpdate, state);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void shutdown() {
        try { executor.shutdownNow(); } catch (Exception ignored) {}
    }
}

