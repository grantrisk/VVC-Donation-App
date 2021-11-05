package com.grisk.vintagevaluesapp;

import java.util.Date;

public class Request {

    private String first;
    private String last;
    private String bags;
    private Date createdTime;
    private String pickupDescription;

    public Request() {}

    public Request(String first, String last, String bags, Date createdTime, String pickupDescription){
        this.first = first;
        this.last = last;
        this.bags = bags;
        this.createdTime = createdTime;
        this.pickupDescription = pickupDescription;
    }

    public String getFirst() {return first;}

    public String getLast() {return last;}

    public String getBags() {return bags;}

    public String getPickupDescription() {return pickupDescription;}

    public Date getCreatedTime() {return createdTime;}

}
