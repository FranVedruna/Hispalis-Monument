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
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;
import com.example.hispalismonumentapp.models.UserPageResponse;
import com.example.hispalismonumentapp.network.TokenManager;

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

    private boolean isViewDestroyed = false;
    private Call<UserPageResponse> currentCall;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

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

        loadUsers(); // carga inicial

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewDestroyed = true;
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    private void loadUsers() {
        TokenManager tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(getContext(), "Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        currentCall = apiService.getUsers("Bearer " + token, currentPage, 10);

        currentCall.enqueue(new Callback<UserPageResponse>() {
            @Override
            public void onResponse(Call<UserPageResponse> call, Response<UserPageResponse> response) {
                if (isViewDestroyed) return;

                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateUsers(response.body().getContent());
                    totalPages = response.body().getTotalPages();
                } else {
                    Toast.makeText(getContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserPageResponse> call, Throwable t) {
                if (isViewDestroyed || call.isCanceled()) return;
                Toast.makeText(getContext(), "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

