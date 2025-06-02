package com.example.hispalismonumentapp.network.directions;

import com.example.hispalismonumentapp.network.map.DirectionsMapResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleDirectionsService {
    @GET("directions/json")
    Call<DirectionsResponse> getOptimizedRoute(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("waypoints") String waypoints,
            @Query("key") String apiKey
    );

    @GET("maps/api/directions/json")
    Call<DirectionsMapResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );
}