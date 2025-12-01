package com.iset.dsi.localeat.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.adapters.RestaurantAdapter;
import com.iset.dsi.localeat.models.Restaurant;
import com.iset.dsi.localeat.models.RestaurantLocation;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorites extends Fragment {

    private RecyclerView rvFavorites;
    private RestaurantAdapter adapter;
    private List<Restaurant> favoriteList = new ArrayList<>();
    private List<RestaurantLocation> locationList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getCurrentUser() != null
            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
            : null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RestaurantAdapter(getContext(), favoriteList, locationList);
        rvFavorites.setAdapter(adapter);

        loadFavorites(); // Charger les favoris

        return view;
    }

    private void loadFavorites() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    favoriteList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Restaurant restaurant = doc.toObject(Restaurant.class);
                        favoriteList.add(restaurant);
                    }
                    adapter.notifyDataSetChanged();

                    loadLocationsForFavorites(); // Charger les locations
                });
    }

    private void loadLocationsForFavorites() {
        db.collection("restaurant_location")
                .get()
                .addOnSuccessListener(snapshot -> {
                    locationList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String restaurantId = doc.getString("restaurantId");
                        if (restaurantId != null) {
                            for (Restaurant r : favoriteList) {
                                if (r.getId().equals(restaurantId)) {
                                    RestaurantLocation loc = doc.toObject(RestaurantLocation.class);
                                    loc.setRestaurantId(restaurantId);
                                    locationList.add(loc);
                                    break;
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged(); // Mise à jour adapter pour que le clic fonctionne
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // Rafraîchir à chaque affichage
    }
}