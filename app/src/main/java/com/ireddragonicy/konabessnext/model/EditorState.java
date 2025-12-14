package com.ireddragonicy.konabessnext.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a snapshot of the GPU table editor state for undo/redo.
 * Unified model class used by both GpuTableEditor and GpuFrequencyViewModel.
 */
public class EditorState {
    // Public fields for direct access (GpuTableEditor compatibility)
    public ArrayList<String> linesInDts;
    public ArrayList<Bin> binsSnapshot;
    public int binPosition;

    /**
     * Default constructor for GpuTableEditor usage.
     */
    public EditorState() {
        this.linesInDts = new ArrayList<>();
        this.binsSnapshot = new ArrayList<>();
        this.binPosition = -1;
    }

    /**
     * Constructor with parameters for ViewModel usage.
     */
    public EditorState(List<String> linesInDts, List<Bin> bins, int binPosition) {
        this.linesInDts = linesInDts != null ? new ArrayList<>(linesInDts) : new ArrayList<>();
        this.binsSnapshot = deepCopyBins(bins);
        this.binPosition = binPosition;
    }

    // Getters for ViewModel usage
    public List<String> getLinesInDts() {
        return linesInDts;
    }

    public List<Bin> getBins() {
        return binsSnapshot;
    }

    public int getBinPosition() {
        return binPosition;
    }

    /**
     * Create a deep copy of bins list.
     */
    public static ArrayList<Bin> deepCopyBins(List<Bin> source) {
        if (source == null)
            return new ArrayList<>();
        ArrayList<Bin> copy = new ArrayList<>();
        for (Bin bin : source) {
            copy.add(bin.copy());
        }
        return copy;
    }
}
