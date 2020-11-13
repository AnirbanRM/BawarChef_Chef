package com.bawarchef.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefIdentity;
import com.bawarchef.Containers.GeoLocationCircle;
import com.bawarchef.android.Fragments.Broadcasts;
import com.bawarchef.android.Fragments.FoodMenu;
import com.bawarchef.android.Fragments.History;
import com.bawarchef.android.Fragments.Home;
import com.bawarchef.android.Fragments.MessageReceiver;
import com.bawarchef.android.Fragments.MyProfile;
import com.bawarchef.android.Fragments.Orders;
import com.bawarchef.android.Fragments.PersonalDetails;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    DrawerLayout drawer;
    NavigationView navView;
    ImageButton menu_but;
    TextView area_circ,navDrUName,navDrName;

    MobileClient.MessageProcessor defaultMessageProcessor;

    static boolean changeCircle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawer = findViewById(R.id.drawer);
        menu_but = findViewById(R.id.menu_but);
        area_circ = findViewById(R.id.ar_circ);
        navView = findViewById(R.id.navView);

        View headerView = navView.getHeaderView(0);
        navDrName = headerView.findViewById(R.id.nav_hdr_name);
        navDrUName = headerView.findViewById(R.id.nav_hdr_uname);

        ChefIdentity ci = ThisApplication.currentUserProfile.getChefIdentity();
        navDrUName.setText(ThisApplication.currentUserProfile.getChefUName());
        navDrName.setText(ci.fname + " " + ci.lname);

        menu_but.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START,true));

        setFragment(navView.getMenu().getItem(0));
        navView.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            drawer.closeDrawer(GravityCompat.START,true);
            setFragment(item);
            return true;
        });
    }

    private void setLocationCircle(LatLng location) {
        Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"GEOLOC_QUERY");
        m.putProperty("LAT",location.latitude);
        m.putProperty("LNG",location.longitude);

        new Thread(()->{
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m),((ThisApplication)getApplication()).mobileClient.getCrypto_key());
                ((ThisApplication)getApplication()).mobileClient.send(ep);
            }catch (Exception e){}
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        defaultMessageProcessor = ((ThisApplication)getApplication()).getMessageProcessor();
        ((ThisApplication)getApplication()).setMessageProcessor(activityMessageProcessor);
        ((ThisApplication)getApplication()).setCurrentContext(this);
        new Thread(() -> ((ThisApplication)getApplication()).startLocationUpdates(onLocationChange)).start();
        new Thread(() -> ((ThisApplication)getApplication()).startLocationSharing()).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((ThisApplication)getApplication()).setMessageProcessor(defaultMessageProcessor);
    }

    public static Fragment activeFragment = null;

    private void setFragment(MenuItem item) {
        activeFragment = null;

        switch(item.getTitle().toString()){

            case "Home":
                activeFragment = new Home();
                break;

            case "My Profile":
                activeFragment = new MyProfile();
                break;

            case "My Menu":
                activeFragment = new FoodMenu();
                break;

            case "Orders":
                activeFragment = new Orders();
                break;

            case "Broadcasts":
                activeFragment = new Broadcasts();
                break;

            case "History":
                activeFragment = new History();
                break;

            case "Personal Details":
                activeFragment = new PersonalDetails();
                break;

            case "Log out":
                try {
                    ((ThisApplication) getApplication()).mobileClient.closeConnection();
                }catch (Exception e){}
                ((ThisApplication) getApplication()).mobileClient = new MobileClient((ThisApplication) getApplication());
                ThisApplication.currentUserProfile = new CurrentUserProfile(getApplication());
                ((ThisApplication)getApplication()).setCryptoKey();

                getApplicationContext().getSharedPreferences("BawarChef_CHEF_AppData", 0).edit().clear().apply();
                Intent i = new Intent(DashboardActivity.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;

            case "Quit":
                finishAndRemoveTask();
                break;
        }
        if(activeFragment!=null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentViewPort, activeFragment)
                    .commit();

        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()==0)
            super.onBackPressed();
        else{
            getSupportFragmentManager().popBackStack();
        }
    }

    MobileClient.MessageProcessor activityMessageProcessor = new MobileClient.MessageProcessor() {
        @Override
        public void process(Message m) {

            if(m.getMsg_type().equals("GEOLOC_RESP")){
                ArrayList<GeoLocationCircle> circles = (ArrayList<GeoLocationCircle>) m.getProperty("NEARBY_P");
                GeoLocationCircle nearest = circles.get(0);
                LatLng currL = ((ThisApplication)getApplication()).getLocation();
                double min = nearest.getDist(currL.latitude,currL.longitude);
                for (GeoLocationCircle loc : circles){
                    double t = loc.getDist(currL.latitude,currL.longitude);
                    if(t<min){
                        min = t;
                        nearest = loc;
                    }
                }
                area_circ.setText(nearest.getPlaceTitle());
                checkforCircleChange(nearest,(String)m.getProperty("REG_CIRCLE"));
            }

            else if (m.getMsg_type().equals("GEOLOC_REG_RESP")){
                if(m.getProperty("RESULT").equals("OK")){
                    SharedPreferences sharedPref1 = getSharedPreferences("BawarChef_CHEF_AppData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref1.edit();
                    if(m.getProperty("CIRCLE_ID")!=null&&m.getProperty("CIRCLE_NAME")!=null) {
                        editor.putString("DEF_CIRCLE_ID", (String) m.getProperty("CIRCLE_ID"));
                        editor.putString("DEF_CIRCLE_NAME", (String) m.getProperty("CIRCLE_NAME"));
                    }
                    editor.apply();
                    runOnUiThread(()->{
                        Toast.makeText(DashboardActivity.this,"Successfully updated circle !",Toast.LENGTH_SHORT).show();
                    });
                }
            }

            else{
                ((MessageReceiver)activeFragment).process(m);
            }


        }
    };

    private void checkforCircleChange(GeoLocationCircle nearest, String regCircle) {
        SharedPreferences sharedPref1 = getSharedPreferences("BawarChef_CHEF_AppData", Context.MODE_PRIVATE);
        String circleID = sharedPref1.getString("DEF_CIRCLE_ID", null);
        if(!regCircle.equals(circleID)){
            SharedPreferences.Editor editor = sharedPref1.edit();
            editor.putString("DEF_CIRCLE_ID",regCircle);
            editor.apply();
            circleID = regCircle;
        }

        if(circleID==null){
            Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"CIRCLE_REGISTRATION");
            m.putProperty("CIRCLE_ID",String.valueOf(nearest.getId()));
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getApplication()).mobileClient.getCrypto_key());
                AsyncSender asyncSender = new AsyncSender();
                asyncSender.execute(ep);
            }catch(Exception e){}
        }

        else if(!circleID.equals(String.valueOf(nearest.getId()))&&changeCircle) {
            Intent i = new Intent(DashboardActivity.this,CircleChange.class);
            i.putExtra("newC",nearest.getPlaceTitle());
            i.putExtra("newI",String.valueOf(nearest.getId()));
            startActivity(i);


        }
    }

    LocationEngine.OnLocationChange onLocationChange = new LocationEngine.OnLocationChange() {
        @Override
        public void onChange(LatLng l) {
            setLocationCircle(l);
        }
    };

    class AsyncSender extends AsyncTask<EncryptedPayload,Void,Void>{

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

}
