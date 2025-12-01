package com.iset.dsi.localeat.models;

public class Restaurant {
    private String id;
    private String name;
    private String address;
    private String type;
    private String photoUrl;
    private double rating;
    private double distance; // distance calculée dynamiquement

    public Restaurant() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public String getPhotoUrl() { return photoUrl; }
    public double getRating() { return rating; }
    public double getDistance() { return distance; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setType(String type) { this.type = type; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setRating(double rating) { this.rating = rating; }
    public void setDistance(double distance) { this.distance = distance; }
}
