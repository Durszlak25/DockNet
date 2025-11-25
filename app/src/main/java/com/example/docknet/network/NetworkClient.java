package com.example.docknet.network;

import java.io.IOException;

public interface NetworkClient {
    String performApiRequest(String urlString) throws IOException;
    void cancelCurrentRequest();
}
