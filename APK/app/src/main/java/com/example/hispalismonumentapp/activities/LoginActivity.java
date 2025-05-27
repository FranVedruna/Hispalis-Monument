package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.hispalismonumentapp.LocaleHelper;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.auth.AuthResponse;
import com.example.hispalismonumentapp.models.auth.LoginRequest;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {
    private Spinner serverSpinner;
    private SharedPreferences sharedPreferences;

    // Establece el idioma seleccionado antes de que se cree la actividad
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa las preferencias compartidas
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Asocia los elementos de la vista
        EditText usernameEditText = findViewById(R.id.userInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        ImageButton buttonSpanish = findViewById(R.id.buttonSpanish);
        ImageButton buttonEnglish = findViewById(R.id.buttonEnglish);
        serverSpinner = findViewById(R.id.serverSpinner);

        // Configurar el Spinner
        setupServerSpinner();

        //Cambio de idiomas
        buttonSpanish.setOnClickListener(v -> changeLanguage("es"));
        buttonEnglish.setOnClickListener(v -> changeLanguage("en"));

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();
            login(username, password);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Configura el spinner con opciones de servidores disponibles para conectarse.
     * Guarda la selección del usuario para reutilizarla en el futuro.
     */
    private void setupServerSpinner() {
        // Opciones para el Spinner
        String[] servers = {
                "hispalismonuments.duckdns.org",
                "localhost",
                "10.0.2.2 (Emulador)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                servers
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverSpinner.setAdapter(adapter);

        // Cargar selección previa si existe
        int savedSelection = sharedPreferences.getInt("serverSelection", 0);
        serverSpinner.setSelection(savedSelection);
    }

    /**
     * Obtiene la URL base según el servidor seleccionado en el spinner.
     * Guarda esta selección en las preferencias compartidas.
     */
    private String getSelectedServerUrl() {
        int position = serverSpinner.getSelectedItemPosition();
        // Guardar selección para futuras sesiones
        sharedPreferences.edit().putInt("serverSelection", position).apply();

        switch (position) {
            case 0: // hispalismonuments.duckdns.org
                return "http://hispalismonuments.duckdns.org:8080/";
            case 1: // localhost
                return "http://192.168.1.33:8080/"; // Para emulador, localhost se traduce a 10.0.2.2
            case 2: // 10.0.2.2 (Emulador)
                return "http://10.0.2.2:8080/";
            default:
                return "http://hispalismonuments.duckdns.org:8080/";
        }
    }

    /**
     * Inicia sesión haciendo una petición al servidor seleccionado.
     * Guarda el token si la respuesta es exitosa y redirige a la actividad principal.
     */
    private void login(String username, String password) {
        String baseUrl = getSelectedServerUrl();
        ApiClient.setBaseUrl(baseUrl); // Actualizamos la URL base antes de hacer la llamada

        LoginRequest request = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getApiService();

        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    new TokenManager(LoginActivity.this).saveToken(token);

                    // Redirigir a MainActivity
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Error en credenciales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de conexión con " + baseUrl, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cambia el idioma de la aplicación y reinicia la actividad actual para aplicar el cambio.
     *
     * @param languageCode Código del idioma a usar (por ejemplo: "es", "en").
     */
    private void changeLanguage(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}