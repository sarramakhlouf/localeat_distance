package com.iset.dsi.localeat.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String imageUrl;

    public User() {}

    public User(String uid, String name, String email, String phone, String imageUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    // getters & setters
    public String getUid(){ return uid; }

    public String getName(){ return name; }
    public String getEmail(){ return email; }
    public String getphone(){ return phone; }
    public String getImgUrl(){ return imageUrl; }

    public void setUid(String id){ this.uid = uid; }
}

