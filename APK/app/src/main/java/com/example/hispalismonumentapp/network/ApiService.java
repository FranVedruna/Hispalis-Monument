package com.example.hispalismonumentapp.network;


import com.example.hispalismonumentapp.models.AuthResponse;
import com.example.hispalismonumentapp.models.LoginRequest;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.MonumentoPageResponse;
import com.example.hispalismonumentapp.models.RegisterRequest;
import com.example.hispalismonumentapp.models.ResponseDTO;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<ResponseDTO> register(@Body RegisterRequest request);

    @GET("api/monumentos")
    Call<MonumentoPageResponse> getMonumentos(
            @Header("Authorization") String authHeader,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @Multipart
    @POST("api/monumentos/crear")
    Call<MonumentoDTO> createMonumento(
            @Header("Authorization") String authHeader,  // Añade este parámetro
            @Part("monumento") RequestBody monumentoJson,
            @Part MultipartBody.Part image
    );
}