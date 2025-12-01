package com.example.docknet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.docknet.data.SystemRepository;
import com.example.docknet.model.SystemInfo;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class SystemViewModel extends ViewModel {
    private final SystemRepository repository;

    private final MutableLiveData<List<String>> systems = new MutableLiveData<>();
    private final MutableLiveData<SystemInfo> selectedSystem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private boolean initialized = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRunnable = null;
    private static final long DEBOUNCE_MS = 300;

    public SystemViewModel(SystemRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<String>> getSystems() { return systems; }
    public LiveData<SystemInfo> getSelectedSystem() { return selectedSystem; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void search(final String query) {
        if (pendingRunnable != null) {
            handler.removeCallbacks(pendingRunnable);
        }
        pendingRunnable = () -> doSearch(query);
        handler.postDelayed(pendingRunnable, DEBOUNCE_MS);
    }

    private void doSearch(String query) {
        loading.postValue(true);
        error.postValue(null);
        repository.searchSystems(query, new SystemRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(List<String> result) {
                systems.postValue(result);
                loading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                systems.postValue(null);
                loading.postValue(false);
            }
        });
    }

    public void fetchSystem(String name) {
        loading.postValue(true);
        error.postValue(null);
        repository.getSystemInfo(name, new SystemRepository.RepositoryCallback<SystemInfo>() {
            @Override
            public void onSuccess(SystemInfo result) {
                selectedSystem.postValue(result);
                loading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                selectedSystem.postValue(null);
                loading.postValue(false);
            }
        });
    }

    public void clearSelection() {
        selectedSystem.postValue(null);
        error.postValue(null);
        loading.postValue(false);
    }

    public boolean shouldClearSelectionOnEnter() {
        return !initialized;
    }

    public void markInitialized() {
        initialized = true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdown();
        try {
            if (pendingRunnable != null) {
                handler.removeCallbacks(pendingRunnable);
                pendingRunnable = null;
            }
        } catch (Exception ignored) {}
    }
}
