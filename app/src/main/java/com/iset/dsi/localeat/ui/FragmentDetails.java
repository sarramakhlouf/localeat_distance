package com.iset.dsi.localeat.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.models.Restaurant;
import com.iset.dsi.localeat.models.RestaurantLocation;

public class FragmentDetails extends Fragment implements OnMapReadyCallback {

    private Restaurant restaurant;
    private RestaurantLocation location;

    private MapView mapView;
    private GoogleMap gMap;

    private TextView tvName, tvAddress, tvDistance, tvCategory, tvOpeningStatus, tvRating;
    private ImageButton btnBack;

    public static FragmentDetails newInstance(Restaurant restaurant, RestaurantLocation location, String parentFragmentName) {
        FragmentDetails fragment = new FragmentDetails();
        Bundle args = new Bundle();
        args.putString("id", restaurant.getId());
        args.putString("name", restaurant.getName());
        args.putString("address", restaurant.getAddress());
        args.putDouble("lat", location.getLocation().getLatitude());
        args.putDouble("lng", location.getLocation().getLongitude());
        args.putFloat("rating", (float) restaurant.getRating());
        args.putDouble("distance", restaurant.getDistance());
        args.putString("opening_hour", restaurant.getOpeningHour());
        args.putString("closing_hour", restaurant.getClosingHour());
        args.putString("parentFragment", parentFragmentName); // ← nom du parent
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        // 📌 Récupération des arguments
        double lat = 0, lng = 0;

        if (getArguments() != null) {
            restaurant = new Restaurant();
            restaurant.setId(getArguments().getString("id"));
            restaurant.setName(getArguments().getString("name"));
            restaurant.setAddress(getArguments().getString("address"));
            restaurant.setRating(getArguments().getFloat("rating", 0f));
            restaurant.setDistance(getArguments().getDouble("distance", -1));
            restaurant.setOpeningHour(getArguments().getString("opening_hour"));
            restaurant.setClosingHour(getArguments().getString("closing_hour"));

            lat = getArguments().getDouble("lat", 0);
            lng = getArguments().getDouble("lng", 0);
        }

        // 📌 Construire RestaurantLocation correctement
        location = new RestaurantLocation(restaurant.getId(), lat, lng);

        // 📌 Initialisation vues
        tvName = view.findViewById(R.id.tvDetailsName);
        tvAddress = view.findViewById(R.id.tvDetailsAddress);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvRating = view.findViewById(R.id.tvRating);
        btnBack = view.findViewById(R.id.btnBack);
        tvOpeningStatus = view.findViewById(R.id.tvOpeningStatus);


        mapView = view.findViewById(R.id.mapViewDetails);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 📌 Affichage infos
        tvName.setText(restaurant.getName());
        tvAddress.setText(restaurant.getAddress());

        tvRating.setText(restaurant.getRating() > 0
                ? String.format("%.1f ★", restaurant.getRating())
                : "No rating");

        tvDistance.setText(
                restaurant.getDistance() >= 0
                        ? String.format("%.2f km away", restaurant.getDistance())
                        : "Distance unavailable"
        );

        tvOpeningStatus.setText(updateOpeningStatus());

        btnBack.setOnClickListener(v -> {
            // simple popBackStack pour revenir au fragment parent existant
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }


    // 📌 GOOGLE MAPS READY
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        LatLng pos = new LatLng(location.getLocation().getLatitude(),
                location.getLocation().getLongitude());

        gMap.addMarker(new MarkerOptions().position(pos).title(restaurant.getName()));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            gMap.setMyLocationEnabled(true);
        }
    }

    // Méthode pour obtenir le statut sous forme de String
    private String updateOpeningStatus() {
        if (restaurant.getOpeningHour() == null || restaurant.getClosingHour() == null) {
            return "Heures non disponibles";
        }

        try {
            // Parser avec Locale.US pour AM/PM
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h a", java.util.Locale.US);
            java.util.Date openingDate = sdf.parse(restaurant.getOpeningHour().toUpperCase());
            java.util.Date closingDate = sdf.parse(restaurant.getClosingHour().toUpperCase());

            java.util.Calendar nowCal = java.util.Calendar.getInstance();
            int nowMinutes = nowCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                    nowCal.get(java.util.Calendar.MINUTE);

            java.util.Calendar openCal = java.util.Calendar.getInstance();
            openCal.setTime(openingDate);
            int openMinutes = openCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + openCal.get(java.util.Calendar.MINUTE);

            java.util.Calendar closeCal = java.util.Calendar.getInstance();
            closeCal.setTime(closingDate);
            int closeMinutes = closeCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + closeCal.get(java.util.Calendar.MINUTE);

            String status;

            if (nowMinutes < openMinutes) {
                if (openMinutes - nowMinutes <= 60) {
                    status = "Ouvre bientôt (" + restaurant.getOpeningHour() + ")";
                } else {
                    status = "Fermé (" + restaurant.getOpeningHour() + ")";
                }
            } else if (nowMinutes >= openMinutes && nowMinutes <= closeMinutes) {
                if (closeMinutes - nowMinutes <= 60) {
                    status = "Ferme bientôt (" + restaurant.getClosingHour() + ")";
                } else {
                    status = "Ouvert (" + restaurant.getClosingHour() + ")";
                }
            } else {
                status = "Fermé (" + restaurant.getOpeningHour() + ")";
            }

            return status;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur horaire";
        }
    }


    // 📌 Lifecycle MapView
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}