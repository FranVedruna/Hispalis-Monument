package com.example.hispalismonumentapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        prefs.edit().putString("language", language).apply();

        return context.createConfigurationContext(config);
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return prefs.getString("language", "es"); // Por defecto español
    }
}