package com.ireddragonicy.konabessnext.viewmodel;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ireddragonicy.konabessnext.core.ChipInfo;
import com.ireddragonicy.konabessnext.core.KonaBessCore;

import java.util.List;

/**
 * ViewModel for device/chipset detection and management.
 * Handles async chipset detection and selection state.
 */
public class DeviceViewModel extends ViewModel {

    // Detection state
    private final MutableLiveData<UiState<List<KonaBessCore.Dtb>>> detectionState = new MutableLiveData<>();
    private final MutableLiveData<KonaBessCore.Dtb> selectedChipset = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPrepared = new MutableLiveData<>(false);

    // Events
    private final MutableLiveData<Event<Integer>> recommendedIndex = new MutableLiveData<>();

    public DeviceViewModel() {
        // Check if already prepared
        isPrepared.setValue(KonaBessCore.isPrepared());
        if (KonaBessCore.isPrepared()) {
            selectedChipset.setValue(KonaBessCore.getCurrentDtb());
        }
    }

    // ========================================================================
    // LiveData Getters
    // ========================================================================

    public LiveData<UiState<List<KonaBessCore.Dtb>>> getDetectionState() {
        return detectionState;
    }

    public LiveData<KonaBessCore.Dtb> getSelectedChipset() {
        return selectedChipset;
    }

    public LiveData<Boolean> getIsPrepared() {
        return isPrepared;
    }

    public LiveData<Event<Integer>> getRecommendedIndex() {
        return recommendedIndex;
    }

    // ========================================================================
    // Operations
    // ========================================================================

    /**
     * Start chipset detection process.
     */
    public void detectChipset(Context context) {
        detectionState.setValue(UiState.loading());

        new Thread(() -> {
            try {
                // Setup environment
                KonaBessCore.setupEnv(context);

                // Get boot image (throws IOException on failure)
                KonaBessCore.getBootImage(context);

                // Convert boot image to DTS (throws IOException on failure)
                KonaBessCore.bootImage2dts(context);

                // Check device - populates KonaBessCore.dtbs static field
                KonaBessCore.checkDevice(context);

                // Get detected chipsets from static field
                List<KonaBessCore.Dtb> dtbs = KonaBessCore.dtbs;
                if (dtbs == null || dtbs.isEmpty()) {
                    postError("No compatible chipset found");
                    return;
                }

                // Find recommended index
                int recommended = findRecommendedIndex(dtbs);

                // Post success
                detectionState.postValue(UiState.success(dtbs));

                if (dtbs.size() == 1) {
                    // Auto-select single chipset
                    selectChipset(dtbs.get(0));
                } else {
                    // Notify UI to show selection
                    recommendedIndex.postValue(Event.of(recommended));
                }

            } catch (Exception e) {
                postError("Detection failed: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Select a specific chipset.
     */
    public void selectChipset(KonaBessCore.Dtb dtb) {
        KonaBessCore.setCurrentDtb(dtb);
        ChipInfo.which = dtb.type;
        selectedChipset.postValue(dtb);
        isPrepared.postValue(true);
    }

    /**
     * Try to restore last used chipset.
     */
    public void tryRestoreLastChipset(Activity activity) {
        new Thread(() -> {
            boolean restored = KonaBessCore.tryRestoreLastChipset(activity);
            if (restored) {
                KonaBessCore.Dtb current = KonaBessCore.getCurrentDtb();
                if (current != null) {
                    selectedChipset.postValue(current);
                    isPrepared.postValue(true);
                }
            }
        }).start();
    }

    /**
     * Clear current chipset selection.
     */
    public void clearChipset(Context context) {
        KonaBessCore.resetState();
        KonaBessCore.clearLastChipset(context);
        selectedChipset.setValue(null);
        isPrepared.setValue(false);
        detectionState.setValue(null);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private int findRecommendedIndex(List<KonaBessCore.Dtb> dtbs) {
        // Find first non-unknown type
        for (int i = 0; i < dtbs.size(); i++) {
            if (dtbs.get(i).type != ChipInfo.type.unknown) {
                return i;
            }
        }
        return 0;
    }

    private void postError(String message) {
        detectionState.postValue(UiState.error(message));
        isPrepared.postValue(false);
    }

    /**
     * Check if device is prepared.
     */
    public boolean isDevicePrepared() {
        Boolean prepared = isPrepared.getValue();
        return prepared != null && prepared;
    }

    /**
     * Get current chipset type.
     */
    public ChipInfo.type getCurrentChipType() {
        KonaBessCore.Dtb dtb = selectedChipset.getValue();
        return dtb != null ? dtb.type : null;
    }
}



