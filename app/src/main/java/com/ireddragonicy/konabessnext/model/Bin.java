package com.ireddragonicy.konabessnext.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GPU power level bin containing multiple frequency levels.
 * Used by MVVM architecture for GPU table editing.
 */
public class Bin {
    // Public fields for direct access
    public int id;
    public ArrayList<String> header;
    public ArrayList<Level> levels;

    public Bin() {
        this.header = new ArrayList<>();
        this.levels = new ArrayList<>();
    }

    public Bin(int id) {
        this();
        this.id = id;
    }

    // Deep copy constructor
    public Bin(Bin other) {
        this.id = other.id;
        this.header = new ArrayList<>(other.header);
        this.levels = new ArrayList<>();
        for (Level level : other.levels) {
            this.levels.add(new Level(level));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header != null ? new ArrayList<>(header) : new ArrayList<>();
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels != null ? new ArrayList<>(levels) : new ArrayList<>();
    }

    public void addLevel(Level level) {
        this.levels.add(level);
    }

    public void addHeaderLine(String line) {
        this.header.add(line);
    }

    public Level getLevel(int index) {
        if (index >= 0 && index < levels.size()) {
            return levels.get(index);
        }
        return null;
    }

    public int getLevelCount() {
        return levels.size();
    }

    /**
     * Create a deep copy of this bin.
     */
    public Bin copy() {
        return new Bin(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bin bin = (Bin) o;
        return id == bin.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}



