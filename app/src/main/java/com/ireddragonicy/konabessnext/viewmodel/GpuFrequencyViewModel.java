package com.ireddragonicy.konabessnext.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ireddragonicy.konabessnext.R;
import com.ireddragonicy.konabessnext.ui.SettingsActivity;
import com.ireddragonicy.konabessnext.model.Bin;
import com.ireddragonicy.konabessnext.model.EditorState;
import com.ireddragonicy.konabessnext.model.Level;
import com.ireddragonicy.konabessnext.repository.GpuTableRepository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel for GPU Frequency editing.
 * Manages state for bins, levels, undo/redo, and dirty tracking.
 * Survives configuration changes and provides observable state.
 */
public class GpuFrequencyViewModel extends ViewModel {

    private static final int MAX_HISTORY_SIZE = 50;

    // Repository
    private final GpuTableRepository repository;

    // UI State
    private final MutableLiveData<UiState<List<Bin>>> binsState = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedBinIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<Integer> selectedLevelIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<Boolean> isDirty = new MutableLiveData<>(false);

    // Undo/Redo State
    private final Deque<EditorState> undoStack = new ArrayDeque<>();
    private final Deque<EditorState> redoStack = new ArrayDeque<>();
    private final MutableLiveData<Boolean> canUndo = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> canRedo = new MutableLiveData<>(false);

    // History
    private final MutableLiveData<List<String>> changeHistory = new MutableLiveData<>(new ArrayList<>());

    // Events (one-time)
    private final MutableLiveData<Event<String>> toastEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> errorEvent = new MutableLiveData<>();

    // Saved signature for dirty detection
    private String lastSavedSignature = null;

    public GpuFrequencyViewModel() {
        this.repository = new GpuTableRepository();
    }

    // ========================================================================
    // LiveData Getters (Observe from UI)
    // ========================================================================

    public LiveData<UiState<List<Bin>>> getBinsState() {
        return binsState;
    }

    public LiveData<Integer> getSelectedBinIndex() {
        return selectedBinIndex;
    }

    public LiveData<Integer> getSelectedLevelIndex() {
        return selectedLevelIndex;
    }

    public LiveData<Boolean> getIsDirty() {
        return isDirty;
    }

    public LiveData<Boolean> getCanUndo() {
        return canUndo;
    }

    public LiveData<Boolean> getCanRedo() {
        return canRedo;
    }

    public LiveData<List<String>> getChangeHistory() {
        return changeHistory;
    }

    public LiveData<Event<String>> getToastEvent() {
        return toastEvent;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return errorEvent;
    }

    // ========================================================================
    // Data Loading
    // ========================================================================

    /**
     * Load GPU table data from DTS file.
     */
    public void loadData() {
        binsState.setValue(UiState.loading());

        try {
            repository.init();
            repository.decode();

            List<Bin> bins = repository.getBins();
            lastSavedSignature = computeSignature();

            binsState.setValue(UiState.success(bins));
            resetState();
        } catch (IOException e) {
            binsState.setValue(UiState.error("Failed to read DTS file: " + e.getMessage(), e));
        } catch (Exception e) {
            binsState.setValue(UiState.error("Failed to decode GPU table: " + e.getMessage(), e));
        }
    }

    /**
     * Get bins directly (for adapters).
     */
    public List<Bin> getBins() {
        UiState<List<Bin>> state = binsState.getValue();
        if (state != null && state.isSuccess()) {
            return state.getDataOrNull();
        }
        return new ArrayList<>();
    }

    // ========================================================================
    // Navigation
    // ========================================================================

    public void selectBin(int index) {
        selectedBinIndex.setValue(index);
        selectedLevelIndex.setValue(-1);
    }

    public void selectLevel(int levelIndex) {
        selectedLevelIndex.setValue(levelIndex);
    }

