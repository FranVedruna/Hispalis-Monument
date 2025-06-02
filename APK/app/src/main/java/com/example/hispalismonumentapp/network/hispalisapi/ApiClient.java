package com.example.hispalismonumentapp.network.hispalisapi;

import android.util.Log;

import com.example.hispalismonumentapp.models.MonumentoDTO;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.hispalismonumentapp.models.MonumentoDTODeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiClient {
    private static String BASE_URL = "http://hispalismonuments.duckdns.org:8080/";
    private static Retrofit retrofit = null;
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(MonumentoDTO.class, new MonumentoDTODeserializer())
            .create();

    public static Gson getGson() {
        return gson;
    }

    public static synchronized void setBaseUrl(String baseUrl) {
        if (!BASE_URL.equals(baseUrl)) {
            BASE_URL = baseUrl;
            retrofit = null; // Forzar recreación del cliente Retrofit con la nueva URL
            Log.d("ApiClient", "Base URL actualizada a: " + BASE_URL);
        }
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }


}

