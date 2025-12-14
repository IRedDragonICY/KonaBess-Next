package com.ireddragonicy.konabessnext;

import android.app.Application;
import android.content.Context;

import com.ireddragonicy.konabessnext.utils.LocaleUtil;
import com.ireddragonicy.konabessnext.ui.SettingsActivity;

public class KonaBessApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.wrap(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SettingsActivity.applyThemeFromSettings(this);
    }
}