    public void navigateBack() {
        Integer levelIdx = selectedLevelIndex.getValue();
        Integer binIdx = selectedBinIndex.getValue();

        if (levelIdx != null && levelIdx >= 0) {
            selectedLevelIndex.setValue(-1);
        } else if (binIdx != null && binIdx >= 0) {
            selectedBinIndex.setValue(-1);
        }
    }

    public boolean isAtBinList() {
        Integer binIdx = selectedBinIndex.getValue();
        return binIdx == null || binIdx < 0;
    }

    public boolean isAtLevelList() {
        Integer binIdx = selectedBinIndex.getValue();
        Integer levelIdx = selectedLevelIndex.getValue();
        return binIdx != null && binIdx >= 0 && (levelIdx == null || levelIdx < 0);
    }

    // ========================================================================
    // Edit Operations
    // ========================================================================

    /**
     * Update a parameter in a level.
     */
    public void updateParameter(int binIndex, int levelIndex, String paramKey, String newValue) {
        captureStateForUndo();

        List<Bin> bins = getBins();
        if (binIndex < 0 || binIndex >= bins.size())
            return;

        Bin bin = bins.get(binIndex);
        if (levelIndex < 0 || levelIndex >= bin.getLevelCount())
            return;

        Level level = bin.getLevel(levelIndex);
        List<String> lines = level.getLines();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(paramKey)) {
                // Parse and replace value
                String updated = line.replaceAll("<[^>]+>", "<" + newValue + ">");
                lines.set(i, updated);
                break;
            }
        }

        completeChange("Updated " + paramKey);
        notifyBinsChanged();
    }

    /**
     * Add a new frequency level.
     */
    public void addFrequency(int binIndex, boolean atTop) {
        captureStateForUndo();

        List<Bin> bins = getBins();
        if (binIndex < 0 || binIndex >= bins.size())
            return;

        Bin bin = bins.get(binIndex);
        if (bin.getLevelCount() == 0)
            return;

        Level source = atTop ? bin.getLevel(0) : bin.getLevel(bin.getLevelCount() - 1);
        Level newLevel = source.copy();

        if (atTop) {
            bin.getLevels().add(0, newLevel);
        } else {
            bin.getLevels().add(newLevel);
        }

        String position = atTop ? "top" : "bottom";
        completeChange("Added frequency at " + position);
        notifyBinsChanged();
    }

    /**
     * Remove a frequency level.
     */
    public void removeFrequency(int binIndex, int levelIndex) {
        captureStateForUndo();

        List<Bin> bins = getBins();
        if (binIndex < 0 || binIndex >= bins.size())
            return;

        Bin bin = bins.get(binIndex);
        if (levelIndex < 0 || levelIndex >= bin.getLevelCount())
            return;

        Level removed = bin.getLevels().remove(levelIndex);
        long freq = removed.getFrequency();

        completeChange("Removed " + (freq / 1000000) + " MHz");
        notifyBinsChanged();
    }

    /**
     * Reorder frequency levels.
     */
    public void reorderFrequency(int binIndex, int fromPosition, int toPosition) {
        if (fromPosition == toPosition)
            return;

        captureStateForUndo();

        List<Bin> bins = getBins();
        if (binIndex < 0 || binIndex >= bins.size())
            return;

        Bin bin = bins.get(binIndex);
        List<Level> levels = bin.getLevels();

        if (fromPosition < 0 || fromPosition >= levels.size())
            return;
        if (toPosition < 0 || toPosition >= levels.size())
            return;

        Level level = levels.remove(fromPosition);
        levels.add(toPosition, level);

        completeChange("Reordered frequencies");
        notifyBinsChanged();
    }

    // ========================================================================
    // Undo/Redo
    // ========================================================================

    private void captureStateForUndo() {
        EditorState state = new EditorState(
                repository.getLinesInDts(),
                repository.getBins(),
                repository.getBinPosition());

        undoStack.push(state);
        while (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeLast();
        }
        redoStack.clear();
        updateUndoRedoState();
    }

    public void undo() {
        if (undoStack.isEmpty())
            return;

        // Save current state to redo
        EditorState current = new EditorState(
                repository.getLinesInDts(),
                repository.getBins(),
                repository.getBinPosition());
        redoStack.push(current);
        while (redoStack.size() > MAX_HISTORY_SIZE) {
            redoStack.removeLast();
        }

        // Restore previous state
        EditorState previous = undoStack.pop();
        restoreState(previous);

        addHistoryEntry("Undo");
        updateUndoRedoState();
        notifyBinsChanged();
    }

    public void redo() {
        if (redoStack.isEmpty())
            return;

        // Save current state to undo
        EditorState current = new EditorState(
                repository.getLinesInDts(),
                repository.getBins(),
                repository.getBinPosition());
        undoStack.push(current);

        // Restore redo state
        EditorState next = redoStack.pop();
        restoreState(next);

        addHistoryEntry("Redo");
        updateUndoRedoState();
        notifyBinsChanged();
    }

    private void restoreState(EditorState state) {
        repository.setLinesInDts(state.getLinesInDts());
        repository.setBins(state.getBins());
        repository.setBinPosition(state.getBinPosition());
        refreshDirtyState();
    }

    private void updateUndoRedoState() {
        canUndo.setValue(!undoStack.isEmpty());
        canRedo.setValue(!redoStack.isEmpty());
    }

    // ========================================================================
    // Save
    // ========================================================================

    /**
     * Save GPU table to DTS file.
     */
    public boolean save(Context context, boolean showToast) {
        try {
            List<String> fullDts = repository.generateFullDts();
            repository.writeOut(fullDts);

            lastSavedSignature = computeSignature();
            isDirty.setValue(false);

            if (showToast) {
                toastEvent.setValue(Event.of(context.getString(R.string.save_success)));
            }

            addHistoryEntry(context.getString(R.string.history_manual_save));
            return true;
        } catch (IOException e) {
            errorEvent.setValue(Event.of(R.string.save_failed));
            return false;
        }
    }

    /**
     * Auto-save if enabled in settings.
     */
    public void autoSaveIfEnabled(Context context) {
        if (!SettingsActivity.isAutoSaveEnabled(context)) {
            return;
        }

        boolean success = save(context, false);
        if (success) {
            addHistoryEntry(context.getString(R.string.history_auto_saved));
        }
    }

    // ========================================================================
    // Dirty State
    // ========================================================================

    private void completeChange(String description) {
        addHistoryEntry(description);
        isDirty.setValue(true);
        updateUndoRedoState();
    }

    private void refreshDirtyState() {
        if (lastSavedSignature == null) {
            isDirty.setValue(true);
            return;
        }
        boolean dirty = !lastSavedSignature.equals(computeSignature());
        isDirty.setValue(dirty);
    }

    private String computeSignature() {
        List<String> snapshot = repository.generateFullDts();
        StringBuilder sb = new StringBuilder();
        for (String line : snapshot) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public boolean hasPendingChanges() {
        Boolean dirty = isDirty.getValue();
        return dirty != null && dirty;
    }

    // ========================================================================
    // History
    // ========================================================================

    private void addHistoryEntry(String description) {
        if (description == null || description.trim().isEmpty())
            return;

        List<String> history = changeHistory.getValue();
        if (history == null)
            history = new ArrayList<>();

        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        history.add(0, timestamp + " • " + description.trim());

        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }

        changeHistory.setValue(history);
    }

    public void clearHistory() {
        changeHistory.setValue(new ArrayList<>());
    }

    // ========================================================================
    // State Management
    // ========================================================================

    private void resetState() {
        undoStack.clear();
        redoStack.clear();
        changeHistory.setValue(new ArrayList<>());
        lastSavedSignature = computeSignature();
        isDirty.setValue(false);
        updateUndoRedoState();
    }

    private void notifyBinsChanged() {
        List<Bin> bins = repository.getBins();
        binsState.setValue(UiState.success(bins));
    }

    public GpuTableRepository getRepository() {
        return repository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up if needed
    }
}



