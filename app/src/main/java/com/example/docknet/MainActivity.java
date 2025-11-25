package com.example.docknet;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.docknet.data.SystemRepository;
import com.example.docknet.network.NetworkClient;
import com.example.docknet.network.ServerStatusManager;
import com.example.docknet.ui.AnimationHelper;
import com.example.docknet.ui.SystemInfoController;
import com.example.docknet.ui.ServerStatusController;
import com.example.docknet.ui.StarsController;
import com.example.docknet.ui.ShipsController;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class MainActivity extends AppCompatActivity {

    private TextView title;

    private final NetworkClient networkClient = new com.example.docknet.network.RetrofitNetworkClient();
    private final SystemRepository systemRepository = new SystemRepository(networkClient);
    private final ServerStatusManager serverStatusManager = new ServerStatusManager(networkClient);
    private SystemInfoController systemInfoController;
    private ServerStatusController serverStatusController;
    private StarsController starsController;

    private enum Screen { MAIN, SYSTEM_INFO, STARS, SHIPS }
    private final Deque<Screen> navStack = new ArrayDeque<>();

    private void pushScreen(Screen s) {
        // push only if different from current top
        if (navStack.isEmpty() || navStack.peek() != s) {
            navStack.push(s);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // register back callback to handle navigation within single Activity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // hide keyboard first
                hideKeyboard();

                if (navStack.size() > 1) {
                    // pop current
                    navStack.pop();
                    Screen prev = navStack.peek();
                    if (prev == Screen.MAIN) {
                        setupMainView();
                    } else if (prev == Screen.SYSTEM_INFO) {
                        setupSystemInfo();
                    } else if (prev == Screen.STARS) {
                        setupStarsList();
                    } else if (prev == Screen.SHIPS) {
                        setupShipsList();
                    } else {
                        finish();
                    }
                } else {
                    // nothing to go back to — finish activity
                    finish();
                }
            }
        });

        // restore nav stack if available (after rotation)
        if (savedInstanceState != null) {
            ArrayList<String> saved = savedInstanceState.getStringArrayList("navStack");
            if (saved != null && !saved.isEmpty()) {
                navStack.clear();
                // saved list has top at index 0 -> push from last to first to restore stack
                for (int i = saved.size() - 1; i >= 0; i--) {
                    try { navStack.push(Screen.valueOf(saved.get(i))); } catch (Exception ignored) {}
                }
                // show top
                Screen top = navStack.peek();
                if (top == Screen.SYSTEM_INFO) setupSystemInfo();
                else if (top == Screen.STARS) setupStarsList();
                else if (top == Screen.SHIPS) setupShipsList();
                else setupMainView();
            } else {
                setupMainView();
            }
        } else {
            setupMainView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // shutdown repository and cancel any network operations to avoid leaks
        systemRepository.shutdown();
        serverStatusManager.shutdown();
        if (systemInfoController != null) systemInfoController.teardown();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save navStack order (top at index 0)
        if (!navStack.isEmpty()) {
            ArrayList<String> list = new ArrayList<>();
            for (Screen s : navStack) list.add(s.name());
            outState.putStringArrayList("navStack", list);
        }
    }

    private void setupMainView() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // delegate server status to controller
        serverStatusController = new ServerStatusController(this, serverStatusManager);
        serverStatusController.setup();

        Button changeToSystemInfo = findViewById(R.id.change_to_system_info);
        changeToSystemInfo.setOnClickListener(v -> setupSystemInfo());

        Button changeToStarsList = findViewById(R.id.change_to_stars_list);
        changeToStarsList.setOnClickListener(v -> setupStarsList());

        Button changeToShipsList = findViewById(R.id.change_to_ships_list);
        changeToShipsList.setOnClickListener(v -> setupShipsList());

        // navigation stack update
        pushScreen(Screen.MAIN);
    }

    private void setupSystemInfo() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.system_info);
        AnimationHelper.setupImageAnimation(findViewById(R.id.star_image));
        // delegate detailed wiring to SystemInfoController to keep Activity slim
        systemInfoController = new SystemInfoController(this, systemRepository);
        systemInfoController.setup();
        setupReturn();

        // navigation stack update
        pushScreen(Screen.SYSTEM_INFO);
    }

    private void setupStarsList() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.stars_list);
        starsController = new StarsController(this);
        starsController.setup();

        setupReturn();

        // navigation stack update
        pushScreen(Screen.STARS);
    }

    private void setupShipsList() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.ships_list);
        ShipsController controller = new ShipsController(this);
        controller.setup();
        setupReturn();

        pushScreen(Screen.SHIPS);
    }

    private void setupReturn() {
        title = findViewById(R.id.title_text);
        title.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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

    // search/filter logic moved into SystemInfoController
}
