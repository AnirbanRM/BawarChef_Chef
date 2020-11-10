package com.bawarchef.Containers;

import com.bawarchef.android.Hierarchy.DataStructure.CartItem;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderSummaryItem implements Serializable {

    public String orderID, name, datetime,address;
    public Order.Status status;
    public byte[] dp;
    public double price;
    public ArrayList<CartItem> ordereditems;

    public OrderSummaryItem(String orderID, String name, String datetime, String address, String status,byte[] dp, double price, ArrayList<CartItem> cartItems){
        this.orderID = orderID;
        this.name = name;
        this.datetime = datetime;
        this.address = address;
        this.status = Order.Status.valueOf(status);
        this.dp = dp;
        this.ordereditems = cartItems;
        this.price = price;
    }
}
