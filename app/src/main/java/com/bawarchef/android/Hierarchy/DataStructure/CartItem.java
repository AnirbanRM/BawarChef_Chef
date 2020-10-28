package com.bawarchef.android.Hierarchy.DataStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CartItem implements Serializable {

    private String category;
    private String foodName;
    private String quantity;
    private HashMap<String,String> customization;
    private float basePrice;
    private float incrPrice;

    public CartItem(String category,String foodName, float basePrice, float incrPrice, String quantity){
        this.category = category;
        this.foodName = foodName;
        this.basePrice = basePrice;
        this.incrPrice = incrPrice;
        this.quantity = quantity;
        customization = new HashMap<String,String>();
    }

    public void addCustomization(String custoName,String custoValue){
        customization.put(custoName,custoValue);
    }

    public void removeCustomization(String custoName){
        customization.remove(custoName);
    }

    public void removeCustomization(String custoName, String custoValue){
        if(customization.get(custoName).equals(custoValue))
            customization.remove(custoName);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public HashMap<String, String> getCustomization() {
        return customization;
    }

    public void setCustomization(HashMap<String, String> customization) {
        this.customization = customization;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    public float getIncrPrice() {
        return incrPrice;
    }

    public void setIncrPrice(float incrPrice) {
        this.incrPrice = incrPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
