package com.example.hispalismonumentapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.DirectionsMapResponse;
import com.example.hispalismonumentapp.network.GoogleMapApiClient;
import com.example.hispalismonumentapp.network.GoogleMapApiService;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private List<MonumentoDTO> monumentList;
    private int currentMonumentIndex = 0;
    private Marker userMarker;
    private Polyline routePolyline;
    private String authToken;
    private Location lastLocation;
    private ImageView imageViewCurrentMonument;
    private TextView textViewCurrentName;
    private TextView textViewCurrentDescription;

    private static final float MIN_DISTANCE_CHANGE_FOR_ROUTE_UPDATE = 10f; // metros
    private static final float PROXIMITY_RADIUS_METERS = 50f; // metros
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM_LEVEL = 15f;
    private boolean isManualLocation = false;
    private Location manualLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Inicializar vistas del monumento actual
        imageViewCurrentMonument = findViewById(R.id.imageViewCurrentMonument);
        textViewCurrentName = findViewById(R.id.textViewCurrentName);
        textViewCurrentDescription = findViewById(R.id.textViewCurrentDescription);

        initializeToken();
        initializeMonumentList();
        initializeMap();
        initializeLocationClient();

        // Mostrar el primer monumento
        if (monumentList != null && !monumentList.isEmpty()) {
            updateCurrentMonumentDisplay(monumentList.get(currentMonumentIndex));
        }
    }

    private void initializeToken() {
        TokenManager tokenManager = new TokenManager(this);
        authToken = tokenManager.getToken();
        if (authToken == null) {
            Toast.makeText(this, "Token de autenticación no encontrado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeMonumentList() {
        monumentList = getIntent().getParcelableArrayListExtra("selected_monuments");
        if (monumentList == null || monumentList.size() < 2) {
            Toast.makeText(this, "Se requieren al menos dos monumentos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        monumentList.remove(0); // Remove user's initial location
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupMapFeatures();
            addAllMonumentMarkers();
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        mMap.setOnMapClickListener(latLng -> {
            isManualLocation = true;

            // Simular una nueva ubicación
            manualLocation = new Location("");
            manualLocation.setLatitude(latLng.latitude);
            manualLocation.setLongitude(latLng.longitude);

            updateUserLocation(manualLocation);
            updateRouteIfNeeded(manualLocation);

            // ✅ NUEVO: forzar verificación de cercanía
            checkProximityToMonument(manualLocation);

            Toast.makeText(this, "Ubicación manual establecida", Toast.LENGTH_SHORT).show();
        });

    }

    private void updateCurrentMonumentDisplay(MonumentoDTO monument) {
        textViewCurrentName.setText(monument.getNombre());
        textViewCurrentDescription.setText(monument.getDescripcionEs());

        if (monument.getFotoUrl() != null && !monument.getFotoUrl().isEmpty()) {
            String fullUrl = "http://hispalismonuments.duckdns.org:8080" + monument.getFotoUrl();

            GlideUrl glideUrl = new GlideUrl(fullUrl, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + authToken)
                    .build());

            Glide.with(this)
                    .load(glideUrl)
                    .placeholder(R.drawable.monument_icon)
                    .error(R.drawable.monument_icon)
                    .into(imageViewCurrentMonument);
        } else {
            imageViewCurrentMonument.setImageResource(R.drawable.monument_icon);
        }
    }

    private void setupMapFeatures() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (!monumentList.isEmpty()) {
            LatLng firstMonument = new LatLng(
                    monumentList.get(0).getLatitud(),
                    monumentList.get(0).getLongitud()
            );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstMonument, DEFAULT_ZOOM_LEVEL));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setupMapFeatures();
                startLocationUpdates();
            }
        } else {
            Toast.makeText(this, "Permisos de ubicación requeridos para la navegación", Toast.LENGTH_SHORT).show();
        }
    }

    private void addAllMonumentMarkers() {
        mMap.clear();

        for (int i = 0; i < monumentList.size(); i++) {
            MonumentoDTO monument = monumentList.get(i);
            LatLng position = new LatLng(monument.getLatitud(), monument.getLongitud());

            MarkerOptions options = new MarkerOptions()
                    .position(position)
                    .title(monument.getNombre());

            if (i < currentMonumentIndex) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (i == currentMonumentIndex) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }

            mMap.addMarker(options);
        }

        if (userMarker != null && lastLocation != null) {
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                    .title("Tu ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (isManualLocation) return; // Ignorar ubicación real si está en modo manual

            Location location = locationResult.getLastLocation();
            if (location != null) {
                updateUserLocation(location);
                checkProximityToMonument(location);
                updateRouteIfNeeded(location);
            }
        }
    };

    private void resetToAutomaticLocation() {
        isManualLocation = false;
        manualLocation = null;
        Toast.makeText(this, "Seguimiento automático reactivado", Toast.LENGTH_SHORT).show();
    }


    private void updateUserLocation(Location location) {
        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(userPosition)
                    .title("Tu ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        } else {
            userMarker.setPosition(userPosition);
        }

        if (lastLocation == null || location.distanceTo(lastLocation) > 5) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, DEFAULT_ZOOM_LEVEL));
        }
    }

    private void updateRouteIfNeeded(Location location) {
        if (lastLocation == null || location.distanceTo(lastLocation) > MIN_DISTANCE_CHANGE_FOR_ROUTE_UPDATE) {
            lastLocation = location;
            if (currentMonumentIndex < monumentList.size()) {
                MonumentoDTO nextMonument = monumentList.get(currentMonumentIndex);
                drawRoute(new LatLng(location.getLatitude(), location.getLongitude()),
                        new LatLng(nextMonument.getLatitud(), nextMonument.getLongitud()));
            }
        }
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        if (routePolyline != null) {
            routePolyline.remove();
        }

        String originStr = origin.latitude + "," + origin.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;
        String apiKey = getString(R.string.google_maps_api_key);

        GoogleMapApiService mapsService = GoogleMapApiClient.getService();
        mapsService.getDirections(originStr, destinationStr, "walking", apiKey).enqueue(new Callback<DirectionsMapResponse>() {
            @Override
            public void onResponse(Call<DirectionsMapResponse> call, Response<DirectionsMapResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        "OK".equals(response.body().status) && !response.body().routes.isEmpty()) {

                    List<LatLng> allRoutePoints = new ArrayList<>();
                    DirectionsMapResponse.Route route = response.body().routes.get(0);

                    for (DirectionsMapResponse.Leg leg : route.legs) {
                        for (DirectionsMapResponse.Step step : leg.steps) {
                            if (step.polyline != null && step.polyline.points != null) {
                                List<LatLng> stepPoints = decodePolyline(step.polyline.points);
                                allRoutePoints.addAll(stepPoints);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        drawRouteOnMap(allRoutePoints, origin);
                    });
                } else {
                    drawStraightLine(origin, destination);
                }
            }

            @Override
            public void onFailure(Call<DirectionsMapResponse> call, Throwable t) {
                Log.e("API_DEBUG", "Error en la API: " + t.getMessage());
            }
        });
    }


    private void drawRouteOnMap(List<LatLng> routePoints, LatLng origin) {
        try {
            Log.d("MAP_DEBUG", "Dibujando ruta con " + routePoints.size() + " puntos");

            // Asegúrate de que el mapa esté inicializado
            if (mMap == null) {
                Log.e("MAP_DEBUG", "GoogleMap es nulo!");
                return;
            }

            // Crea la polyline con parámetros visibles
            routePolyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(routePoints)
                    .color(Color.BLUE)
                    .width(15f)  // Grosor aumentado para mejor visibilidad
                    .geodesic(true));

            // Zoom a la ruta
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            for (LatLng point : routePoints) {
                builder.include(point);
            }

            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (IllegalStateException e) {
                Log.e("MAP_DEBUG", "Error en animación de cámara: " + e.getMessage());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 17f));
            }

        } catch (Exception e) {
            Log.e("MAP_DEBUG", "Error crítico al dibujar: " + e.getMessage());
        }
    }

    private void zoomToRoute(List<LatLng> routePoints, LatLng origin) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        for (LatLng point : routePoints) {
            builder.include(point);
        }
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        } catch (IllegalStateException e) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, DEFAULT_ZOOM_LEVEL));
        }
    }

    private void drawStraightLine(LatLng origin, LatLng destination) {
        routePolyline = mMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .color(Color.GRAY)
                .width(8f)
                .pattern(Arrays.asList(new Dot(), new Gap(10f))));
    }

    private void checkProximityToMonument(Location userLocation) {
        if (currentMonumentIndex >= monumentList.size()) return;

        MonumentoDTO currentMonument = monumentList.get(currentMonumentIndex);
        Location monumentLocation = new Location("");
        monumentLocation.setLatitude(currentMonument.getLatitud());
        monumentLocation.setLongitude(currentMonument.getLongitud());

        if (userLocation.distanceTo(monumentLocation) <= PROXIMITY_RADIUS_METERS) {

            markMonumentAsVisited(currentMonument);
            currentMonumentIndex++;

            if (currentMonumentIndex < monumentList.size()) {
                // Actualizar la visualización del monumento
                updateCurrentMonumentDisplay(monumentList.get(currentMonumentIndex));
                updateRouteIfNeeded(userLocation);
            } else {
                Toast.makeText(this, "¡Ruta completada!", Toast.LENGTH_LONG).show();
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }
    }

    private void markMonumentAsVisited(MonumentoDTO monument) {
        ApiService apiService = ApiClient.getApiService();
        apiService.marcarVisitado("Bearer " + authToken, monument.getNombre()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                String message = response.isSuccessful() ?
                        "Monumento visitado: " + monument.getNombre() :
                        "Error al marcar como visitado";
                Toast.makeText(NavigationActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NavigationActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return poly;
        }

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        try {
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20 && index < len);

                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20 && index < len);

                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
                poly.add(p);
            }
        } catch (Exception e) {
            Log.e("NavigationActivity", "Error decodificando polilínea: " + e.getMessage());
        }
        return poly;
    }
}