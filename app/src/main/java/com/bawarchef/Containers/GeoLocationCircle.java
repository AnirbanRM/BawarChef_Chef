package com.bawarchef.Containers;

import java.io.Serializable;

public class GeoLocationCircle implements Serializable {

    long id;
    String placeTitle;
    double lat;
    double lng;

    public GeoLocationCircle(long id,String placeTitle,double lat, double lng){
        this.placeTitle = placeTitle;
        this.lat = lat;
        this.lng = lng;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getDist(double sLat, double sLng){
        return Math.sqrt(Math.pow((this.lat-sLat),2) + Math.pow((this.lng-sLng),2));
    }

    public String getPlaceTitle() {
        return placeTitle;
    }

    public void setPlaceTitle(String placeTitle) {
        this.placeTitle = placeTitle;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
