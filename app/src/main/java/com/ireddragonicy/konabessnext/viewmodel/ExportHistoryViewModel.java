package com.ireddragonicy.konabessnext.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ireddragonicy.konabessnext.model.ExportHistoryItem;
import com.ireddragonicy.konabessnext.utils.ExportHistoryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Export History screen.
 * Manages history items state for MVVM pattern.
 */
public class ExportHistoryViewModel extends AndroidViewModel {

    private final ExportHistoryManager historyManager;
    private final MutableLiveData<List<ExportHistoryItem>> historyItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(true);

    public ExportHistoryViewModel(@NonNull Application application) {
        super(application);
        historyManager = new ExportHistoryManager(application);
        loadHistory();
    }

    // LiveData getters
    public LiveData<List<ExportHistoryItem>> getHistoryItems() {
        return historyItems;
    }

    public LiveData<Boolean> getIsEmpty() {
        return isEmpty;
    }

    // Load history from manager
    public void loadHistory() {
        List<ExportHistoryItem> items = historyManager.getHistory();
        historyItems.setValue(items);
        isEmpty.setValue(items.isEmpty());
    }

    // Clear all history
    public void clearHistory() {
        historyManager.clearHistory();
        loadHistory();
    }

    // Delete single item
    public void deleteItem(ExportHistoryItem item) {
        // ExportHistoryManager handles deletion
        loadHistory();
    }

    public ExportHistoryManager getHistoryManager() {
        return historyManager;
    }
}



