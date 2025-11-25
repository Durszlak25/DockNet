package com.example.docknet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.docknet.data.SystemRepository;
import com.example.docknet.data.SystemRepository.SystemResult;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SystemViewModel extends ViewModel {
    private final SystemRepository repository;

    private final MutableLiveData<List<String>> systems = new MutableLiveData<>();
    private final MutableLiveData<SystemResult> selectedSystem = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private volatile boolean initialized = false;

    // debouncer for search
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingSearch = null;
    private final Object searchLock = new Object();

    public SystemViewModel(SystemRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<String>> getSystems() { return systems; }
    public LiveData<SystemResult> getSelectedSystem() { return selectedSystem; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    /**
     * Schedule a search with debounce (defaults to 300ms). Cancel previous scheduled search.
     */
    public void search(final String query) {
        synchronized (searchLock) {
            if (pendingSearch != null && !pendingSearch.isDone()) {
                pendingSearch.cancel(false);
            }
            // schedule search after 300ms
            pendingSearch = scheduler.schedule(() -> doSearch(query), 300, TimeUnit.MILLISECONDS);
        }
    }

    private void doSearch(String query) {
        loading.postValue(true);
        error.postValue(null);
        repository.searchSystems(query, new SystemRepository.RepositoryCallback<List<String>>() {
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
        repository.getSystemInfo(name, new SystemRepository.RepositoryCallback<SystemResult>() {
            @Override
            public void onSuccess(SystemResult result) {
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

    /**
     * Clear currently selected system and any error state.
     * Should be called when entering the screen to avoid showing stale selection.
     */
    public void clearSelection() {
        selectedSystem.postValue(null);
        error.postValue(null);
        loading.postValue(false);
    }

    /**
     * Return true if we should clear selection on entering the screen (only the first time).
     */
    public synchronized boolean shouldClearSelectionOnEnter() {
        return !initialized;
    }

    /**
     * Mark the ViewModel as initialized (so subsequent enters — e.g. after rotation — won't clear selection).
     */
    public synchronized void markInitialized() {
        initialized = true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdown();
        try {
            synchronized (searchLock) {
                if (pendingSearch != null) pendingSearch.cancel(false);
            }
            scheduler.shutdownNow();
        } catch (Exception ignored) {}
    }
}
