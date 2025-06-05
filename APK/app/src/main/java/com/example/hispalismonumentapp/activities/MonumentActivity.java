package com.example.hispalismonumentapp.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.hispalismonumentapp.LocaleHelper;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import com.example.hispalismonumentapp.models.TypeDTO;
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;
import com.example.hispalismonumentapp.network.hispalisapi.ApiService;
import com.example.hispalismonumentapp.network.TokenManager;
import android.widget.Button;

import okhttp3.ResponseBody;
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
    private Button btnEliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument);

        // Asignación de vistas
        imageView = findViewById(R.id.imageViewMonumento);
        tvNombre = findViewById(R.id.tvNombreMonumento);
        tvDescripcion = findViewById(R.id.tvDescripcionMonumento);
        linearLayoutTipos = findViewById(R.id.linearLayoutTipos);

        // Obtención del token de autenticación
        tokenManager = new TokenManager(this);
        authToken = tokenManager.getToken();

        btnEliminar = findViewById(R.id.btnEliminarMonumento);
        btnEliminar.setOnClickListener(view -> eliminarMonumento());

        monumentName = getIntent().getStringExtra("monument_name");

        if (monumentName == null || monumentName.isEmpty()) {
            Toast.makeText(this, "Monumento no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkIfUserVisitedMonument(monumentName);
        checkUserRole();
    }

    /**
     * Consulta al backend si el usuario ha visitado el monumento.
     * Si no hay token, se asume que no lo ha visitado.
     */
    private void checkIfUserVisitedMonument(String monumentName) {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            fetchMonument(false);
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<Boolean> call = apiService.hasUserVisitedMonument("Bearer " + token, monumentName);

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean hasVisited = response.body();
                    fetchMonument(hasVisited);
                } else {
                    fetchMonument(false);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                fetchMonument(false);
            }
        });
    }

    /**
     * Solicita los detalles del monumento al servidor y los muestra en la UI.
     * Si el usuario no ha visitado el monumento, se muestra un mensaje informativo.
     */
    private void fetchMonument(boolean hasVisited) {
        ApiService apiService = ApiClient.getApiService();
        Call<MonumentoDTO> call = apiService.buscarMonumento("Bearer " + authToken, monumentName);

        call.enqueue(new Callback<MonumentoDTO>() {
            @Override
            public void onResponse(Call<MonumentoDTO> call, Response<MonumentoDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showMonument(response.body(), hasVisited);
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

    /**
     * Muestra la información del monumento en pantalla.
     * Carga imagen, nombre, descripción (según idioma) y tipos asociados.
     */
    private void showMonument(MonumentoDTO monument, boolean hasVisited) {
        tvNombre.setText(monument.getNombre());

        String language = LocaleHelper.getLanguage(this);
        String descripcion = language.equals("en") ? monument.getDescripcionEn() : monument.getDescripcionEs();

        if (!hasVisited) {
            descripcion = getString(R.string.not_visited);  // Mensaje "no visitado"
        } else if (descripcion == null || descripcion.isEmpty()) {
            descripcion = "Sin descripción";
        }

        tvDescripcion.setText(descripcion);

        // --- Carga segura de la imagen ---
        try {
            String imageUrl = monument.getFotoUrl();

            if (TextUtils.isEmpty(imageUrl) || authToken == null) {
                imageView.setImageResource(R.drawable.monument_icon);
            } else {
                // Asegurar URL completa
                if (!imageUrl.startsWith("http")) {
                    imageUrl = ApiClient.getBaseUrl() + (imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl);
                }

                Log.d("ImageLoad", "URL: " + imageUrl);
                Log.d("ImageLoad", "Token length: " + authToken.length());

                GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                        .addHeader("Authorization", "Bearer " + authToken.trim())
                        .addHeader("Accept", "image/*")
                        .build());

                Glide.with(this)
                        .load(glideUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.monument_icon)
                                .error(R.drawable.monument_icon)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                if (e != null) {
                                    Log.e("GlideError", "Load failed: " + e.getMessage());
                                    for (Throwable t : e.getRootCauses()) {
                                        Log.e("GlideRootCause", t.getMessage());
                                    }
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(imageView);
            }

        } catch (Exception e) {
            Log.e("ImageError", "Exception in image loading", e);
            imageView.setImageResource(R.drawable.monument_icon);
        }

        // --- Cargar tipos ---
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


    /**
     * Verifica si el usuario tiene el rol ADMIN y, en ese caso, muestra el botón de eliminar.
     */
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
                    if ("ADMIN".equalsIgnoreCase(user.getUserRol())) {
                        runOnUiThread(() -> btnEliminar.setVisibility(View.VISIBLE));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDTO> call, Throwable t) {
                Toast.makeText(MonumentActivity.this, "Error al verificar rol de usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Envía una solicitud para eliminar el monumento actual.
     * Solo está disponible para usuarios con rol ADMIN.
     */
    private void eliminarMonumento() {
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody> call = apiService.deleteMonumentoByNombre("Bearer " + authToken, monumentName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MonumentActivity.this, "Monumento eliminado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MonumentActivity.this, "Error al eliminar el monumento", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MonumentActivity.this, "Error de conexión al eliminar", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
