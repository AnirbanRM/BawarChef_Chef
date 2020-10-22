package com.bawarchef.Containers;

import java.io.Serializable;

public class ChefAdvertMajorContainer implements Serializable {

    private String chefID;
    private String fName;
    private String lName;
    private float rating;
    private String bio;
    private String dp=null;

    public ChefAdvertMajorContainer(String chefID, String fName, String lName, float rating,String bio){
        this.chefID = chefID;
        this.fName = fName;
        this.lName = lName;
        this.rating = rating;
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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
