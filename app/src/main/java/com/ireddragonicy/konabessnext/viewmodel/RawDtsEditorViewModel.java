package com.ireddragonicy.konabessnext.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Raw DTS Editor.
 * Manages DTS content state, search, and modification tracking.
 */
public class RawDtsEditorViewModel extends ViewModel {

    // Content state
    private final MutableLiveData<String> content = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDirty = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Search state
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<List<SearchResult>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> currentSearchIndex = new MutableLiveData<>(-1);

    // Editor state
    private final MutableLiveData<Integer> cursorPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> scrollPosition = new MutableLiveData<>(0);

    // Events
    private final MutableLiveData<Event<String>> saveEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> errorEvent = new MutableLiveData<>();

    private String originalContent = "";

    public RawDtsEditorViewModel() {
        // Initial state
    }

    // ========================================================================
    // LiveData Getters
    // ========================================================================

    public LiveData<String> getContent() {
        return content;
    }

    public LiveData<Boolean> getIsDirty() {
        return isDirty;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<List<SearchResult>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Integer> getCurrentSearchIndex() {
        return currentSearchIndex;
    }

    public LiveData<Event<String>> getSaveEvent() {
        return saveEvent;
    }

    public LiveData<Event<String>> getErrorEvent() {
        return errorEvent;
    }

    // ========================================================================
    // Content Operations
    // ========================================================================

    /**
     * Load content from DTS file.
     */
    public void loadContent(String dtsContent) {
        originalContent = dtsContent != null ? dtsContent : "";
        content.setValue(originalContent);
        isDirty.setValue(false);
    }

    /**
     * Update content from editor.
     */
    public void updateContent(String newContent) {
        content.setValue(newContent);
        isDirty.setValue(!newContent.equals(originalContent));
    }

    /**
     * Mark content as saved.
     */
    public void markAsSaved() {
        originalContent = content.getValue();
        isDirty.setValue(false);
    }

    /**
     * Check if content has unsaved changes.
     */
    public boolean hasUnsavedChanges() {
        Boolean dirty = isDirty.getValue();
        return dirty != null && dirty;
    }

    // ========================================================================
    // Search Operations
    // ========================================================================

    /**
     * Perform search in content.
     */
    public void search(String query) {
        searchQuery.setValue(query);

        if (query == null || query.isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            currentSearchIndex.setValue(-1);
            return;
        }

        String text = content.getValue();
        if (text == null) {
            return;
        }

        List<SearchResult> results = new ArrayList<>();
        int index = 0;
        while ((index = text.indexOf(query, index)) != -1) {
            results.add(new SearchResult(index, query.length()));
            index += query.length();
        }

        searchResults.setValue(results);
        if (!results.isEmpty()) {
            currentSearchIndex.setValue(0);
        } else {
            currentSearchIndex.setValue(-1);
        }
    }

    /**
     * Navigate to next search result.
     */
    public void nextSearchResult() {
        List<SearchResult> results = searchResults.getValue();
        Integer current = currentSearchIndex.getValue();

        if (results == null || results.isEmpty() || current == null) {
            return;
        }

        int next = (current + 1) % results.size();
        currentSearchIndex.setValue(next);
    }

    /**
     * Navigate to previous search result.
     */
    public void previousSearchResult() {
        List<SearchResult> results = searchResults.getValue();
        Integer current = currentSearchIndex.getValue();

        if (results == null || results.isEmpty() || current == null) {
            return;
        }

        int prev = (current - 1 + results.size()) % results.size();
        currentSearchIndex.setValue(prev);
    }

    /**
     * Clear search.
     */
    public void clearSearch() {
        searchQuery.setValue("");
        searchResults.setValue(new ArrayList<>());
        currentSearchIndex.setValue(-1);
    }

    /**
     * Get current search result for highlighting.
     */
    public SearchResult getCurrentResult() {
        List<SearchResult> results = searchResults.getValue();
        Integer current = currentSearchIndex.getValue();

        if (results == null || results.isEmpty() || current == null || current < 0) {
            return null;
        }

        return results.get(current);
    }

    // ========================================================================
    // Editor State
    // ========================================================================

    public void setCursorPosition(int position) {
        cursorPosition.setValue(position);
    }

    public int getCursorPositionValue() {
        Integer pos = cursorPosition.getValue();
        return pos != null ? pos : 0;
    }

    public void setScrollPosition(int position) {
        scrollPosition.setValue(position);
    }

    public int getScrollPositionValue() {
        Integer pos = scrollPosition.getValue();
        return pos != null ? pos : 0;
    }

    // ========================================================================
    // Search Result Model
    // ========================================================================

    public static class SearchResult {
        public final int startIndex;
        public final int length;

        public SearchResult(int startIndex, int length) {
            this.startIndex = startIndex;
            this.length = length;
        }
    }
}



