package com.bawarchef.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.GeoLocationCircle;
import com.bawarchef.Containers.UserIdentity;
import com.bawarchef.android.Fragments.Cart;
import com.bawarchef.android.Fragments.MessageReceiver;
import com.bawarchef.android.Fragments.UHome;
import com.bawarchef.android.Fragments.UOrders;
import com.bawarchef.android.Fragments.UserProfile;
import com.bawarchef.android.Hierarchy.DataStructure.CartContainer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {

    DrawerLayout drawer;
    NavigationView navView;
    ImageButton menu_but,cartButton;
    ImageView profile_pic;
    TextView area_circ,navDrUName,navDrName;
    TextView cartcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        drawer = findViewById(R.id.drawer);
        menu_but = findViewById(R.id.menu_but);
        area_circ = findViewById(R.id.ar_circ);
        navView = findViewById(R.id.navView);
        cartButton = findViewById(R.id.cartB);

        cartButton.setOnClickListener(cartClicked);
        cartcount = findViewById(R.id.cart_no);

        CartContainer.ui_count = cartcount;

        View headerView = navView.getHeaderView(0);
        navDrName = (TextView) headerView.findViewById(R.id.nav_hdr_name);
        navDrUName = (TextView) headerView.findViewById(R.id.nav_hdr_uname);
        profile_pic = headerView.findViewById(R.id.profile_pic);

        UserIdentity ui = ThisApplication.currentUserProfile.getUserIdentity();
        navDrUName.setText(ThisApplication.currentUserProfile.getUserUName());
        navDrName.setText(ui.fname + " " + ui.lname);

        if(ui.dp!=null){
            profile_pic.setImageBitmap(BitmapFactory.decodeByteArray(ui.dp,0,ui.dp.length));
        }

        menu_but.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START,true));

        setFragment(navView.getMenu().getItem(0));
        navView.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            drawer.closeDrawer(GravityCompat.START,true);
            setFragment(item);
            return true;
        });
    }

    MobileClient.MessageProcessor defaultMessageProcessor = null;
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
                ThisApplication.currentUserProfile.setUserCircle(String.valueOf(nearest.getId()));
                ((MessageReceiver)activeFragment).process(new Message(Message.Direction.SERVER_TO_CLIENT,"LOCATION_CALLBACK"));
            }

            if(activeFragment!=null){
                ((MessageReceiver)activeFragment).process(m);
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        defaultMessageProcessor = ((ThisApplication)getApplication()).getMessageProcessor();
        ((ThisApplication)getApplication()).setMessageProcessor(activityMessageProcessor);
        ((ThisApplication)getApplication()).setCurrentContext(this);
        new Thread(() -> ((ThisApplication)getApplication()).startLocationUpdates(onLocationChange)).start();
    }

    public static Fragment activeFragment = null;

    private void setFragment(MenuItem item) {

        switch(item.getTitle().toString()){

            case "Home":
                activeFragment = new UHome();
                break;

            case "My Profile":
                activeFragment = new UserProfile();
                break;

            case "My Orders":
                activeFragment = new UOrders();
                break;

            case "Log out":
                try {
                    ((ThisApplication) getApplication()).mobileClient.closeConnection();
                }catch (Exception e){}
                ((ThisApplication) getApplication()).mobileClient = new MobileClient((ThisApplication) getApplication());
                ThisApplication.currentUserProfile = new CurrentUserProfile(getApplication());
                ((ThisApplication)getApplication()).setCryptoKey();

                getApplicationContext().getSharedPreferences("BawarChef_USER_AppData", 0).edit().clear().apply();
                Intent i = new Intent(DashboardUserActivity.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                break;

            case "Quit":
                finishAndRemoveTask();
                break;
        }
        if(activeFragment!=null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentViewPort,activeFragment)
                    .commit();
    }

    View.OnClickListener cartClicked = v -> {
        activeFragment = new Cart();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        activeFragment.setTargetFragment(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount()), 9999);
        ft.add(R.id.fragmentViewPort,activeFragment);
        ft.addToBackStack(null);
        ft.commit();;
    };

    @Override
    protected void onPause() {
        super.onPause();
        ((ThisApplication)getApplication()).setMessageProcessor(defaultMessageProcessor);
        ((ThisApplication)getApplication()).setCurrentContext(null);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()==0) {
            super.onBackPressed();
            try{
                if(getApplication() instanceof ThisApplication) {
                    ((ThisApplication) getApplication()).mobileClient.closeConnection();
                    ((ThisApplication) getApplication()).mobileClient.setDefaultCryptoKey();
                    ((ThisApplication) getApplication()).setCryptoKey();
                }

            }catch (Exception e){}
        }
        else{
            getSupportFragmentManager().popBackStack();
        }
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

    LocationEngine.OnLocationChange onLocationChange = new LocationEngine.OnLocationChange() {
        @Override
        public void onChange(LatLng l) {
            setLocationCircle(l);
        }
    };
}