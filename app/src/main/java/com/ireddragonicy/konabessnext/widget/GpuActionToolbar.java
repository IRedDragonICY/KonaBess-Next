package com.ireddragonicy.konabessnext.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.ireddragonicy.konabessnext.ChipInfo;
import com.ireddragonicy.konabessnext.GpuTableEditor;
import com.ireddragonicy.konabessnext.GpuVoltEditor;
import com.ireddragonicy.konabessnext.MainActivity;
import com.ireddragonicy.konabessnext.R;

public class GpuActionToolbar extends LinearLayout implements GpuTableEditor.OnHistoryStateChangedListener {

    private MaterialButton btnSave, btnUndo, btnRedo, btnHistory, btnVolt, btnRepack;
    private View parentViewForVolt;
    private boolean showVolt = false;
    private boolean showRepack = false;

    public GpuActionToolbar(Context context) {
        super(context);
        init(context);
    }

    public GpuActionToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);

        // Default configs
        if (context instanceof MainActivity) {
            showRepack = true;
            showVolt = !ChipInfo.which.ignoreVoltTable;
        }
    }

    public void setParentViewForVolt(View view) {
        this.parentViewForVolt = view;
    }

    public void build(Activity activity) {
        removeAllViews();

        int chipSpacing = (int) (activity.getResources().getDisplayMetrics().density * 8);

        // First Row: Save, Undo, Redo, History
        LinearLayout firstRow = new LinearLayout(activity);
        firstRow.setOrientation(HORIZONTAL);
        firstRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        btnSave = GpuTableEditor.createCompactChip(activity, R.string.save_freq_table, R.drawable.ic_file_upload);
        btnSave.setOnClickListener(v -> GpuTableEditor.saveFrequencyTable(activity, true, "Saved manually"));

        btnUndo = GpuTableEditor.createCompactChip(activity, R.string.undo, R.drawable.ic_undo);
        btnUndo.setOnClickListener(v -> GpuTableEditor.handleUndo());

        btnRedo = GpuTableEditor.createCompactChip(activity, R.string.redo, R.drawable.ic_redo);
        btnRedo.setOnClickListener(v -> GpuTableEditor.handleRedo());

        btnHistory = GpuTableEditor.createCompactChip(activity, R.string.history, R.drawable.ic_history);
        btnHistory.setOnClickListener(v -> GpuTableEditor.showHistoryDialog(activity));

        // Layout Params (Weight 1.0f)
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        chipParams.setMargins(0, 0, chipSpacing, 0);

        LinearLayout.LayoutParams lastChipParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);

        btnSave.setLayoutParams(chipParams);
        btnUndo.setLayoutParams(chipParams);
        btnRedo.setLayoutParams(chipParams);
        btnHistory.setLayoutParams(lastChipParams);

        firstRow.addView(btnSave);
        firstRow.addView(btnUndo);
        firstRow.addView(btnRedo);
        firstRow.addView(btnHistory);

        addView(firstRow);

        // Second Row: Volt & Repack
        boolean hasSecondRow = false;
        LinearLayout secondRow = new LinearLayout(activity);
        secondRow.setOrientation(HORIZONTAL);
        LayoutParams secondRowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        secondRowParams.setMargins(0, chipSpacing, 0, 0);
        secondRow.setLayoutParams(secondRowParams);

        if (showVolt) {
            btnVolt = GpuTableEditor.createCompactChip(activity, R.string.edit_gpu_volt_table, R.drawable.ic_voltage);
            btnVolt.setOnClickListener(v -> {
                if (parentViewForVolt != null && activity instanceof MainActivity) {
                    new GpuVoltEditor.gpuVoltLogic((MainActivity) activity, (LinearLayout) parentViewForVolt).start();
                } else if (parentViewForVolt == null) {
                    // Fallback using this view's parent? tricky if parent is not the main container
                    Toast.makeText(activity, "Error: Parent view not set for Voltage Editor", Toast.LENGTH_SHORT)
                            .show();
                }
            });

            LinearLayout.LayoutParams voltParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
            voltParams.setMargins(0, 0, chipSpacing, 0);
            btnVolt.setLayoutParams(voltParams);
            secondRow.addView(btnVolt);
            hasSecondRow = true;
        }

        if (showRepack && activity instanceof MainActivity) {
            btnRepack = GpuTableEditor.createCompactChip(activity, R.string.repack_flash, R.drawable.ic_flash);
            btnRepack.setOnClickListener(v -> ((MainActivity) activity).new repackLogic().start());

            LinearLayout.LayoutParams repackParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
            if (!hasSecondRow) {
                repackParams.setMargins(0, 0, chipSpacing, 0);
            }
            btnRepack.setLayoutParams(repackParams);
            secondRow.addView(btnRepack);
            hasSecondRow = true;
        }

        if (hasSecondRow) {
            addView(secondRow);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        GpuTableEditor.addHistoryListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        GpuTableEditor.removeHistoryListener(this);
    }

    @Override
    public void onHistoryStateChanged(boolean canUndo, boolean canRedo) {
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).runOnUiThread(() -> {
                if (btnUndo != null) {
                    btnUndo.setEnabled(canUndo);
                    btnUndo.setAlpha(canUndo ? 1f : 0.5f);
                }
                if (btnRedo != null) {
                    btnRedo.setEnabled(canRedo);
                    btnRedo.setAlpha(canRedo ? 1f : 0.5f);
                }
            });
        }
    }
}
