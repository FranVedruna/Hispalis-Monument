package com.example.hispalismonumentapp.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.AddMonumentActivity;
import com.example.hispalismonumentapp.activities.LoginActivity;
import com.example.hispalismonumentapp.activities.NavigationActivity;
import com.example.hispalismonumentapp.adapters.MonumentAdapterHome;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.MonumentoPageResponse;
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;
import com.example.hispalismonumentapp.network.directions.DirectionsResponse;
import com.example.hispalismonumentapp.network.directions.GoogleDirectionsService;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private MonumentAdapterHome adapter;
    private List<MonumentoDTO> monumentList = new ArrayList<>();
    private TokenManager tokenManager;
    private ProgressBar progressBar;
    private TextView tvEmptyView, tvPageInfo, tvSelectedCount;
    private Button btnPrevious, btnNext, btnStartTrip;
    private LinearLayout selectionControlsLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etSearch;
    private ImageButton btnSearch;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private FloatingActionButton fabAddMonument;
    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private int totalPages = 1;
    CheckBox cbOptimizedTrip;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupPaginationButtons();
        setupTripControls();
        loadFirstItems();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        btnSearch.setOnClickListener(v -> performSearch());
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
        checkUserRole();
        setupFab();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMonuments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyView = view.findViewById(R.id.tvEmptyView);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext = view.findViewById(R.id.btnNext);
        tokenManager = new TokenManager(requireActivity());
        btnStartTrip = view.findViewById(R.id.btnStartTrip);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        selectionControlsLayout = view.findViewById(R.id.selectionControlsLayout);
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        fabAddMonument = view.findViewById(R.id.fabAddMonument);
        fabAddMonument.setVisibility(View.GONE);
        cbOptimizedTrip = view.findViewById(R.id.cbOptimizedTrip);
    }

    private void checkUserRole() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<UserDTO> call = apiService.getCurrentUser("Bearer " + token);

        call.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDTO user = response.body();
                    if ("ADMIN".equals(user.getUserRol())) {
                        requireActivity().runOnUiThread(() -> {
                            fabAddMonument.setVisibility(View.VISIBLE);
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                Log.e("MonumentsFragment", "Error al obtener información del usuario", t);
            }
        });
    }

    private void setupFab() {
        fabAddMonument.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddMonumentActivity.class);
            startActivity(intent);
        });
    }

    private void setupPaginationButtons() {
        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                fetchMonumentsFromApi(currentPage);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                fetchMonumentsFromApi(currentPage);
            }
        });
    }

    private void performSearch() {
        String searchQuery = etSearch.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            // Si el campo de búsqueda está vacío, cargar la lista normal
            loadFirstItems();
        } else {
            // Realizar búsqueda por nombre parcial
            currentPage = 0;
            showLoading(true);
            searchMonumentsByPartialName(searchQuery, currentPage);
        }
    }

    private void searchMonumentsByPartialName(String name, int page) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            showError("Token no válido");
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<MonumentoPageResponse> call = apiService.searchMonumentsByPartialName(
                "Bearer " + token,
                name,
                page,
                PAGE_SIZE,
                "nombre,asc"
        );

        call.enqueue(new Callback<MonumentoPageResponse>() {
            @Override
            public void onResponse(Call<MonumentoPageResponse> call, Response<MonumentoPageResponse> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    MonumentoPageResponse pageResponse = response.body();
                    if (pageResponse != null) {
                        totalPages = pageResponse.getTotalPages();
                        List<MonumentoDTO> monumentosDTO = pageResponse.getContent();

                        if (monumentosDTO != null && !monumentosDTO.isEmpty()) {
                            handleApiResponse(monumentosDTO);
                        } else {
                            showEmptyView(page == 0);
                        }
                    }
                } else {
                    handleApiError(response.code());
                }
            }

            @Override
            public void onFailure(Call<MonumentoPageResponse> call, Throwable t) {
                showLoading(false);
                if (!call.isCanceled()) {
                    Log.e("NETWORK_ERROR", "Error de conexión", t);
                    showError("Error de conexión: " + t.getMessage());
                }
            }
        });
    }



    private void setupTripControls() {
        btnStartTrip.setOnClickListener(v -> startOptimizedTrip());
        updateSelectionControls();
    }

    private void startOptimizedTrip() {
        List<MonumentoDTO> selectedMonuments = adapter.getSelectedMonuments();

        if (selectedMonuments.size() < 2) {
            Toast.makeText(requireContext(), "Selecciona al menos 2 monumentos", Toast.LENGTH_SHORT).show();
            return;
        }

        for (MonumentoDTO monument : selectedMonuments) {
            if (monument.getLatitud() == null || monument.getLongitud() == null) {
                Toast.makeText(requireContext(), "Algunos monumentos no tienen coordenadas", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();

                // Verificar si el checkbox está activado para optimizar
                boolean shouldOptimize = cbOptimizedTrip.isChecked();

                if (shouldOptimize) {
                    optimizeRouteFromUserPosition(userLat, userLng, selectedMonuments);
                } else {
                    // Si no está activado, lanzar la navegación SIN optimizar
                    launchNavigationActivity(selectedMonuments, false);
                }
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void optimizeRouteFromUserPosition(double userLat, double userLng, List<MonumentoDTO> monuments) {
        if (monuments == null) {
            Toast.makeText(requireContext(), "Se necesitan al menos 2 monumentos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encontrar el monumento más lejano desde la posición del usuario
        MonumentoDTO farthestMonument = null;
        double maxDistance = -1;

        for (MonumentoDTO monument : monuments) {
            if (monument.getLatitud() != null && monument.getLongitud() != null) {
                double dx = monument.getLatitud() - userLat;
                double dy = monument.getLongitud() - userLng;
                double distance = dx * dx + dy * dy; // distancia al cuadrado
                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestMonument = monument;
                }
            }
        }

        if (farthestMonument == null) {
            Toast.makeText(requireContext(), "No se pudo determinar el destino", Toast.LENGTH_SHORT).show();
            return;
        }

        final MonumentoDTO destination = farthestMonument;
        List<MonumentoDTO> waypoints = new ArrayList<>(monuments);
        waypoints.remove(destination);

        String originStr = userLat + "," + userLng;
        String destinationStr = destination.getLatitud() + "," + destination.getLongitud();
        StringBuilder waypointsStr = new StringBuilder("optimize:true");

        for (MonumentoDTO wp : waypoints) {
            waypointsStr.append("|").append(wp.getLatitud()).append(",").append(wp.getLongitud());
        }

        showLoading(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoogleDirectionsService service = retrofit.create(GoogleDirectionsService.class);
        String apiKey = getString(R.string.google_maps_api_key);

        service.getOptimizedRoute(originStr, destinationStr, waypointsStr.toString(), apiKey)
                .enqueue(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && !response.body().routes.isEmpty()) {
                            DirectionsResponse.Route route = response.body().routes.get(0);
                            List<Integer> order = route.waypoint_order;

                            List<MonumentoDTO> finalRoute = new ArrayList<>();
                            MonumentoDTO userOrigin = new MonumentoDTO();
                            userOrigin.setNombre("Tu ubicación");
                            userOrigin.setLatitud(userLat);
                            userOrigin.setLongitud(userLng);

                            finalRoute.add(userOrigin);
                            for (int index : order) {
                                finalRoute.add(waypoints.get(index));
                            }
                            finalRoute.add(destination);

                            launchNavigationActivity(finalRoute, true);
                        } else {
                            Toast.makeText(requireContext(), "Error al optimizar la ruta", Toast.LENGTH_SHORT).show();
                            launchNavigationActivity(monuments, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        launchNavigationActivity(monuments, false);
                    }
                });
    }

    private void launchNavigationActivity(List<MonumentoDTO> monuments, boolean isOptimized) {
        // Log para verificar los monumentos y el orden
        Log.d("NAVIGATION_DEBUG", "Iniciando navegación con " + monuments.size() + " monumentos");
        Log.d("NAVIGATION_DEBUG", "Ruta optimizada: " + isOptimized);

        for (int i = 0; i < monuments.size(); i++) {
            MonumentoDTO m = monuments.get(i);
            Log.d("NAVIGATION_DEBUG", "Monumento " + (i+1) + ": " + m.getNombre() +
                    " (ID: " + m.getId() + ") - " +
                    "Coordenadas: " + m.getLatitud() + "," + m.getLongitud());
        }

        Intent intent = new Intent(requireActivity(), NavigationActivity.class);
        intent.putParcelableArrayListExtra("selected_monuments", new ArrayList<>(monuments));
        intent.putExtra("is_optimized", isOptimized);
        startActivity(intent);

        adapter.clearSelection();
        updateSelectionControls();
    }

    private void updateSelectionControls() {
        requireActivity().runOnUiThread(() -> {
            int selectedCount = adapter.getSelectedMonuments().size();
            if (selectedCount == 0) {
                selectionControlsLayout.setVisibility(View.GONE);
            } else {
                selectionControlsLayout.setVisibility(View.VISIBLE);
                tvSelectedCount.setText(selectedCount + " seleccionados");
            }
        });
    }

    private void setupRecyclerView() {
        String token = tokenManager.getToken();
        adapter = new MonumentAdapterHome(requireActivity(), monumentList, token);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnLongItemClickListener((view, position) -> {
            updateSelectionControls();
        });
    }

    private void loadFirstItems() {
        currentPage = 0;
        showLoading(true);
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
                showLoading(false);

                if (response.isSuccessful()) {
                    MonumentoPageResponse pageResponse = response.body();
                    if (pageResponse != null) {
                        totalPages = pageResponse.getTotalPages();
                        List<MonumentoDTO> monumentosDTO = pageResponse.getContent();

                        if (monumentosDTO != null && !monumentosDTO.isEmpty()) {
                            handleApiResponse(monumentosDTO);
                        } else {
                            showEmptyView(page == 0);
                        }
                    }
                } else {
                    handleApiError(response.code());
                }
            }

            @Override
            public void onFailure(Call<MonumentoPageResponse> call, Throwable t) {
                showLoading(false);
                if (!call.isCanceled()) {
                    Log.e("NETWORK_ERROR", "Error de conexión", t);
                    showError("Error de conexión: " + t.getMessage());
                }
            }
        });
    }

    private void handleApiResponse(List<MonumentoDTO> monumentosDTO) {
        requireActivity().runOnUiThread(() -> {
            monumentList.clear();
            monumentList.addAll(monumentosDTO);
            adapter.notifyDataSetChanged();

            btnPrevious.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < totalPages - 1);
            tvPageInfo.setText("Página " + (currentPage + 1) + " de " + totalPages);

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
                startActivity(new Intent(requireActivity(), LoginActivity.class));
                requireActivity().finish();
            } else {
                showError("Error del servidor: " + errorCode);
            }
        });
    }

    private void showLoading(boolean show) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            tvEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
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
}