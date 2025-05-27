package com.example.hispalismonumentapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.MonumentActivity;
import com.example.hispalismonumentapp.models.MonumentoDTO;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

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

        if (selectedMonuments.contains(monument)) {
            holder.container.setBackgroundResource(R.drawable.item_background_selected);
        } else {
            holder.container.setBackgroundResource(R.drawable.item_background_default);
        }


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
            holder.imageView.setImageResource(R.drawable.monument_icon);
        }

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

                // Esto es suficiente: se volverá a pintar con el fondo correcto en onBindViewHolder
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
