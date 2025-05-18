package com.example.hispalismonumentapp.network;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleMapApiService {

    @GET("directions/json")
    Call<DirectionsMapResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );
}