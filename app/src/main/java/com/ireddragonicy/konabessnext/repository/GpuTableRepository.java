package com.ireddragonicy.konabessnext.repository;

import com.ireddragonicy.konabessnext.core.ChipInfo;
import com.ireddragonicy.konabessnext.core.KonaBessCore;
import com.ireddragonicy.konabessnext.model.Bin;
import com.ireddragonicy.konabessnext.model.Level;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for GPU frequency table data operations.
 * Wraps DTS parsing logic previously in GpuTableEditor.
 * This is a data layer component in MVVM architecture.
 */
public class GpuTableRepository {

    private List<String> linesInDts;
    private List<Bin> bins;
    private int binPosition;

    public GpuTableRepository() {
        this.linesInDts = new ArrayList<>();
        this.bins = new ArrayList<>();
        this.binPosition = -1;
    }

    /**
     * Initialize by reading DTS file.
     */
    public void init() throws IOException {
        linesInDts = new ArrayList<>();
        bins = new ArrayList<>();
        binPosition = -1;

        File file = new File(KonaBessCore.dts_path);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                linesInDts.add(line);
            }
        }
    }

    /**
     * Decode GPU power levels from DTS lines.
     */
    public void decode() throws Exception {
        int i = -1;
        String thisLine;
        int start = -1;
        int bracket = 0;

        while (++i < linesInDts.size()) {
            thisLine = linesInDts.get(i).trim();

            // Check for single-bin chip types
            if (isSingleBinChip() && thisLine.equals("qcom,gpu-pwrlevels {")) {
                start = i;
                if (binPosition < 0)
                    binPosition = i;
                bracket++;
                continue;
            }

            // Check for tuna chip type
            if (ChipInfo.which == ChipInfo.type.tuna
                    && thisLine.contains("qcom,gpu-pwrlevels-")
                    && !thisLine.contains("compatible = ")
                    && !thisLine.contains("qcom,gpu-pwrlevel-bins")) {
                start = i;
                if (binPosition < 0)
                    binPosition = i;
                if (bracket != 0)
                    throw new Exception("Unexpected bracket state");
                bracket++;
                continue;
            }

            // Check for multi-bin chip types
            if (isMultiBinChip()
                    && thisLine.contains("qcom,gpu-pwrlevels-")
                    && !thisLine.contains("compatible = ")) {
                start = i;
                if (binPosition < 0)
                    binPosition = i;
                if (bracket != 0)
                    throw new Exception("Unexpected bracket state");
                bracket++;
                continue;
            }

            if (thisLine.contains("{") && start >= 0)
                bracket++;
            if (thisLine.contains("}") && start >= 0)
                bracket--;

            // Multi-bin end detection
            if (bracket == 0 && start >= 0 && (isMultiBinChip() || ChipInfo.which == ChipInfo.type.tuna)) {
                int end = i;
                if (end >= start) {
                    decodeBin(linesInDts.subList(start, end + 1));
                    int removedLines = end - start + 1;
                    linesInDts.subList(start, end + 1).clear();
                    i -= removedLines;
                } else {
                    throw new Exception("Invalid bin range");
                }
                start = -1;
                continue;
            }

            // Single-bin end detection
            if (bracket == 0 && start >= 0 && isSingleBinChip()) {
                int end = i;
                if (end >= start) {
                    decodeBin(linesInDts.subList(start, end + 1));
                    linesInDts.subList(start, end + 1).clear();
                } else {
                    throw new Exception("Invalid bin range");
                }
                break;
            }
        }
    }

    private boolean isSingleBinChip() {
        return ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin;
    }

    private boolean isMultiBinChip() {
        return ChipInfo.which == ChipInfo.type.kona
                || ChipInfo.which == ChipInfo.type.msmnile
                || ChipInfo.which == ChipInfo.type.lahaina
                || ChipInfo.which == ChipInfo.type.lito_v1
                || ChipInfo.which == ChipInfo.type.lito_v2
                || ChipInfo.which == ChipInfo.type.lagoon
                || ChipInfo.which == ChipInfo.type.shima
                || ChipInfo.which == ChipInfo.type.yupik
                || ChipInfo.which == ChipInfo.type.kalama
                || ChipInfo.which == ChipInfo.type.diwali
                || ChipInfo.which == ChipInfo.type.pineapple
                || ChipInfo.which == ChipInfo.type.sun
                || ChipInfo.which == ChipInfo.type.canoe;
    }

    private void decodeBin(List<String> lines) throws Exception {
        Bin bin = new Bin(bins.size());
        int i = 0;
        int bracket = 0;
        int start = 0;

        // Get bin ID from first line
        bin.setId(parseBinId(lines.get(0), bins.size()));

        while (++i < lines.size() && bracket >= 0) {
            String line = lines.get(i).trim();
            if (line.isEmpty())
                continue;

            if (line.contains("{")) {
                if (bracket != 0)
                    throw new Exception("Nested bracket error");
                start = i;
                bracket++;
                continue;
            }

            if (line.contains("}")) {
                if (--bracket < 0)
                    continue;
                int end = i;
                if (end >= start) {
                    bin.addLevel(decodeLevel(lines.subList(start, end + 1)));
                }
                continue;
            }

            if (bracket == 0) {
                bin.addHeaderLine(line);
            }
        }
        bins.add(bin);
    }

    private Level decodeLevel(List<String> lines) {
        Level level = new Level();
        for (String line : lines) {
            line = line.trim();
            if (line.contains("{") || line.contains("}"))
                continue;
            if (line.contains("reg"))
                continue;
            level.addLine(line);
        }
        return level;
    }

    private int parseBinId(String line, int defaultId) {
        line = line.trim().replace(" {", "").replace("-", "");
        try {
            for (int i = line.length() - 1; i >= 0; i--) {
                defaultId = Integer.parseInt(line.substring(i));
            }
        } catch (NumberFormatException ignored) {
        }
        return defaultId;
    }

    /**
     * Generate DTS table from current bins.
     */
    public List<String> generateTable() {
        List<String> lines = new ArrayList<>();

        if (isMultiBinChip() || ChipInfo.which == ChipInfo.type.tuna) {
            for (Bin bin : bins) {
                lines.add("qcom,gpu-pwrlevels-" + bin.getId() + " {");
                lines.addAll(bin.getHeader());
                for (int j = 0; j < bin.getLevelCount(); j++) {
                    lines.add("qcom,gpu-pwrlevel@" + j + " {");
                    lines.add("reg = <" + j + ">;");
                    lines.addAll(bin.getLevel(j).getLines());
                    lines.add("};");
                }
                lines.add("};");
            }
        } else if (isSingleBinChip() && !bins.isEmpty()) {
            lines.add("qcom,gpu-pwrlevels {");
            lines.addAll(bins.get(0).getHeader());
            for (int j = 0; j < bins.get(0).getLevelCount(); j++) {
                lines.add("qcom,gpu-pwrlevel@" + j + " {");
                lines.add("reg = <" + j + ">;");
                lines.addAll(bins.get(0).getLevel(j).getLines());
                lines.add("};");
            }
            lines.add("};");
        }
        return lines;
    }

    /**
     * Generate complete DTS with table inserted.
     */
    public List<String> generateFullDts() {
        List<String> result = new ArrayList<>(linesInDts);
        result.addAll(binPosition, generateTable());
        return result;
    }

    /**
     * Write DTS to file.
     */
    public void writeOut(List<String> newDts) throws IOException {
        File file = new File(KonaBessCore.dts_path);

        if (file.exists()) {
            file.setWritable(true);
            if (!file.delete()) {
                throw new IOException("Cannot delete existing file: " + file.getAbsolutePath());
            }
        }

        if (!file.createNewFile()) {
            throw new IOException("Failed to create file: " + file.getAbsolutePath());
        }

        file.setReadable(true, false);
        file.setWritable(true, false);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : newDts) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // Getters
    public List<String> getLinesInDts() {
        return linesInDts;
    }

    public List<Bin> getBins() {
        return bins;
    }

    public int getBinPosition() {
        return binPosition;
    }

    // Setters for state restoration
    public void setLinesInDts(List<String> lines) {
        this.linesInDts = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public void setBins(List<Bin> bins) {
        this.bins = new ArrayList<>();
        if (bins != null) {
            for (Bin bin : bins) {
                this.bins.add(new Bin(bin));
            }
        }
    }

    public void setBinPosition(int position) {
        this.binPosition = position;
    }
}



