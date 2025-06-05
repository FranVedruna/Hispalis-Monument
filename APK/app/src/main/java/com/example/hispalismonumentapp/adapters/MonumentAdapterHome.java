package com.example.hispalismonumentapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.MonumentActivity;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.hispalismonumentapp.network.hispalisapi.ApiClient;

public class MonumentAdapterHome extends RecyclerView.Adapter<MonumentAdapterHome.MonumentViewHolder> {
    private Context context;
    private List<MonumentoDTO> monuments;
    private String authToken;
    private List<MonumentoDTO> selectedMonuments = new ArrayList<>();
    private OnLongItemClickListener onLongItemClickListener;


    public interface OnLongItemClickListener {
        void onLongItemClick(View view, int position);
    }

    public MonumentAdapterHome(Context context, List<MonumentoDTO> monuments, String authToken) {
        this.context = context;
        this.monuments = monuments != null ? monuments : new ArrayList<>();
        this.authToken = authToken;


    }

    public void setOnLongItemClickListener(OnLongItemClickListener listener) {
        this.onLongItemClickListener = listener;
    }

    public List<MonumentoDTO> getSelectedMonuments() {
        return selectedMonuments;
    }

    public void clearSelection() {
        selectedMonuments.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MonumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_monument, parent, false);
        return new MonumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonumentViewHolder holder, int position) {
        MonumentoDTO monument = monuments.get(position);

        // 1. Configuración visual básica
        holder.container.setBackgroundResource(
                selectedMonuments.contains(monument)
                        ? R.drawable.item_background_selected
                        : R.drawable.item_background_default
        );

        holder.textViewName.setText(monument.getNombre());
        holder.textViewDescription.setText(monument.getDescripcionEs());

        // 2. Carga de imagen con verificación reforzada
        try {
            String imageUrl = monument.getFotoUrl();
            Context context = holder.itemView.getContext();

            // Verificar condiciones mínimas
            if (TextUtils.isEmpty(imageUrl) || authToken == null) {
                holder.imageView.setImageResource(R.drawable.monument_icon);
                return;
            }

            // Construir URL completa
            if (!imageUrl.startsWith("http")) {
                imageUrl = ApiClient.getBaseUrl() + (imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl);
            }

            // Debug: Verificar URL y token
            Log.d("ImageLoad", "URL: " + imageUrl);
            Log.d("ImageLoad", "Token length: " + authToken.length());

            // Configurar headers
            LazyHeaders headers = new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + authToken.trim())
                    .addHeader("Accept", "image/*")
                    .build();

            Glide.with(context)
                    .load(new GlideUrl(imageUrl, headers))
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
                    .into(holder.imageView);

        } catch (Exception e) {
            Log.e("ImageError", "Exception in image loading", e);
            holder.imageView.setImageResource(R.drawable.monument_icon);
        }

        // 3. Click listeners (mantener igual)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MonumentActivity.class);
            intent.putExtra("monument_name", monument.getNombre());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onLongItemClickListener != null) {
                onLongItemClickListener.onLongItemClick(holder.itemView, position);
                if (selectedMonuments.contains(monument)) {
                    selectedMonuments.remove(monument);
                } else {
                    selectedMonuments.add(monument);
                }
                notifyItemChanged(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return monuments.size();
    }

    public void updateData(List<MonumentoDTO> newMonuments) {
        this.monuments = newMonuments != null ? newMonuments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class MonumentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        TextView textViewDescription;
        LinearLayout container;

        public MonumentViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.imageViewMonument);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);

        }
    }
    public interface OnSelectionChangedListener {
        void onSelectionChanged(MonumentoDTO monument, boolean isSelected);
    }
}
