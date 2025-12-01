package com.iset.dsi.localeat.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.iset.dsi.localeat.R;
import com.iset.dsi.localeat.adapters.RestaurantAdapter;
import com.iset.dsi.localeat.models.Restaurant;
import com.iset.dsi.localeat.models.RestaurantLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "HOME_DEBUG";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private GoogleMap gMap;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;

    private EditText inputSearch;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private List<RestaurantLocation> locationList = new ArrayList<>();

    private List<Restaurant> fullRestaurantList = new ArrayList<>();
    private double userLat = 0, userLng = 0;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean isLocationReady = false;
    private boolean isDataLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        inputSearch = view.findViewById(R.id.inputSearch);

        recyclerView = view.findViewById(R.id.recyclerRestaurants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchRestaurants(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        requestLocationPermission();

        return view;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    updateUserLocation(location.getLatitude(), location.getLongitude());
                } else {
                    requestRealTimeLocationUpdates();
                }
            }).addOnFailureListener(e -> requestRealTimeLocationUpdates());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void requestRealTimeLocationUpdates() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(10000);
        request.setFastestInterval(5000);

        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    Location loc = locationResult.getLastLocation();
                    if (loc != null) {
                        updateUserLocation(loc.getLatitude(), loc.getLongitude());
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, callback, null);
        }
    }

    private void updateUserLocation(double lat, double lng) {
        userLat = lat;
        userLng = lng;
        isLocationReady = true;
        Log.e(TAG, "User location: " + userLat + ", " + userLng);

        if (isDataLoaded) {
            calculateDistances();
        } else {
            loadRestaurantsData();
        }
    }

    private void loadRestaurantsData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("restaurants").get().addOnSuccessListener(snapshot -> {
            restaurantList.clear();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Restaurant restaurant = doc.toObject(Restaurant.class);
                if (restaurant != null) {
                    restaurant.setId(doc.getId());
                    restaurant.setDistance(-1); // distance inconnue initialement
                    restaurantList.add(restaurant);
                    Log.e(TAG, "Restaurant loaded: " + restaurant.getName() + " | ID = " + restaurant.getId());
                    fullRestaurantList.add(restaurant);
                }
            }

            db.collection("restaurant_location").get().addOnSuccessListener(locSnapshot -> {
                locationList.clear();
                for (DocumentSnapshot doc : locSnapshot.getDocuments()) {
                    String restaurantId = doc.getString("restaurantId");
                    GeoPoint geo = doc.getGeoPoint("location");
                    if (restaurantId != null && geo != null) {
                        RestaurantLocation loc = new RestaurantLocation();
                        loc.setRestaurantId(restaurantId);
                        loc.setLocation(geo);
                        locationList.add(loc);
                        Log.e(TAG, "Location loaded: restaurantId='" + restaurantId + "' | geo=" + geo);
                    } else {
                        Log.e(TAG, "Location missing restaurantId or GeoPoint for doc: " + doc.getId());
                    }
                }

                isDataLoaded = true;

                if (isLocationReady) {
                    Log.e(TAG, "All data ready → calculateDistances()");
                    calculateDistances();
                } else {
                    setupAdapter();
                    addMarkersOnMap();
                }
            });
        });
    }

    private void calculateDistances() {
        for (Restaurant r : restaurantList) {
            boolean matched = false;
            for (RestaurantLocation loc : locationList) {
                Log.e(TAG, "Checking R=" + r.getId() + " vs L=" + loc.getRestaurantId());
                if (r.getId().equals(loc.getRestaurantId())) {
                    matched = true;
                    if(loc.getLocation() != null){
                        double dist = calculateDistance(userLat, userLng,
                                loc.getLocation().getLatitude(),
                                loc.getLocation().getLongitude());
                        r.setDistance(dist);
                        Log.e(TAG, "MATCH FOUND for " + r.getName() + " | distance=" + dist);
                    } else {
                        Log.e(TAG, "GeoPoint NULL for restaurant: " + r.getName());
                        r.setDistance(-1);
                    }
                    break;
                }
            }
            if(!matched){
                Log.e(TAG, "NO MATCH found for restaurant: " + r.getName());
            }
        }

        Collections.sort(restaurantList, (r1, r2) -> Double.compare(r1.getDistance(), r2.getDistance()));

        setupAdapter();
        addMarkersOnMap();
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        Location a = new Location("");
        a.setLatitude(lat1);
        a.setLongitude(lng1);
        Location b = new Location("");
        b.setLatitude(lat2);
        b.setLongitude(lng2);
        double d = a.distanceTo(b) / 1000.0;
        Log.e(TAG, "Calculated distance: " + d + " km");
        return d;
    }

    private void setupAdapter() {
        if (adapter == null) {
            adapter = new RestaurantAdapter(getContext(), restaurantList, locationList, false);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void addMarkersOnMap() {
        if (gMap == null) return;
        gMap.clear();

        if (userLat != 0 && userLng != 0) {
            LatLng userPos = new LatLng(userLat, userLng);
            gMap.addMarker(new MarkerOptions().position(userPos).title("Vous êtes ici"));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 13f));
        }

        for (Restaurant r : restaurantList) {
            for (RestaurantLocation loc : locationList) {
                if (r.getId().equals(loc.getRestaurantId()) && loc.getLocation() != null) {
                    LatLng pos = new LatLng(loc.getLocation().getLatitude(), loc.getLocation().getLongitude());
                    String snippet = r.getDistance() >= 0 ? String.format("%.2f km", r.getDistance()) : "Distance inconnue";
                    gMap.addMarker(new MarkerOptions().position(pos).title(r.getName()).snippet(snippet));
                    break;
                }
            }
        }
    }

    private void searchRestaurants(String text) {
        restaurantList.clear();
        if (text.isEmpty()) {
            restaurantList.addAll(fullRestaurantList);
        } else {
            String search = text.toLowerCase().trim();
            for (Restaurant r : fullRestaurantList) {
                if (r.getName().toLowerCase().contains(search)) {
                    restaurantList.add(r);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }
        if (isDataLoaded) addMarkersOnMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            loadRestaurantsData();
        }
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}
