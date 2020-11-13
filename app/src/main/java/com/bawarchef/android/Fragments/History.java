package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.bawarchef.Containers.ChefOrderListItemClass;
import com.bawarchef.android.DashboardUserActivity;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;

public class History extends Fragment implements MessageReceiver{

    View v;

    RecyclerView list;
    RecyclerListAdapter listAdapter;
    ArrayList<ChefOrderListItemClass> orders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.history,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.list);

        orders = new ArrayList<ChefOrderListItemClass>();

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(recyMngr);
        list.setItemAnimator(new DefaultItemAnimator());
        listAdapter = new RecyclerListAdapter();
        list.setAdapter(listAdapter);

        Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"ORDER_FETCH_PAST");
        try{
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor();
            executor.execute(ep);
        }catch (Exception e){}

    }



    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_ORDER")){
            orders = (ArrayList<ChefOrderListItemClass>) m.getProperty("Orders");
            getActivity().runOnUiThread(() -> {
                if(dialog!=null && dialog.isShowing()){
                    dialog.dismiss();
                    dialog = null;
                }
                listAdapter.notifyDataSetChanged();
            });
        }

    }


    private ProgressDialog dialog;

    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Fetching present orders... Please wait !");
            dialog.show();
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ViewHolder>{


        @NonNull
        @Override
        public RecyclerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
            return new RecyclerListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerListAdapter.ViewHolder holder, int position) {
            ChefOrderListItemClass order = orders.get(position);
            holder.orderID.setText(order.orderiD);

            if(order.userDP!=null && order.userDP.length!=0) {
                holder.dp.setImageBitmap(BitmapFactory.decodeByteArray(order.userDP, 0, order.userDP.length));
                holder.dp.setImageTintList(null);
            }

            holder.bookingDateTime.setText(order.bookingDatetime);
            holder.price.setText(String.format("%.2f",order.price));
            holder.userName.setText(order.userName);
            holder.status.setTextColor(ColorStateList.valueOf(Color.parseColor("#00FF00")));
            holder.status.setText("Completed");
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView dp;
            TextView orderID, userName, price, bookingDateTime, status;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                dp  = itemView.findViewById(R.id.dp);
                orderID = itemView.findViewById(R.id.orderno);
                userName = itemView.findViewById(R.id.name);
                price = itemView.findViewById(R.id.price);
                bookingDateTime = itemView.findViewById(R.id.orderDateTime);
                status = itemView.findViewById(R.id.status);
            }
        }
    }
}
