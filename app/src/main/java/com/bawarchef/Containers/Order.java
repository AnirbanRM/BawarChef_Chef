package com.bawarchef.Containers;

import com.bawarchef.android.Hierarchy.DataStructure.CartItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Order implements Serializable {

    public static enum Status{PENDING,CHEF_APPROVED,CHEF_DECLINED,USER_CANCELLED,COMPLETED};

    String chefID, address, userID;
    double lati,longi;
    String bookingDate,bookingTime;
    String currentDate,currentTime;
    Status status;

    ArrayList<CartItem> ordereditems;

    public Order(String chefID,String address, String userID, double lati, double longi, String bookingDate, String bookingTime){
        Calendar cal = Calendar.getInstance();
        currentDate = cal.get(Calendar.YEAR)+"-"+ (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DATE);
        currentTime = cal.get(Calendar.HOUR_OF_DAY)+ ":" + cal.get(Calendar.MINUTE);
        this.chefID = chefID;
        this.address = address;
        this.userID = userID;
        this.lati = lati;
        this.longi = longi;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = Status.PENDING;
    }

    public void setOrdereditems(ArrayList<CartItem> cartItems){
        this.ordereditems = cartItems;
    }

    public String getChefID() {
        return chefID;
    }

    public void setChefID(String chefID) {
        this.chefID = chefID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ArrayList<CartItem> getOrdereditems() {
        return ordereditems;
    }
}
