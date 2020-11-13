package com.bawarchef.Containers;

import com.bawarchef.android.Hierarchy.DataStructure.CartItem;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderSummaryItem implements Serializable {

    public String chefID,orderID, name, mob, datetime,address;
    public double bookingLat, bookingLng;
    public Order.Status status;
    public byte[] dp;
    public double price;
    public ArrayList<CartItem> ordereditems;

    public OrderSummaryItem(String orderID, String chefID, String name, String mob, String datetime, String address, String status,byte[] dp, double price, ArrayList<CartItem> cartItems, double bookingLat, double bookingLng){
        this.orderID = orderID;
        this.chefID = chefID;
        this.mob = mob;
        this.name = name;
        this.datetime = datetime;
        this.address = address;
        this.status = Order.Status.valueOf(status);
        this.dp = dp;
        this.ordereditems = cartItems;
        this.price = price;
        this.bookingLat = bookingLat;
        this.bookingLng = bookingLng;
    }
}
