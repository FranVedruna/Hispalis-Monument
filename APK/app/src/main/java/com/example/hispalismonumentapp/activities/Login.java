package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.AuthResponse;
import com.example.hispalismonumentapp.models.LoginRequest;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usernameEditText = findViewById(R.id.userInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            login(username, password);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }



    private void login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getApiService();

        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    new TokenManager(Login.this).saveToken(token);
//                    Toast.makeText(Login.this, "Token guardado", Toast.LENGTH_SHORT).show();

                    // Redirigir a MainActivity
                    Intent intent = new Intent(Login.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Opcional: cierra la actividad actual
                } else {
                    Toast.makeText(Login.this, "Error en credenciales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(Login.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}