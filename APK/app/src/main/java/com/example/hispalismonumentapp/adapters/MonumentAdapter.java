package com.example.hispalismonumentapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hispalismonumentapp.R;
import com.example.hispalismonumentapp.activities.MonumentActivity;
import com.example.hispalismonumentapp.models.Monument;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MonumentAdapter extends RecyclerView.Adapter<MonumentAdapter.MonumentViewHolder> {
    private Context context;
    private List<Monument> monuments;
    private String authToken; // Añade este campo

    public MonumentAdapter(Context context, List<Monument> monuments, String authToken) {
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
        Monument monument = monuments.get(position);

        holder.textViewName.setText(monument.getNombre());
        holder.textViewDescription.setText(monument.getDescripcion());

        if (monument.getFotoUrl() != null && !monument.getFotoUrl().isEmpty()) {
            String fullUrl = "http://hispalismonuments.duckdns.org:8080" + monument.getFotoUrl();
            Log.d("Foto", fullUrl);

            // Crea un cliente OkHttp personalizado con el token
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + authToken)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(client))
                    .build();

            picasso.load(fullUrl)
                    .into(holder.imageView);
        }
    }


    @Override
    public int getItemCount() {
        return monuments.size();
    }

    public void updateData(List<Monument> newMonuments) {
        this.monuments = newMonuments != null ? newMonuments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class MonumentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName;
        TextView textViewDescription;

        public MonumentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de que estos IDs coincidan con tu layout
            imageView = itemView.findViewById(R.id.imageViewMonument);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
    }
}