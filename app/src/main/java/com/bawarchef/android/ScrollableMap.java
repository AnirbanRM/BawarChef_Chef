package com.bawarchef.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

public class ScrollableMap extends MapView {


    public ScrollableMap(Context context) {
        super(context);
    }

    public ScrollableMap(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ScrollableMap(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public ScrollableMap(Context context, GoogleMapOptions googleMapOptions) {
        super(context, googleMapOptions);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}
