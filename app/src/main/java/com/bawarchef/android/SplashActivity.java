package com.bawarchef.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Space;

public class SplashActivity extends AppCompatActivity {

    int permissionRequestCode=1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ((ThisApplication)getApplication()).setCurrentContext(this);

        Permissions p =Permissions.getInstance(this);
        p.grantFromUser(permissionRequestCode);

        ((ThisApplication) getApplication()).setCryptoKey();

        SharedPreferences sharedPref1 = getSharedPreferences("BawarChef_CHEF_AppData",Context.MODE_PRIVATE);
        SharedPreferences sharedPref2 = getSharedPreferences("BawarChef_USER_AppData",Context.MODE_PRIVATE);

        String unameChef = sharedPref1.getString("UNAME", null);
        String pwdChef = sharedPref1.getString("PWD", null);
        String unameUser = sharedPref2.getString("UNAME", null);
        String pwdUser = sharedPref2.getString("PWD", null);


        if(unameChef==null&&unameUser==null){
            (new Handler()).postDelayed(() -> {
                Intent i = new Intent(SplashActivity.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            },2000);
        }
        else{
            if(unameChef!=null){
                ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.CHEF);
                new Thread(()->{
                    ((ThisApplication)getApplication()).mobileClient.connect(unameChef,pwdChef,true);
                }).start();
            }
            else if(unameUser!=null){
                ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.USER);
                new Thread(()->{
                    ((ThisApplication)getApplication()).mobileClient.connectAsCusto(unameUser,pwdUser,true);
                }).start();
            }
        }

    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i = 0; i< permissions.length; i++){
            if(permissions[i]== Manifest.permission.READ_PHONE_STATE)
                if(grantResults[i]== PackageManager.PERMISSION_GRANTED)
                    ((ThisApplication)getApplication()).setCryptoKey();
                else {
                    Permissions p = Permissions.getInstance(SplashActivity.this);
                    p.grantFromUser(permissionRequestCode);
                }
        }
    }

    public void failedAuth() {
        runOnUiThread(()->{
            Intent i = new Intent(SplashActivity.this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}