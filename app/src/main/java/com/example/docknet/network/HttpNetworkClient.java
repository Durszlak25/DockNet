package com.example.docknet.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpNetworkClient implements NetworkClient {
    private final Object connectionLock = new Object();
    private volatile HttpURLConnection currentConnection = null;

    @Override
    public String performApiRequest(String urlString) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            synchronized (connectionLock) {
                currentConnection = connection;
            }
            connection.setRequestMethod("GET");
            connection.connect();
            return readStream(connection.getInputStream());
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignored) {}
            }
            synchronized (connectionLock) {
                if (currentConnection == connection) {
                    currentConnection = null;
                }
            }
        }
    }

    @Override
    public void cancelCurrentRequest() {
        synchronized (connectionLock) {
            if (currentConnection != null) {
                try {
                    currentConnection.disconnect();
                } catch (Exception ignored) {
                } finally {
                    currentConnection = null;
                }
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}

