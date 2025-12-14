package com.ireddragonicy.konabessnext.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single GPU power level containing DTS property lines.
 * Used by MVVM architecture for GPU table editing.
 */
public class Level {
    // Public field for direct access
    public ArrayList<String> lines;

    public Level() {
        this.lines = new ArrayList<>();
    }

    // Deep copy constructor
    public Level(Level other) {
        this.lines = new ArrayList<>(other.lines);
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public void addLine(String line) {
        this.lines.add(line);
    }

    public String getLine(int index) {
        if (index >= 0 && index < lines.size()) {
            return lines.get(index);
        }
        return null;
    }

    public void setLine(int index, String line) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, line);
        }
    }

    public int getLineCount() {
        return lines.size();
    }

    /**
     * Get the frequency value from this level's lines.
     * Looks for "qcom,gpu-freq = <value>;" pattern.
     *
     * @return frequency in Hz, or -1 if not found
     */
    public long getFrequency() {
        for (String line : lines) {
            if (line.contains("qcom,gpu-freq")) {
                try {
                    String value = line.replaceAll("[^0-9]", "");
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Get the voltage level from this level's lines.
     * Looks for "qcom,level = <value>;" pattern.
     *
     * @return voltage level, or -1 if not found
     */
    public int getVoltageLevel() {
        for (String line : lines) {
            if (line.contains("qcom,level") && !line.contains("qcom,gpu-freq")) {
                try {
                    String value = line.replaceAll("[^0-9]", "");
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Create a deep copy of this level.
     */
    public Level copy() {
        return new Level(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Level level = (Level) o;
        return lines.equals(level.lines);
    }

    @Override
    public int hashCode() {
        return lines.hashCode();
    }
}



