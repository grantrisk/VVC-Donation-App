package com.grisk.vintagevaluesapp;

public class BagPicture {

    private String name;
    private String imageFile;

    // No-argument constructor is required to support conversion of Firestore document to POJO
    public BagPicture() {  }

    // All-argument constructor is required to support conversion of Firestore document to POJO
    public BagPicture(String name, String imageFile) {
        this.name = name;
        this.imageFile = imageFile;
    }

    public String getName() {
        return name;
    }

    public String getImageFile() {
        return imageFile;
    }
}
