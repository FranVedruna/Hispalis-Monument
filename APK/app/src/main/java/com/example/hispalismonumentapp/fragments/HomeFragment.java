package com.example.hispalismonumentapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.AddMonumentActivity;
import com.example.hispalismonumentapp.activities.Login;
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

public class HomeFragment extends Fragment {

    private static final String TAG = "HOME_FRAGMENT";
    private RecyclerView recyclerView;
    private MonumentAdapter adapter;
    private List<MonumentoDTO> monumentList = new ArrayList<>();
    private TokenManager tokenManager;
    private ProgressBar progressBar;
    private TextView tvEmptyView;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        setupRecyclerView();
        loadFirstItems();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMonuments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        tokenManager = new TokenManager(requireActivity());
    }

    private void setupRecyclerView() {
        String token = tokenManager.getToken();
        adapter = new MonumentAdapter(requireActivity(), monumentList, token);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

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
        if (monumentosDTO != null && monumentosDTO.size() < PAGE_SIZE) {
            isLastPage = true;
        }

        requireActivity().runOnUiThread(() -> {
            if (page == 0) {
                monumentList.clear();
                monumentList.addAll(monumentosDTO);
                adapter.notifyDataSetChanged();
            } else {
                int previousSize = monumentList.size();
                monumentList.addAll(monumentosDTO);
                adapter.notifyItemRangeInserted(previousSize, monumentosDTO.size());
            }

            if (monumentList.isEmpty()) {
                showEmptyView(true);
            } else {
                tvEmptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleApiError(int errorCode) {
        requireActivity().runOnUiThread(() -> {
            if (errorCode == 401) {
                tokenManager.clearToken();
                Toast.makeText(requireActivity(), "Sesión expirada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(requireActivity(), Login.class));
                requireActivity().finish();
            } else {
                showError("Error del servidor: " + errorCode);
            }
        });
    }

    private void showLoading(boolean show) {
        requireActivity().runOnUiThread(() -> {
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
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
            if (monumentList.isEmpty()) {
                tvEmptyView.setText(message);
                tvEmptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showEmptyView(boolean show) {
        requireActivity().runOnUiThread(() -> {
            if (show) {
                tvEmptyView.setText("No se encontraron monumentos");
                tvEmptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
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
}