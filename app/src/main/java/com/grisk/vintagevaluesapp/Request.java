package com.grisk.vintagevaluesapp;

import java.util.Date;

public class Request {

    private String Uid;
    private String first;
    private String last;
    private String bags;
    private Date createdTime;
    private String pickupDescription;
    private Boolean requestAccepted = false;
    private String pickupUid = "";
    private Boolean requestCompleted = false;
    private String imageFile;

    public Request() {}

    public Request(String Uid, String first, String last, String bags, Date createdTime, String pickupDescription, String imageFile){
        this.Uid = Uid;
        this.first = first;
        this.last = last;
        this.bags = bags;
        this.createdTime = createdTime;
        this.pickupDescription = pickupDescription;
        this.imageFile = imageFile;
    }

    public String getUid() {return  Uid;}

    public String getFirst() {return first;}

    public String getLast() {return last;}

    public String getBags() {return bags;}

    public String getPickupDescription() {return pickupDescription;}

    public Date getCreatedTime() {return createdTime;}

    public Boolean getRequestAccepted() { return requestAccepted; }

    public Boolean getRequestCompleted() { return requestCompleted; }

    public String getPickupUid() { return pickupUid; }

    public String getImageFile() { return imageFile; }
}
