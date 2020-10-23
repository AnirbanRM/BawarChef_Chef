package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefAdvertMajorContainer;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class UserChefView extends Fragment implements MessageReceiver {

    View v;
    String chefID;

    public UserChefView(String id) {
        this.chefID = id;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_chefprofile,container,false);
        return v;
    }

    ImageView dp;
    TextView rating,name,bio;
    ConstraintLayout body;
    Button bookButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dp = view.findViewById(R.id.dp);
        rating = view.findViewById(R.id.rating);
        name = view.findViewById(R.id.name);
        bio = view.findViewById(R.id.bio);
        body = view.findViewById(R.id.body);
        body.setVisibility(View.INVISIBLE);
        bookButton = view.findViewById(R.id.bookButton);

        bookButton.setOnClickListener(book_Clicked);


        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"FETCH_CHEF_IND");
        newm.putProperty("chefID", chefID);

        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor();
            executor.execute(ep);
        }catch (Exception e){}
    }

    Fragment activeFragment = null;
    Bitmap user_dp=null;
    View.OnClickListener book_Clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activeFragment = new UserChefMenu(chefID,name.getText().toString(),user_dp);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            activeFragment.setTargetFragment(UserChefView.this,9999);
            ft.add(R.id.fragmentViewPort,activeFragment);
            ft.addToBackStack(null);

            ft.commit();
        }
    };

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_IND_CHEF")){
            ChefAdvertMajorContainer chef = (ChefAdvertMajorContainer) m.getProperty("CHEF");

            getActivity().runOnUiThread(() -> {
                name.setText(chef.getfName()+" "+ chef.getlName());
                if(chef.getDp()!=null){
                    byte [] encodeByte= Base64.decode(chef.getDp(),Base64.DEFAULT);
                    InputStream inputStream  = new ByteArrayInputStream(encodeByte);
                    user_dp  = BitmapFactory.decodeStream(inputStream);
                    dp.setImageBitmap(user_dp);
                    dp.setImageTintList(null);
                }
                rating.setText(String.format("%.1f", chef.getRating()));
                bio.setText(chef.getBio());
                body.setVisibility(View.VISIBLE);
                if(dialog!=null&& dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
            });
        }

        else if(activeFragment!=null){
            ((MessageReceiver)activeFragment).process(m);
        }
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

    private ProgressDialog dialog;
    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading... Please wait !");
            dialog.show();
        }


        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }
}
