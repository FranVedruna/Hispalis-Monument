package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.fragments.CommunityFragment;
import com.example.hispalismonumentapp.fragments.HomeFragment;
import com.example.hispalismonumentapp.fragments.ProfileFragment;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tokenManager = new TokenManager(this);
        if (!checkAuth()) {
            return;
        }
        initializeViews();
        setupNavigation();
        loadInitialFragment();
        checkAndShowIntroDialogs();
    }

    /**
     * Verifica si hay un token válido para acceder a la aplicación.
     * Si no lo hay, muestra un mensaje y redirige a la pantalla de login.
     *
     * @return true si el usuario está autenticado, false si no.
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


    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    /**
     * Configura la navegación inferior con sus respectivos fragmentos según la opción seleccionada.
     */
    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
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
     * Carga el fragmento inicial (por defecto HomeFragment) solo si no hay uno ya cargado.
     */
    private void loadInitialFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }


    /**
     * Muestra tres ventanas de diálogo informativas la primera vez que se abre la aplicación.
     */
    private void checkAndShowIntroDialogs() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("first_run", true);

        if (isFirstRun) {
            showDialog1(() -> {
                showDialog2(() -> {
                    showDialog3(() -> {
                        // Final: guardar que ya se mostraron los diálogos
                        prefs.edit().putBoolean("first_run", false).apply();
                    });
                });
            });
        }
    }

    private void showDialog1(Runnable onNext) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog1_title))
                .setMessage(getString(R.string.dialog1_message))
                .setPositiveButton(getString(R.string.next), (dialog, which) -> onNext.run())
                .setCancelable(false)
                .show();
    }

    private void showDialog2(Runnable onNext) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog2_title))
                .setMessage(getString(R.string.dialog2_message))
                .setPositiveButton(getString(R.string.next), (dialog, which) -> onNext.run())
                .setCancelable(false)
                .show();
    }

    private void showDialog3(Runnable onNext) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog3_title))
                .setMessage(getString(R.string.dialog3_message))
                .setPositiveButton(getString(R.string.done), (dialog, which) -> onNext.run())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}