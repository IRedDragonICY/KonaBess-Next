package com.ireddragonicy.konabessnext.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ireddragonicy.konabessnext.core.KonaBessCore;
import com.ireddragonicy.konabessnext.core.TableIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Import/Export operations.
 * Manages export history and operation state.
 */
public class ImportExportViewModel extends ViewModel {

    // State
    private final MutableLiveData<UiState<Void>> operationState = new MutableLiveData<>();
    private final MutableLiveData<List<String>> exportHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> hasUnsavedChanges = new MutableLiveData<>(false);

    // Events
    private final MutableLiveData<Event<String>> successEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> errorEvent = new MutableLiveData<>();

    public ImportExportViewModel() {
        // Check if device is prepared
        updateExportAvailability();
    }

    // ========================================================================
    // LiveData Getters
    // ========================================================================

    public LiveData<UiState<Void>> getOperationState() {
        return operationState;
    }

    public LiveData<List<String>> getExportHistory() {
        return exportHistory;
    }

    public LiveData<Boolean> getHasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public LiveData<Event<String>> getSuccessEvent() {
        return successEvent;
    }

    public LiveData<Event<String>> getErrorEvent() {
        return errorEvent;
    }

    // ========================================================================
    // Operations
    // ========================================================================

    /**
     * Check if export actions should be available.
     */
    public boolean isExportAvailable() {
        return KonaBessCore.isPrepared() && KonaBessCore.dts_path != null;
    }

    /**
     * Update export availability state.
     */
    public void updateExportAvailability() {
        hasUnsavedChanges.setValue(isExportAvailable());
    }

    /**
     * Add entry to export history.
     */
    public void addExportHistoryEntry(String entry) {
        List<String> history = exportHistory.getValue();
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(0, entry);

        // Limit history size
        while (history.size() > 20) {
            history.remove(history.size() - 1);
        }

        exportHistory.setValue(history);
    }

    /**
     * Clear export history.
     */
    public void clearExportHistory() {
        exportHistory.setValue(new ArrayList<>());
    }

    /**
     * Get the current DTS path if available.
     */
    public String getDtsPath() {
        return KonaBessCore.dts_path;
    }

    /**
     * Check if device is prepared for export.
     */
    public boolean isDevicePrepared() {
        return KonaBessCore.isPrepared();
    }

    /**
     * Set operation loading state.
     */
    public void setLoading() {
        operationState.setValue(UiState.loading());
    }

    /**
     * Set operation success state.
     */
    public void setSuccess(String message) {
        operationState.setValue(UiState.success(null));
        successEvent.setValue(Event.of(message));
    }

    /**
     * Set operation error state.
     */
    public void setError(String message) {
        operationState.setValue(UiState.error(message));
        errorEvent.setValue(Event.of(message));
    }
}



