package com.example.hispalismonumentapp.network;


import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.models.UserPageResponse;
import com.example.hispalismonumentapp.models.auth.AuthResponse;
import com.example.hispalismonumentapp.models.auth.LoginRequest;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.MonumentoPageResponse;
import com.example.hispalismonumentapp.models.auth.RegisterRequest;
import com.example.hispalismonumentapp.models.ResponseDTO;
import com.example.hispalismonumentapp.models.TypeDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @GET("api/type")
    Call<List<TypeDTO>> getAllTypes(@Header("Authorization") String authHeader);

    @GET("api/monumentos/buscar")
    Call<MonumentoDTO> buscarMonumento(@Header("Authorization") String authHeader, @Query("nombre") String nombre);

    @GET("api/users/me")
    Call<UserDTO> getCurrentUser(@Header("Authorization") String authHeader);

    @POST("api/monumentos/visitado/{nombre}")
    Call<Void> marcarVisitado(@Header("Authorization") String authHeader, @Path("nombre") String nombreMonumento);

    // En tu ApiService.java
    @Multipart
    @POST("/api/users/me/photo")
    Call<ResponseBody> uploadUserPhoto(  // Cambiado a ResponseBody
                                         @Header("Authorization") String authHeader,
                                         @Part MultipartBody.Part file
    );

    @GET("api/users")
    Call<UserPageResponse> getUsers(
            @Header("Authorization") String authHeader,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/users/find")
    Call<UserDTO> findUserByName(@Header("Authorization") String authHeader, @Query("nombre") String nombre);


}