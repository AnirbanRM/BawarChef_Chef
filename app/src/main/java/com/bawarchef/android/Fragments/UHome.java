package com.bawarchef.android.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefAdvertMinorContainer;
import com.bawarchef.android.CurrentUserProfile;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class UHome extends Fragment implements MessageReceiver, OnMapReadyCallback {

    View v;

    ArrayList<ChefAdvertMinorContainer> chefs;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_uhome,container,false);
        return v;
    }

    BottomNavigationView navigationView;
    RecyclerView recyclerView;
    MapView mapView;
    ImageView dp;
    TextView name,rating;
    View map_ind_v;
    FrameLayout map,list;

    RecyclerViewChefAdapter recyclerViewChefAdapter;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = v.findViewById(R.id.map_view);
        list = v.findViewById(R.id.list_view);

        chefs = new ArrayList<ChefAdvertMinorContainer>();

        navigationView = v.findViewById(R.id.list_map_nav);
        recyclerView = v.findViewById(R.id.chefList);

        navigationView.setOnNavigationItemSelectedListener(navChanged);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(recyMngr);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerViewChefAdapter = new RecyclerViewChefAdapter(this);
        recyclerView.setAdapter(recyclerViewChefAdapter);

        mapView = v.findViewById(R.id.chef_map);
        dp = v.findViewById(R.id.dp);
        name = v.findViewById(R.id.name);
        rating = v.findViewById(R.id.rating);
        map_ind_v = v.findViewById(R.id.map_ind_det);

        map_ind_v.setOnClickListener(map_ind_v_clicked);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        recyclerViewChefAdapter.notifyDataSetChanged();

        getData();
        list.setVisibility(View.VISIBLE);
    }

    View.OnClickListener map_ind_v_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToChef(((ChefAdvertMinorContainer)selectedMarker.getTag()).getChefID());
        }
    };

    BottomNavigationView.OnNavigationItemSelectedListener navChanged = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(item.getItemId()==R.id.uMap){
                map.setVisibility(View.VISIBLE);
                list.setVisibility(View.INVISIBLE);
            }
            else if(item.getItemId()==R.id.uList){
                map.setVisibility(View.INVISIBLE);
                list.setVisibility(View.VISIBLE);
            }
            return true;
        }
    };

    Fragment activeFragment = null;
    public void goToChef(String id){
        activeFragment = new UserChefView(id);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        activeFragment.setTargetFragment(UHome.this,9999);
        ft.add(R.id.fragmentViewPort,activeFragment);
        ft.addToBackStack(null);

        ft.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode==9999){
                activeFragment=null;
            }
        }
    }

    private void getData(){
        if(ThisApplication.currentUserProfile.getUserCircle()==null)return;

        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"FETCH_CHEF");
        newm.putProperty("CIRCLE", ThisApplication.currentUserProfile.getUserCircle());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
                    AsyncExecutor executor = new AsyncExecutor();
                    executor.execute(ep);
                }catch (Exception e){}
            }
        });
    }

    @Override
    public void process(Message m) {

        if(m.getMsg_type().equals("LOCATION_CALLBACK") && activeFragment==null){
            getData();
        }

        else if(m.getMsg_type().equals("FETCH_CHEF_RESULT")){
            recyclerViewChefAdapter.chefs = (ArrayList<ChefAdvertMinorContainer>) m.getProperty("CHEFS");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(dialog!=null && dialog.isShowing()){
                        dialog.dismiss();
                        dialog = null;
                    }
                    recyclerViewChefAdapter.notifyDataSetChanged();
                    refreshMap();
                }
            });
        }

        else if(activeFragment!=null){
            ((MessageReceiver)activeFragment).process(m);
        }

    }

    Marker selectedMarker=null;
    private void refreshMap(){

        gMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        for(ChefAdvertMinorContainer c : recyclerViewChefAdapter.chefs){
            gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(c.getLocation().getLat(),c.getLocation().getLng())));
            MarkerOptions options = new MarkerOptions().flat(false).position(new LatLng(c.getLocation().getLat(),c.getLocation().getLng())).draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            gMap.addMarker(options).setTag(c);

            gMap.setOnMarkerClickListener(marker -> {
                selectedMarker = marker;
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                ChefAdvertMinorContainer c1 = (ChefAdvertMinorContainer) marker.getTag();
                map_ind_v.setVisibility(View.VISIBLE);
                name.setText(c1.getfName()+" "+ c1.getlName());
                if(c1.getDp()!=null){
                    byte [] encodeByte= Base64.decode(c1.getDp(),Base64.DEFAULT);
                    InputStream inputStream  = new ByteArrayInputStream(encodeByte);
                    Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
                    dp.setImageBitmap(bitmap);
                    dp.setImageTintList(null);
                }
                rating.setText(String.format("%.1f", c1.getRating()));

                return false;
            });

            gMap.setOnMapClickListener(latLng -> {
                if(selectedMarker!=null){
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }

                name.setText("null");
                rating.setText("null");
                dp.setImageBitmap(null);
                dp.setImageDrawable(getResources().getDrawable(R.drawable.person_black,null));
                dp.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color,null)));
                map_ind_v.setVisibility(View.INVISIBLE);
            });
        }
    }

    private ProgressDialog dialog;

    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void>{

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Looking for nearby chefs... Please wait !");
            dialog.show();
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    //---------------------------------------------------MAP------------------------------------------------------

    GoogleMap gMap=null;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        gMap.getUiSettings().setMapToolbarEnabled(false);
    }

    //------------------------------------------------------------------------------------------------------------
}

class RecyclerViewChefAdapter extends RecyclerView.Adapter<RecyclerViewChefAdapter.ViewHolder>{

    public ArrayList<ChefAdvertMinorContainer> chefs=new ArrayList<ChefAdvertMinorContainer>();
    UHome uHome;
    RecyclerViewChefAdapter(UHome uHome){
        this.uHome = uHome;
    }

    @NonNull
    @Override
    public RecyclerViewChefAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.chef_list_item_design,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewChefAdapter.ViewHolder holder, int position) {
        ChefAdvertMinorContainer chef = chefs.get(position);
        holder.name.setText(chef.getfName()+" "+chef.getlName());
        if(chef.getDp()!=null){
            byte [] encodeByte= Base64.decode(chef.getDp(),Base64.DEFAULT);
            InputStream inputStream  = new ByteArrayInputStream(encodeByte);
            Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
            holder.dp.setImageBitmap(bitmap);
            holder.dp.setImageTintList(null);
        }
        holder.rating.setText(String.format("%.1f",chef.getRating()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uHome.goToChef(chef.getChefID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chefs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView dp;
        TextView name;
        TextView rating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}
