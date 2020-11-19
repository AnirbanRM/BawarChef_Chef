package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.bawarchef.Containers.BroadcastItemContainer;
import com.bawarchef.android.DashboardActivity;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;

public class Broadcasts extends Fragment implements MessageReceiver {

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.broadcasts,container,false);
        return v;
    }

    RecyclerView recylist;
    BroadcastListAdapter blistAdapter;
    ArrayList<BroadcastItemContainer> broadcastItemContainers= new ArrayList<BroadcastItemContainer>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recylist = v.findViewById(R.id.bcastrecylist);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        recylist.setLayoutManager(recyMngr);
        recylist.setItemAnimator(new DefaultItemAnimator());
        blistAdapter = new BroadcastListAdapter();
        recylist.setAdapter(blistAdapter);

        fetch();
    }

    private void fetch(){
        DashboardActivity.activeFragment = this;
        try {
            Message newm = new Message(Message.Direction.CLIENT_TO_SERVER, "FETCH_BROADCASTS");
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor();
            executor.execute(ep);
        }catch (Exception e){}
    }

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_BROADCASTS")){
            broadcastItemContainers = (ArrayList<BroadcastItemContainer>)m.getProperty("BROADCASTS");
            getActivity().runOnUiThread(()->{
                if(dialog!=null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                blistAdapter.notifyDataSetChanged();
            });
        }

        if(m.getMsg_type().equals("BROADCAST_REPLY_RESP")){
            if(m.getProperty("RESULT").equals("SUCCESS")){
                getActivity().runOnUiThread(()->{
                    if(dialog!=null && dialog.isShowing()) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    fetch();
                });
            }
        }
    }

    private ProgressDialog dialog;

    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Fetching broadcasts... Please wait !");
            dialog.show();
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    class BroadcastListAdapter extends RecyclerView.Adapter<BroadcastListAdapter.ViewHolder>{


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.chef_bcast_item_design,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BroadcastItemContainer bic = broadcastItemContainers.get(position);
            holder.msgbox.setText(bic.message);

            holder.send.setOnClickListener(v -> {
                Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"BROADCAST_REPLY");
                m.putProperty("ID",bic.id);
                m.putProperty("NAME",ThisApplication.currentUserProfile.getChefIdentity().fname+" "+ThisApplication.currentUserProfile.getChefIdentity().lname);
                m.putProperty("MSG",holder.chef_input.getText().toString());
                try{
                    EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                    AsyncExecutor executor = new AsyncExecutor();
                    executor.execute(ep);
                }catch (Exception e){}
            });
        }

        @Override
        public int getItemCount() {
            return broadcastItemContainers.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            TextView msgbox;
            EditText chef_input;
            ImageButton send;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                msgbox = itemView.findViewById(R.id.msgbox);
                chef_input = itemView.findViewById(R.id.chef_input);
                send = itemView.findViewById(R.id.send);
            }
        }
    }
}
