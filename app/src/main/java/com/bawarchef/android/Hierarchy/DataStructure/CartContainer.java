package com.bawarchef.android.Hierarchy.DataStructure;

import android.widget.TextView;

import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;

public class CartContainer {

    public static TextView ui_count;
    private String chefID;
    private String chefname;
    private byte[] chefDP;
    private ArrayList<CartItem> cartItems;

    public CartContainer(){
        cartItems = new ArrayList<CartItem>();
    }

    public float getCartPrice(){
        float sum = 0;
        for(CartItem i : cartItems)
            sum+=(i.getBasePrice());
        return sum;
    }

    public String getChefID() {
        return chefID;
    }

    public void setChefID(String chefID) {
        this.chefID = chefID;
    }

    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(ArrayList<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void refresh(){
        String size = cartItems.size()>10?"10+":String.valueOf(cartItems.size());
        ui_count.setText(size);
    }

    public String getChefname() {
        return chefname;
    }

    public void setChefname(String chefname) {
        this.chefname = chefname;
    }

    public byte[] getChefDP() {
        return chefDP;
    }

    public void setChefDP(byte[] chefDP) {
        this.chefDP = chefDP;
    }
}
