package com.iset.dsi.localeat.models;

import com.google.firebase.firestore.GeoPoint;

public class RestaurantLocation {
    private String restaurantId; // doit correspondre EXACTEMENT au champ Firestore
    private GeoPoint location;

    public RestaurantLocation() {} // constructeur vide obligatoire pour Firestore

    public RestaurantLocation(String restaurantId, double latitude, double longitude) {
        this.restaurantId = restaurantId;
        this.location = new GeoPoint(latitude, longitude);
    }
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
}
