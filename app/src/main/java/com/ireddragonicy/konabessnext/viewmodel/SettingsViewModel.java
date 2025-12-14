package com.ireddragonicy.konabessnext.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ireddragonicy.konabessnext.ui.SettingsActivity;

/**
 * ViewModel for Settings management.
 * Provides observable settings state.
 */
public class SettingsViewModel extends ViewModel {

    private static final String PREFS_NAME = SettingsActivity.PREFS_NAME;

    private final MutableLiveData<Integer> themeMode = new MutableLiveData<>();
    private final MutableLiveData<String> language = new MutableLiveData<>();
    private final MutableLiveData<Integer> frequencyUnit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> autoSaveEnabled = new MutableLiveData<>();
    private final MutableLiveData<Integer> colorPalette = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dynamicColorEnabled = new MutableLiveData<>();

    // Events
    private final MutableLiveData<Event<Boolean>> restartRequired = new MutableLiveData<>();

    public SettingsViewModel() {
        // Initial values will be loaded via loadSettings()
    }

    // ========================================================================
    // LiveData Getters
    // ========================================================================

    public LiveData<Integer> getThemeMode() {
        return themeMode;
    }

    public LiveData<String> getLanguage() {
        return language;
    }

    public LiveData<Integer> getFrequencyUnit() {
        return frequencyUnit;
    }

    public LiveData<Boolean> getAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public LiveData<Integer> getColorPalette() {
        return colorPalette;
    }

    public LiveData<Boolean> getDynamicColorEnabled() {
        return dynamicColorEnabled;
    }

    public LiveData<Event<Boolean>> getRestartRequired() {
        return restartRequired;
    }

    // ========================================================================
    // Load Settings
    // ========================================================================

    public void loadSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        themeMode.setValue(prefs.getInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_SYSTEM));
        language.setValue(prefs.getString(SettingsActivity.KEY_LANGUAGE, "system"));
        frequencyUnit.setValue(prefs.getInt(SettingsActivity.KEY_FREQ_UNIT, 1)); // Default MHz
        autoSaveEnabled.setValue(prefs.getBoolean(SettingsActivity.KEY_AUTO_SAVE_GPU_TABLE, false));
        colorPalette.setValue(prefs.getInt(SettingsActivity.KEY_COLOR_PALETTE, 0));
        dynamicColorEnabled.setValue(prefs.getBoolean(SettingsActivity.KEY_DYNAMIC_COLOR, false));
    }

    // ========================================================================
    // Update Settings
    // ========================================================================

    public void setThemeMode(Context context, int theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(SettingsActivity.KEY_THEME, theme).apply();
        themeMode.setValue(theme);
        restartRequired.setValue(Event.of(true));
    }

    public void setLanguage(Context context, String lang) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(SettingsActivity.KEY_LANGUAGE, lang).apply();
        language.setValue(lang);
        restartRequired.setValue(Event.of(true));
    }

    public void setFrequencyUnit(Context context, int unit) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(SettingsActivity.KEY_FREQ_UNIT, unit).apply();
        frequencyUnit.setValue(unit);
    }

    public void toggleAutoSave(Context context) {
        Boolean current = autoSaveEnabled.getValue();
        boolean newValue = current == null || !current;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SettingsActivity.KEY_AUTO_SAVE_GPU_TABLE, newValue).apply();
        autoSaveEnabled.setValue(newValue);
    }

    public void setColorPalette(Context context, int palette) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(SettingsActivity.KEY_COLOR_PALETTE, palette).apply();
        colorPalette.setValue(palette);
        restartRequired.setValue(Event.of(true));
    }

    public void toggleDynamicColor(Context context) {
        Boolean current = dynamicColorEnabled.getValue();
        boolean newValue = current == null || !current;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SettingsActivity.KEY_DYNAMIC_COLOR, newValue).apply();
        dynamicColorEnabled.setValue(newValue);
        restartRequired.setValue(Event.of(true));
    }

    // ========================================================================
    // Getters for current values
    // ========================================================================

    public int getCurrentTheme() {
        Integer theme = themeMode.getValue();
        return theme != null ? theme : SettingsActivity.THEME_SYSTEM;
    }

    public int getCurrentFrequencyUnit() {
        Integer unit = frequencyUnit.getValue();
        return unit != null ? unit : 1;
    }

    public boolean isAutoSaveEnabled() {
        Boolean enabled = autoSaveEnabled.getValue();
        return enabled != null && enabled;
    }
}



