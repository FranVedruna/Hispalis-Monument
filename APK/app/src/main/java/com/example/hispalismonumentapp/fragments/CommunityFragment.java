package com.example.hispalismonumentapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.adapters.UserAdapter;
import com.example.hispalismonumentapp.models.UserPageResponse;
import com.example.hispalismonumentapp.network.TokenManager;
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button buttonNext, buttonPrevious;
    private UserAdapter adapter;
    private int currentPage = 0;
    private int totalPages = 1;
    private Call<UserPageResponse> currentCall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Configurar botones de paginación
        buttonNext = view.findViewById(R.id.buttonNext);
        buttonPrevious = view.findViewById(R.id.buttonPrevious);

        buttonNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadUsers();
            }
        });

        buttonPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadUsers();
            }
        });

        // Carga inicial de datos
        loadUsers();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancelar petición en curso al salir del fragment
        if (currentCall != null) {
            currentCall.cancel();
        }
    }

    private void loadUsers() {
        // Verificar que el fragment esté activo
        if (!isAdded() || getContext() == null) {
            return;
        }

        // Obtener token
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(getContext(), "Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cancelar petición anterior si existe
        if (currentCall != null) {
            currentCall.cancel();
        }

        // Realizar nueva petición
        ApiService apiService = ApiClient.getApiService();
        currentCall = apiService.getUsers("Bearer " + token, currentPage, 10);

        currentCall.enqueue(new Callback<UserPageResponse>() {
            @Override
            public void onResponse(Call<UserPageResponse> call, Response<UserPageResponse> response) {
                // Verificar que el fragment esté activo
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    // Actualizar UI con los nuevos datos
                    adapter.updateUsers(response.body().getContent());
                    totalPages = response.body().getTotalPages();

                    // Actualizar estado de los botones
                    buttonNext.setEnabled(currentPage < totalPages - 1);
                    buttonPrevious.setEnabled(currentPage > 0);
                } else {
                    Toast.makeText(getContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserPageResponse> call, Throwable t) {
                // Solo mostrar error si no fue cancelado y el fragment está activo
                if (!call.isCanceled() && isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}