package com.example.docknet;

import android.os.Bundle;
import android.os.Handler; // CHANGED: Added import
import android.os.Looper; // CHANGED: Added import
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor; // CHANGED: Added import
import java.util.concurrent.Executors; // CHANGED: Added import

public class MainActivity extends AppCompatActivity {

    EditText bar;
    TextView result;
    Button btn;

    // CHANGED: Added an Executor to run background tasks
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bar = findViewById(R.id.search_bar);
        result = findViewById(R.id.result);
        btn = findViewById(R.id.search_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String systemName = String.valueOf(bar.getText());

                // CHANGED: Removed the try/catch block.
                // The background task will handle its own errors.
                getSystemInfo(systemName);
            }
        });
    }

    // CHANGED: This method now runs the network logic on a background thread
    private void getSystemInfo(String systemName) {

        // Use the executor to run this code block on a background thread
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection http = null;
                String responseText = ""; // To store the result or error

                // CHANGED: Added a StringBuilder to build the JSON response
                StringBuilder jsonResponse = new StringBuilder();

                try {
                    // This code is running on the BACKGROUND THREAD
                    // CHANGED: I'm using your original code to use the 'systemName' variable again
                    URL url = new URL(String.format("https://www.edsm.net/api-v1/system?systemName=%s&showInformation=1&showCoordinates=1&showPrimaryStar=1", systemName));
                    http = (HttpURLConnection) url.openConnection();

                    // Check if the response is successful (HTTP 200)
                    if (http.getResponseCode() == 200) {

                        // CHANGED: Get the InputStream to read the data
                        java.io.InputStream inputStream = http.getInputStream();
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(inputStream)
                        );

                        // CHANGED: Read the data line by line
                        String line;
                        while ((line = reader.readLine()) != null) {
                            jsonResponse.append(line);
                        }
                        reader.close(); // Close the reader

                        // Set the response text to the JSON we just read
                        responseText = jsonResponse.toString();

                    } else {
                        // Store the error response
                        responseText = http.getResponseCode() + " " + http.getResponseMessage();
                    }

                } catch (IOException e) {
                    // Store the error message
                    responseText = "Error: " + e.getMessage();
                    e.printStackTrace();
                } finally {
                    // Always disconnect in a finally block
                    if (http != null) {
                        http.disconnect();
                    }
                }

                // CHANGED: We need to update the UI on the MAIN THREAD.
                // We post the result back using runOnUiThread.
                final String finalResponseText = responseText;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code runs on the UI thread
                        // This will now show the full JSON data or an error
                        result.setText(finalResponseText);
                    }
                });
            }
        });
    }
}