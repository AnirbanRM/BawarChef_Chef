package com.bawarchef.Containers;

import java.io.Serializable;

public class OrderListItemClass implements Serializable {

    public String orderiD;
    public String chefName;
    public byte[] chefDP;
    public String bookingDatetime;
    public Order.Status status;
    public double price;

    public OrderListItemClass(String orderID, String name, byte[] dp, double price, String status, String bookingDatetime){
        this.orderiD = orderID;
        this.chefName= name;
        this.chefDP = dp;
        this.bookingDatetime = bookingDatetime;
        this.price = price;
        this.status = Order.Status.valueOf(status);
    }
}
