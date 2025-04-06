package com.example.hispalismonumentapp;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthPreferences {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";

    private final SharedPreferences sharedPreferences;

    public AuthPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}