package com.example.hispalismonumentapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.MonumentActivity;
import com.example.hispalismonumentapp.models.MonumentoDTO;

import java.util.ArrayList;
import java.util.List;

public class MonumentAdapterMonuments extends RecyclerView.Adapter<MonumentAdapterMonuments.MonumentViewHolder> {
    private Context context;
    private List<MonumentoDTO> monuments;
    private String authToken;

    public MonumentAdapterMonuments(Context context, List<MonumentoDTO> monuments, String authToken) {
        this.context = context;
        this.monuments = monuments != null ? monuments : new ArrayList<>();
        this.authToken = authToken;
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

        holder.textViewName.setText(monument.getNombre());
        holder.textViewDescription.setText(monument.getDescripcionEs());

        if (monument.getFotoUrl() != null && !monument.getFotoUrl().isEmpty()) {
            String fullUrl = "http://hispalismonuments.duckdns.org:8080" + monument.getFotoUrl();
            Log.d("Foto", fullUrl);

            GlideUrl glideUrl = new GlideUrl(fullUrl, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + authToken)
                    .build());

            Glide.with(context)
                    .load(glideUrl)
                    .placeholder(R.drawable.monument_icon)
                    .error(R.drawable.monument_icon)
                    .into(holder.imageView);
        } else {
            // Imagen por defecto si no hay URL
            holder.imageView.setImageResource(R.drawable.monument_icon);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MonumentActivity.class);
            intent.putExtra("monument_name", monument.getNombre());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return monuments.size();
    }

    public static class MonumentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        TextView textViewDescription;

        public MonumentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewMonument);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
    }
}
