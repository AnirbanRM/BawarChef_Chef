package com.bawarchef.android;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;

public class ThisApplication extends Application {

    public MobileClient mobileClient;
    public static CurrentUserProfile currentUserProfile;
    public Context currentContext;
    public LocationEngine locationEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        currentUserProfile = new CurrentUserProfile(this);
        mobileClient = new MobileClient(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        locationEngine.stopLocationUpdate();
    }

    public void setMessageProcessor(MobileClient.MessageProcessor messageProcessor){
        mobileClient.setMessageProcessor(messageProcessor);
    }

    public MobileClient.MessageProcessor getMessageProcessor(){
        return mobileClient.getMessageProcessor();
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    @SuppressLint("MissingPermission")
    public void setCryptoKey(){
        String imei=null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getImei();
            } else {
                imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            }
        }catch (SecurityException e){}
        if(imei!=null) {
            MessageDigest md5 = null;
            try { md5 = MessageDigest.getInstance("SHA-256"); } catch (Exception e) { Log.e("T",e.getMessage()); }
            currentUserProfile.setCrypto_Key(md5.digest(imei.getBytes()));
        }
        else{
            byte b[] = new byte[32];
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(b);
            currentUserProfile.setCrypto_Key(b);
        }
    }

    public void startLocationUpdates(LocationEngine.OnLocationChange locationChange) {
        locationEngine = new LocationEngine(this,locationChange);
        LatLng l = locationEngine.getLastLocation();
        if(l==null){
            Log.e("Error","Error detecting location");
            return;}
        double lati = l.latitude;
    }

    public LatLng getLocation(){
        return locationEngine.lastLocation;
    }

    public static ArrayList<Object> sharableObject = new ArrayList<Object>();
}
