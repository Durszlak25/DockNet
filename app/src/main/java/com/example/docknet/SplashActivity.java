package com.example.docknet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_MS = 350;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finish();
        }, SPLASH_MS);
    }
}
