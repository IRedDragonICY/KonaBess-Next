package xzr.konabess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import xzr.konabess.R;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xzr.konabess.adapters.GpuBinAdapter;
import xzr.konabess.adapters.GpuFreqAdapter;
import xzr.konabess.adapters.ParamAdapter;
import xzr.konabess.utils.DialogUtil;
import xzr.konabess.utils.DtsHelper;
import xzr.konabess.utils.ItemTouchHelperCallback;

public class GpuTableEditor {
    private static int bin_position;
    private static ArrayList<bin> bins;

    private static class bin {
        int id;
        ArrayList<String> header;
        ArrayList<level> levels;
    }

    private static class level {
        ArrayList<String> lines;
    }

    private static ArrayList<String> lines_in_dts;

    public static void init() throws IOException {
        lines_in_dts = new ArrayList<>();
        bins = new ArrayList<>();
        bin_position = -1;
        BufferedReader bufferedReader =
                new BufferedReader(new FileReader(new File(KonaBessCore.dts_path)));
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            lines_in_dts.add(s);
        }
    }

    public static void decode() throws Exception {
        int i = -1;
        String this_line;
        int start = -1;
        int end;
        int bracket = 0;
        while (++i < lines_in_dts.size()) {
            this_line = lines_in_dts.get(i).trim();

            if ((ChipInfo.which == ChipInfo.type.kona_singleBin
                    || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                    || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                    || ChipInfo.which == ChipInfo.type.waipio_singleBin
                    || ChipInfo.which == ChipInfo.type.cape_singleBin
                    || ChipInfo.which == ChipInfo.type.ukee_singleBin
                    || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                    || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                    || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin)
                    && this_line.equals("qcom,gpu-pwrlevels {")) {
                start = i;
                if (bin_position < 0)
                    bin_position = i;
                bracket++;
                continue;
            }
            if (ChipInfo.which == ChipInfo.type.tuna
                    && this_line.contains("qcom,gpu-pwrlevels-")
                    && !this_line.contains("compatible = ")
                    && !this_line.contains("qcom,gpu-pwrlevel-bins")) {
                start = i;
                if (bin_position < 0)
                    bin_position = i;
                if (bracket != 0)
                    throw new Exception();
                bracket++;
                continue;
            }
            if ((ChipInfo.which == ChipInfo.type.kona
                    || ChipInfo.which == ChipInfo.type.msmnile
                    || ChipInfo.which == ChipInfo.type.lahaina
                    || ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2
                    || ChipInfo.which == ChipInfo.type.lagoon
                    || ChipInfo.which == ChipInfo.type.shima
                    || ChipInfo.which == ChipInfo.type.yupik
                    || ChipInfo.which == ChipInfo.type.kalama
                    || ChipInfo.which == ChipInfo.type.diwali
                    || ChipInfo.which == ChipInfo.type.pineapple
                    || ChipInfo.which == ChipInfo.type.sun
                    || ChipInfo.which == ChipInfo.type.canoe)
                    && this_line.contains("qcom,gpu-pwrlevels-")
                    && !this_line.contains("compatible = ")) {
                start = i;
                if (bin_position < 0)
                    bin_position = i;
                if (bracket != 0)
                    throw new Exception();
                bracket++;
                continue;
            }

            if (this_line.contains("{") && start >= 0)
                bracket++;
            if (this_line.contains("}") && start >= 0)
                bracket--;

            if (bracket == 0 && start >= 0
                    && (ChipInfo.which == ChipInfo.type.kona
                    || ChipInfo.which == ChipInfo.type.msmnile
                    || ChipInfo.which == ChipInfo.type.lahaina
                    || ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2
                    || ChipInfo.which == ChipInfo.type.lagoon
                    || ChipInfo.which == ChipInfo.type.shima
                    || ChipInfo.which == ChipInfo.type.yupik
                    || ChipInfo.which == ChipInfo.type.kalama
                    || ChipInfo.which == ChipInfo.type.diwali
                    || ChipInfo.which == ChipInfo.type.pineapple
                    || ChipInfo.which == ChipInfo.type.sun
                    || ChipInfo.which == ChipInfo.type.canoe
                    || ChipInfo.which == ChipInfo.type.tuna)) {
                end = i;
                if (end >= start) {
                    try {
                        decode_bin(lines_in_dts.subList(start, end + 1));
                        int removedLines = end - start + 1;
                        lines_in_dts.subList(start, end + 1).clear();
                        i = i - removedLines; // Adjust index after removing lines
                    } catch (Exception e) {
                        throw e;
                    }
                } else {
                    throw new Exception();
                }
                start = -1;
                continue;
            }

            if (bracket == 0 && start >= 0 && (ChipInfo.which == ChipInfo.type.kona_singleBin
                    || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                    || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                    || ChipInfo.which == ChipInfo.type.waipio_singleBin
                    || ChipInfo.which == ChipInfo.type.cape_singleBin
                    || ChipInfo.which == ChipInfo.type.ukee_singleBin
                    || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                    || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                    || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin)) {
                end = i;
                if (end >= start) {
                    decode_bin(lines_in_dts.subList(start, end + 1));
                    lines_in_dts.subList(start, end + 1).clear();
                } else {
                    throw new Exception();
                }
                break;
            }
        }
    }

    private static int getBinID(String line, int prev_id) {
        line = line.trim();
        line = line.replace(" {", "")
                .replace("-", "");
        try {
            for (int i = line.length() - 1; i >= 0; i--) {
                prev_id = Integer.parseInt(line.substring(i));
            }
        } catch (Exception ignored) {
        }
        return prev_id;
    }

    private static void decode_bin(List<String> lines) throws Exception {
        bin bin = new bin();
        bin.header = new ArrayList<>();
        bin.levels = new ArrayList<>();
        bin.id = bins.size();
        int i = 0;
        int bracket = 0;
        int start = 0;
        int end;
        bin.id = getBinID(lines.get(0), bin.id);
        while (++i < lines.size() && bracket >= 0) {
            String line = lines.get(i);

            line = line.trim();
            if (line.equals(""))
                continue;

            if (line.contains("{")) {
                if (bracket != 0)
                    throw new Exception();
                start = i;
                bracket++;
                continue;
            }

            if (line.contains("}")) {
                if (--bracket < 0)
                    continue;
                end = i;
                if (end >= start)
                    bin.levels.add(decode_level(lines.subList(start, end + 1)));
                continue;
            }

            if (bracket == 0) {
                bin.header.add(line);
            }
        }
        bins.add(bin);
    }

    private static level decode_level(List<String> lines) {
        level level = new level();
        level.lines = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.contains("{") || line.contains("}"))
                continue;
            if (line.contains("reg"))
                continue;
            level.lines.add(line);
        }

        return level;
    }

    public static List<String> genTable() {
        ArrayList<String> lines = new ArrayList<>();
        if (ChipInfo.which == ChipInfo.type.kona
                || ChipInfo.which == ChipInfo.type.msmnile
                || ChipInfo.which == ChipInfo.type.lahaina
                || ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2
                || ChipInfo.which == ChipInfo.type.lagoon
                || ChipInfo.which == ChipInfo.type.shima
                || ChipInfo.which == ChipInfo.type.yupik
                || ChipInfo.which == ChipInfo.type.kalama
                || ChipInfo.which == ChipInfo.type.diwali
                || ChipInfo.which == ChipInfo.type.pineapple
                || ChipInfo.which == ChipInfo.type.sun
                || ChipInfo.which == ChipInfo.type.canoe
                || ChipInfo.which == ChipInfo.type.tuna) {
            for (int bin_id = 0; bin_id < bins.size(); bin_id++) {
                lines.add("qcom,gpu-pwrlevels-" + bins.get(bin_id).id + " {");
                lines.addAll(bins.get(bin_id).header);
                for (int pwr_level_id = 0; pwr_level_id < bins.get(bin_id).levels.size(); pwr_level_id++) {
                    lines.add("qcom,gpu-pwrlevel@" + pwr_level_id + " {");
                    lines.add("reg = <" + pwr_level_id + ">;");
                    lines.addAll(bins.get(bin_id).levels.get(pwr_level_id).lines);
                    lines.add("};");
                }
                lines.add("};");
            }
        } else if (ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin) {
            lines.add("qcom,gpu-pwrlevels {");
            lines.addAll(bins.get(0).header);
            for (int pwr_level_id = 0; pwr_level_id < bins.get(0).levels.size(); pwr_level_id++) {
                lines.add("qcom,gpu-pwrlevel@" + pwr_level_id + " {");
                lines.add("reg = <" + pwr_level_id + ">;");
                lines.addAll(bins.get(0).levels.get(pwr_level_id).lines);
                lines.add("};");
            }
            lines.add("};");
        }
        return lines;
    }

    public static List<String> genBack(List<String> table) {
        ArrayList<String> new_dts = new ArrayList<>(lines_in_dts);
        new_dts.addAll(bin_position, table);
        return new_dts;
    }

    public static void writeOut(List<String> new_dts) throws IOException {
        File file = new File(KonaBessCore.dts_path);
        
        // If file exists, delete it first to avoid permission issues
        if (file.exists()) {
            if (!file.delete()) {
                // If can't delete, try to set writable first
                file.setWritable(true);
                if (!file.delete()) {
                    throw new IOException("Cannot delete existing file: " + file.getAbsolutePath());
                }
            }
        }
        
        // Create new file
        if (!file.createNewFile()) {
            throw new IOException("Failed to create file: " + file.getAbsolutePath());
        }
        
        // Set proper permissions
        file.setReadable(true, false);
        file.setWritable(true, false);
        
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String s : new_dts) {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            }
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    // Ignore close errors
                }
            }
        }
    }

    private static String generateSubtitle(String line) throws Exception {
        String raw_name = DtsHelper.decode_hex_line(line).name;
        if ("qcom,level".equals(raw_name) || "qcom,cx-level".equals(raw_name)) {
            return GpuVoltEditor.levelint2str(DtsHelper.decode_int_line(line).value);
        }
        return DtsHelper.shouldUseHex(line) ? DtsHelper.decode_hex_line(line).value :
                DtsHelper.decode_int_line(line).value + "";
    }

    private static void generateALevel(Activity activity, int last, int levelid,
                                       LinearLayout page) throws Exception {
        ((MainActivity) activity).onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                try {
                    generateLevels(activity, last, page);
                } catch (Exception ignored) {
                }
            }
        };

        ListView listView = new ListView(activity);
        ArrayList<ParamAdapter.item> items = new ArrayList<>();

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.back);
            subtitle = "";
        }});

        for (String line : bins.get(last).levels.get(levelid).lines) {
            items.add(new ParamAdapter.item() {{
                title = KonaBessStr.convert_level_params(DtsHelper.decode_hex_line(line).name,
                        activity);
                subtitle = generateSubtitle(line);
            }});
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                if (position == 0) {
                    generateLevels(activity, last, page);
                    return;
                }
                String raw_name =
                        DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position - 1)).name;
                String raw_value =
                        DtsHelper.shouldUseHex(bins.get(last).levels.get(levelid).lines.get(position - 1))
                                ?
                                DtsHelper.decode_hex_line(bins.get(last).levels.get(levelid).lines.get(position - 1)).value
                                :
                                DtsHelper.decode_int_line(bins.get(last).levels.get(levelid).lines.get(position - 1)).value + "";

                if (raw_name.equals("qcom,level") || raw_name.equals("qcom,cx-level")) {
                    try {
                        Spinner spinner = new Spinner(activity);
                        spinner.setAdapter(new ArrayAdapter(activity,
                                android.R.layout.simple_dropdown_item_1line,
                                ChipInfo.rpmh_levels.level_str()));
                        spinner.setSelection(GpuVoltEditor.levelint2int(Integer.parseInt(raw_value)));

                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.edit)
                                .setView(spinner)
                                .setMessage(R.string.editvolt_msg)
                                .setPositiveButton(R.string.save, (dialog, which) -> {
                                    try {
                                        bins.get(last).levels.get(levelid).lines.set(
                                                position - 1,
                                                DtsHelper.encodeIntOrHexLine(raw_name,
                                                        ChipInfo.rpmh_levels.levels()[spinner.getSelectedItemPosition()] + ""));
                                        generateALevel(activity, last, levelid, page);
                                        Toast.makeText(activity, R.string.save_success,
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception exception) {
                                        DialogUtil.showError(activity, R.string.save_failed);
                                        exception.printStackTrace();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .create().show();

                    } catch (Exception e) {
                        DialogUtil.showError(activity, R.string.error_occur);
                    }
                } else {
                    EditText editText = new EditText(activity);
                    editText.setInputType(DtsHelper.shouldUseHex(raw_name) ?
                            InputType.TYPE_CLASS_TEXT : InputType.TYPE_CLASS_NUMBER);
                    editText.setText(raw_value);
                    new AlertDialog.Builder(activity)
                            .setTitle(activity.getResources().getString(R.string.edit) + " \"" + items.get(position).title + "\"")
                            .setView(editText)
                            .setMessage(KonaBessStr.help(raw_name, activity))
                            .setPositiveButton(R.string.save, (dialog, which) -> {
                                try {
                                    bins.get(last).levels.get(levelid).lines.set(
                                            position - 1,
                                            DtsHelper.encodeIntOrHexLine(raw_name,
                                                    editText.getText().toString()));
                                    generateALevel(activity, last, levelid, page);
                                    Toast.makeText(activity, R.string.save_success,
                                            Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    DialogUtil.showError(activity, R.string.save_failed);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();
                }
            } catch (Exception e) {
                DialogUtil.showError(activity, R.string.error_occur);
            }
        });

        listView.setAdapter(new ParamAdapter(items, activity));

        page.removeAllViews();
        page.addView(listView);
    }

    private static level level_clone(level from) {
        level next = new level();
        next.lines = new ArrayList<>(from.lines);
        return next;
    }

    private static void offset_initial_level_old(int offset) throws Exception {
        boolean started = false;
        int bracket = 0;
        for (int i = 0; i < lines_in_dts.size(); i++) {
            String line = lines_in_dts.get(i);

            if (line.contains("qcom,kgsl-3d0") && line.contains("{")) {
                started = true;
                bracket++;
                continue;
            }

            if (line.contains("{")) {
                bracket++;
                continue;
            }

            if (line.contains("}")) {
                bracket--;
                if (bracket == 0)
                    break;
                continue;
            }

            if (!started)
                continue;

            if (line.contains("qcom,initial-pwrlevel")) {
                lines_in_dts.set(i,
                        DtsHelper.encodeIntOrHexLine(DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value + offset + ""));
            }

        }
    }

    private static void offset_initial_level(int bin_id, int offset) throws Exception {
        if (ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin) {
            offset_initial_level_old(offset);
            return;
        }
        for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
            String line = bins.get(bin_id).header.get(i);
            if (line.contains("qcom,initial-pwrlevel")) {
                bins.get(bin_id).header.set(i,
                        DtsHelper.encodeIntOrHexLine(
                                DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value + offset + ""));
                break;
            }
        }
    }

    private static void offset_ca_target_level(int bin_id, int offset) throws Exception {
        for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
            String line = bins.get(bin_id).header.get(i);
            if (line.contains("qcom,ca-target-pwrlevel")) {
                bins.get(bin_id).header.set(i,
                        DtsHelper.encodeIntOrHexLine(
                                DtsHelper.decode_int_line(line).name,
                                DtsHelper.decode_int_line(line).value + offset + ""));
                break;
            }
        }
    }

    private static void patch_throttle_level_old() throws Exception {
        boolean started = false;
        int bracket = 0;
        for (int i = 0; i < lines_in_dts.size(); i++) {
            String line = lines_in_dts.get(i);

            if (line.contains("qcom,kgsl-3d0") && line.contains("{")) {
                started = true;
                bracket++;
                continue;
            }

            if (line.contains("{")) {
                bracket++;
                continue;
            }

            if (line.contains("}")) {
                bracket--;
                if (bracket == 0)
                    break;
                continue;
            }

            if (!started)
                continue;

            if (line.contains("qcom,throttle-pwrlevel")) {
                lines_in_dts.set(i,
                        DtsHelper.encodeIntOrHexLine(DtsHelper.decode_int_line(line).name,
                                "0"));
            }

        }
    }

    private static void patch_throttle_level() throws Exception {
        if (ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin) {
            patch_throttle_level_old();
            return;
        }
        for (int bin_id = 0; bin_id < bins.size(); bin_id++) {
            for (int i = 0; i < bins.get(bin_id).header.size(); i++) {
                String line = bins.get(bin_id).header.get(i);
                if (line.contains("qcom,throttle-pwrlevel")) {
                    bins.get(bin_id).header.set(i,
                            DtsHelper.encodeIntOrHexLine(
                                    DtsHelper.decode_int_line(line).name, "0"));
                    break;
                }
            }
        }
    }

    public static boolean canAddNewLevel(int binID, Context context) throws Exception {
        int max_levels = ChipInfo.getMaxTableLevels(ChipInfo.which) - min_level_chip_offset();
        if (bins.get(binID).levels.size() <= max_levels)
            return true;
        Toast.makeText(context, R.string.unable_add_more, Toast.LENGTH_SHORT).show();
        return false;
    }

    public static int min_level_chip_offset() throws Exception {
        if (ChipInfo.which == ChipInfo.type.lahaina || ChipInfo.which == ChipInfo.type.lahaina_singleBin
                || ChipInfo.which == ChipInfo.type.shima || ChipInfo.which == ChipInfo.type.yupik
                || ChipInfo.which == ChipInfo.type.waipio_singleBin
                || ChipInfo.which == ChipInfo.type.cape_singleBin
                || ChipInfo.which == ChipInfo.type.kalama
                || ChipInfo.which == ChipInfo.type.diwali
                || ChipInfo.which == ChipInfo.type.ukee_singleBin
                || ChipInfo.which == ChipInfo.type.pineapple
                || ChipInfo.which == ChipInfo.type.cliffs_singleBin
                || ChipInfo.which == ChipInfo.type.cliffs_7_singleBin
                || ChipInfo.which == ChipInfo.type.kalama_sg_singleBin
                || ChipInfo.which == ChipInfo.type.sun
                || ChipInfo.which == ChipInfo.type.canoe
                || ChipInfo.which == ChipInfo.type.tuna)
            return 1;
        if (ChipInfo.which == ChipInfo.type.kona || ChipInfo.which == ChipInfo.type.kona_singleBin
                || ChipInfo.which == ChipInfo.type.msmnile || ChipInfo.which == ChipInfo.type.msmnile_singleBin
                || ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2
                || ChipInfo.which == ChipInfo.type.lagoon)
            return 2;
        throw new Exception();
    }

    private static void generateLevels(Activity activity, int id, LinearLayout page) throws Exception {
        MainActivity mainActivity = (MainActivity) activity;
        mainActivity.updateGpuToolbarTitle(activity.getString(R.string.edit_freq_table)
                + " - " + KonaBessStr.convert_bins(bins.get(id).id, activity));

        mainActivity.onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                try {
                    generateBins(activity, page);
                    mainActivity.updateGpuToolbarTitle(activity.getString(R.string.edit_freq_table));
                } catch (Exception ignored) {
                }
            }
        };

        RecyclerView recyclerView = new RecyclerView(activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        
        // Set padding to prevent content from being hidden behind bottom toolbar
        float density = activity.getResources().getDisplayMetrics().density;
        int bottomPadding = (int) (density * 80); // Toolbar height + extra space
        recyclerView.setClipToPadding(false);
        recyclerView.setPadding(0, 0, 0, bottomPadding);
        
        ArrayList<GpuFreqAdapter.FreqItem> items = new ArrayList<>();

        // Back button (header)
        items.add(new GpuFreqAdapter.FreqItem(
            activity.getResources().getString(R.string.back),
            "",
            GpuFreqAdapter.FreqItem.ActionType.BACK
        ));

        // Add new at top button (header)
        items.add(new GpuFreqAdapter.FreqItem(
            activity.getResources().getString(R.string.add_freq_top),
            activity.getResources().getString(R.string.add_freq_top_desc),
            GpuFreqAdapter.FreqItem.ActionType.ADD_TOP
        ));

        // Add all frequency levels
        for (int i = 0; i < bins.get(id).levels.size(); i++) {
            level level = bins.get(id).levels.get(i);
            long freq = getFrequencyFromLevel(level);
            if (freq == 0)
                continue;
            
            GpuFreqAdapter.FreqItem item = new GpuFreqAdapter.FreqItem(
                SettingsActivity.formatFrequency(freq, activity),
                ""
            );
            item.originalPosition = i;
            
            // Extract spec details from DTS lines
            try {
                for (String line : level.lines) {
                    String paramName = DtsHelper.decode_hex_line(line).name;
                    
                    if ("qcom,bus-max".equals(paramName)) {
                        long busMax = DtsHelper.decode_int_line(line).value;
                        item.busMax = String.valueOf(busMax);
                    } else if ("qcom,bus-min".equals(paramName)) {
                        long busMin = DtsHelper.decode_int_line(line).value;
                        item.busMin = String.valueOf(busMin);
                    } else if ("qcom,bus-freq".equals(paramName)) {
                        long busFreq = DtsHelper.decode_int_line(line).value;
                        // Bus-freq is a level/index, not frequency in MHz
                        item.busFreq = String.valueOf(busFreq);
                    } else if ("qcom,level".equals(paramName) || "qcom,cx-level".equals(paramName)) {
                        long voltLevel = DtsHelper.decode_int_line(line).value;
                        item.voltageLevel = GpuVoltEditor.levelint2str(voltLevel);
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors for individual specs
            }
            
            items.add(item);
        }

        // Add new at bottom button (footer)
        items.add(new GpuFreqAdapter.FreqItem(
            activity.getResources().getString(R.string.add_freq_bottom),
            activity.getResources().getString(R.string.add_freq_bottom_desc),
            GpuFreqAdapter.FreqItem.ActionType.ADD_BOTTOM
        ));

        GpuFreqAdapter adapter = new GpuFreqAdapter(items, activity);
        
        // Item click listener
        adapter.setOnItemClickListener(position -> {
            GpuFreqAdapter.FreqItem item = items.get(position);

            switch (item.actionType) {
                case ADD_BOTTOM:
                    try {
                        if (!canAddNewLevel(id, activity))
                            return;
                        bins.get(id).levels.add(bins.get(id).levels.size() - min_level_chip_offset(),
                                level_clone(bins.get(id).levels.get(bins.get(id).levels.size() - min_level_chip_offset())));
                        generateLevels(activity, id, page);
                        offset_initial_level(id, 1);
                        if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                            offset_ca_target_level(id, 1);
                    } catch (Exception e) {
                        DialogUtil.showError(activity, R.string.error_occur);
                    }
                    return;
                case BACK:
                    try {
                        generateBins(activity, page);
                        mainActivity.updateGpuToolbarTitle(activity.getString(R.string.edit_freq_table));
                    } catch (Exception ignored) {
                    }
                    return;
                case ADD_TOP:
                    try {
                        if (!canAddNewLevel(id, activity))
                            return;
                        bins.get(id).levels.add(0, level_clone(bins.get(id).levels.get(0)));
                        generateLevels(activity, id, page);
                        offset_initial_level(id, 1);
                        if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                            offset_ca_target_level(id, 1);
                    } catch (Exception e) {
                        DialogUtil.showError(activity, R.string.error_occur);
                    }
                    return;
                case DUPLICATE:
                    try {
                        if (!canAddNewLevel(id, activity))
                            return;
                        bins.get(id).levels.add(item.targetPosition + 1,
                                level_clone(bins.get(id).levels.get(item.targetPosition)));
                        generateLevels(activity, id, page);
                        offset_initial_level(id, 1);
                        if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                            offset_ca_target_level(id, 1);
                    } catch (Exception e) {
                        DialogUtil.showError(activity, R.string.error_occur);
                    }
                    return;
                case NONE:
                default:
                    if (item.isLevelItem()) {
                        int levelIndex = 0;
                        for (int i = 0; i < position; i++) {
                            if (items.get(i).isLevelItem()) {
                                levelIndex++;
                            }
                        }
                        try {
                            generateALevel(activity, id, levelIndex, page);
                        } catch (Exception e) {
                            DialogUtil.showError(activity, R.string.error_occur);
                        }
                    }
                    break;
            }
        });
        
        // Long-press listener for inline duplicate
        adapter.setOnItemLongClickListener(position -> {
            GpuFreqAdapter.FreqItem item = items.get(position);
            
            // Only allow duplicate for regular frequency items
            if (!item.isLevelItem()) {
                return;
            }
            
            // Clear all highlights and duplicates
            for (int i = items.size() - 1; i >= 0; i--) {
                GpuFreqAdapter.FreqItem currentItem = items.get(i);
                
                // Remove duplicate items
                if (currentItem.isDuplicateItem()) {
                    items.remove(i);
                    adapter.notifyItemRemoved(i);
                    continue;
                }
                
                // Clear highlights
                if (currentItem.isHighlighted) {
                    currentItem.isHighlighted = false;
                    adapter.notifyItemChanged(i);
                }
            }
            
            // Recalculate position after removal
            int adjustedPosition = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) == item) {
                    adjustedPosition = i;
                    break;
                }
            }
            
            if (adjustedPosition == -1) return;
            
            // Highlight the selected item
            item.isHighlighted = true;
            adapter.notifyItemChanged(adjustedPosition);
            
            // Calculate level index
            int levelIndex = 0;
            for (int i = 0; i < adjustedPosition; i++) {
                if (items.get(i).isLevelItem()) {
                    levelIndex++;
                }
            }
            
            // Create duplicate card
            GpuFreqAdapter.FreqItem duplicate = new GpuFreqAdapter.FreqItem(
                activity.getString(R.string.duplicate_frequency),
                "",
                GpuFreqAdapter.FreqItem.ActionType.DUPLICATE
            );
            duplicate.targetPosition = levelIndex;
            
            // Insert card below the selected item
            items.add(adjustedPosition + 1, duplicate);
            adapter.notifyItemInserted(adjustedPosition + 1);
            recyclerView.smoothScrollToPosition(adjustedPosition);
        });
        
        // Delete button click listener
        adapter.setOnDeleteClickListener(position -> {
            if (bins.get(id).levels.size() == 1) {
                Toast.makeText(activity, R.string.unable_add_more, Toast.LENGTH_SHORT).show();
                return;
            }
            
            int levelPosition = position - 2; // Adjust for header items
            try {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.remove)
                        .setMessage("Are you sure to remove " + SettingsActivity.formatFrequency(getFrequencyFromLevel(bins.get(id).levels.get(levelPosition)), activity) + "?")
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            bins.get(id).levels.remove(levelPosition);
                            try {
                                generateLevels(activity, id, page);
                                offset_initial_level(id, -1);
                                if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon)
                                    offset_ca_target_level(id, -1);
                            } catch (Exception e) {
                                DialogUtil.showError(activity, R.string.error_occur);
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .create().show();
            } catch (Exception e) {
                DialogUtil.showError(activity, R.string.error_occur);
            }
        });
        
        // Setup drag and drop
        ItemTouchHelperCallback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        
        adapter.setOnStartDragListener(viewHolder -> {
            touchHelper.startDrag(viewHolder);
        });
        
        recyclerView.setAdapter(adapter);

        page.removeAllViews();
        page.addView(recyclerView);
        
        // Apply any reordering changes when drag completes
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Update the bins order based on adapter items
                    try {
                        updateBinsFromAdapter(id, adapter.getItems());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    
    private static void updateBinsFromAdapter(int binId, List<GpuFreqAdapter.FreqItem> items) throws Exception {
        ArrayList<level> newLevels = new ArrayList<>();
        
        for (GpuFreqAdapter.FreqItem item : items) {
            if (!item.isHeader && !item.isFooter && item.originalPosition >= 0) {
                // Find the corresponding level
                for (level l : bins.get(binId).levels) {
                    long freq = getFrequencyFromLevel(l);
                    String formattedFreq = SettingsActivity.formatFrequency(freq, null);
                    if (item.title.equals(formattedFreq)) {
                        newLevels.add(l);
                        break;
                    }
                }
            }
        }
        
        // Update bins with new order if we found all levels
        if (newLevels.size() == bins.get(binId).levels.size()) {
            bins.get(binId).levels = newLevels;
        }
    }

    private static long getFrequencyFromLevel(level level) throws Exception {
        for (String line : level.lines) {
            if (line.contains("qcom,gpu-freq")) {
                return DtsHelper.decode_int_line(line).value;
            }
        }
        throw new Exception();
    }

    private static View createChipsetSelectorCard(Activity activity, LinearLayout page) {
        float density = activity.getResources().getDisplayMetrics().density;
        int padding = (int) (density * 16);
        
        // Main card container
        com.google.android.material.card.MaterialCardView card = 
                new com.google.android.material.card.MaterialCardView(activity);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(padding, padding, padding, (int)(density * 8));
        card.setLayoutParams(cardParams);
        card.setCardElevation(density * 2);
        card.setRadius(density * 12);
        
        // Inner layout
        LinearLayout innerLayout = new LinearLayout(activity);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(padding, padding, padding, padding);
        
        // Title
        TextView titleView = new TextView(activity);
        titleView.setText("Target Chipset");
        titleView.setTextSize(12);
        titleView.setAlpha(0.6f);
        titleView.setPadding(0, 0, 0, (int)(density * 8));
        innerLayout.addView(titleView);
        
        // Current chipset display with click to change
        LinearLayout chipsetRow = new LinearLayout(activity);
        chipsetRow.setOrientation(LinearLayout.HORIZONTAL);
        chipsetRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        chipsetRow.setClickable(true);
        chipsetRow.setFocusable(true);
        
        // Set ripple effect
        android.content.res.TypedArray typedArray = activity.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.selectableItemBackground});
        int selectableItemBackground = typedArray.getResourceId(0, 0);
        typedArray.recycle();
        chipsetRow.setBackgroundResource(selectableItemBackground);
        chipsetRow.setPadding((int)(density * 12), (int)(density * 12), 
                (int)(density * 12), (int)(density * 12));
        
        // Chipset icon (Material Design)
        ImageView chipIcon = new ImageView(activity);
        chipIcon.setImageResource(R.drawable.ic_developer_board);
        int iconSize = (int)(density * 24);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.setMarginEnd((int)(density * 12));
        chipIcon.setLayoutParams(iconParams);
        chipIcon.setColorFilter(MaterialColors.getColor(chipIcon,
                com.google.android.material.R.attr.colorOnSurface));
        chipsetRow.addView(chipIcon);
        
        // Chipset name
        TextView chipsetName = new TextView(activity);
        KonaBessCore.dtb currentDtb = KonaBessCore.getCurrentDtb();
        if (currentDtb != null) {
            chipsetName.setText(currentDtb.id + " " + 
                    ChipInfo.name2chipdesc(currentDtb.type, activity));
        } else {
            chipsetName.setText("Unknown");
        }
        chipsetName.setTextSize(16);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        chipsetName.setLayoutParams(nameParams);
        chipsetRow.addView(chipsetName);
        
        // Settings/Change icon (Material Design)
        ImageView changeIcon = new ImageView(activity);
        changeIcon.setImageResource(R.drawable.ic_tune);
        LinearLayout.LayoutParams changeIconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        changeIcon.setLayoutParams(changeIconParams);
        changeIcon.setAlpha(0.7f);
        changeIcon.setColorFilter(MaterialColors.getColor(changeIcon,
                com.google.android.material.R.attr.colorOnSurfaceVariant));
        chipsetRow.addView(changeIcon);
        
        // Click listener to show chipset selector dialog
        chipsetRow.setOnClickListener(v -> showChipsetSelectorDialog(activity, page, chipsetName));
        
        innerLayout.addView(chipsetRow);
        card.addView(innerLayout);
        
        return card;
    }
    
    private static void showChipsetSelectorDialog(Activity activity, LinearLayout page, 
                                                   TextView chipsetNameView) {
        if (KonaBessCore.dtbs == null || KonaBessCore.dtbs.isEmpty()) {
            Toast.makeText(activity, "No chipsets available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ListView listView = new ListView(activity);
        ArrayList<ParamAdapter.item> items = new ArrayList<>();
        
        KonaBessCore.dtb currentDtb = KonaBessCore.getCurrentDtb();
        int currentDtbIndex = KonaBessCore.getDtbIndex();
        
        for (KonaBessCore.dtb dtb : KonaBessCore.dtbs) {
            items.add(new ParamAdapter.item() {{
                title = dtb.id + " " + ChipInfo.name2chipdesc(dtb.type, activity);
                // Highlight current selected
                subtitle = (currentDtb != null && dtb.id == currentDtb.id) ? "Currently Selected" :
                        (dtb.id == currentDtbIndex ? "Possible DTB" : "");
            }});
        }
        
        listView.setAdapter(new ParamAdapter(items, activity));
        
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Select Target Chipset")
                .setMessage("Choose the chipset configuration you want to edit")
                .setView(listView)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            KonaBessCore.dtb selectedDtb = KonaBessCore.dtbs.get(position);
            
            // Show confirmation if switching chipset
            if (currentDtb != null && selectedDtb.id != currentDtb.id) {
                new AlertDialog.Builder(activity)
                        .setTitle("Switch Chipset?")
                        .setMessage("Switching chipset will reload the GPU frequency table. Continue?")
                        .setPositiveButton("Yes", (d, w) -> {
                            dialog.dismiss();
                            switchChipset(activity, page, selectedDtb, chipsetNameView);
                        })
                        .setNegativeButton("No", null)
                        .create().show();
            } else {
                dialog.dismiss();
            }
        });
    }
    
    private static void switchChipset(Activity activity, LinearLayout page, 
                                      KonaBessCore.dtb newDtb, TextView chipsetNameView) {
        AlertDialog waiting = DialogUtil.getWaitDialog(activity, R.string.getting_freq_table);
        waiting.show();
        
        new Thread(() -> {
            try {
                // Switch to new chipset
                KonaBessCore.chooseTarget(newDtb, activity);
                
                // Reload GPU table for new chipset
                init();
                decode();
                patch_throttle_level();
                
                activity.runOnUiThread(() -> {
                    waiting.dismiss();
                    
                    // Update chipset name in card
                    chipsetNameView.setText(newDtb.id + " " + 
                            ChipInfo.name2chipdesc(newDtb.type, activity));
                    
                    // Regenerate bins view
                    try {
                        generateBins(activity, page);
                        Toast.makeText(activity, "Switched to chipset " + newDtb.id, 
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        DialogUtil.showError(activity, R.string.error_occur);
                    }
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    waiting.dismiss();
                    DialogUtil.showError(activity, R.string.getting_freq_table_failed);
                });
            }
        }).start();
    }

    private static void generateBins(Activity activity, LinearLayout page) throws Exception {
        ((MainActivity) activity).onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                ((MainActivity) activity).showMainView();
            }
        };

        // Create main vertical layout
        LinearLayout mainLayout = new LinearLayout(activity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        float density = activity.getResources().getDisplayMetrics().density;
        
        // Add chipset selector card if multiple chipsets are available
        if (KonaBessCore.dtbs != null && KonaBessCore.dtbs.size() > 1) {
            mainLayout.addView(createChipsetSelectorCard(activity, page));
        }

        RecyclerView recyclerView = new RecyclerView(activity);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setClipToPadding(false);
        recyclerView.setPadding(0, (int) (density * 8), 0, (int) (density * 16));

        ArrayList<GpuBinAdapter.BinItem> items = new ArrayList<>();
        for (int i = 0; i < bins.size(); i++) {
            items.add(new GpuBinAdapter.BinItem(
                    KonaBessStr.convert_bins(bins.get(i).id, activity),
                    ""));
        }

        GpuBinAdapter adapter = new GpuBinAdapter(items);
        adapter.setOnItemClickListener(new GpuBinAdapter.OnItemClickListener() {
            @Override
            public void onBinClick(int position) {
                try {
                    generateLevels(activity, position, page);
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.error_occur);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        mainLayout.addView(recyclerView);

        page.removeAllViews();
        page.addView(mainLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }

    private static View generateToolBar(Activity activity, LinearLayout showedView) {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(activity);
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.setHorizontalScrollBarEnabled(false);

        LinearLayout toolbar = new LinearLayout(activity);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        int padding = (int) (activity.getResources().getDisplayMetrics().density * 8);
        toolbar.setPadding(padding, padding, padding, padding);
        horizontalScrollView.addView(toolbar);

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(0, 0, padding, 0);

        MaterialButton saveButton = createActionButton(activity, R.string.save_freq_table,
                R.drawable.ic_file_upload);
        saveButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
        toolbar.addView(saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    writeOut(genBack(genTable()));
                    Toast.makeText(activity, R.string.save_success, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.save_failed);
                }
            }
        });

        if (activity instanceof MainActivity && !ChipInfo.shouldIgnoreVoltTable(ChipInfo.which)) {
            MaterialButton voltButton = createActionButton(activity, R.string.edit_gpu_volt_table,
                    R.drawable.ic_voltage);
            voltButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
            toolbar.addView(voltButton);
            voltButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new GpuVoltEditor.gpuVoltLogic((MainActivity) activity, showedView).start();
                }
            });
        }

        if (activity instanceof MainActivity) {
            MaterialButton repackButton = createActionButton(activity, R.string.repack_flash,
                    R.drawable.ic_flash);
            repackButton.setLayoutParams(new LinearLayout.LayoutParams(buttonLayoutParams));
            toolbar.addView(repackButton);
            repackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) activity).new repackLogic().start();
                }
            });
        }

        return horizontalScrollView;
    }

    private static MaterialButton createActionButton(Activity activity, int textRes, int iconRes) {
        MaterialButton button = new MaterialButton(activity);
        button.setAllCaps(false);
        button.setText(textRes);
        button.setIconResource(iconRes);
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);

        float density = activity.getResources().getDisplayMetrics().density;
        int iconSize = (int) (density * 20);
        int iconPadding = (int) (density * 8);
        int horizontalPadding = (int) (density * 20);
        int verticalPadding = (int) (density * 12);

        button.setIconSize(iconSize);
        button.setIconPadding(iconPadding);
        button.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

        int backgroundColor = MaterialColors.getColor(button,
                com.google.android.material.R.attr.colorSecondaryContainer);
        int foregroundColor = MaterialColors.getColor(button,
                com.google.android.material.R.attr.colorOnSecondaryContainer);
        int rippleColor = MaterialColors.getColor(button,
                com.google.android.material.R.attr.colorSecondary);

        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(foregroundColor);
        button.setIconTint(ColorStateList.valueOf(foregroundColor));
        button.setRippleColor(ColorStateList.valueOf(rippleColor));
        button.setStrokeWidth(0);

        return button;
    }

    public static class gpuTableLogic extends Thread {
        Activity activity;
        AlertDialog waiting;
        LinearLayout showedView;
        LinearLayout page;

        public gpuTableLogic(Activity activity, LinearLayout showedView) {
            this.activity = activity;
            this.showedView = showedView;
        }

        public void run() {
            activity.runOnUiThread(() -> {
                waiting = DialogUtil.getWaitDialog(activity, R.string.getting_freq_table);
                waiting.show();
            });

            try {
                init();
                decode();
                patch_throttle_level();
            } catch (Exception e) {
                activity.runOnUiThread(() -> DialogUtil.showError(activity,
                        R.string.getting_freq_table_failed));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                showedView.addView(generateToolBar(activity, showedView));
                page = new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                try {
                    generateBins(activity, page);
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.getting_freq_table_failed);
                }
                showedView.addView(page);
            });

        }
    }
}
