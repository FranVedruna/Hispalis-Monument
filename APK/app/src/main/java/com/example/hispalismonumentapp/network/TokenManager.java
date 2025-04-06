package com.example.hispalismonumentapp.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_TOKEN = "auth_token";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Guarda el token
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply(); // Usamos apply() en lugar de commit() porque es asíncrono
    }

    // Obtiene el token (retorna null si no existe)
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Borra el token (para logout)
    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}