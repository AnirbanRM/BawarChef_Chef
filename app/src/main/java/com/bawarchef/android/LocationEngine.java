package com.bawarchef.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class LocationEngine {

    FusedLocationProviderClient locationProviderClient;
    ThisApplication appRfs;
    LatLng lastLocation=null;
    LocationRequest lr;
    OnLocationChange onLocationChange;

    LocationEngine(ThisApplication application,OnLocationChange onLocationChange) {
        this.onLocationChange = onLocationChange;
        this.appRfs = application;
        locationProviderClient = LocationServices.getFusedLocationProviderClient(appRfs.currentContext);
        fetchLastLocation();

        lr = new LocationRequest();

        if (ActivityCompat.checkSelfPermission(appRfs.getCurrentContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appRfs.getCurrentContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
        else
            locationProviderClient.requestLocationUpdates(lr,lc, Looper.getMainLooper());
    }

    LocationCallback lc = new LocationCallback(){

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(locationResult==null)return;

            List<Location> list = locationResult.getLocations();
            if(list.size()!=0){
                LatLng loc = new LatLng(list.get(0).getLatitude(),list.get(0).getLongitude());
                lastLocation = loc;
            }
        }
    };

    public LatLng getLastLocation() {
        return lastLocation;
    }

    void fetchLastLocation(){
        if (ActivityCompat.checkSelfPermission(appRfs.getCurrentContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appRfs.getCurrentContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            Log.e("Error","No location permission");

        locationProviderClient.getLastLocation().addOnSuccessListener((AppCompatActivity) appRfs.currentContext, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.e("Success", String.valueOf(location.getLatitude()));
                lastLocation = new LatLng(location.getLatitude(),location.getLongitude());
                if(onLocationChange!=null)
                    onLocationChange.onChange(lastLocation);

            }
        });
    }

    void stopLocationUpdate(){
        locationProviderClient.removeLocationUpdates(lc);
    }

    public static abstract class OnLocationChange{
        public abstract void onChange(LatLng l);
    }
}
