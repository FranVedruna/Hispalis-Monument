package com.example.hispalismonumentapp;

import android.app.Application;
import android.content.Context;


public class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(
                LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base))
        );
    }
}