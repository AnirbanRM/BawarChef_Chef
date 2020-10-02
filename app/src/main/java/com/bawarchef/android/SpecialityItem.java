package com.bawarchef.android;

import android.graphics.Bitmap;

public class SpecialityItem{

    Bitmap bmp;
    String caption;
    boolean adder=false;

    public SpecialityItem(Bitmap bmp, String caption){
        this.bmp = bmp;
        this.caption = caption;
        adder=false;
    }

    public static SpecialityItem getAdder(String addText){
        SpecialityItem si = new SpecialityItem(null,addText);
        si.adder = true;
        return si;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isAdder() {
        return adder;
    }
}
