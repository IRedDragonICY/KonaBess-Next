package xzr.konabess;

import android.app.Application;
import android.content.Context;

import xzr.konabess.utils.LocaleUtil;

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
