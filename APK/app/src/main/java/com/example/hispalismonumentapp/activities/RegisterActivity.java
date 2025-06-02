package com.example.hispalismonumentapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.auth.RegisterRequest;
import com.example.hispalismonumentapp.models.ResponseDTO;
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;

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

        etName = findViewById(R.id.editTextText);
        etEmail = findViewById(R.id.editTextText2);
        etPassword = findViewById(R.id.editTextText3);
        btnRegister = findViewById(R.id.button);

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        String validationError = validateInputs(name, email, password);
        if (validationError != null) {
            showDialogError(() -> {}, validationError);
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(name, email, password);
        registerUser(registerRequest);
    }

    // ✅ Método extraído para validar datos (puede ser probado en tests)
    public static String validateInputs(String name, String email, String password) {
        if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return "Por favor, complete todos los campos";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Ingrese un correo electrónico válido";
        }

        if (password.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }

        return null; // ✅ Todo válido
    }

    private void registerUser(RegisterRequest registerRequest) {
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseDTO> call = apiService.register(registerRequest);

        call.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                if (response.isSuccessful()) {
                    showDialogSuccess(() -> {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        showDialogError(() -> {}, "Usuario o correo electrónico ya registrados: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                showDialogError(() -> {}, "Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showDialogSuccess(Runnable onNext) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.register_dialog_success_title))
                .setMessage(getString(R.string.register_dialog_success_message))
                .setPositiveButton(getString(R.string.next), (dialog, which) -> onNext.run())
                .setCancelable(false)
                .show();
    }

    private void showDialogError(Runnable onNext, String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.register_dialog_error_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.retry), (dialog, which) -> onNext.run())
                .setCancelable(false)
                .show();
    }
}

