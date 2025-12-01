package com.iset.dsi.localeat.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Restaurant {
    private String id;
    private String name;
    private String address;
    private String type;
    private String photoUrl;
    private double rating;
    private double distance; // distance calculée dynamiquement

    private String opening_hour; // ex: "10AM"
    private String closing_hour;
    public Restaurant() {}



    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public String getPhotoUrl() { return photoUrl; }
    public double getRating() { return rating; }
    public double getDistance() { return distance; }
    @PropertyName("opening_hour")
    public String getOpeningHour() { return opening_hour; }
    @PropertyName("closing_hour")
    public String getClosingHour() { return closing_hour; }


    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setType(String type) { this.type = type; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setRating(double rating) { this.rating = rating; }
    public void setDistance(double distance) { this.distance = distance; }

    @PropertyName("opening_hour")
    public void setOpeningHour(String openingHour) { this.opening_hour = openingHour; }
    @PropertyName("closing_hour")
    public void setClosingHour(String closingHour) { this.closing_hour = closingHour; }
}
