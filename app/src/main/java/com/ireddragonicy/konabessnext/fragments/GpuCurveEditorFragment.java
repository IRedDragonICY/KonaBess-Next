package com.ireddragonicy.konabessnext.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.slider.Slider;
import com.ireddragonicy.konabessnext.GpuTableEditor;
import com.ireddragonicy.konabessnext.GpuVoltEditor;
import com.ireddragonicy.konabessnext.R;
import com.ireddragonicy.konabessnext.utils.DtsHelper;
import com.ireddragonicy.konabessnext.widget.GpuActionToolbar;

import java.util.ArrayList;

/**
 * GPU Curve Editor Fragment - Allows visual editing of GPU frequency curves.
 * Uses shared history with GpuTableEditor for unified Undo/Redo.
 */
public class GpuCurveEditorFragment extends Fragment implements GpuTableEditor.OnHistoryStateChangedListener {

    private LineChart chart;
    private Slider globalOffsetSlider;
    private TextView offsetValueText;
    private MaterialButton btnPlus, btnMinus;
    private LinearLayout toolbarContainer;

    // Data
    private ArrayList<Entry> referenceEntries = new ArrayList<>();
    private ArrayList<Entry> activeEntries = new ArrayList<>();
    private ArrayList<GpuTableEditor.level> originalLevels;
    private ArrayList<String> xLabels = new ArrayList<>();
    private int binIndex = 0;
    private int globalOffset = 0;
    private Entry draggingEntry = null;
    private int[] voltageLevels; // Store voltage level for each entry
    private float touchDownX, touchDownY; // For tap detection
    private static final float TAP_THRESHOLD = 20f; // Pixels

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gpu_curve_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Parse arguments
        if (getArguments() != null) {
            binIndex = getArguments().getInt("binId", 0);
        }

        initViews(view);
        loadData();
        setupChart();
        setupInteractions();

