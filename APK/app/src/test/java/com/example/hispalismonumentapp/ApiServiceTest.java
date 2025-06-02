package com.example.hispalismonumentapp;

import static org.junit.Assert.*;

import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.models.UserPageResponse;
import com.example.hispalismonumentapp.models.auth.AuthResponse;
import com.example.hispalismonumentapp.models.auth.LoginRequest;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.MonumentoPageResponse;
import com.example.hispalismonumentapp.models.TypeDTO;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiServiceTest {

    private ApiService apiService;
    private String authToken;

    @Before
    public void setup() throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://hispalismonuments.duckdns.org:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Login para obtener token antes de cada test
        LoginRequest loginRequest = new LoginRequest("Ejemplo", "ejemplo");
        retrofit2.Response<AuthResponse> loginResponse = apiService.login(loginRequest).execute();

        assertTrue("Login fallido", loginResponse.isSuccessful());
        assertNotNull("Token no debe ser null", loginResponse.body().getToken());

        authToken = "Bearer " + loginResponse.body().getToken();
    }

    @Test
    public void testGetMonumentos() throws Exception {
        retrofit2.Response<MonumentoPageResponse> response = apiService.getMonumentos(authToken, 0, 10, "nombre").execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().getContent().size() > 0);
    }

    @Test
    public void testBuscarMonumento() throws Exception {
        retrofit2.Response<MonumentoDTO> response = apiService.buscarMonumento(authToken, "Plaza de Armas").execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals("Plaza de Armas", response.body().getNombre());
    }

    @Test
    public void testGetAllTypes() throws Exception {
        retrofit2.Response<List<TypeDTO>> response = apiService.getAllTypes(authToken).execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().size() > 0);
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        retrofit2.Response<UserDTO> response = apiService.getCurrentUser(authToken).execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals("Ejemplo", response.body().getUserName());
    }


    @Test
    public void testGetUsers() throws Exception {
        retrofit2.Response<UserPageResponse> response = apiService.getUsers(authToken, 0, 10).execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().getContent().size() > 0);
    }

    @Test
    public void testFindUserByName() throws Exception {
        retrofit2.Response<UserDTO> response = apiService.findUserByName(authToken, "Ejemplo").execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals("Ejemplo", response.body().getUserName());
    }

    @Test
    public void testGetVisitedMonumentCount() throws Exception {
        retrofit2.Response<Integer> response = apiService.getVisitedMonumentCount(authToken, "Ejemplo").execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body() >= 0);
    }

    @Test
    public void testUpgradeUser() throws Exception {
        retrofit2.Response<okhttp3.ResponseBody> response = apiService.upgradeUser(authToken, "Ejemplo").execute();

        assertTrue(response.isSuccessful());
    }





}
