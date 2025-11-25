package com.example.docknet.ui;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.docknet.R;
import com.example.docknet.data.SystemRepository;
import com.example.docknet.viewmodel.SystemViewModel;
import com.example.docknet.viewmodel.SystemViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class SystemInfoController {
    private final AppCompatActivity activity;
    private final SystemRepository repository;
    private SystemViewModel viewModel;
    private SystemAdapter adapter;
    private String lastRequestedSystem = null;

    public SystemInfoController(AppCompatActivity activity, SystemRepository repository) {
        this.activity = activity;
        this.repository = repository;
    }

    public void setup() {
        TextView result = activity.findViewById(R.id.result);
        EditText searchList = activity.findViewById(R.id.searchList);
        RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
        ImageView starImage = activity.findViewById(R.id.star_image);

        AnimationHelper.setupImageAnimation(starImage);

        adapter = new SystemAdapter((position, text) -> {
            // hide keyboard when a system is chosen from the list
            hideKeyboard();
            fetchSystem(text);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(activity, new SystemViewModelFactory(repository)).get(SystemViewModel.class);
        // clear any previous selection only on the very first enter (so rotation keeps the current selection)
        if (viewModel.shouldClearSelectionOnEnter()) {
            viewModel.clearSelection();
            viewModel.markInitialized();
        }

        viewModel.getSystems().observe(activity, list -> {
            if (adapter != null) adapter.submitList(list != null ? new ArrayList<>(list) : java.util.Collections.emptyList());
        });
        viewModel.getSelectedSystem().observe(activity, sr -> {
            if (sr != null) {
                String displayText = com.example.docknet.data.SystemParser.formatSystemInfo(sr.info);
                result.setText(displayText);
                Integer imageResId = com.example.docknet.ui.StarImageMapper.getResId(sr.primaryStarType);
                if (imageResId != null) {
                    starImage.setVisibility(View.VISIBLE);
                    Glide.with(activity).load(imageResId).into(starImage);
                } else {
                    starImage.setVisibility(View.GONE);
                }
            } else {
                // clear UI when there's no selected system (e.g. on enter)
                if (result != null) result.setText("");
                if (starImage != null) starImage.setVisibility(View.GONE);
            }
        });
        // progress bar binding
        android.widget.ProgressBar progress = activity.findViewById(R.id.system_progress);
        viewModel.getLoading().observe(activity, isLoading -> {
            if (progress != null) progress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(activity, err -> {
            if (err != null) {
                if (result != null) {
                    result.setText(activity.getString(R.string.error_fetching_system_retry, err));
                    result.setClickable(true);
                    result.setOnClickListener(v -> {
                        if (lastRequestedSystem != null) fetchSystem(lastRequestedSystem);
                    });
                }
            }
        });

        // wire search box to viewModel
        searchList.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { search(s.toString()); }
        });

        // init
        // no master items: UI starts empty
    }

    public void search(String name) {
        if (name.length() < 3) {
            if (adapter != null) adapter.submitList(new ArrayList<>());
            return;
        }
        if (viewModel != null) viewModel.search(name);
    }

    public void fetchSystem(String name) {
        lastRequestedSystem = name;
        if (viewModel != null) viewModel.fetchSystem(name);
    }

    public void teardown() {
        // nothing for now
    }

    private void hideKeyboard() {
        View v = activity.getCurrentFocus();
        if (v == null) v = activity.findViewById(android.R.id.content);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && v != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            v.clearFocus();
        }
    }
}
