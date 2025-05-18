package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAddMonument;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);
        if (!checkAuth()) return;

        initializeViews();
        setupNavigation();
        loadInitialFragment();
    }

    private boolean checkAuth() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return false;
        }
        return true;
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabAddMonument = findViewById(R.id.fabAddMonument);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(navListener);

        fabAddMonument.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMonumentActivity.class);
            startActivity(intent);
        });
    }

    private void loadInitialFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            fabAddMonument.show();
        } else if (itemId == R.id.nav_monuments) {
            selectedFragment = new MonumentsFragment();
            fabAddMonument.hide();
        } else if (itemId == R.id.nav_community) {
            selectedFragment = new CommunityFragment();
            fabAddMonument.hide();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
            fabAddMonument.hide();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }
        return true;
    };
}