package com.bawarchef.Containers;

import java.io.Serializable;

public class ChefOrderListItemClass implements Serializable {

    public String orderiD;
    public String userName;
    public byte[] userDP;
    public String bookingDatetime;
    public Order.Status status;
    public double price;

    public ChefOrderListItemClass(String orderID, String name, byte[] dp, double price, String status, String bookingDatetime){
        this.orderiD = orderID;
        this.userName= name;
        this.userDP = dp;
        this.bookingDatetime = bookingDatetime;
        this.price = price;
        this.status = Order.Status.valueOf(status);
    }
}
