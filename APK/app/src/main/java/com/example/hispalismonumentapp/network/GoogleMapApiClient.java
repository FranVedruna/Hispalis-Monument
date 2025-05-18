package com.example.hispalismonumentapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleMapApiClient {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static Retrofit retrofit = null;

    public static GoogleMapApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(GoogleMapApiService.class);
    }
}
