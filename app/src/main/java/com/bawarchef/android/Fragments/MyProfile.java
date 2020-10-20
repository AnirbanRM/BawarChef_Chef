package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.bawarchef.Containers.ProfileContainer;
import com.bawarchef.android.DP_opt_dialog;
import com.bawarchef.android.ImagePicker;
import com.bawarchef.android.R;
import com.bawarchef.android.SpecialityCardActivity;
import com.bawarchef.android.SpecialityItem;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class MyProfile extends Fragment implements OnMapReadyCallback,MessageReceiver {

    View v;

    private enum PictureDestination{X,DP,FoodPicture};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.profile, container, false);
        return v;
    }

    RecyclerView speciality_listV;
    ArrayList<SpecialityItem> specialityItems;
    ImageButton mod_DP,profile_upd_button;
    EditText uname;
    TextView name;
    EditText bio;
    ImageView dp_box;
    Button addPhoto;
    TextView changeLoc;
    PictureDestination pictureDestination = PictureDestination.X;
    MapView mapView;

    ImagePicker picker;
    GridView photosGrid;
    SeekBar mapZoom;

    ArrayList<Bitmap> foodPhotos;

    TextView photosOverlay;

    Bitmap dp = null;
    LatLng currentLocation=null;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        speciality_listV = v.findViewById(R.id.sp_recy_list);
        mod_DP = v.findViewById(R.id.edit_dp);
        mod_DP.setOnClickListener(dp_change);

        uname = v.findViewById(R.id.profile_un);
        name = v.findViewById(R.id.profile_n);
        bio = v.findViewById(R.id.profile_bio_box);
        dp_box = v.findViewById(R.id.profile_dp);
        photosGrid = v.findViewById(R.id.photoview);
        photosOverlay = v.findViewById(R.id.noPhotoOverlay);
        addPhoto = v.findViewById(R.id.addPhoto);
        mapView = v.findViewById(R.id.profile_location_image);
        mapZoom = v.findViewById(R.id.zoomseek);
        changeLoc = v.findViewById(R.id.chgLoc);
        mapZoom.setOnSeekBarChangeListener(zoomchanged);

        profile_upd_button = v.findViewById(R.id.profile_upd_but);
        profile_upd_button.setOnClickListener(upd_profile);

        changeLoc.setOnClickListener(changeLocation);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        addPhoto.setOnClickListener(addPhotoclicked);

        uname.setText(ThisApplication.currentUserProfile.getChefUName());
        name.setText(ThisApplication.currentUserProfile.getChefIdentity().fname + " " + ThisApplication.currentUserProfile.getChefIdentity().lname);

        specialityItems = new ArrayList<SpecialityItem>();
        specialityItems.add(SpecialityItem.getAdder("Add new highlight"));

        foodPhotos = new ArrayList<Bitmap>();
        PhotosGridAdapter photosGridAdapter = new PhotosGridAdapter();
        photosGrid.setAdapter(photosGridAdapter);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.HORIZONTAL, false);

        speciality_listV.setLayoutManager(recyMngr);
        speciality_listV.setItemAnimator(new DefaultItemAnimator());

        SpecialityRecyclerAdapter specialityRecyclerAdapter = new SpecialityRecyclerAdapter();
        speciality_listV.setAdapter(specialityRecyclerAdapter);

        Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"PROFILE_FETCH");
        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m),((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
            FetchProfileAsyncTask fetchProfileAsyncTask = new FetchProfileAsyncTask();
            fetchProfileAsyncTask.execute(ep);
        }catch (Exception e){}
    }

    View.OnClickListener upd_profile = v -> {
        ProfileContainer profileContainer = new ProfileContainer();
        if(dp!=null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            dp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            profileContainer.dp = stream.toByteArray();
        }
        profileContainer.bio = bio.getText().toString();
        profileContainer.resiLat = (float) currentLocation.latitude;
        profileContainer.resiLng = (float) currentLocation.longitude;
        profileContainer.uName = uname.getText().toString();

        Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"UPD_PROFILE_CHEF");
        m.putProperty("DATA",profileContainer);

        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            UpdateProfileAsyncTask updateProfileAsyncTask = new UpdateProfileAsyncTask();
            updateProfileAsyncTask.execute(ep);
        }catch (Exception e){}
    };

    View.OnClickListener changeLocation = v -> {

        Fragment fragment = new ProfileLocationChangeFragment(MyProfile.this);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        fragment.setTargetFragment(MyProfile.this, 9999);
        ft.add(R.id.fragmentViewPort, fragment);
        ft.addToBackStack(null);
        ft.commit();
    };

    SeekBar.OnSeekBarChangeListener zoomchanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float zoom = ((20-0)*progress/100);
            gMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            gMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    View.OnClickListener addPhotoclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pictureDestination=PictureDestination.FoodPicture;
            Intent i = new Intent(getActivity(), DP_opt_dialog.class);
            startActivityForResult(i, 1000);
        }
    };

    View.OnClickListener dp_change = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pictureDestination=PictureDestination.DP;
            Intent i = new Intent(getActivity(), DP_opt_dialog.class);
            startActivityForResult(i, 1000);
        }
    };

    void refreshLocation(){
        if(gMap!=null) {
            gMap.clear();
            gMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            MarkerOptions options = new MarkerOptions().flat(false).position(currentLocation).draggable(false);
            gMap.addMarker(options);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==9999){
            refreshLocation();
        }

        else if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getStringExtra("ACTION").equals("CAM")) {
                        picker = new ImagePicker(ImagePicker.Source.CAMERA, MyProfile.this, 100,1,1);
                        picker.fetchImageByRequestCode();
                    }
                    if (data.getStringExtra("ACTION").equals("STO")) {
                        picker = new ImagePicker(ImagePicker.Source.INTERNAL, MyProfile.this, 200,1,1);
                        picker.fetchImageByRequestCode();
                    }
                    if (data.getStringExtra("ACTION").equals("REM")) {
                        dp_box.setImageBitmap(null);
                        dp_box.setImageResource(R.drawable.person);
                        dp = null;
                    }
                }
            }
        }

        else if(requestCode==2000){

            SpecialityItem si = (SpecialityItem) ThisApplication.sharableObject.get(0);
            if(si.getBmp()==null||si.getCaption()=="");
            else{
                specialityItems.add(0,si);
                speciality_listV.removeAllViews();
                speciality_listV.getAdapter().notifyDataSetChanged();
            }
            ThisApplication.sharableObject.clear();
        }

        else if (requestCode == 100) {
            picker.fromCamera();
        }
        else if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    picker.fromInternal(data);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            try {
                Bitmap img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), UCrop.getOutput(data));
                if(pictureDestination==PictureDestination.DP) {
                    dp_box.setImageBitmap(img);
                    dp = img;
                }
                else if(pictureDestination==PictureDestination.FoodPicture)
                    addToPhotos(img);
            }catch(Exception e){}
            pictureDestination = PictureDestination.X;
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addToPhotos(Bitmap img) {
        foodPhotos.add(img);
        ((PhotosGridAdapter)photosGrid.getAdapter()).notifyDataSetChanged();
    }

    private class SpecialityRecyclerAdapter extends RecyclerView.Adapter<SpecialityRecyclerAdapter.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.speciality_card_design,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(specialityItems.get(position).getCaption());
            holder.imageView.setImageBitmap(specialityItems.get(position).getBmp());
            if(specialityItems.get(position).isAdder()){
                holder.textView.setTextColor(Color.parseColor("#55000000"));
                holder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.add,null));
                holder.imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#55000000")));
                holder.imageView.setScaleX(0.6f);
                holder.imageView.setScaleY(0.6f);
                holder.itemView.setOnClickListener(adderClicked);
            }
        }

        View.OnClickListener adderClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThisApplication.sharableObject.add(new SpecialityItem((Bitmap) null,""));
                Intent i = new Intent(MyProfile.this.getActivity(), SpecialityCardActivity.class);
                startActivityForResult(i, 2000);
            }
        };

        @Override
        public int getItemCount() {
            return specialityItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ImageView imageView;
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.sp_img);
                textView = itemView.findViewById(R.id.sp_text);
            }
        }
    }

    private class PhotosGridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return foodPhotos.size();
        }

        @Override
        public Object getItem(int position) {
            return foodPhotos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.gri_view_photo_item,null,false);
            ImageView i = v.findViewById(R.id.foodPicture);

            i.setImageBitmap((Bitmap)getItem(position));
            return v;
        }
    }

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("UPD_PROFILE_RESP")){
            getActivity().runOnUiThread(()->{
                if(dialog!=null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                Toast.makeText(getActivity(),"Updated Succcessfully",Toast.LENGTH_SHORT).show();
                ThisApplication.currentUserProfile.setChefUName(uname.getText().toString());
            });
        }
        else if(m.getMsg_type().equals("PROFILE_FETCH_RESP")){
            ProfileContainer profileContainer = (ProfileContainer) m.getProperty("DATA");
            getActivity().runOnUiThread(()->{
                if(profileContainer.bio!=null)
                    bio.setText(profileContainer.bio);
                if(profileContainer.resiLng!=0&& profileContainer.resiLat!=0) {
                    currentLocation = new LatLng(profileContainer.resiLat, profileContainer.resiLng);
                    refreshLocation();
                }
                if(profileContainer.dp!=null && profileContainer.dp.length!=0) {
                    dp = BitmapFactory.decodeByteArray(profileContainer.dp, 0, profileContainer.dp.length);
                    dp_box.setImageBitmap(dp);
                }
                if(dialog!=null&& dialog.isShowing())
                    dialog.dismiss();
            });
        }

    }

//------------------------------------------MAP------------------------------------------------

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
        LatLng latLng = ((ThisApplication)getActivity().getApplication()).getLocation();
        gMap.getUiSettings().setAllGesturesEnabled(false);

        gMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        if(latLng!=null) {
            if(currentLocation==null)
                currentLocation = latLng;
            mapZoom.setProgress((int)(100*((float)18/20)));
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            MarkerOptions options = new MarkerOptions().flat(false).position(latLng).draggable(false);
            gMap.addMarker(options);
        }
    }


    private ProgressDialog dialog;

    class FetchProfileAsyncTask extends AsyncTask<EncryptedPayload,Void,Void>{

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Fetching your profile. Please wait !");
            dialog.show();
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    class UpdateProfileAsyncTask extends AsyncTask<EncryptedPayload,Void,Void>{

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Updating your profile. Please wait !");
            dialog.show();
        }

        public UpdateProfileAsyncTask() {
            dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

}
