package com.example.hispalismonumentapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMonumentActivity extends AppCompatActivity {
    private EditText etNombre, etDescripcion, etLatitud, etLongitud;
    private Button btnSelectImage, btnUpload, btnGetLocation;
    private ImageView ivPreview;
    private Uri imageUri;

    // Variables para ubicación
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean gettingLocation = false;

    // Launcher para seleccionar imagen
    private ActivityResultLauncher<String> mGetContent;

    // Launcher para permisos de ubicación
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_monument);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar servicios de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        setupLocationCallback();

        // Referencias de los elementos de la interfaz
        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        ivPreview = findViewById(R.id.ivPreview);

        // Inicializar el launcher para permisos de ubicación
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        getCurrentLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        getCurrentLocation();
                    } else {
                        Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher para seleccionar imagen
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        ivPreview.setImageURI(uri);
                    }
                });

        // Botón para seleccionar imagen
        btnSelectImage.setOnClickListener(v -> mGetContent.launch("image/*"));

        // Botón para obtener ubicación
        btnGetLocation.setOnClickListener(v -> requestLocationPermissions());

        // Botón para subir monumento
        btnUpload.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String latStr = etLatitud.getText().toString().trim();
            String lonStr = etLongitud.getText().toString().trim();

            if (nombre.isEmpty() || descripcion.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                Toast.makeText(AddMonumentActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double latitud, longitud;
            try {
                latitud = Double.parseDouble(latStr);
                longitud = Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AddMonumentActivity.this, "Latitud y longitud deben ser numéricos", Toast.LENGTH_SHORT).show();
                return;
            }

            MonumentoDTO monumento = new MonumentoDTO();
            monumento.setNombre(nombre);
            monumento.setDescripcion(descripcion);
            monumento.setLatitud(latitud);
            monumento.setLongitud(longitud);

            if (imageUri != null) {
                File imageFile = getFileFromUri(imageUri);
                if (imageFile != null) {
                    uploadMonumento(monumento, imageFile);
                } else {
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadMonumento(MonumentoDTO monumento, File imageFile) {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo monumento...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = ApiClient.getApiService();

        Gson gson = new Gson();
        String monumentoJson = gson.toJson(monumento);
        RequestBody monumentoBody = RequestBody.create(
                MediaType.parse("application/json"),
                monumentoJson
        );

        RequestBody imageRequestBody = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
        );
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                imageRequestBody
        );

        Call<MonumentoDTO> call = apiService.createMonumento(
                "Bearer " + token,
                monumentoBody,
                imagePart
        );

        call.enqueue(new Callback<MonumentoDTO>() {
            @Override
            public void onResponse(Call<MonumentoDTO> call, Response<MonumentoDTO> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    MonumentoDTO createdMonument = response.body();
                    Toast.makeText(AddMonumentActivity.this,
                            "Monumento subido: " + (createdMonument != null ? createdMonument.getNombre() : ""),
                            Toast.LENGTH_LONG).show();

                    // Limpiar formulario
                    etNombre.setText("");
                    etDescripcion.setText("");
                    etLatitud.setText("");
                    etLongitud.setText("");
                    ivPreview.setImageResource(android.R.color.transparent);
                    imageUri = null;
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Toast.makeText(AddMonumentActivity.this,
                                "Error al subir: " + response.code() + " - " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(AddMonumentActivity.this,
                                "Error al procesar respuesta",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MonumentoDTO> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AddMonumentActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || gettingLocation) {
                    return;
                }

                gettingLocation = true;
                stopLocationUpdates();

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    runOnUiThread(() -> {
                        etLatitud.setText(String.valueOf(location.getLatitude()));
                        etLongitud.setText(String.valueOf(location.getLongitude()));
                        Toast.makeText(AddMonumentActivity.this,
                                "Ubicación obtenida", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        };
    }

    private File getFileFromUri(Uri uri) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String prefix = "IMG_" + timeStamp + "_";
            File tempFile = File.createTempFile(prefix, ".jpg", getCacheDir());

            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location location = task.getResult();
                        etLatitud.setText(String.valueOf(location.getLatitude()));
                        etLongitud.setText(String.valueOf(location.getLongitude()));
                        Toast.makeText(AddMonumentActivity.this,
                                "Ubicación obtenida", Toast.LENGTH_SHORT).show();
                    } else {
                        startLocationUpdates();
                    }
                });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
}