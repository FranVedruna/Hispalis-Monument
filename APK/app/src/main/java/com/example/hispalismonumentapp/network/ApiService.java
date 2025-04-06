package com.example.hispalismonumentapp.network;


import com.example.hispalismonumentapp.models.AuthResponse;
import com.example.hispalismonumentapp.models.LoginRequest;
import com.example.hispalismonumentapp.models.RegisterRequest;
import com.example.hispalismonumentapp.models.ResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<ResponseDTO> register(@Body RegisterRequest request);
}