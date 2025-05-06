package com.example.ballzbeta.objects;

import android.net.Uri;

public class Item {
    private int id;
    private String name;
    private String imageUri;
    private String description;

    public Item() {}

    public Item(int id, String name,  String imageUri, String description) {
        this.id = id;
        this.name = name;
        this.imageUri = imageUri;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUri() {
        return imageUri;
    }


    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
