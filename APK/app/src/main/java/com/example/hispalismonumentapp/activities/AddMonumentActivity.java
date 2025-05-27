package com.example.hispalismonumentapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.TypeDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMonumentActivity extends AppCompatActivity {
    // Campos de texto y botones de la interfaz
    private EditText etNombre, etDescripcionEs, etDescripcionEn, etLatitud, etLongitud;
    private Button btnSelectImage, btnUpload, btnGetLocation, btnGenerateDescription;
    private ImageView ivPreview;
    private Uri imageUri;

    // Variables para manejar la ubicación
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean gettingLocation = false;

    // Launchers para resultados de actividades
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<String[]> locationPermissionRequest;

    // Contenedor y listas para tipos de monumentos
    private LinearLayout typesContainer;
    private List<String> allTypes = new ArrayList<>();
    private List<String> selectedTypes = new ArrayList<>();
    private static final int MAX_SELECTED_TYPES = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri photoUri;

    // Configuración para Gemini AI
    private static final String GEMINI_API_KEY = "AIzaSyC3Tv3Lh-q--JgskVW2sEQJ0071q3alz1U";
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_monument);

        // Configuración inicial de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización del launcher para tomar fotos
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        imageUri = photoUri;
                        ivPreview.setImageURI(photoUri);
                    }
                });

        // Inicializar servicios de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        setupLocationCallback();

        // Configuración del modelo generativo de Gemini
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.0-flash",
                GEMINI_API_KEY
        );
        model = GenerativeModelFutures.from(gm);

        /**
         * Vincula los elementos de la interfaz con las variables
         */
        etNombre = findViewById(R.id.etNombre);
        etDescripcionEs = findViewById(R.id.etDescripcionEs);
        etDescripcionEn = findViewById(R.id.etDescripcionEn);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnGenerateDescription = findViewById(R.id.btnGenerateDescription);
        ivPreview = findViewById(R.id.ivPreview);
        typesContainer = findViewById(R.id.typesContainer);
        Button btnTakePicture = findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(v -> dispatchTakePictureIntent());

        // Carga los tipos de monumentos disponibles
        loadMonumentTypes();

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

        // Botón para generar descripciones
        btnGenerateDescription.setOnClickListener(v -> generateDescriptions());

        // Botón para subir monumento
        btnUpload.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String descripcionEs = etDescripcionEs.getText().toString().trim();
            String descripcionEn = etDescripcionEn.getText().toString().trim();
            String latStr = etLatitud.getText().toString().trim();
            String lonStr = etLongitud.getText().toString().trim();

            if (nombre.isEmpty() || descripcionEs.isEmpty() || descripcionEn.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
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
            monumento.setDescripcionEs(descripcionEs);
            monumento.setDescripcionEn(descripcionEn);
            monumento.setLatitud(latitud);
            monumento.setLongitud(longitud);

            List<TypeDTO> typeDTOList = new ArrayList<>();
            for (String typeName : selectedTypes) {
                TypeDTO type = new TypeDTO();
                Log.e("Type", typeName);
                Log.e("TypeName", "Código: " + typeName);
                type.setTypeName(typeName);
                typeDTOList.add(type);
            }
            monumento.setTypes(typeDTOList);

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

    /**
     * Inicia el intent para tomar una foto con la cámara
     */
    private void dispatchTakePictureIntent() {
        // Verifica permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Crea un archivo para guardar la foto
        File photoFile = createImageFile();
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            takePictureLauncher.launch(photoUri);
        }
    }

    /**
     * Crea un archivo temporal para almacenar una imagen
     */
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return image;
        } catch (IOException ex) {
            Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Maneja la respuesta de la solicitud de permisos
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Se necesita permiso de la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Carga los tipos de monumentos desde el servidor
     */
    private void loadMonumentTypes() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando tipos...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiService apiService = ApiClient.getApiService();
        Call<List<TypeDTO>> call = apiService.getAllTypes("Bearer " + token);

        call.enqueue(new Callback<List<TypeDTO>>() {
            @Override
            public void onResponse(Call<List<TypeDTO>> call, Response<List<TypeDTO>> response) {
                progressDialog.dismiss();

                Log.d("API_RESPONSE", "Código: " + response.code());
                Log.d("API_RESPONSE", "Cuerpo: " + new Gson().toJson(response.body()));

                if (response.isSuccessful() && response.body() != null) {
                    allTypes.clear();
                    for (TypeDTO type : response.body()) {
                        allTypes.add(type.getTypeName());
                        Log.d("TYPE_ADDED", "Tipo añadido: " + type.getTypeName());
                    }
                    createTypeCheckboxes();
                } else {
                    Log.e("API_ERROR", "Error en la respuesta");
                }
            }
            @Override
            public void onFailure(Call<List<TypeDTO>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AddMonumentActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Crea checkboxes dinámicos para los tipos de monumentos
     */
    private void createTypeCheckboxes() {
        typesContainer.removeAllViews();

        if (allTypes.isEmpty()) return;

        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout column1 = new LinearLayout(this);
        column1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout column2 = new LinearLayout(this);
        column2.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < allTypes.size(); i++) {
            String type = allTypes.get(i);
            CheckBox checkBox = new CheckBox(this);

            // Aplicar estilo
            checkBox.setText(type);
            checkBox.setButtonTintList(ContextCompat.getColorStateList(this, R.color.hispanis_accent));
            checkBox.setTextColor(ContextCompat.getColor(this, R.color.hispanis_dark));
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 60, 8);
            checkBox.setLayoutParams(params);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Misma lógica de selección
            });

            if (i % 2 == 0) {
                column1.addView(checkBox);
            } else {
                column2.addView(checkBox);
            }
        }

        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        column1.setLayoutParams(columnParams);
        column2.setLayoutParams(columnParams);

        horizontalLayout.addView(column1);
        horizontalLayout.addView(column2);
        typesContainer.addView(horizontalLayout);
    }

    /**
     * Genera descripciones automáticas usando Gemini AI
     */
    private void generateDescriptions() {
        String monumentName = etNombre.getText().toString().trim();

        if (monumentName.isEmpty()) {
            Toast.makeText(this, "Primero escribe el nombre del monumento", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generando descripciones...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Generar descripción en español
        generateGeminiDescription(monumentName, "es", progressDialog, descriptionEs -> {
            runOnUiThread(() -> etDescripcionEs.setText(descriptionEs));

            // Generar descripción en inglés
            generateGeminiDescription(monumentName, "en", progressDialog, descriptionEn -> {
                runOnUiThread(() -> {
                    etDescripcionEn.setText(descriptionEn);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            });
        });
    }


    /**
     * Genera una descripción usando el modelo Gemini
     */
    private void generateGeminiDescription(String monumentName, String language,
                                           ProgressDialog progressDialog,
                                           GeminiCallback callback) {
        String prompt = language.equals("es")
                ? "Genera una descripción (1000-2000 caracteres) sobre " + monumentName +
                " destacando su importancia histórica o cultural. Idioma: español."
                : "Generate a concise description (1000-2000 chars) about " + monumentName +
                " highlighting its historical or cultural significance. Language: English.";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text != null) {
                    // Limpiar la respuesta
                    text = text.replace("\"", "").trim();
                    callback.onResult(text);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AddMonumentActivity.this,
                                "No se pudo generar la descripción", Toast.LENGTH_SHORT).show();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(AddMonumentActivity.this,
                            "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }


    /**
     * Sube el monumento al servidor con su imagen
     */
    private void uploadMonumento(MonumentoDTO monumento, File imageFile) {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Subiendo monumento...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (selectedTypes.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un tipo", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TypeDTO> typeDTOList = new ArrayList<>();
        for (String typeName : selectedTypes) {
            TypeDTO type = new TypeDTO();
            type.setTypeName(typeName); // Usa setTypeName()
            typeDTOList.add(type);
        }
        monumento.setTypes(typeDTOList);


        Gson gson = new Gson();
        String json = gson.toJson(monumento);
        Log.d("MONUMENT_JSON", json);
        ApiService apiService = ApiClient.getApiService();
        RequestBody monumentoBody = RequestBody.create(
                MediaType.parse("application/json"),
                new Gson().toJson(monumento)
        );

        MultipartBody.Part imagePart = null;
        if (imageFile != null) {
            RequestBody imageRequestBody = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
            );
            imagePart = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.getName(),
                    imageRequestBody
            );
        }
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
                    resetForm();

                    // Volver a HomeActivity
                    navigateToHome();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<MonumentoDTO> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AddMonumentActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("UPLOAD_ERROR", "Error en la petición", t);
            }
        });
    }

    /**
     * Reinicia el formulario a su estado inicial
     */
    private void resetForm() {
        etNombre.setText("");
        etDescripcionEs.setText("");
        etDescripcionEn.setText("");
        etLatitud.setText("");
        etLongitud.setText("");
        ivPreview.setImageResource(android.R.color.transparent);
        imageUri = null;
        selectedTypes.clear();
    }

    /**
     * Navega a la actividad principal
     */
    private void navigateToHome() {
        Intent intent = new Intent(AddMonumentActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Maneja errores de la respuesta del servidor
     */
    private void handleErrorResponse(Response<MonumentoDTO> response) {
        try {
            String errorBody = response.errorBody() != null ?
                    response.errorBody().string() : "Error desconocido";
            Log.e("API_ERROR", "Código: " + response.code() + " - Error: " + errorBody);
            Toast.makeText(AddMonumentActivity.this,
                    "Error al subir: " + response.code() + " - " + errorBody,
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(AddMonumentActivity.this,
                    "Error al procesar respuesta",
                    Toast.LENGTH_LONG).show();
            Log.e("API_ERROR", "Error al leer cuerpo de error", e);
        }
    }

    /**
     * Configura la solicitud de ubicación
     */
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Configura el callback para actualizaciones de ubicación
     */
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

    /**
     * Convierte un URI de imagen a un archivo
     */
    private File getFileFromUri(Uri uri) {
        try {
            if (uri.getScheme().equals("content")) {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                File tempFile = createTempImageFile();

                OutputStream outputStream = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();
                return tempFile;
            }
            else if (uri.getScheme().equals("file")) {
                return new File(uri.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Crea un archivo temporal para imágenes
     */
    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String prefix = "IMG_" + timeStamp + "_";
        return File.createTempFile(prefix, ".jpg", getCacheDir());
    }

    /**
     * Solicita permisos de ubicación
     */
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

    /**
     * Obtiene la ubicación actual del dispositivo
     */
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

    /**
     * Inicia las actualizaciones periódicas de ubicación
     */
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

    /**
     * Detiene las actualizaciones de ubicación
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Interfaz para callback de Gemini AI
     */
    interface GeminiCallback {
        void onResult(String description);
    }
}