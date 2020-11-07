package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.Order;
import com.bawarchef.Containers.OrderListItemClass;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class UOrders extends Fragment implements MessageReceiver{

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.orders, container, false);
        return v;
    }

    RecyclerView list;
    RecyclerListAdapter listAdapter;
    ArrayList<OrderListItemClass> orders;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.list);

        orders = new ArrayList<OrderListItemClass>();

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(recyMngr);
        list.setItemAnimator(new DefaultItemAnimator());
        listAdapter = new RecyclerListAdapter();
        list.setAdapter(listAdapter);

        Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"ORDER_FETCH");
        try{
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor();
            executor.execute(ep);
        }catch (Exception e){}

    }


    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_ORDER")){
            orders = (ArrayList<OrderListItemClass>) m.getProperty("Orders");
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
            dialog.setMessage("Fetching your past orders... Please wait !");
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrderListItemClass order = orders.get(position);
            holder.orderID.setText(order.orderiD);

            if(order.chefDP!=null && order.chefDP.length!=0) {
                holder.dp.setImageBitmap(BitmapFactory.decodeByteArray(order.chefDP, 0, order.chefDP.length));
                holder.dp.setImageTintList(null);
            }

            holder.bookingDateTime.setText(order.bookingDatetime);
            holder.price.setText(String.format("%.2f",order.price));
            holder.chefName.setText(order.chefName);

            switch(order.status){
                case PENDING:
                    holder.status.setTextColor(ColorStateList.valueOf(Color.parseColor("#F9A834")));
                    holder.status.setText("Pending");
                    break;
                case COMPLETED:
                    holder.status.setTextColor(ColorStateList.valueOf(Color.parseColor("#00FF00")));
                    holder.status.setText("Completed");
                    break;
                case CHEF_APPROVED:
                    holder.status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                    holder.status.setText("Approved");
                    break;
                case CHEF_DECLINED:
                    holder.status.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    holder.status.setText("Declined");
                    break;
                case USER_CANCELLED:
                    holder.status.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    holder.status.setText("Cancelled");
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView dp;
            TextView orderID, chefName, price, bookingDateTime, status;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                dp  = itemView.findViewById(R.id.dp);
                orderID = itemView.findViewById(R.id.orderno);
                chefName = itemView.findViewById(R.id.name);
                price = itemView.findViewById(R.id.price);
                bookingDateTime = itemView.findViewById(R.id.orderDateTime);
                status = itemView.findViewById(R.id.status);
            }
        }
    }
}
