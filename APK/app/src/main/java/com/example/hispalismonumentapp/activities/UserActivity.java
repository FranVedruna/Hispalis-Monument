// UserActivity.java
package com.example.hispalismonumentapp.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private TextView userNameText;
    private ImageView userImage;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userNameText = findViewById(R.id.userNameText);
        userImage = findViewById(R.id.userImage);
        tokenManager = new TokenManager(this);

        String userName = getIntent().getStringExtra("userName");

        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        apiService.findUserByName("Bearer " + token, userName).enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDTO user = response.body();
                    userNameText.setText(user.getUserName());

                    if (user.getUserPhotoURL() != null) {
                        String baseUrl = ApiClient.getBaseUrl();
                        String photoUrl = user.getUserPhotoURL();

// Evitar doble slash al concatenar baseUrl y photoUrl
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
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                // Manejo de errores
            }
        });
    }
}
