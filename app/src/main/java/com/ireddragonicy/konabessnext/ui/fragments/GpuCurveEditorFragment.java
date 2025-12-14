package com.ireddragonicy.konabessnext.ui.fragments;

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

import androidx.activity.OnBackPressedCallback;
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
import com.ireddragonicy.konabessnext.core.GpuTableEditor;
import com.ireddragonicy.konabessnext.core.GpuVoltEditor;
import com.ireddragonicy.konabessnext.R;
import com.ireddragonicy.konabessnext.model.Bin;
import com.ireddragonicy.konabessnext.model.Level;
import com.ireddragonicy.konabessnext.utils.DtsHelper;
import com.ireddragonicy.konabessnext.ui.widget.GpuActionToolbar;

import java.util.ArrayList;
import java.util.List;

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
    private List<Level> originalLevels;
    private ArrayList<String> xLabels = new ArrayList<>();
    private java.util.HashMap<Integer, String> voltageLabelMap = new java.util.HashMap<>(); // Map voltage level ->
                                                                                            // label
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

        // Register OnBackPressedCallback for gesture navigation
        // This handles the edge swipe back gesture properly
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Pop this fragment from backstack
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
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
        Bin targetBin = null;
        int idx = 0;
        for (Bin b : GpuTableEditor.bins) {
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
        voltageLabelMap.clear();

        // Populate entries - X = voltage level (for proportional spacing)
        int size = originalLevels.size();
        voltageLevels = new int[size];
        for (int i = size - 1; i >= 0; i--) {
            Level lvl = originalLevels.get(i);
            int entryIndex = size - 1 - i; // Index in our arrays

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
            voltageLevels[entryIndex] = voltLevel;

            // Use voltage level as X coordinate for proportional spacing
            referenceEntries.add(new Entry(voltLevel, freqMhz));
            activeEntries.add(new Entry(voltLevel, freqMhz));

            // Get voltage label - format: "LABEL_NAME (level)" or just level number
            String voltLabel = null;
            try {
                String fullLabel = GpuVoltEditor.levelint2str(voltLevel);
                // Extract just the name part (e.g., "256 - NOM" -> "NOM")
                if (fullLabel != null && fullLabel.contains(" - ")) {
                    voltLabel = fullLabel.split(" - ")[1];
                } else if (fullLabel != null) {
                    voltLabel = fullLabel;
                }
            } catch (Exception ignored) {
            }
            String finalLabel = voltLabel != null ? voltLabel : String.valueOf(voltLevel);
            xLabels.add(finalLabel);
            voltageLabelMap.put(voltLevel, finalLabel);
        }

        // Sort entries by X (voltage level) for proper line connection
        java.util.Collections.sort(referenceEntries, (a, b) -> Float.compare(a.getX(), b.getX()));
        java.util.Collections.sort(activeEntries, (a, b) -> Float.compare(a.getX(), b.getX()));
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

        // Chart config - Timeline-like zoom experience
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setScaleXEnabled(true); // Allow X-axis zoom independently
        chart.setScaleYEnabled(true); // Allow Y-axis zoom independently
        chart.setPinchZoom(false); // Independent X/Y zoom for timeline-like experience
        chart.setDoubleTapToZoomEnabled(false); // Disable - conflicts with tap-to-edit
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraBottomOffset(16f); // More space for rotated labels
        chart.setVisibleXRangeMinimum(3); // Minimum 3 points visible when zoomed

        // X-Axis - shows voltage level values with proportional spacing
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(colorOnSurface);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(colorOutline);
        xAxis.setGridLineWidth(0.8f);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int voltLevel = (int) value;
                // Show voltage label at exact voltage positions
                if (voltageLabelMap.containsKey(voltLevel)) {
                    return voltageLabelMap.get(voltLevel) + " (" + voltLevel + ")";
                }
                return String.valueOf(voltLevel);
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
            int pointerCount = event.getPointerCount();

            // Request parent to not intercept touches so chart can pan/zoom
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }

            // Let chart handle multi-touch (pinch zoom) natively
            if (pointerCount > 1) {
                if (draggingEntry != null) {
                    // Cancel any ongoing drag when pinch starts
                    draggingEntry = null;
                    chart.highlightValue(null);
                }
                return false; // Let chart handle pinch
            }

            Highlight h = chart.getHighlightByTouchPoint(event.getX(), event.getY());

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownX = event.getX();
                    touchDownY = event.getY();
                    if (h != null) {
                        float voltageX = h.getX();
                        // Find entry by matching X value (voltage level)
                        Entry matchedEntry = null;
                        for (Entry e : activeEntries) {
                            if (Math.abs(e.getX() - voltageX) < 0.5f) {
                                matchedEntry = e;
                                break;
                            }
                        }

                        if (matchedEntry != null) {
                            // Calculate pixel position of the data point
                            com.github.mikephil.charting.utils.MPPointD pointPixel = chart
                                    .getTransformer(YAxis.AxisDependency.LEFT)
                                    .getPixelForValues(matchedEntry.getX(), matchedEntry.getY());

                            // Check if touch is within 50 pixels of the point
                            float dx = Math.abs(event.getX() - (float) pointPixel.x);
                            float dy = Math.abs(event.getY() - (float) pointPixel.y);
                            float distance = (float) Math.sqrt(dx * dx + dy * dy);

                            if (distance < 50f) { // Only start drag if close to point
                                draggingEntry = matchedEntry;
                                chart.highlightValue(h);
                                v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                                return true; // Consume to start drag
                            }
                        }
                    }
                    return false; // No point hit or too far, let chart handle pan

                case MotionEvent.ACTION_MOVE:
                    if (draggingEntry != null) {
                        float yVal = (float) chart.getValuesByTouchPoint(event.getX(), event.getY(),
                                YAxis.AxisDependency.LEFT).y;
                        yVal = Math.max(50, Math.min(3000, yVal)); // Clamp
                        draggingEntry.setY(yVal);
                        refreshChart();
                        return true; // Consume drag
                    }
                    return false; // Not dragging, let chart handle

                case MotionEvent.ACTION_UP:
                    if (draggingEntry != null) {
                        // Check if this was a tap (minimal movement)
                        float dx = Math.abs(event.getX() - touchDownX);
                        float dy = Math.abs(event.getY() - touchDownY);
                        boolean wasTap = dx < TAP_THRESHOLD && dy < TAP_THRESHOLD;

                        if (wasTap) {
                            // Tap detected - show voltage edit dialog
                            // Find index in voltageLevels array by matching draggingEntry's X value
                            int voltLevel = (int) draggingEntry.getX();
                            int entryIdx = -1;
                            for (int i = 0; i < voltageLevels.length; i++) {
                                if (voltageLevels[i] == voltLevel) {
                                    entryIdx = i;
                                    break;
                                }
                            }
                            if (entryIdx >= 0) {
                                showVoltageEditDialog(entryIdx);
                            }
                        } else {
                            // Drag completed - save changes
                            saveToMemory(true);
                        }

                        draggingEntry = null;
                        chart.highlightValue(null);
                        return true;
                    }
                    return false;

                case MotionEvent.ACTION_CANCEL:
                    if (draggingEntry != null) {
                        draggingEntry = null;
                        chart.highlightValue(null);
                    }
                    return false;
            }
            return false;
        });
    }

    private void showVoltageEditDialog(int entryIndex) {
        if (getContext() == null || voltageLevels == null)
            return;

        // Get available voltage levels from ChipInfo
        String[] levelStrings = com.ireddragonicy.konabessnext.core.ChipInfo.rpmh_levels.level_str();
        int[] levelValues = com.ireddragonicy.konabessnext.core.ChipInfo.rpmh_levels.levels();

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

        Bin targetBin = null;
        int idx = 0;
        for (Bin b : GpuTableEditor.bins) {
            if (idx == binIndex) {
                targetBin = b;
                break;
            }
            idx++;
        }

        if (targetBin == null)
            return;

        final Bin finalTargetBin = targetBin;
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

                    Level lvl = finalTargetBin.levels.get(originalIdx);
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
