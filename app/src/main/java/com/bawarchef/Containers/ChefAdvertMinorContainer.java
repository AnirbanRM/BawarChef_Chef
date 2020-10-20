package com.bawarchef.Containers;

import java.io.Serializable;

public class ChefAdvertMinorContainer implements Serializable {

    public static class LatLng implements Serializable{
        private double lat;
        private double lng;

        public LatLng(double lat, double lng){
            this.lat = lat;
            this.lng = lng;
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

    private String chefID;
    private String fName;
    private String lName;
    private float rating;
    private LatLng location;

    private String dp=null;

    public ChefAdvertMinorContainer(String chefID, String fName, String lName, float rating){
        this.chefID = chefID;
        this.fName = fName;
        this.lName = lName;
        this.rating = rating;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getChefID() {
        return chefID;
    }

    public void setChefID(String chefID) {
        this.chefID = chefID;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
