package com.example.docknet.data;

import android.util.Log;

import com.example.docknet.model.SystemInfo;
import com.example.docknet.network.NetworkClient;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemRepository {
    private static final String TAG = "SystemRepository";

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private final NetworkClient networkClient;
    // searchCounter / infoCounter są tokenami do ignorowania starych odpowiedzi.
    private final AtomicInteger searchCounter = new AtomicInteger(0);
    private final AtomicInteger infoCounter = new AtomicInteger(0);

    public SystemRepository(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public void searchSystems(String query, RepositoryCallback<List<String>> callback) {
        if (query == null) {
            callback.onError(new IllegalArgumentException("query is null"));
            return;
        }
        final int myId = searchCounter.incrementAndGet();
        // prosty Thread zamiast puli — łatwiejszy do zrozumienia
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                String url = "https://www.edsm.net/api-v1/systems?systemName=" + encoded + "&showInformation=1&showCoordinates=1&showPrimaryStar=1";
                String jsonResponse = networkClient.performApiRequest(url);
                List<String> list = SystemParser.parseSystemListFromJson(jsonResponse);
                if (myId != searchCounter.get()) return; // stale
                callback.onSuccess(list);
            } catch (Exception e) {
                if (myId != searchCounter.get()) return;
                Log.w(TAG, "searchSystems failed for query=" + query, e);
                callback.onError(e);
            }
        }).start();
    }

    public void getSystemInfo(String systemName, RepositoryCallback<SystemInfo> callback) {
        if (systemName == null) {
            callback.onError(new IllegalArgumentException("systemName is null"));
            return;
        }
        final int myId = infoCounter.incrementAndGet();
        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(systemName, "UTF-8");
                String url = "https://www.edsm.net/api-v1/system?systemName=" + encoded + "&showInformation=1&showCoordinates=1&showPrimaryStar=1";
                String jsonResponse = networkClient.performApiRequest(url);
                JSONObject systemObject = new JSONObject(jsonResponse);
                SystemInfo info = SystemParser.parseSystemInfoFromJson(systemObject);
                if (myId != infoCounter.get()) return; // stale
                callback.onSuccess(info);
            } catch (Exception e) {
                if (myId != infoCounter.get()) return;
                Log.w(TAG, "getSystemInfo failed for system=" + systemName, e);
                callback.onError(e);
            }
        }).start();
    }

    public void cancelCurrentRequest() {
        // oznaczamy aktualne odpowiedzi jako przestarzałe
        searchCounter.incrementAndGet();
        infoCounter.incrementAndGet();
        networkClient.cancelCurrentRequest();
    }

    public void shutdown() {
        cancelCurrentRequest();
        // nic więcej do zamknięcia, bo nie trzymamy puli wątków
    }
}
