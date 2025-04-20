package com.example.hispalismonumentapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://hispalismonuments.duckdns.org:8080/";
    private static Retrofit retrofit = null;
    private static Gson gson = new GsonBuilder().create();

    public static Gson getGson() {
        return gson;
    }
    // Método sin parámetro Context
    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}