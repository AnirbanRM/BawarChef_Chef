package com.bawarchef.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;

public class Permissions {

    Context appContext = null;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static enum CONDITION{GRANTED,DENIED};
    static Permissions inst = new Permissions();
    private Permissions(){ }

    public static Permissions getInstance(Context ctx){
        inst.appContext = ctx;
        return inst;
    }

    boolean isPermissionGranted(String permission){
        return ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    boolean isPermissionGranted(String[] permissions){
        for(String i : permissions) {
            boolean l = ContextCompat.checkSelfPermission(appContext, i) == PackageManager.PERMISSION_GRANTED;
            if(!l)
                return false;
        }
        return true;
    }

    public String[] getList(CONDITION condition){
        int t=0;
        if(condition==CONDITION.GRANTED) t = PackageManager.PERMISSION_GRANTED;
        else if(condition==CONDITION.DENIED) t = PackageManager.PERMISSION_DENIED;

        ArrayList<String> permissions = new ArrayList<String>();
        for(String i : REQUIRED_PERMISSIONS)
            if(ContextCompat.checkSelfPermission(appContext,i)==t)
                permissions.add(i);

        return permissions.toArray(new String[permissions.size()]);
    }

    public void grantFromUser(int requestCode){
        for(String i : getList(CONDITION.DENIED))
            Log.e("LOL",i);
        String[] toGrant = getList(CONDITION.DENIED);
        if(toGrant.length==0)return;
        ((AppCompatActivity)appContext).requestPermissions(getList(CONDITION.DENIED),requestCode);
    }





}
