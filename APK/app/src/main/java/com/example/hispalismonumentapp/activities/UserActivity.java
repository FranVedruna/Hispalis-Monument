package com.example.hispalismonumentapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private TextView userNameText;
    private ImageView userImage;
    private ImageView roleArrow;
    private ImageView medalImage;
    private ImageView activeBadgeImage;
    private LinearLayout medalContainer;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userNameText = findViewById(R.id.userNameText);
        userImage = findViewById(R.id.userImage);
        roleArrow = findViewById(R.id.roleArrow);
        medalImage = findViewById(R.id.medalImage);
        activeBadgeImage = findViewById(R.id.activeBadgeImage);
        medalContainer = findViewById(R.id.medalContainer);
        tokenManager = new TokenManager(this);

        String userName = getIntent().getStringExtra("userName");
        String token = tokenManager.getToken();
        ApiService apiService = ApiClient.getApiService();

        apiService.getCurrentUser("Bearer " + token).enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> currentUserResponse) {
                if (currentUserResponse.isSuccessful() && currentUserResponse.body() != null) {
                    UserDTO currentUser = currentUserResponse.body();

                    apiService.findUserByName("Bearer " + token, userName).enqueue(new Callback<UserDTO>() {
                        @Override
                        public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                UserDTO user = response.body();
                                userNameText.setText(user.getUserName());

                                if ("ADMIN".equals(currentUser.getUserRol()) && "USER".equals(user.getUserRol())) {
                                    roleArrow.setVisibility(View.VISIBLE);

                                    roleArrow.setOnClickListener(v -> {
                                        apiService.upgradeUser("Bearer " + token, user.getUserName())
                                                .enqueue(new Callback<ResponseBody>() {
                                                    @Override
                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                        if (response.isSuccessful()) {
                                                            roleArrow.setVisibility(View.GONE);
                                                            Toast.makeText(UserActivity.this, "Usuario actualizado a ADMIN", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(UserActivity.this, "No se pudo actualizar el usuario", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                        Toast.makeText(UserActivity.this, "Error de red al actualizar usuario", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                                } else {
                                    roleArrow.setVisibility(View.GONE);
                                }

                                // Mostrar imagen de perfil
                                if (user.getUserPhotoURL() != null) {
                                    String baseUrl = ApiClient.getBaseUrl();
                                    String photoUrl = user.getUserPhotoURL();
                                    String imageUrl;

                                    if (baseUrl.endsWith("/") && photoUrl.startsWith("/")) {
                                        imageUrl = baseUrl + photoUrl.substring(1);
                                    } else if (!baseUrl.endsWith("/") && !photoUrl.startsWith("/")) {
                                        imageUrl = baseUrl + "/" + photoUrl;
                                    } else {
                                        imageUrl = baseUrl + photoUrl;
                                    }

                                    GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                                            .addHeader("Authorization", "Bearer " + token)
                                            .build());

                                    Glide.with(UserActivity.this)
                                            .load(glideUrl)
                                            .placeholder(R.drawable.default_user)
                                            .error(R.drawable.default_user)
                                            .into(userImage);
                                }

                                // Mostrar medalla según número de monumentos (usando datos del DTO)
                                List<String> monuments = user.getMonuments();
                                if (monuments != null) {
                                    int count = monuments.size();

                                    if (count >= 5) {
                                        if (count < 10) {
                                            medalImage.setImageResource(R.drawable.medalla_bronce);
                                        } else if (count < 15) {
                                            medalImage.setImageResource(R.drawable.medalla_plata);
                                        } else {
                                            medalImage.setImageResource(R.drawable.medalla_oro);
                                        }

                                        medalImage.setVisibility(View.VISIBLE);
                                        medalContainer.setVisibility(View.VISIBLE);
                                    }
                                }

                                // Mostrar insignia si es usuario activo
                                apiService.isUserActive("Bearer " + token, user.getUserName())
                                        .enqueue(new Callback<Boolean>() {
                                            @Override
                                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                if (response.isSuccessful()) {
                                                    if (Boolean.TRUE.equals(response.body())) {
                                                        activeBadgeImage.setVisibility(View.VISIBLE);
                                                        medalContainer.setVisibility(View.VISIBLE);
                                                    } else {
                                                        Toast.makeText(UserActivity.this, "Usuario no activo", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(UserActivity.this, "Respuesta no exitosa", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Boolean> call, Throwable t) {
                                                Toast.makeText(UserActivity.this, "Fallo en la red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onFailure(Call<UserDTO> call, Throwable t) {
                            // Error obteniendo usuario
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                // Error al obtener usuario actual
            }
        });
    }
}
