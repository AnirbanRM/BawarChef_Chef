package com.bawarchef.android.Hierarchy.DataStructure;

import java.io.Serializable;

public class Ingredient implements Serializable {

    private String title;
    public enum Unit{KG,GRAM,L,ML,UNITS};
    private Unit unit;
    private float magnitude;

    public Ingredient(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public float getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }
}
