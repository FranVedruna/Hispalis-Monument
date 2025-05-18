package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.fragments.CommunityFragment;
import com.example.hispalismonumentapp.fragments.HomeFragment;
import com.example.hispalismonumentapp.fragments.MonumentsFragment;
import com.example.hispalismonumentapp.fragments.ProfileFragment;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicialización del TokenManager
        tokenManager = new TokenManager(this);

        // Verificar autenticación
        if (!checkAuth()) {
            return;
        }

        // Inicializar vistas
        initializeViews();

        // Configurar navegación
        setupNavigation();

        // Cargar fragment inicial
        loadInitialFragment();
    }

    /**
     * Verifica si el usuario está autenticado
     */
    private boolean checkAuth() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return false;
        }
        return true;
    }

    /**
     * Inicializa las vistas principales
     */
    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    /**
     * Configura la navegación y los listeners
     */
    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_monuments) {
                selectedFragment = new MonumentsFragment();
            } else if (itemId == R.id.nav_community) {
                selectedFragment = new CommunityFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    /**
     * Carga el fragment inicial (HomeFragment)
     */
    private void loadInitialFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}