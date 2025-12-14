package com.ireddragonicy.konabessnext.editor.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the text content of the editor.
 * Optimized for line-based access and modifications.
 */
public class TextDocument {
    private final ArrayList<StringBuilder> lines = new ArrayList<>();

    public TextDocument() {
        lines.add(new StringBuilder());
    }

    public TextDocument(CharSequence text) {
        setText(text);
    }

    public void setText(CharSequence text) {
        lines.clear();
        if (text == null || text.length() == 0) {
            lines.add(new StringBuilder());
        } else {
            String[] lineArray = text.toString().split("\n", -1);
            for (String line : lineArray) {
                lines.add(new StringBuilder(line));
            }
        }
    }

    public StringBuilder getLine(int index) {
        if (index >= 0 && index < lines.size()) {
            return lines.get(index);
        }
        return null; // Or throw generic exception if preferred, but null safe for now
    }

    public int getLineCount() {
        return lines.size();
    }

    public int getLineLength(int index) {
        if (index >= 0 && index < lines.size()) {
            return lines.get(index).length();
        }
        return 0;
    }

    public List<String> getLines() {
        List<String> result = new ArrayList<>(lines.size());
        for (StringBuilder sb : lines) {
            result.add(sb.toString());
        }
        return result;
    }

    public void setLines(List<String> newLines) {
        lines.clear();
        if (newLines == null || newLines.isEmpty()) {
            lines.add(new StringBuilder());
        } else {
            for (String line : newLines) {
                lines.add(new StringBuilder(line != null ? line : ""));
            }
        }
    }

    // Modification methods return info about the change if needed, or void

    public void insert(int line, int column, String text) {
        if (line >= lines.size()) {
            // pad with empty lines if necessary, or just append?
            // Editor usually enforces line continuity.
            // For now, assume append if at end, else clamp
            if (line == lines.size()) {
                lines.add(new StringBuilder());
            } else {
                return; // Invalid
            }
        }

        String[] parts = text.split("\n", -1);
        StringBuilder currentLine = lines.get(line);
        int safeCol = Math.min(column, currentLine.length());

        if (parts.length == 1) {
            currentLine.insert(safeCol, text);
        } else {
            String remainder = currentLine.substring(safeCol);
            currentLine.delete(safeCol, currentLine.length());
            currentLine.append(parts[0]);

            for (int i = 1; i < parts.length; i++) {
                line++;
                StringBuilder newLine = new StringBuilder(parts[i]);
                if (i == parts.length - 1) {
                    newLine.append(remainder);
                }
                lines.add(line, newLine);
            }
        }
    }

    public void delete(int startLine, int startCol, int endLine, int endCol) {
        if (startLine > endLine || (startLine == endLine && startCol > endCol)) {
            // Swap
            int tL = startLine;
            int tC = startCol;
            startLine = endLine;
            startCol = endCol;
            endLine = tL;
            endCol = tC;
        }

        if (startLine >= lines.size())
            return;

        if (startLine == endLine) {
            StringBuilder line = lines.get(startLine);
            int safeStart = Math.min(startCol, line.length());
            int safeEnd = Math.min(endCol, line.length());
            if (safeStart < safeEnd) {
                line.delete(safeStart, safeEnd);
            }
        } else {
            StringBuilder firstLine = lines.get(startLine);
            StringBuilder lastLine = (endLine < lines.size()) ? lines.get(endLine) : new StringBuilder();

            String prefix = firstLine.substring(0, Math.min(startCol, firstLine.length()));
            String suffix = (endLine < lines.size()) ? lastLine.substring(Math.min(endCol, lastLine.length())) : "";

            firstLine.setLength(0);
            firstLine.append(prefix).append(suffix);

            // Remove intermediate lines
            for (int i = endLine; i > startLine; i--) {
                if (i < lines.size()) {
                    lines.remove(i);
                }
            }
        }
    }

    public void deleteChar(int line, int col) {
        if (line >= lines.size())
            return;

        StringBuilder sb = lines.get(line);
        if (col > 0 && col <= sb.length()) {
            sb.deleteCharAt(col - 1);
        } else if (col == 0 && line > 0) {
            // Merge with previous line
            StringBuilder prev = lines.get(line - 1);
            prev.append(sb);
            lines.remove(line);
        }
    }

    public CharSequence getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb;
    }
}



