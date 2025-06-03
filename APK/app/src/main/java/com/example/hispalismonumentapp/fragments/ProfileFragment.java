package com.example.hispalismonumentapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.LoginActivity;
import com.example.hispalismonumentapp.adapters.MonumentAdapterHome;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ProfileFragment extends Fragment {
    private Uri selectedImageUri;
    private TextView tvUserName, tvUserRole, tvVisitedMonuments;
    private ProgressBar progressBar;
    private TokenManager tokenManager;
    private ApiService apiService;
    private ImageView ivProfilePicture;
    private LinearLayout layoutImageButtons;
    private Button btnSelectFromGallery, btnTakePhoto;
    private Button btnSaveProfile;
    private ImageButton btnEditProfile;
    private ImageView ivMedal;
    private TextView tvDeleteAccount;
    private static final int REQUEST_GALLERY = 1001;
    private static final int REQUEST_CAMERA = 1002;
    private RecyclerView rvVisitedMonuments;
    private MonumentAdapterHome monumentAdapter;
    private List<MonumentoDTO> visitedMonuments = new ArrayList<>();
    private Call<UserDTO> currentUserCall;
    private Call<Integer> visitedCountCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(requireContext());
        apiService = ApiClient.getApiService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        tvVisitedMonuments = view.findViewById(R.id.tvVisitedMonuments);
        progressBar = view.findViewById(R.id.progressBar);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        layoutImageButtons = view.findViewById(R.id.layoutImageButtons);
        btnSelectFromGallery = view.findViewById(R.id.btnSelectFromGallery);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        ivMedal = view.findViewById(R.id.ivMedal);
        layoutImageButtons.setVisibility(View.GONE);
        tvDeleteAccount = view.findViewById(R.id.tvDeleteAccount);
        rvVisitedMonuments = view.findViewById(R.id.rvVisitedMonuments);
        rvVisitedMonuments.setLayoutManager(new LinearLayoutManager(getContext()));
        monumentAdapter = new MonumentAdapterHome(getContext(), visitedMonuments, tokenManager.getToken());
        rvVisitedMonuments.setAdapter(monumentAdapter);

        tvDeleteAccount.setOnClickListener(v -> {
            deleteUserAccount();
        });

        btnSelectFromGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GALLERY);
        });

        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            } else {
                openCamera();
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            if (layoutImageButtons.getVisibility() == View.GONE) {
                layoutImageButtons.setVisibility(View.VISIBLE);
                btnSaveProfile.setVisibility(View.VISIBLE);
            } else {
                layoutImageButtons.setVisibility(View.GONE);
                btnSaveProfile.setVisibility(View.GONE);
            }
        });

        btnSaveProfile.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                showError("Selecciona una imagen primero");
                return;
            }
            uploadProfilePhoto(selectedImageUri);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancelar todas las peticiones en curso
        if (currentUserCall != null) {
            currentUserCall.cancel();
        }
        if (visitedCountCall != null) {
            visitedCountCall.cancel();
        }
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }
    private void deleteUserAccount() {
        String token = tokenManager.getToken();
        if (token == null) {
            showError("No autenticado");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.deleteUser("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_LONG).show();
                    // Redirigir a LoginActivity
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia el backstack
                    startActivity(intent);
                } else {
                    showError("Error al eliminar cuenta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void getVisitedCountAndHandle(String username) {
        // Verificar que el fragment esté activo
        if (!isAdded() || getContext() == null) return;

        String token = tokenManager.getToken();
        if (token == null) {
            showError("No autenticado");
            return;
        }
        String authHeader = "Bearer " + token;

        // Cancelar petición anterior si existe
        if (visitedCountCall != null) {
            visitedCountCall.cancel();
        }

        visitedCountCall = apiService.getVisitedMonumentCount(authHeader, username);
        visitedCountCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                // Verificar que el fragment esté activo
                if (!isAdded() || getContext() == null) return;

                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    int count = response.body();
                    Log.d("VisitedCount", "Monumentos visitados: " + count);

                    ivMedal.setVisibility(View.GONE);

                    if (count >= 5 && count < 10) {
                        ivMedal.setVisibility(View.VISIBLE);
                        ivMedal.setImageResource(R.drawable.medalla_bronce);
                    } else if (count >= 10 && count < 15) {
                        ivMedal.setVisibility(View.VISIBLE);
                        ivMedal.setImageResource(R.drawable.medalla_plata);
                    } else if (count >= 15) {
                        ivMedal.setVisibility(View.VISIBLE);
                        ivMedal.setImageResource(R.drawable.medalla_oro);
                    }
                } else {
                    showError("Error al obtener el recuento: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                if (!call.isCanceled() && isAdded() && getContext() != null) {
                    progressBar.setVisibility(View.GONE);
                    showError("Fallo en recuento: " + t.getMessage());
                }
            }
        });
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserData();
    }

    private void loadUserData() {
        // Verificar que el fragment esté activo
        if (!isAdded() || getContext() == null) return;

        String token = tokenManager.getToken();
        if (token == null) {
            showError("No autenticado");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + token;

        // Cancelar petición anterior si existe
        if (currentUserCall != null) {
            currentUserCall.cancel();
        }

        currentUserCall = apiService.getCurrentUser(authHeader);
        currentUserCall.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                // Verificar que el fragment esté activo
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    UserDTO user = response.body();
                    displayUserData(user);
                    getVisitedCountAndHandle(user.getUserName());
                } else {
                    progressBar.setVisibility(View.GONE);
                    showError("Error al cargar datos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                if (!call.isCanceled() && isAdded() && getContext() != null) {
                    progressBar.setVisibility(View.GONE);
                    showError("Error de conexión: " + t.getMessage());
                }
            }
        });
    }


    private void displayUserData(UserDTO user) {
        tvUserName.setText(user.getUserName());
        tvUserRole.setText(user.getUserRol());
        if (!isAdded() || getContext() == null) return;
        String token = tokenManager.getToken();

        // Cargar foto de perfil (código existente)
        if (user.getUserPhotoURL() != null && !user.getUserPhotoURL().isEmpty() && token != null) {
            String fullPhotoUrl = user.getUserPhotoURL();
            if (!fullPhotoUrl.startsWith("http")) {
                if (fullPhotoUrl.startsWith("/")) {
                    fullPhotoUrl = fullPhotoUrl.substring(1);
                }
                fullPhotoUrl = ApiClient.getBaseUrl() + fullPhotoUrl;
            }

            GlideUrl glideUrl = new GlideUrl(
                    fullPhotoUrl,
                    new LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build()
            );

            Glide.with(requireContext())
                    .load(glideUrl)
                    .placeholder(R.drawable.default_user)
                    .error(R.drawable.default_user)
                    .circleCrop()
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.default_user);
        }

        // Obtener detalles completos de monumentos visitados
        if (user.getMonuments() != null && !user.getMonuments().isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            List<MonumentoDTO> monumentosVisitados = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(0);

            for (String monumentName : user.getMonuments()) {
                apiService.buscarMonumento("Bearer " + token, monumentName).enqueue(new Callback<MonumentoDTO>() {
                    @Override
                    public void onResponse(Call<MonumentoDTO> call, Response<MonumentoDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            monumentosVisitados.add(response.body());
                        }

                        // Verificar si hemos terminado todas las solicitudes
                        if (counter.incrementAndGet() == user.getMonuments().size()) {
                            progressBar.setVisibility(View.GONE);
                            updateMonumentsList(monumentosVisitados);
                        }
                    }

                    @Override
                    public void onFailure(Call<MonumentoDTO> call, Throwable t) {
                        if (counter.incrementAndGet() == user.getMonuments().size()) {
                            progressBar.setVisibility(View.GONE);
                            updateMonumentsList(monumentosVisitados);
                        }
                        Log.e("ProfileFragment", "Error al buscar monumento: " + monumentName, t);
                    }
                });
            }
        } else {
            tvVisitedMonuments.setText("No has visitado monumentos aún");
            updateMedal(0);
        }
    }

    private void updateMonumentsList(List<MonumentoDTO> monuments) {
        if (monuments.isEmpty()) {
            tvVisitedMonuments.setText("No se pudieron cargar los monumentos visitados");
            return;
        }
        if (!isAdded() || getContext() == null) return;
        // Actualizar RecyclerView
        monumentAdapter.updateData(monuments);

        // Actualizar medalla según el conteo
        updateMedal(monuments.size());
    }

    private void updateMedal(int count) {
        ivMedal.setVisibility(View.GONE);

        if (count >= 5 && count < 10) {
            ivMedal.setVisibility(View.VISIBLE);
            ivMedal.setImageResource(R.drawable.medalla_bronce);
        } else if (count >= 10 && count < 15) {
            ivMedal.setVisibility(View.VISIBLE);
            ivMedal.setImageResource(R.drawable.medalla_plata);
        } else if (count >= 15) {
            ivMedal.setVisibility(View.VISIBLE);
            ivMedal.setImageResource(R.drawable.medalla_oro);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_GALLERY && data.getData() != null) {
                selectedImageUri = data.getData();  // <--- GUARDAR URI
                Glide.with(this).load(selectedImageUri).circleCrop().into(ivProfilePicture);
            } else if (requestCode == REQUEST_CAMERA && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ivProfilePicture.setImageBitmap(bitmap);
                selectedImageUri = getImageUriFromBitmap(bitmap);
            }
        }
    }


    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(), bitmap, "ProfilePic", null);
        return Uri.parse(path);
    }

    private void uploadProfilePhoto(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        if (!isAdded() || getContext() == null) return;
        try {
            // Usa el mismo método que ya tienes para crear el archivo temporal
            File file = getFileFromUri(imageUri);

            // Prepara el archivo igual que en monumentos
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            String token = tokenManager.getToken();
            if (token == null) {
                showError("No autenticado");
                return;
            }

            // En tu ProfileFragment.java
            apiService.uploadUserPhoto("Bearer " + token, filePart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        try {
                            String photoUrl = response.body().string();
                            // Actualizar UI
                            Glide.with(requireContext())
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(ivProfilePicture);
                            showError("Foto actualizada");
                        } catch (IOException e) {
                            showError("Error procesando respuesta");
                        }
                    } else {
                        try {
                            showError("Error del servidor: " + response.errorBody().string());
                        } catch (IOException e) {
                            showError("Error en respuesta");
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    showError("Error de conexión: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            showError("Error al procesar imagen");
            Log.e("PhotoUpload", "Error: ", e);
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File file = new File(requireContext().getCacheDir(), "temp_profile_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[4 * 1024]; // 4KB buffer
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return file;
    }

    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
