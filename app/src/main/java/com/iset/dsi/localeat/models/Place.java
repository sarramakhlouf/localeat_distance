package com.iset.dsi.localeat.models;

import java.io.Serializable;

public class Place implements Serializable {
    private String name;
    private String address;
    private double lat;
    private double lng;
    private String id;

    public Place() {}

    public Place(String name, String address, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    // getters / setters
    public String getName(){ return name; }
    public String getAddress(){ return address; }
    public double getLat(){ return lat; }
    public double getLng(){ return lng; }
    public void setId(String id){ this.id = id; }
    public String getId(){ return id; }
}
