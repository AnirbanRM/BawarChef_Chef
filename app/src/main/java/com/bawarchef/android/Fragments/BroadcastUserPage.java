package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.BroadcastItemUser;
import com.bawarchef.Containers.BroadcastReply;
import com.bawarchef.android.DashboardUserActivity;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;

public class BroadcastUserPage extends DialogFragment implements MessageReceiver{

    View v;

    BroadcastUserPage(){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_broadcast_page,container,false);
        return v;
    }

    EditText msg;
    ImageButton submit;
    RecyclerView actBCastList;
    BroadcastAdapter bcastAdapter;

    ArrayList<BroadcastItemUser> items = new ArrayList<BroadcastItemUser>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        msg = view.findViewById(R.id.bmsgbox);
        submit = view.findViewById(R.id.send);
        actBCastList = view.findViewById(R.id.activeBCastList);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        actBCastList.setLayoutManager(recyMngr);
        actBCastList.setItemAnimator(new DefaultItemAnimator());
        bcastAdapter = new BroadcastAdapter();
        actBCastList.setAdapter(bcastAdapter);

        submit.setOnClickListener(submitclicked);

        DashboardUserActivity.activeFragment = BroadcastUserPage.this;
        Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"FETCH_BCAST");
        m.putProperty("CIRCLE",ThisApplication.currentUserProfile.getUserCircle());

        try{
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor("Fetching active broadcasts... Please wait !");
            executor.execute(ep);
        }catch (Exception e){}
    }

    View.OnClickListener submitclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(msg.getText().toString().length()==0)return;

            Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"NEW_BCAST");
            m.putProperty("MSG",msg.getText().toString());
            m.putProperty("CIRCLE",ThisApplication.currentUserProfile.getUserCircle());

            try{
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
                AsyncExecutor executor = new AsyncExecutor("Broadcasting... Please wait !");
                executor.execute(ep);
            }catch (Exception e){}
        }
    };

    private ProgressDialog dialog;

    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        String msg;
        AsyncExecutor(String msg){
            this.msg = msg;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(msg);
            dialog.show();
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void process(Message m) {

        if(m.getMsg_type().equals("NEW_BCAST_REPLY")){
            getActivity().runOnUiThread(()->{
                if(dialog!=null&&dialog.isShowing()){
                    dialog.dismiss();
                    dialog = null;
                }
                Toast.makeText(getActivity(),"Broadcast successful !",Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }

        else if(m.getMsg_type().equals("BCAST_RESP")){
            items = (ArrayList<BroadcastItemUser>) m.getProperty("DATA");
            getActivity().runOnUiThread(()->{
                if(dialog!=null&&dialog.isShowing()){
                    dialog.dismiss();
                    dialog = null;
                }
                bcastAdapter.notifyDataSetChanged();
            });
        }
    }

    class BroadcastAdapter extends RecyclerView.Adapter<BroadcastAdapter.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.user_broadcast_list_item,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            holder.msg.setText(items.get(position).message);

            LinearLayoutManager recyMngr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            holder.chefs.setLayoutManager(recyMngr);
            holder.chefs.setItemAnimator(new DefaultItemAnimator());
            BroadcastChefsAdapter broadcastChefsAdapter = new BroadcastChefsAdapter(items.get(position).broadcast_replies);
            holder.chefs.setAdapter(broadcastChefsAdapter);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView msg;
            RecyclerView chefs;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                msg = itemView.findViewById(R.id.msgbox);
                chefs = itemView.findViewById(R.id.chefs);
            }
        }
    }

    class BroadcastChefsAdapter extends RecyclerView.Adapter<BroadcastChefsAdapter.ViewHolder>{

        ArrayList<BroadcastReply> replies = new ArrayList<BroadcastReply>();

        BroadcastChefsAdapter(ArrayList<BroadcastReply> replies){
            this.replies = replies;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.user_bcast_chef_msg_item_design,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            holder.chef_name.setText(replies.get(position).chefName);
            holder.chef_msg.setText(replies.get(position).message);
            holder.chef_name.setOnClickListener(v -> {
                DashboardUserActivity.activeFragment = new UserChefView(replies.get(position).chefID);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                DashboardUserActivity.activeFragment.setTargetFragment(BroadcastUserPage.this,9999);
                ft.add(R.id.fragmentViewPort,DashboardUserActivity.activeFragment);
                ft.addToBackStack(null);
                ft.commit();

                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return replies.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView chef_name,chef_msg;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                chef_name = itemView.findViewById(R.id.chef_name);
                chef_msg = itemView.findViewById(R.id.chef_msg);
            }
        }
    }



}

