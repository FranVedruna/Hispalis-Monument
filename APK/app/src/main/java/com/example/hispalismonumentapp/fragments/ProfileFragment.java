package com.example.hispalismonumentapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


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
    private static final int REQUEST_GALLERY = 1001;
    private static final int REQUEST_CAMERA = 1002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(requireContext());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://hispalismonuments.duckdns.org:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
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
        layoutImageButtons.setVisibility(View.GONE);

        btnSelectFromGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GALLERY);
        });

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CAMERA);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserData();
    }

    private void loadUserData() {
        String token = tokenManager.getToken();
        if (token == null) {
            showError("No autenticado");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String authHeader = "Bearer " + token;

        apiService.getCurrentUser(authHeader).enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayUserData(response.body());
                } else {
                    showError("Error al cargar datos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void displayUserData(UserDTO user) {
        tvUserName.setText(user.getUserName());
        tvUserRole.setText(user.getUserRol());

        String token = tokenManager.getToken();

        if (user.getUserPhotoURL() != null && !user.getUserPhotoURL().isEmpty() && token != null) {
            // Asegurarse de que la URL es absoluta
            String fullPhotoUrl = user.getUserPhotoURL();
            if (!fullPhotoUrl.startsWith("http")) {
                if (fullPhotoUrl.startsWith("/")) {
                    fullPhotoUrl = fullPhotoUrl.substring(1); // quitar la barra inicial
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

        if (user.getMonuments() != null && !user.getMonuments().isEmpty()) {
            String monumentsText = "Monumentos visitados:\n" +
                    TextUtils.join("\n", user.getMonuments());
            tvVisitedMonuments.setText(monumentsText);
        } else {
            tvVisitedMonuments.setText("No has visitado monumentos aún");
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
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
