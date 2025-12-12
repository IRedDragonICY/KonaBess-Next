package com.ireddragonicy.konabessnext.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Manages undo/redo state for the text editor.
 * Uses snapshot-based approach where full state is captured on line focus
 * change.
 */
public class TextEditorStateManager {

    private static final int MAX_HISTORY = 50;

    private final Stack<List<String>> undoStack;
    private final Stack<List<String>> redoStack;

    public TextEditorStateManager() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
    }

    /**
     * Take a snapshot of the current state.
     * Should be called when user changes focus to a different line.
     * 
     * @param lines Current state of all lines (will be deep copied)
     */
    public void snapshot(List<String> lines) {
        if (lines == null)
            return;

        // Don't add duplicate snapshots
        if (!undoStack.isEmpty()) {
            List<String> lastSnapshot = undoStack.peek();
            if (areEqual(lastSnapshot, lines)) {
                return;
            }
        }

        // Deep copy to prevent reference issues
        List<String> copy = new ArrayList<>(lines.size());
        for (String line : lines) {
            copy.add(line);
        }

        undoStack.push(copy);

        // Limit stack size
        while (undoStack.size() > MAX_HISTORY) {
            undoStack.remove(0);
        }

        // Clear redo stack when new snapshot is taken
        redoStack.clear();
    }

    /**
     * Undo to the previous state.
     * 
     * @param currentLines Current state to push to redo stack
     * @return Previous state, or null if cannot undo
     */
    public List<String> undo(List<String> currentLines) {
        if (undoStack.isEmpty()) {
            return null;
        }

        // Push current state to redo stack
        if (currentLines != null) {
            List<String> copy = new ArrayList<>(currentLines.size());
            for (String line : currentLines) {
                copy.add(line);
            }
            redoStack.push(copy);
        }

        return undoStack.pop();
    }

    /**
     * Redo to the next state.
     * 
     * @param currentLines Current state to push to undo stack
     * @return Next state, or null if cannot redo
     */
    public List<String> redo(List<String> currentLines) {
        if (redoStack.isEmpty()) {
            return null;
        }

        // Push current state to undo stack
        if (currentLines != null) {
            List<String> copy = new ArrayList<>(currentLines.size());
            for (String line : currentLines) {
                copy.add(line);
            }
            undoStack.push(copy);
        }

        return redoStack.pop();
    }

    /**
     * @return true if undo is available
     */
    public boolean canUndo() {
        // Need at least 2 states: current + previous
        // Single snapshot is the current state, so no undo available
        return undoStack.size() > 1;
    }

    /**
     * @return true if redo is available
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Clear all history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Compare two lists for equality.
     */
    private boolean areEqual(List<String> a, List<String> b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        if (a.size() != b.size())
            return false;

        for (int i = 0; i < a.size(); i++) {
            String lineA = a.get(i);
            String lineB = b.get(i);
            if (lineA == null && lineB == null)
                continue;
            if (lineA == null || !lineA.equals(lineB))
                return false;
        }

        return true;
    }
}
