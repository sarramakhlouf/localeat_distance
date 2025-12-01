package com.iset.dsi.localeat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.models.Restaurant;
import com.iset.dsi.localeat.models.RestaurantLocation;
import com.iset.dsi.localeat.ui.FragmentDetails;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private Context context;
    private List<Restaurant> restaurantList;
    private List<RestaurantLocation> locationList;

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList, List<RestaurantLocation> locationList) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        Log.e("ADAPTER_DEBUG", "Restaurant=" + restaurant.getName() +
                " | Distance=" + restaurant.getDistance());

        // Nom
        holder.tvName.setText(restaurant.getName());

        // Adresse
        holder.tvAddress.setText(restaurant.getAddress());

        // Distance
        holder.tvDistance.setText(restaurant.getDistance() >= 0
                ? String.format("%.2f km away", restaurant.getDistance())
                : "Distance inconnue");

        // Photo
        if (restaurant.getPhotoUrl() != null && !restaurant.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(restaurant.getPhotoUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Click sur item
        holder.itemView.setOnClickListener(v -> {
            // Chercher location correspondante
            RestaurantLocation loc = null;
            for (RestaurantLocation rLoc : locationList) {
                if (rLoc.getRestaurantId().equals(restaurant.getId())) {
                    loc = rLoc;
                    break;
                }
            }

            if (loc != null) {
                FragmentDetails fragment = FragmentDetails.newInstance(restaurant, loc);
                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistance;
        ImageView ivPhoto;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }
}
