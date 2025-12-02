package com.example.docknet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docknet.data.SystemRepository;
import com.example.docknet.network.ServerStatusManager;
import com.example.docknet.ui.AnimationHelper;
import com.example.docknet.ui.FactionController;
import com.example.docknet.ui.SystemInfoController;
import com.example.docknet.ui.ServerStatusController;
import com.example.docknet.ui.StarsController;
import com.example.docknet.ui.ShipsController;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class MainActivity extends AppCompatActivity {

    private SystemRepository systemRepository;
    private ServerStatusManager serverStatusManager;

    private enum Screen { MAIN, SYSTEM_INFO, STARS, SHIPS, FACTIONS }
    private final Deque<Screen> navStack = new ArrayDeque<>();

    private void pushScreen(Screen s) {
        if (navStack.isEmpty() || navStack.peek() != s) {
            navStack.push(s);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_DockNet);

        com.example.docknet.network.NetworkClient networkClient = new com.example.docknet.network.RetrofitNetworkClient();
        systemRepository = new SystemRepository(networkClient);
        serverStatusManager = new ServerStatusManager(networkClient);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                hideKeyboard();
                if (navStack.size() > 1) {
                    navStack.pop();
                    Screen prev = navStack.peek();
                    if (prev == null) { finish(); return; }
                    switch (prev) {
                        case MAIN: setupMainView(); break;
                        case SYSTEM_INFO: setupSystemInfo(); break;
                        case STARS: setupStarsList(); break;
                        case SHIPS: setupShipsList(); break;
                        case FACTIONS: setupFactionLookup(); break;
                        default: finish();
                    }
                } else {
                    finish();
                }
            }
        });

        if (savedInstanceState != null) {
            ArrayList<String> saved = savedInstanceState.getStringArrayList("navStack");
            if (saved != null && !saved.isEmpty()) {
                navStack.clear();
                for (int i = saved.size() - 1; i >= 0; i--) {
                    try { navStack.push(Screen.valueOf(saved.get(i))); } catch (Exception ignored) {}
                }
                Screen top = navStack.peek();
                if (top == null) { setupMainView(); return; }
                switch (top) {
                    case SYSTEM_INFO: setupSystemInfo(); break;
                    case STARS: setupStarsList(); break;
                    case SHIPS: setupShipsList(); break;
                    case FACTIONS: setupFactionLookup(); break;
                    default: setupMainView();
                }
                return;
            }
        }

        setupMainView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (systemRepository != null) systemRepository.shutdown();
        if (serverStatusManager != null) serverStatusManager.shutdown();
        stopAnimations();
    }

    private void stopAnimations() {
        try {
            android.view.View v = findViewById(R.id.star_image);
            if (v instanceof android.widget.ImageView) {
                com.example.docknet.ui.AnimationHelper.stopImageAnimation((android.widget.ImageView) v);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!navStack.isEmpty()) {
            ArrayList<String> list = new ArrayList<>();
            for (Screen s : navStack) list.add(s.name());
            outState.putStringArrayList("navStack", list);
        }
    }

    private void setupMainView() {
        stopAnimations();
        setContentView(R.layout.activity_main);

        ServerStatusController serverStatusController = new ServerStatusController(this, serverStatusManager);
        serverStatusController.setup();

        bindButton(R.id.change_to_system_info, this::setupSystemInfo);
        bindButton(R.id.change_to_stars_list, this::setupStarsList);
        bindButton(R.id.change_to_ships_list, this::setupShipsList);
        bindButton(R.id.change_to_faction_lookup, this::setupFactionLookup);

        pushScreen(Screen.MAIN);
    }

    private void setupSystemInfo() {
        stopAnimations();
        setContentView(R.layout.system_info);
        AnimationHelper.setupImageAnimation(findViewById(R.id.star_image));

        SystemInfoController systemInfoController = new SystemInfoController(this, systemRepository);
        systemInfoController.setup();

        setupReturn();
        pushScreen(Screen.SYSTEM_INFO);
    }

    private void setupStarsList() {
        stopAnimations();
        setContentView(R.layout.stars_list);
        StarsController starsController = new StarsController(this);
        starsController.setup();

        setupReturn();
        pushScreen(Screen.STARS);
    }

    private void setupShipsList() {
        stopAnimations();
        setContentView(R.layout.ships_list);
        ShipsController controller = new ShipsController(this);
        controller.setup();

        setupReturn();
        pushScreen(Screen.SHIPS);
    }

    private void setupFactionLookup() {
        stopAnimations();
        setContentView(R.layout.faction_lookup);
        FactionController controller = new FactionController(this);
        controller.setup();
        setupReturn();
        pushScreen(Screen.FACTIONS);
    }

    private void setupReturn() {
        TextView title = findViewById(R.id.title_text);
        if (title != null) title.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v == null) v = findViewById(android.R.id.content);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && v != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            v.clearFocus();
        }
    }

    private void bindButton(int resId, Runnable action) {
        View v = findViewById(resId);
        if (v instanceof Button) v.setOnClickListener(x -> action.run());
    }

    public void openBrowser(View view){

        //Get url from tag
        String url = (String)view.getTag();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        //pass the url to intent data
        intent.setData(Uri.parse(url));

        startActivity(intent);
    }
}
