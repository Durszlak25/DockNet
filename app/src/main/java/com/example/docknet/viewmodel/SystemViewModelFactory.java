package com.example.docknet.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.docknet.data.SystemRepository;

public class SystemViewModelFactory implements ViewModelProvider.Factory {
    private final SystemRepository repository;

    public SystemViewModelFactory(SystemRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SystemViewModel.class)) {
            return (T) new SystemViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

