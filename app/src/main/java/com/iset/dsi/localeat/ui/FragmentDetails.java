package com.iset.dsi.localeat.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.models.Restaurant;
import com.iset.dsi.localeat.models.RestaurantLocation;

import java.util.ArrayList;

public class FragmentDetails extends Fragment implements OnMapReadyCallback {

    private Restaurant restaurant;
    private RestaurantLocation location;

    private MapView mapView;
    private GoogleMap gMap;

    private TextView tvName, tvAddress, tvDistance, tvCategory, tvRating;
    private ImageButton btnBack;

    public static FragmentDetails newInstance(Restaurant restaurant, RestaurantLocation location) {
        FragmentDetails fragment = new FragmentDetails();
        Bundle args = new Bundle();
        args.putString("id", restaurant.getId());
        args.putString("name", restaurant.getName());
        args.putString("address", restaurant.getAddress());
        args.putDouble("lat", location.getLocation().getLatitude());
        args.putDouble("lng", location.getLocation().getLongitude());
        args.putFloat("rating", (float) restaurant.getRating());
        args.putDouble("distance", restaurant.getDistance()); // distance calculée
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        // Récupération des arguments
        if (getArguments() != null) {
            restaurant = new Restaurant();
            restaurant.setId(getArguments().getString("id"));
            restaurant.setName(getArguments().getString("name"));
            restaurant.setAddress(getArguments().getString("address"));
            restaurant.setRating(getArguments().getFloat("rating", 0f));
            restaurant.setDistance(getArguments().getDouble("distance", -1));
        }

        // Initialisation des vues
        tvName = view.findViewById(R.id.tvDetailsName);
        tvAddress = view.findViewById(R.id.tvDetailsAddress);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvRating = view.findViewById(R.id.tvRating);
        btnBack = view.findViewById(R.id.btnBack);
        mapView = view.findViewById(R.id.mapViewDetails);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Affichage des infos
        tvName.setText(restaurant.getName());
        tvAddress.setText(restaurant.getAddress());
        tvRating.setText(restaurant.getRating() > 0
                ? String.format("%.1f ★", restaurant.getRating()) : "No rating");

        // Affichage distance
        if (restaurant.getDistance() >= 0) {
            tvDistance.setText(String.format("%.2f km away", restaurant.getDistance()));
        } else {
            tvDistance.setText("Distance unavailable");
        }

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Marker restaurant
        LatLng pos = new LatLng(location.getLocation().getLatitude(),
                location.getLocation().getLongitude());
        gMap.addMarker(new MarkerOptions().position(pos).title(restaurant.getName()));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        // Activer localisation utilisateur si permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }
    }

    // Lifecycle methods
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}