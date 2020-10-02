package com.bawarchef.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefIdentity;
import com.bawarchef.Containers.UserIdentity;
import com.bawarchef.android.Fragments.Broadcasts;
import com.bawarchef.android.Fragments.FoodMenu;
import com.bawarchef.android.Fragments.History;
import com.bawarchef.android.Fragments.Home;
import com.bawarchef.android.Fragments.MyProfile;
import com.bawarchef.android.Fragments.Orders;
import com.bawarchef.android.Fragments.PersonalDetails;
import com.bawarchef.android.Fragments.Preferences;
import com.bawarchef.android.Fragments.UHome;
import com.bawarchef.android.Fragments.UMyProfile;
import com.bawarchef.android.Fragments.UOrders;
import com.bawarchef.android.Fragments.UPreferences;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

public class DashboardUserActivity extends AppCompatActivity {

    DrawerLayout drawer;
    NavigationView navView;
    ImageButton menu_but;
    TextView area_circ,navDrUName,navDrName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        new Thread(() -> ((ThisApplication)getApplication()).startLocationUpdates(onLocationChange)).start();

        drawer = findViewById(R.id.drawer);
        menu_but = findViewById(R.id.menu_but);
        area_circ = findViewById(R.id.ar_circ);
        navView = findViewById(R.id.navView);

        View headerView = navView.getHeaderView(0);
        navDrName = (TextView) headerView.findViewById(R.id.nav_hdr_name);
        navDrUName = (TextView) headerView.findViewById(R.id.nav_hdr_uname);

        UserIdentity ui = ThisApplication.currentUserProfile.getUserIdentity();
        navDrUName.setText(ThisApplication.currentUserProfile.getUserUName());
        navDrName.setText(ui.fname + " " + ui.lname);

        menu_but.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START,true));

        setFragment(navView.getMenu().getItem(0));
        navView.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            drawer.closeDrawer(GravityCompat.START,true);
            setFragment(item);
            return true;
        });

    }

    private void setFragment(MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch(item.getTitle().toString()){

            case "Home":
                fragment = new UHome();
                break;

            case "My Profile":
                fragment = new UMyProfile();
                break;

            case "My Orders":
                fragment = new UOrders();
                break;

            case "Preferences":
                fragment = new UPreferences();
                break;

            case "Quit":
                finishAndRemoveTask();
                break;
        }
        if(fragment!=null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentViewPort,fragment)
                    .commit();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()==0)
            super.onBackPressed();
        else{
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount()-1).onResume();
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