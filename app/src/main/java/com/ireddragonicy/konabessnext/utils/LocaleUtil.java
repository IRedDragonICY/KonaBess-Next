package com.ireddragonicy.konabessnext.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

import com.ireddragonicy.konabessnext.ui.SettingsActivity;

public class LocaleUtil {
    private static final String PREFS_NAME = "KonaBessSettings";
    private static final String KEY_LANGUAGE = "language";

    private LocaleUtil() {
        // Utility class
    }

    public static Context wrap(Context context) {
        return applyLocale(context);
    }

    private static Context applyLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, SettingsActivity.LANGUAGE_ENGLISH);

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
        } else {
            configuration.setLocale(locale);
        }

        return context.createConfigurationContext(configuration);
    }
}



