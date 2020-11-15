package com.bawarchef.android;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefIdentity;
import com.bawarchef.Containers.UserIdentity;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
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

    public void startLocationSharing(){
        while (true){
            try{
                Thread.sleep(5000);
            }catch (Exception e){}
            if(currentUserProfile.getChefIdentity()==null)
                break;
            if(currentUserProfile.getChefIdentity().regNo!=null && locationEngine!=null && locationEngine.lastLocation!=null) {
                Message m = new Message(Message.Direction.CLIENT_TO_SERVER, "LOC_UPD");
                m.putProperty("CHEF",currentUserProfile.getChefIdentity().regNo);
                m.putProperty("LAT",locationEngine.lastLocation.latitude);
                m.putProperty("LNG",locationEngine.lastLocation.longitude);

                try {
                    EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), mobileClient.getCrypto_key());
                    AsyncSender asyncSender = new AsyncSender();
                    asyncSender.execute(ep);
                }catch(Exception e){}
            }
        }
    }

    class AsyncSender extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    public void reEstablish(){
        Log.e("REESTABLISH","RETRYING");
        mobileClient = new MobileClient(this);

        setCryptoKey();
        SharedPreferences sharedPref1 = getSharedPreferences("BawarChef_CHEF_AppData",Context.MODE_PRIVATE);
        SharedPreferences sharedPref2 = getSharedPreferences("BawarChef_USER_AppData",Context.MODE_PRIVATE);

        String unameChef = sharedPref1.getString("UNAME", null);
        String pwdChef = sharedPref1.getString("PWD", null);
        String unameUser = sharedPref2.getString("UNAME", null);
        String pwdUser = sharedPref2.getString("PWD", null);

        if(unameChef!=null){
            ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.CHEF);
            ThisApplication.currentUserProfile.setType(CurrentUserProfile.Type.REGISTERED);
            ThisApplication.currentUserProfile.setChefUName(unameChef);

            ThisApplication.currentUserProfile.setPassword(pwdChef);

            final String pwd2 = pwdChef;
            new Thread(()->{
                try {
                    mobileClient.initialSocketSetup();
                } catch (IOException e) {
                    Log.e("SOCKET","NOT ESTAB");
                    try {
                        Thread.sleep(4000);
                    }catch (Exception e2){}
                    reEstablish();
                    return;
                }

                AuthenticationManager authenticationManager = new AuthenticationManager(mobileClient) {
                    @Override
                    public void onSuccessResponse(Message m) {
                        ThisApplication.currentUserProfile.setChefIdentity((ChefIdentity) m.getProperty("CHEF_IDENTITY"));
                        setMessageProcessor(((DashboardActivity)currentContext).activityMessageProcessor);
                    }

                    @Override
                    public void onFailureResponse(Message m) { }
                };

                authenticationManager.waitAndWork();
                new Thread(()->mobileClient.startListening()).start();
            }).start();
        }
        else if(unameUser!=null){
            mobileClient.setDefaultCryptoKey();
            ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.USER);
            ThisApplication.currentUserProfile.setType(CurrentUserProfile.Type.REGISTERED);
            ThisApplication.currentUserProfile.setUserUName(unameUser);

            ThisApplication.currentUserProfile.setPassword(pwdUser);

            final String pwd2 = pwdUser;
            new Thread(()->{
                try {
                    mobileClient.initialSocketSetup();
                } catch (IOException e) {
                    Log.e("SOCKET","NOT ESTAB");
                    try {
                        Thread.sleep(4000);
                    }catch (Exception e2){}
                    reEstablish();
                    return;
                }

                AuthenticationManager authenticationManager = new AuthenticationManager(mobileClient) {
                    @Override
                    public void onSuccessResponse(Message m) {
                        ThisApplication.currentUserProfile.setUserIdentity((UserIdentity) m.getProperty("USER_IDENTITY"));
                        setMessageProcessor(((DashboardUserActivity)currentContext).activityMessageProcessor);
                    }

                    @Override
                    public void onFailureResponse(Message m) { }
                };

                authenticationManager.waitAndWork();
                mobileClient.startListening();
            }).start();

        }




    }
}
