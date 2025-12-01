package com.iset.dsi.localeat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.models.Restaurant;
import com.iset.dsi.localeat.models.RestaurantLocation;
import com.iset.dsi.localeat.ui.FragmentDetails;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private Context context;
    private List<Restaurant> restaurantList;
    private List<RestaurantLocation> locationList;

    private boolean isFavoritesAdapter;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null; // ou "anonymous" si pas de login

    public RestaurantAdapter(Context context, List<Restaurant> restaurantList,
                             List<RestaurantLocation> locationList,
                             boolean isFavoritesAdapter) {
        this.context = context;
        this.restaurantList = restaurantList;
        this.locationList = locationList;
        this.isFavoritesAdapter = isFavoritesAdapter;
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

        // Charger l'état initial du coeur
        loadFavoriteState(restaurant, holder);

        // Clic sur le coeur
        holder.ivFavorite.setOnClickListener(v -> {
            db.collection("users").document(userId).collection("favorites")
                    .document(restaurant.getId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            removeFavorite(restaurant);
                            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
                        } else {
                            addFavorite(restaurant);
                            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                        }
                    });
        });

        // Click sur item pour ouvrir détail
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

                Log.e("ADAPTER_DEBUG", "Click restaurant: " + restaurant.getName());
                Log.e("ADAPTER_DEBUG", "OpeningHour: " + restaurant.getOpeningHour());
                Log.e("ADAPTER_DEBUG", "ClosingHour: " + restaurant.getClosingHour());

                // Récupérer le fragment parent actuel (Home ou Favorites)
                Fragment parentFragment = ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                String parentName = parentFragment != null ? parentFragment.getClass().getSimpleName() : "HomeFragment";

                // Créer FragmentDetails avec le nom du parent
                FragmentDetails fragment = FragmentDetails.newInstance(restaurant, loc, parentName);

                FragmentTransaction transaction = ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction();

                // Cacher le parent fragment si existe
                if (parentFragment != null) {
                    transaction.hide(parentFragment);
                }

                // Ajouter FragmentDetails et l'ajouter au backstack
                transaction.add(R.id.fragment_container, fragment)
                        .addToBackStack(parentFragment.getClass().getSimpleName()) // <- nom du parent
                        .commit();

            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    private void addFavorite(Restaurant restaurant) {
        if (userId == null) return;
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(restaurant.getId())
                .set(restaurant)
                .addOnSuccessListener(unused ->
                        Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void removeFavorite(Restaurant restaurant) {
        if (userId == null) return;
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(restaurant.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();

                    if (isFavoritesAdapter) {
                        restaurantList.remove(restaurant);
                        notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadFavoriteState(Restaurant restaurant, RestaurantViewHolder holder) {
        if (userId == null) {
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(restaurant.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        holder.ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                    } else {
                        holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
                    }
                });
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistance;
        ImageView ivPhoto, ivFavorite;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}