package com.example.hispalismonumentapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.adapters.MonumentAdapter;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.MonumentoPageResponse;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HOME";
    private RecyclerView recyclerView;
    private MonumentAdapter adapter;
    private List<MonumentoDTO> monumentList = new ArrayList<>(); // Cambiado de Monument a MonumentoDTO
    private TokenManager tokenManager;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private FloatingActionButton fabAddMonument;

    // Variables para paginación
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar vistas
        progressBar = findViewById(R.id.progressBar);
        tvEmptyView = findViewById(R.id.tvEmptyView);
        recyclerView = findViewById(R.id.recyclerViewMonuments);
        fabAddMonument = findViewById(R.id.fabAddMonument); // Nuevo FAB

        fabAddMonument.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMonumentActivity.class);
            startActivity(intent);
        });

        // Inicializar TokenManager
        tokenManager = new TokenManager(this);

        // Verificar autenticación
        if (!checkAuth()) {
            return;
        }

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar primeros monumentos
        loadFirstItems();
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

    private void setupRecyclerView() {
        String token = tokenManager.getToken();
        // Cambiar el adapter para usar MonumentoDTO directamente
        adapter = new MonumentAdapter(this, monumentList, token);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Listener para paginación
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        });
    }

    private void loadFirstItems() {
        isLoading = true;
        currentPage = 0;
        isLastPage = false;
        monumentList.clear();
        showLoading(true);
        fetchMonumentsFromApi(currentPage);
    }

    private void loadMoreItems() {
        isLoading = true;
        currentPage++;
        fetchMonumentsFromApi(currentPage);
    }

    private void fetchMonumentsFromApi(int page) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showError("Token no válido");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<MonumentoPageResponse> call = apiService.getMonumentos(
                "Bearer " + token,
                page,
                PAGE_SIZE,
                "nombre,asc"
        );

        call.enqueue(new Callback<MonumentoPageResponse>() {
            @Override
            public void onResponse(Call<MonumentoPageResponse> call, Response<MonumentoPageResponse> response) {
                isLoading = false;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<MonumentoDTO> monumentosDTO = response.body().getContent();
                    if (monumentosDTO != null && !monumentosDTO.isEmpty()) {
                        handleApiResponse(monumentosDTO, page);
                    } else {
                        showEmptyView(page == 0);
                    }
                } else {
                    try {
                        // Intenta parsear como array directo si falla como objeto
                        Type listType = new TypeToken<List<MonumentoDTO>>(){}.getType();
                        List<MonumentoDTO> monumentos = ApiClient.getGson().fromJson(
                                response.errorBody().string(),
                                listType);
                        handleApiResponse(monumentos, page);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al parsear respuesta", e);
                        handleApiError(response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<MonumentoPageResponse> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Log.e(TAG, "Error de conexión: ", t);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void handleApiResponse(List<MonumentoDTO> monumentosDTO, int page) {
        // Ya no necesitamos convertDtosToModels, usamos los DTOs directamente
        if (monumentosDTO != null && monumentosDTO.size() < PAGE_SIZE) {
            isLastPage = true;
        }

        runOnUiThread(() -> {
            if (page == 0) {
                monumentList.clear();
                monumentList.addAll(monumentosDTO);
                adapter.notifyDataSetChanged();
            } else {
                int previousSize = monumentList.size();
                monumentList.addAll(monumentosDTO);
                adapter.notifyItemRangeInserted(previousSize, monumentosDTO.size());
            }

            // Actualizar UI
            if (monumentList.isEmpty()) {
                showEmptyView(true);
            } else {
                tvEmptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<MonumentoDTO> convertDtosToModels(List<MonumentoDTO> dtos) {
        List<MonumentoDTO> monuments = new ArrayList<>();
        if (dtos != null) {
            for (MonumentoDTO dto : dtos) {
                monuments.add(new MonumentoDTO(
                        dto.getId(),
                        dto.getNombre(),
                        dto.getDescripcion(),
                        dto.getFotoUrl(),
                        dto.getLatitud(),
                        dto.getLongitud(),
                        dto.getTypes() != null ? dto.getTypes() : new ArrayList<>(),
                        dto.getWikiPath() != null ? dto.getWikiPath() : ""
                ));
            }
        }
        return monuments;
    }

    private void handleApiError(int errorCode) {
        runOnUiThread(() -> {
            if (errorCode == 401) { // Unauthorized
                tokenManager.clearToken();
                Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Login.class));
                finish();
            } else {
                showError("Error del servidor: " + errorCode);
            }
        });
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (tvEmptyView != null) {
                tvEmptyView.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            if (monumentList.isEmpty()) {
                tvEmptyView.setText(message);
                tvEmptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showEmptyView(boolean show) {
        runOnUiThread(() -> {
            if (show) {
                tvEmptyView.setText("No se encontraron monumentos");
                tvEmptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}