package com.example.hourgate.models;

import java.io.Serializable;

public class SitesModel implements Serializable {

    String name;
    float hourRate;
    Double lat;
    Double lng;
    Long addedTime;

    public SitesModel() {} // empty constructed needed for firebase

    // constructor for sites which take in site name, hourly rate , lat/lng values and added time (time site was added)
    public SitesModel(String name, float hourRate, Double lat, Double lng, Long addedTime) {
        this.name = name;
        this.hourRate = hourRate;
        this.lat = lat;
        this.lng = lng;
        this.addedTime = addedTime;
    }

    // getters

    public String getName() { return name; }

    public float getHourRate() {
        return hourRate;
    }

    public Long getAddedTime() {
        return addedTime;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    // setters

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setHourRate(float hourRate) {
        this.hourRate = hourRate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddedTime(Long addedTime) {
        this.addedTime = addedTime;
    }
}