        // Register for history changes to refresh chart on Undo/Redo
        GpuTableEditor.addHistoryListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GpuTableEditor.removeHistoryListener(this);
    }

    @Override
    public void onHistoryStateChanged(boolean canUndo, boolean canRedo) {
        // Refresh chart data when history changes (Undo/Redo)
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                loadData();
                refreshChart();
            });
        }
    }

    private void initViews(View view) {
        chart = view.findViewById(R.id.gpu_curve_chart);
        globalOffsetSlider = view.findViewById(R.id.slider_global_offset);
        offsetValueText = view.findViewById(R.id.text_offset_value);
        btnPlus = view.findViewById(R.id.btn_offset_plus);
        btnMinus = view.findViewById(R.id.btn_offset_minus);
        toolbarContainer = view.findViewById(R.id.toolbar_container);

        // Setup toolbar back navigation
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Add action toolbar using modular widget
        if (GpuTableEditor.bins != null && getActivity() != null) {
            GpuActionToolbar actionToolbar = new GpuActionToolbar(requireContext());
            actionToolbar.setParentViewForVolt(toolbarContainer);
            actionToolbar.build(requireActivity());
            toolbarContainer.addView(actionToolbar);
        }
    }

    private void loadData() {
        if (GpuTableEditor.bins == null || GpuTableEditor.bins.isEmpty()) {
            Toast.makeText(getContext(), "No GPU data available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find bin by index
        GpuTableEditor.bin targetBin = null;
        int idx = 0;
        for (GpuTableEditor.bin b : GpuTableEditor.bins) {
            if (idx == binIndex) {
                targetBin = b;
                break;
            }
            idx++;
        }

        if (targetBin == null || targetBin.levels == null || targetBin.levels.isEmpty()) {
            Toast.makeText(getContext(), "Invalid bin data", Toast.LENGTH_SHORT).show();
            return;
        }

        originalLevels = targetBin.levels;
        referenceEntries.clear();
        activeEntries.clear();
        xLabels.clear();

        // Populate entries (reversed for correct graph display)
        int size = originalLevels.size();
        voltageLevels = new int[size];
        for (int i = size - 1; i >= 0; i--) {
            GpuTableEditor.level lvl = originalLevels.get(i);
            int xIndex = size - 1 - i;

            // Get frequency
            long freq = 0;
            for (String line : lvl.lines) {
                if (line.contains("qcom,gpu-freq")) {
                    try {
                        freq = DtsHelper.decode_int_line(line).value;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            float freqMhz = freq / 1_000_000f;

            // Get voltage level
            int voltLevel = 0;
            for (String line : lvl.lines) {
                if (line.contains("qcom,level") || line.contains("qcom,cx-level")) {
                    try {
                        voltLevel = (int) DtsHelper.decode_int_line(line).value;
                    } catch (Exception ignored) {
                    }
                    break;
                }
            }
            voltageLevels[xIndex] = voltLevel;

            referenceEntries.add(new Entry(xIndex, freqMhz));
            activeEntries.add(new Entry(xIndex, freqMhz));

            // Get voltage label
            String voltLabel = null;
            try {
                voltLabel = GpuVoltEditor.levelint2str(voltLevel);
            } catch (Exception ignored) {
            }
            xLabels.add(voltLabel != null ? voltLevel + " - " + voltLabel : String.valueOf(voltLevel));
        }
    }

    private void setupChart() {
        if (getContext() == null)
            return;

        int colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary,
                Color.GREEN);
        int colorOnSurface = MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorOnSurface, Color.WHITE);
        int colorOutline = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOutline,
                Color.GRAY);

        // Chart config
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraBottomOffset(8f);

        // X-Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(colorOnSurface);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                return (idx >= 0 && idx < xLabels.size()) ? xLabels.get(idx) : "";
            }
        });

        // Y-Axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(colorOnSurface);
        leftAxis.setGridColor(colorOutline);
        leftAxis.setAxisLineColor(colorOutline);
        chart.getAxisRight().setEnabled(false);

        refreshChart();
    }

    private void refreshChart() {
        if (getContext() == null)
            return;

        int colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary,
                Color.GREEN);
        int colorOnSurface = MaterialColors.getColor(requireContext(),
                com.google.android.material.R.attr.colorOnSurface, Color.WHITE);

        // Reference line (dashed - original values)
        LineDataSet refSet = new LineDataSet(referenceEntries, "Reference");
        refSet.setColor(colorOnSurface);
        refSet.setLineWidth(1.5f);
        refSet.setDrawCircles(false);
        refSet.setDrawValues(false);
        refSet.enableDashedLine(10f, 5f, 0f);
        refSet.setFormLineDashEffect(new DashPathEffect(new float[] { 10f, 5f }, 0f));

        // Active line (current values)
        LineDataSet activeSet = new LineDataSet(activeEntries, "Current");
        activeSet.setColor(colorPrimary);
        activeSet.setLineWidth(2.5f);
        activeSet.setCircleColor(colorPrimary);
        activeSet.setCircleRadius(5f);
        activeSet.setDrawCircleHole(true);
        activeSet.setCircleHoleRadius(3f);
        activeSet.setValueTextColor(colorPrimary);
        activeSet.setValueTextSize(10f);
        activeSet.setDrawValues(true);
        activeSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });
        activeSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        activeSet.setDrawFilled(true);

        if (android.os.Build.VERSION.SDK_INT >= 18) {
            GradientDrawable drawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] { MaterialColors.compositeARGBWithAlpha(colorPrimary, 100), Color.TRANSPARENT });
            activeSet.setFillDrawable(drawable);
        } else {
            activeSet.setFillColor(colorPrimary);
            activeSet.setFillAlpha(50);
        }

        chart.setData(new LineData(refSet, activeSet));
        chart.invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupInteractions() {
        // Slider
        globalOffsetSlider.addOnChangeListener((slider, value, fromUser) -> {
            applyGlobalOffset((int) value);
        });

        globalOffsetSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                saveToMemory(true);
            }
        });

        // Buttons
        btnMinus.setOnClickListener(v -> {
            float val = globalOffsetSlider.getValue();
            int newOffset = Math.max((int) globalOffsetSlider.getValueFrom(), (int) val - 10);
            globalOffsetSlider.setValue(newOffset);
            applyGlobalOffset(newOffset);
            saveToMemory(true);
        });

        btnPlus.setOnClickListener(v -> {
            float val = globalOffsetSlider.getValue();
            int newOffset = Math.min((int) globalOffsetSlider.getValueTo(), (int) val + 10);
            globalOffsetSlider.setValue(newOffset);
            applyGlobalOffset(newOffset);
            saveToMemory(true);
        });

        // Chart drag listener
        chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
            }
        });

        chart.setOnTouchListener((v, event) -> {
            Highlight h = chart.getHighlightByTouchPoint(event.getX(), event.getY());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownX = event.getX();
                    touchDownY = event.getY();
                    if (h != null) {
                        int idx = (int) h.getX();
                        if (idx >= 0 && idx < activeEntries.size()) {
                            draggingEntry = activeEntries.get(idx);
                            chart.highlightValue(h);
                            chart.getParent().requestDisallowInterceptTouchEvent(true);
                            v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (draggingEntry != null) {
                        float yVal = (float) chart.getValuesByTouchPoint(event.getX(), event.getY(),
                                YAxis.AxisDependency.LEFT).y;
                        yVal = Math.max(50, Math.min(3000, yVal)); // Clamp
                        draggingEntry.setY(yVal);
                        refreshChart();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (draggingEntry != null) {
                        // Check if this was a tap (minimal movement)
                        float dx = Math.abs(event.getX() - touchDownX);
                        float dy = Math.abs(event.getY() - touchDownY);
                        boolean wasTap = dx < TAP_THRESHOLD && dy < TAP_THRESHOLD;

                        if (wasTap && h != null) {
                            // Tap detected - show voltage edit dialog
                            int idx = (int) h.getX();
                            if (idx >= 0 && idx < activeEntries.size()) {
                                showVoltageEditDialog(idx);
                            }
                        } else {
                            // Drag completed - save changes
                            saveToMemory(true);
                        }

                        draggingEntry = null;
                        chart.highlightValue(null);
                        chart.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                    if (draggingEntry != null) {
                        draggingEntry = null;
                        chart.highlightValue(null);
                        chart.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;
            }
            return true;
        });
    }

    private void showVoltageEditDialog(int entryIndex) {
        if (getContext() == null || voltageLevels == null)
            return;

        // Get available voltage levels from ChipInfo
        String[] levelStrings = com.ireddragonicy.konabessnext.ChipInfo.rpmh_levels.level_str();
        int[] levelValues = com.ireddragonicy.konabessnext.ChipInfo.rpmh_levels.levels();

        if (levelStrings == null || levelStrings.length == 0) {
            Toast.makeText(getContext(), "Voltage levels not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find current selection
        int currentLevel = voltageLevels[entryIndex];
        int currentSelection = 0;
        for (int i = 0; i < levelValues.length; i++) {
            if (levelValues[i] == currentLevel) {
                currentSelection = i;
                break;
            }
        }

        final int[] selectedIndex = { currentSelection };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Voltage Level")
                .setSingleChoiceItems(levelStrings, currentSelection, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton("Apply", (dialog, which) -> {
                    int newLevel = levelValues[selectedIndex[0]];
                    voltageLevels[entryIndex] = newLevel;

                    // Update x-axis label
                    String newLabel;
                    try {
                        String voltLabel = GpuVoltEditor.levelint2str(newLevel);
                        newLabel = newLevel + " - " + voltLabel;
                    } catch (Exception e) {
                        newLabel = String.valueOf(newLevel);
                    }
                    xLabels.set(entryIndex, newLabel);

                    refreshChart();
                    saveToMemory(true);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyGlobalOffset(int offset) {
        globalOffset = offset;
        offsetValueText.setText(offset + " MHz");

        for (int i = 0; i < activeEntries.size(); i++) {
            float refVal = referenceEntries.get(i).getY();
            activeEntries.get(i).setY(Math.max(50, refVal + offset));
        }
        refreshChart();
    }

    private void saveToMemory(boolean silent) {
        if (GpuTableEditor.bins == null || GpuTableEditor.bins.isEmpty())
            return;

        GpuTableEditor.bin targetBin = null;
        int idx = 0;
        for (GpuTableEditor.bin b : GpuTableEditor.bins) {
            if (idx == binIndex) {
                targetBin = b;
                break;
            }
            idx++;
        }

        if (targetBin == null)
            return;

        final GpuTableEditor.bin finalTargetBin = targetBin;
        final int size = finalTargetBin.levels.size();

        // Capture new frequency values from chart
        final long[] newFreqValues = new long[activeEntries.size()];
        for (int i = 0; i < activeEntries.size(); i++) {
            newFreqValues[i] = (long) (activeEntries.get(i).getY() * 1_000_000);
        }

        // Capture voltage levels
        final int[] newVoltLevels = voltageLevels != null ? voltageLevels.clone() : new int[0];

        // Apply change with proper undo support - changes happen INSIDE the lambda
        try {
            GpuTableEditor.applyChange("Curve Edit: Updated frequencies & voltages", () -> {
                for (int i = 0; i < newFreqValues.length; i++) {
                    int originalIdx = size - 1 - i;
                    if (originalIdx < 0 || originalIdx >= size)
                        continue;

                    GpuTableEditor.level lvl = finalTargetBin.levels.get(originalIdx);
                    for (int j = 0; j < lvl.lines.size(); j++) {
                        String line = lvl.lines.get(j);

                        // Update frequency
                        if (line.contains("qcom,gpu-freq")) {
                            String propName = line.substring(0, line.indexOf("=")).trim();
                            String newLine = propName + " = <0x" + Long.toHexString(newFreqValues[i]) + ">;";
                            lvl.lines.set(j, newLine);
                        }

                        // Update voltage level
                        if ((line.contains("qcom,level") || line.contains("qcom,cx-level"))
                                && i < newVoltLevels.length) {
                            String propName = line.substring(0, line.indexOf("=")).trim();
                            String newLine = propName + " = <0x" + Integer.toHexString(newVoltLevels[i]) + ">;";
                            lvl.lines.set(j, newLine);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!silent) {
            Toast.makeText(getContext(), "Changes saved to memory", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        saveToMemory(false);
        if (getContext() != null) {
            GpuTableEditor.saveFrequencyTable(getContext(), true, "Saved from Curve Editor");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data in case it changed externally
        loadData();
        refreshChart();
    }
}
