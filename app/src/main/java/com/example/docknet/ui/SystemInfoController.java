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
import com.example.docknet.data.SystemParser;
import com.example.docknet.viewmodel.SystemViewModel;
import com.example.docknet.viewmodel.SystemViewModelFactory;

import java.util.ArrayList;
import android.text.Html;
import java.util.Map;
import java.util.Locale;

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
        View root = activity.findViewById(R.id.main);

        String initialQuery = searchList.getText() != null ? searchList.getText().toString().trim() : "";

        if (root != null) root.setVisibility(View.INVISIBLE);
        disableViewStateRestore(result, searchList, recyclerView, starImage);
        if (initialQuery.isEmpty()) {
            clearUiImmediately(result, starImage, searchList);
        }

        AnimationHelper.setupImageAnimation(starImage);
        initAdapter(recyclerView);

        viewModel = new ViewModelProvider(activity, new SystemViewModelFactory(repository)).get(SystemViewModel.class);
        if (initialQuery.isEmpty()) viewModel.clearSelection();
        if (viewModel.shouldClearSelectionOnEnter()) {
            viewModel.clearSelection();
            viewModel.markInitialized();
        }
        attachObservers(result, starImage);

        setupSearchWatcher(searchList);

        if (initialQuery.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            lastRequestedSystem = null;
            clearUiImmediately(result, starImage, searchList);
        } else {
            search(initialQuery);
        }

        if (root != null) root.setVisibility(View.VISIBLE);
    }

    private void disableViewStateRestore(View result, EditText searchList, RecyclerView recyclerView, ImageView starImage) {
        if (result != null) result.setSaveEnabled(false);
        if (searchList != null) searchList.setSaveEnabled(false);
        if (recyclerView != null) recyclerView.setSaveEnabled(false);
        if (starImage != null) starImage.setSaveEnabled(false);
    }

    private void clearUiImmediately(TextView result, ImageView starImage, EditText searchList) {
        if (result != null) result.setText("");
        if (starImage != null) starImage.setVisibility(View.GONE);
        if (searchList != null) searchList.setText("");
    }

    private void initAdapter(RecyclerView recyclerView) {
        adapter = new SystemAdapter((position, text) -> {
            hideKeyboard();
            fetchSystem(text);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
        adapter.submitList(new ArrayList<>());
    }

    private void attachObservers(TextView result, ImageView starImage) {
        viewModel.getSystems().observe(activity, this::onSystemsUpdated);
        viewModel.getSelectedSystem().observe(activity, info -> onSelectedSystemChanged(info, result, starImage));
        android.widget.ProgressBar progress = activity.findViewById(R.id.system_progress);
        viewModel.getLoading().observe(activity, isLoading -> onLoadingChanged(isLoading, progress));
        viewModel.getError().observe(activity, err -> onErrorChanged(err, result));
    }

    private void onSystemsUpdated(java.util.List<String> list) {
        if (adapter != null) adapter.submitList(list != null ? new ArrayList<>(list) : java.util.Collections.emptyList());
    }

    private void onSelectedSystemChanged(com.example.docknet.model.SystemInfo info, TextView result, ImageView starImage) {
        if (info != null) {
            com.example.docknet.model.SystemSummary sum = SystemParser.toSummary(info);
            String html = buildHtmlFromInfo(info, sum);
            if (result != null) result.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));

            Integer imageResId = com.example.docknet.ui.StarImageMapper.getResId(info.primaryStarType);
            if (imageResId != null && starImage != null) {
                starImage.setVisibility(View.VISIBLE);
                Glide.with(activity).load(imageResId).into(starImage);
            } else if (starImage != null) {
                starImage.setVisibility(View.GONE);
            }
        } else {
            if (result != null) result.setText("");
            if (starImage != null) starImage.setVisibility(View.GONE);
        }
    }

    private String buildHtmlFromInfo(com.example.docknet.model.SystemInfo info, com.example.docknet.model.SystemSummary sum) {
        if (info == null) return "";
        StringBuilder sb = new StringBuilder();
        String header = info.name != null ? info.name : "Unknown";
        if (info.primaryStarType != null && !info.primaryStarType.isEmpty()) header += " — " + info.primaryStarType;
        if (info.isScoopable) header += " (scoopable)";
        sb.append("<b><big>").append(Html.escapeHtml(header)).append("</big></b><br/>");

        Map<String, String> infoMap = info.information;
        appendLabelHtml(sb, "Star", info.primaryStarName);
        String coords = String.format(Locale.US, "[%.2f, %.2f, %.2f]%s",
                info.x, info.y, info.z, info.coordsLocked ? " (locked)" : "");
        appendLabelHtml(sb, "Coords", coords);

        if (infoMap != null) {
            appendLabelHtml(sb, "Allegiance", infoMap.get("allegiance"));
            appendLabelHtml(sb, "Government", infoMap.get("government"));
            appendLabelHtml(sb, "Faction", infoMap.get("faction"));
            appendLabelHtml(sb, "Faction State", infoMap.get("factionState"));
            if (info.population >= 0) appendLabelHtml(sb, "Population", String.format(Locale.US, "%,d", info.population));
            appendLabelHtml(sb, "Security", infoMap.get("security"));
            String economy = infoMap.get("economy");
            String secondEconomy = infoMap.get("secondEconomy");
            if (economy != null && !economy.isEmpty()) {
                String econ = economy;
                if (secondEconomy != null && !secondEconomy.isEmpty()) econ += " / " + secondEconomy;
                appendLabelHtml(sb, "Economy", econ);
            }
            appendLabelHtml(sb, "Reserve", infoMap.get("reserve"));
        }

        if (sum != null) appendLabelHtml(sb, "Distance to Sol", String.format(Locale.US, "%.2f ly", sum.distanceToSol));

        return sb.toString();
    }

    private void appendLabelHtml(StringBuilder sb, String label, String value) {
        if (value == null || value.isEmpty()) return;
        sb.append("<b>").append(Html.escapeHtml(label)).append(":</b> ")
          .append(Html.escapeHtml(value)).append("<br/>");
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

    private void hideKeyboard() {
        View v = activity.getCurrentFocus();
        if (v == null) v = activity.findViewById(android.R.id.content);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && v != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            v.clearFocus();
        }
    }

    private void onLoadingChanged(Boolean isLoading, android.widget.ProgressBar progress) {
        if (progress != null) progress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
    }

    private void onErrorChanged(String err, TextView result) {
        if (err != null) {
            if (result != null) {
                result.setText(activity.getString(R.string.error_fetching_system_retry, err));
                result.setClickable(true);
                result.setOnClickListener(v -> {
                    if (lastRequestedSystem != null) fetchSystem(lastRequestedSystem);
                });
            }
        } else if (result != null) {
            result.setClickable(false);
            result.setOnClickListener(null);
        }
    }

    private void setupSearchWatcher(EditText searchList) {
        if (searchList == null) return;
        searchList.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { search(s.toString()); }
        });
    }
}
