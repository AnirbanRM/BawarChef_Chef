package com.bawarchef.android.Hierarchy.DataStructure;

import java.util.ArrayList;

public class FoodNode extends Node{

    ArrayList<Ingredient> ingredients;
    float baseP,subP;

    public FoodNode(Node parent, String nodeText, boolean expandable) {
        super(parent, nodeText, expandable);
        ingredients = new ArrayList<Ingredient>();
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public float getBaseP() {
        return baseP;
    }

    public void setBaseP(float baseP) {
        this.baseP = baseP;
    }

    public float getSubP() {
        return subP;
    }

    public void setSubP(float subP) {
        this.subP = subP;
    }
}
