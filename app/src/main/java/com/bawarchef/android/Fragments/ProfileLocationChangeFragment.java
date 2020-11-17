package com.bawarchef.android.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.app.Activity.RESULT_OK;

public class ProfileLocationChangeFragment extends Fragment implements OnMapReadyCallback {

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.profile_location_map, container, false);
        return v;
    }

    MapView mapView;
    ImageButton ok,back;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = v.findViewById(R.id.mapView);
        ok = v.findViewById(R.id.location_ok);
        back = v.findViewById(R.id.location_back);

        back.setOnClickListener(backC);
        ok.setOnClickListener(okC);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }

    View.OnClickListener backC = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), ProfileLocationChangeFragment.class);
            getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
            getFragmentManager().popBackStack();
        }
    };

    View.OnClickListener okC = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            profile.currentLocation = changableLocation;
            profile.refreshLocation();
            Intent intent = new Intent(getContext(), ProfileLocationChangeFragment.class);
            getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
            getFragmentManager().popBackStack();
        }
    };

    ProfileLocationChangeFragment(MyProfile profile){
        this.profile = profile;
    }

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

    MyProfile profile;
    LatLng changableLocation;

    GoogleMap gMap;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        LatLng l = profile.currentLocation;
        changableLocation = new LatLng(l.latitude,l.longitude);

        gMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        if(changableLocation!=null) {
            gMap.moveCamera(CameraUpdateFactory.newLatLng(changableLocation));
            MarkerOptions options = new MarkerOptions().flat(false).position(changableLocation).draggable(false);
            gMap.addMarker(options);
        }

        gMap.setOnMapClickListener(mapclicked);
    }

    GoogleMap.OnMapClickListener mapclicked = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            changableLocation = latLng;
            gMap.clear();
            MarkerOptions options = new MarkerOptions().flat(false).position(changableLocation).draggable(false);
            gMap.addMarker(options);
        }
    };

}
