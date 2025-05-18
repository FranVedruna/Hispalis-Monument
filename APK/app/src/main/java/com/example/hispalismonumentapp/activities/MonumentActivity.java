package com.example.hispalismonumentapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hispalismonumentapp.LocaleHelper;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.TypeDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonumentActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvNombre, tvDescripcion;
    private String monumentName;
    private String authToken;
    private LinearLayout linearLayoutTipos;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument);

        imageView = findViewById(R.id.imageViewMonumento);
        tvNombre = findViewById(R.id.tvNombreMonumento);
        tvDescripcion = findViewById(R.id.tvDescripcionMonumento);
        linearLayoutTipos = findViewById(R.id.linearLayoutTipos);
        tokenManager = new TokenManager(this);
        authToken = tokenManager.getToken();

        monumentName = getIntent().getStringExtra("monument_name");
        if (monumentName != null && !monumentName.isEmpty()) {
            fetchMonument();
        } else {
            Toast.makeText(this, "Monumento no válido", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchMonument() {
        ApiService apiService = ApiClient.getApiService();
        Call<MonumentoDTO> call = apiService.buscarMonumento("Bearer " + authToken, monumentName);

        call.enqueue(new Callback<MonumentoDTO>() {
            @Override
            public void onResponse(Call<MonumentoDTO> call, Response<MonumentoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showMonument(response.body());
                } else {
                    Toast.makeText(MonumentActivity.this, "No se encontró el monumento", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<MonumentoDTO> call, Throwable t) {
                Toast.makeText(MonumentActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showMonument(MonumentoDTO monument) {
        tvNombre.setText(monument.getNombre());

        String language = LocaleHelper.getLanguage(this);
        String descripcion = language.equals("en") ? monument.getDescripcionEn() : monument.getDescripcionEs();
        tvDescripcion.setText(descripcion != null ? descripcion : "Sin descripción");

        if (monument.getFotoUrl() != null && !monument.getFotoUrl().isEmpty()) {
            String fullUrl = "http://hispalismonuments.duckdns.org:8080" + monument.getFotoUrl();

            // Create GlideUrl with authorization header
            GlideUrl glideUrl = new GlideUrl(fullUrl, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + authToken)
                    .build());

            // Load image with Glide
            Glide.with(this)
                    .load(glideUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }

        linearLayoutTipos.removeAllViews();

        if (monument.getTypes() != null) {
            for (TypeDTO type : monument.getTypes()) {
                TextView tag = new TextView(this);
                tag.setText(type.getTypeName());
                tag.setTextSize(12);
                tag.setTextColor(Color.WHITE);
                tag.setBackgroundColor(Color.DKGRAY);
                tag.setPadding(20, 10, 20, 10);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(8, 0, 8, 0);
                tag.setLayoutParams(params);

                linearLayoutTipos.addView(tag);
            }
        }
    }
}