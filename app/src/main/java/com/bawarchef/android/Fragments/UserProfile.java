package com.bawarchef.android.Fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefProfileContainer;
import com.bawarchef.Containers.UserIdentity;
import com.bawarchef.android.ChefPDetailsActivity;
import com.bawarchef.android.DP_opt_dialog;
import com.bawarchef.android.DashboardUserActivity;
import com.bawarchef.android.ImagePicker;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class UserProfile extends Fragment implements OnMapReadyCallback,MessageReceiver {

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_profile_fragment, container, false);
        return v;
    }

    ImageButton mod_DP,profile_upd_button;
    EditText uname,resAddr,resCity,resState,resPincode,mob,email,fname,lname;
    ImageView dp_box;
    MapView mapView;
    TextView dobbox;
    ImagePicker picker;

    Bitmap dp = null;
    LatLng currentLocation=null;

    ImageView maleB,femaleB;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dp_box = view.findViewById(R.id.profile_dp);
        mod_DP = view.findViewById(R.id.edit_dp);
        fname = view.findViewById(R.id.fnameBox);
        lname = view.findViewById(R.id.lnameBox);
        uname = view.findViewById(R.id.usernameBox);
        dobbox = view.findViewById(R.id.dobbox);
        maleB = view.findViewById(R.id.malebut);
        femaleB = view.findViewById(R.id.femalebut);
        resAddr = view.findViewById(R.id.resaddrbox);
        resCity = view.findViewById(R.id.rescitybox);
        resState = view.findViewById(R.id.resstatebox);
        resPincode = view.findViewById(R.id.respinbox);
        mob = view.findViewById(R.id.mobbox);
        email = view.findViewById(R.id.emailbox);
        mapView = view.findViewById(R.id.profile_location_image);
        profile_upd_button = view.findViewById(R.id.profile_upd_but);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        profile_upd_button.setOnClickListener(updateProfile);

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        mod_DP.setOnClickListener(dp_change);
        maleB.setOnClickListener(genderchange);
        femaleB.setOnClickListener(genderchange);
        dobbox.setOnClickListener(calendar);

        setInfo();
    }

    View.OnClickListener updateProfile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserIdentity ui = new UserIdentity();
            ui.userID = uname.getText().toString();
            ui.gender = String.valueOf(gender);
            ui.addr.address = resAddr.getText().toString();
            ui.addr.city = resCity.getText().toString();
            ui.addr.pinNo = resPincode.getText().toString();
            ui.addr.state = resState.getText().toString();
            if(!dobbox.getText().toString().equals("YYYY/MM/DD")&&dobbox.getText().toString().length()!=0) {
                String[] datecomponenets = dobbox.getText().toString().split("/");
                ui.dob = datecomponenets[2] + "-" + datecomponenets[1] + "-" + datecomponenets[0];
            }
            ui.email = email.getText().toString();
            ui.mob = mob.getText().toString();
            ui.lati = currentLocation.latitude;
            ui.longi = currentLocation.longitude;
            ui.fname = fname.getText().toString();
            ui.lname = lname.getText().toString();
            if(dp!=null){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                dp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                ui.dp = stream.toByteArray();
            }

            Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"UPD_PROFILE_USER");
            m.putProperty("DATA", ui);

            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                UpdateProfileAsyncTask updateProfileAsyncTask = new UpdateProfileAsyncTask();
                updateProfileAsyncTask.execute(ep);
            }catch (Exception e){}
        }
    };

    View.OnClickListener calendar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    dobbox.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                }
            },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        }
    };

    char gender = '\0';
    View.OnClickListener genderchange = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            maleB.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightgray,null)));
            femaleB.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightgray,null)));
            ((ImageView)v).setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color,null)));
            if(v.getId()==maleB.getId())
                gender = 'M';
            else if(v.getId()==femaleB.getId())
                gender = 'F';
        }
    };

    private void setInfo(){
        UserIdentity ui = ThisApplication.currentUserProfile.getUserIdentity();
        uname.setText(ThisApplication.currentUserProfile.getUserUName());
        fname.setText(ui.fname);
        lname.setText(ui.lname);
        dobbox.setText(ui.dob);
        if(ui.gender!=null) {
            if (ui.gender.equals("M")) {
                maleB.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color, null)));
                gender = 'M';
            }
            if (ui.gender.equals("F")) {
                femaleB.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color, null)));
                gender = 'F';
            }
        }
        resAddr.setText(ui.addr.address);
        resCity.setText(ui.addr.city);
        resState.setText(ui.addr.state);
        resPincode.setText(ui.addr.pinNo);
        mob.setText(ui.mob);
        email.setText(ui.email);
        if(ui.dp!=null && ui.dp.length!=0) {
            dp = BitmapFactory.decodeByteArray(ui.dp, 0, ui.dp.length);
            dp_box.setImageBitmap(dp);
        }
    }

    View.OnClickListener dp_change = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getActivity(), DP_opt_dialog.class);
            startActivityForResult(i, 1000);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==9999){
            //refreshLocation();
        }

        else if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getStringExtra("ACTION").equals("CAM")) {
                        picker = new ImagePicker(ImagePicker.Source.CAMERA, UserProfile.this, 100,1,1);
                        picker.fetchImageByRequestCode();
                    }
                    if (data.getStringExtra("ACTION").equals("STO")) {
                        picker = new ImagePicker(ImagePicker.Source.INTERNAL, UserProfile.this, 200,1,1);
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
                dp_box.setImageBitmap(img);
                dp = img;
            }catch(Exception e){}
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("UPD_PROFILE_RESP")){
            ThisApplication.currentUserProfile.setUserIdentity((UserIdentity) m.getProperty("DATA"));
            getActivity().runOnUiThread(()->{
                if(dialog!=null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                Toast.makeText(getActivity(),"Updated Succcessfully",Toast.LENGTH_SHORT).show();
                ThisApplication.currentUserProfile.setChefUName(uname.getText().toString());
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

        gMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        if(latLng!=null) {
            if(currentLocation==null)
                currentLocation = latLng;
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            MarkerOptions options = new MarkerOptions().flat(false).position(latLng).draggable(false);
            gMap.addMarker(options);
        }
    }
//------------------------------------------------------------------------------------------------------------------

    private ProgressDialog dialog;

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
