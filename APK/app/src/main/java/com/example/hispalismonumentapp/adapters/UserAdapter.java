package com.example.hispalismonumentapp.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.hispalismonumentapp.activities.UserActivity;
import com.example.hispalismonumentapp.models.UserDTO;
import com.example.hispalismonumentapp.network.ApiClient;
import com.example.hispalismonumentapp.network.TokenManager;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserDTO> users;
    private TokenManager tokenManager;

    public UserAdapter(Context context, List<UserDTO> users) {
        this.users = users;
        this.tokenManager = new TokenManager(context);
    }

    public void updateUsers(List<UserDTO> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserDTO user = users.get(position);
        holder.userName.setText(user.getUserName());

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

            String token = tokenManager.getToken();

            GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build());

            Glide.with(holder.userImage.getContext())
                    .load(glideUrl)
                    .placeholder(R.drawable.default_user)
                    .error(R.drawable.default_user)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.default_user);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, UserActivity.class);
            intent.putExtra("userName", user.getUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
        }
    }
}
