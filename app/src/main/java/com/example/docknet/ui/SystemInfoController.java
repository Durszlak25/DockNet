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

/**
 * SystemInfoController - prosty, krok-po-kroku kontroler widoku 'system_info'.
 * Cel refaktoryzacji: uczynić kod łatwiejszym do wytłumaczenia i przejrzenia.
 * Zamiast jednej długiej metody "setup" rozbijamy logikę na małe, nazwane funkcje
 * (inicjalizacja widoków, adaptera, obserwatorów, watcher tekstu, itd.).
 * Zachowanie nie zostało zmienione — tylko struktura kodu i komentarze.
 */
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

    /**
     * Główna metoda konfigurująca ekran. Jest to wywoływane przy każdym wejściu na ekran.
     * Ta metoda teraz deleguje pracę do wyraźnych, krótkich pomocników — łatwiej to
     * opisać i wytłumaczyć w prezentacji.
     */
    public void setup() {
        // Pobierz referencje widoków (szybki, czytelny blok)
        TextView result = activity.findViewById(R.id.result);
        EditText searchList = activity.findViewById(R.id.searchList);
        RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
        ImageView starImage = activity.findViewById(R.id.star_image);
        View root = activity.findViewById(R.id.main);

        // Jeśli pole wyszukiwania jest puste, natychmiast wyczyść interfejs, by
        // uniknąć chwilowego wyświetlenia starego wyboru (tzw. flash).
        String initialQuery = searchList.getText() != null ? searchList.getText().toString().trim() : "";

        // 1) Przygotuj UI tak, żeby nic nie migało — ukryj root i wyłącz restore state
        if (root != null) root.setVisibility(View.INVISIBLE);
        disableViewStateRestore(result, searchList, recyclerView, starImage);
        if (initialQuery.isEmpty()) {
            clearUiImmediately(result, starImage, searchList);
        }

        // 2) Inicjalizacja elementów (animacje, adapter, layout manager)
        AnimationHelper.setupImageAnimation(starImage);
        initAdapter(recyclerView);

        // 3) Przygotuj ViewModel i obserwatory
        viewModel = new ViewModelProvider(activity, new SystemViewModelFactory(repository)).get(SystemViewModel.class);
        if (initialQuery.isEmpty()) viewModel.clearSelection(); // jeśli nie ma query, usuń selekcję
        if (viewModel.shouldClearSelectionOnEnter()) {
            viewModel.clearSelection();
            viewModel.markInitialized();
        }
        attachObservers(result, starImage);

        // 4) Ustaw watcher dla pola wyszukiwania
        setupSearchWatcher(searchList);

        // 5) Na końcu, jeśli query jest pusty, upewnij się, że adapter i UI są puste;
        //    w przeciwnym razie uruchom wyszukiwanie dla istniejącego query.
        if (initialQuery.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            lastRequestedSystem = null;
            clearUiImmediately(result, starImage, searchList);
        } else {
            search(initialQuery);
        }

        // Zakończ inicjalizację i pokaż widok
        if (root != null) root.setVisibility(View.VISIBLE);
    }

    // --------------------------- Helper methods ---------------------------

    // Wyłącza automatyczne zapisywanie/przywracanie stanu widoków — upraszcza zachowanie
    private void disableViewStateRestore(View result, EditText searchList, RecyclerView recyclerView, ImageView starImage) {
        if (result != null) result.setSaveEnabled(false);
        if (searchList != null) searchList.setSaveEnabled(false);
        if (recyclerView != null) recyclerView.setSaveEnabled(false);
        if (starImage != null) starImage.setSaveEnabled(false);
    }

    // Czyści UI natychmiast (używane gdy nie ma zapytania w polu wyszukiwania)
    private void clearUiImmediately(TextView result, ImageView starImage, EditText searchList) {
        if (result != null) result.setText("");
        if (starImage != null) starImage.setVisibility(View.GONE);
        if (searchList != null) searchList.setText("");
    }

    // Inicjalizuje adapter i przypisuje go do RecyclerView
    private void initAdapter(RecyclerView recyclerView) {
        adapter = new SystemAdapter((position, text) -> {
            hideKeyboard();
            fetchSystem(text);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
        // początkowo pusty - nie pokazujemy nic przed załadowaniem
        adapter.submitList(new ArrayList<>());
    }

    // Podłącz obserwatory LiveData (systems, selectedSystem, loading, error)
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
            String displayText = SystemParser.formatSystemInfo(info);
            if (sum != null) displayText += "\nDistance to Sol " + String.format(java.util.Locale.US, "%.2f", sum.distanceToSol);
            if (result != null) result.setText(displayText);

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
        }
    }

    // Ustawia prostego TextWatcher, który wywołuje `search()` po zmianie tekstu
    private void setupSearchWatcher(EditText searchList) {
        searchList.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { search(s.toString()); }
        });
    }

    // --------------------------- Existing public API ---------------------------
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
}
