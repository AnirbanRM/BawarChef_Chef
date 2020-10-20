package com.bawarchef.android.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class UHome extends Fragment implements MessageReceiver{

    View v;

    ArrayList<ChefAdvertMinorContainer> chefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_uhome,container,false);
        return v;
    }

    BottomNavigationView navigationView;
    RecyclerView recyclerView;

    RecyclerViewChefAdapter recyclerViewChefAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chefs = new ArrayList<ChefAdvertMinorContainer>();

        navigationView = v.findViewById(R.id.list_map_nav);
        recyclerView = v.findViewById(R.id.chefList);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(recyMngr);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerViewChefAdapter = new RecyclerViewChefAdapter(chefs);

        recyclerView.setAdapter(recyclerViewChefAdapter);

        recyclerViewChefAdapter.notifyDataSetChanged();
    }

    @Override
    public void process(Message m) {

        if(m.getMsg_type().equals("LOCATION_CALLBACK")){
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
                }
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
}

class RecyclerViewChefAdapter extends RecyclerView.Adapter<RecyclerViewChefAdapter.ViewHolder>{

    ArrayList<ChefAdvertMinorContainer> chefs;
    RecyclerViewChefAdapter(ArrayList<ChefAdvertMinorContainer> chefAdvertMinorContainers){
        this.chefs = chefAdvertMinorContainers;
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
