package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.auth.RegisterRequest;
import com.example.hispalismonumentapp.models.ResponseDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        etName = findViewById(R.id.editTextText);
        etEmail = findViewById(R.id.editTextText2);
        etPassword = findViewById(R.id.editTextText3);
        btnRegister = findViewById(R.id.button);

        // Configurar el clic del botón de registro
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        // Obtener los valores de los campos
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validar campos
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(name, email, password);
        registerUser(registerRequest);
    }

    private void registerUser(RegisterRequest registerRequest) {
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseDTO> call = apiService.register(registerRequest);

        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Toast.makeText(RegisterActivity.this,
                                "Usuario o correo electrónico ya registrados: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}