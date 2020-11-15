package com.bawarchef.android.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.Order;
import com.bawarchef.Containers.OrderSummaryItem;
import com.bawarchef.android.DashboardActivity;
import com.bawarchef.android.DashboardUserActivity;
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.R;
import com.bawarchef.android.ScrollableMap;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ChefOrderInfo extends Fragment implements OnMapReadyCallback, MessageReceiver{

    View v;
    String orderNo;
    Orders orders;

    public ChefOrderInfo(String orderNo,Orders orders){
        this.orders = orders;
        this.orderNo = orderNo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.chef_order_info_fragment,container,false);
        v.setVisibility(View.INVISIBLE);
        return v;
    }

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    TextView orderID,name,mobno,date,status,address,pricebasetotal;
    RecyclerView cartist;
    ImageView dp;
    ScrollableMap trackmap;
    ConstraintLayout mapCL,controls,part2,part4;
    Button approve,decline,startend;

    ArrayList<CartItem> cartItems = new ArrayList<CartItem>();
    CartListAdapter cartListAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderID = view.findViewById(R.id.orderID);
        name = view.findViewById(R.id.name);
        date = view.findViewById(R.id.datetime);
        status = view.findViewById(R.id.status);
        dp = view.findViewById(R.id.dp);
        mobno = view.findViewById(R.id.mob);
        address = view.findViewById(R.id.address);
        pricebasetotal = view.findViewById(R.id.totalbasePrice);
        cartist = view.findViewById(R.id.cartlist);
        trackmap = view.findViewById(R.id.location);
        mapCL = view.findViewById(R.id.part3);
        part2 = view.findViewById(R.id.part2);
        part4 = view.findViewById(R.id.part4);
        approve = view.findViewById(R.id.approve);
        decline = view.findViewById(R.id.decline);
        controls = view.findViewById(R.id.controls);
        startend = view.findViewById(R.id.startend);

        approve.setOnClickListener(approve_decline);
        decline.setOnClickListener(approve_decline);
        startend.setOnClickListener(startendclicked);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        trackmap.onCreate(mapViewBundle);
        trackmap.getMapAsync(this);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        cartist.setLayoutManager(recyMngr);
        cartist.setItemAnimator(new DefaultItemAnimator());
        cartListAdapter = new CartListAdapter();
        cartist.setAdapter(cartListAdapter);

        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"ORDER_INFO");
        newm.putProperty("ORDERID", orderNo);
        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor("Getting your order... Please wait!");
            executor.execute(ep);
        }catch (Exception e){}
    }

    private ProgressDialog dialog;
    OrderSummaryItem osi;

    @Override
    public void process(Message m) {

        if(m.getMsg_type().equals("RESP_ORDERID_INFO")){
            osi = (OrderSummaryItem) m.getProperty("OrderDetail");

            getActivity().runOnUiThread(() -> {
                if(dialog!=null&&dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                show(osi);
                v.setVisibility(View.VISIBLE);
            });
        }

        else if(m.getMsg_type().equals("RESP_ORDER_APPROVE_DECLINE")){
            getActivity().runOnUiThread(() -> {
                if(dialog!=null&&dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                Toast.makeText(getActivity(),m.getProperty("RESULT").equals("SUCCESS")?"Successfully updated":"Error! Please try again later",Toast.LENGTH_SHORT).show();

                if(m.getProperty("RESPONSE").equals(Order.Status.CHEF_APPROVED)) {
                    disableControl();
                    mapCL.setVisibility(View.VISIBLE);
                    status.setText("Approved");
                    status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                    osi.status = Order.Status.CHEF_APPROVED;
                    cartist.removeAllViews();
                    cartListAdapter.notifyDataSetChanged();

                    startend.setVisibility(View.VISIBLE);
                }

                if(m.getProperty("RESPONSE").equals(Order.Status.CHEF_DECLINED)) {
                    disableControl();
                    part2.setVisibility(View.INVISIBLE);
                    part4.setVisibility(View.GONE);
                    osi.status = Order.Status.CHEF_DECLINED;
                    status.setText("Declined");
                    status.setTextColor(Color.parseColor("#FF0000"));
                }

                orders.updateList(this);

            });
        }

        else if(m.getMsg_type().equals("ORDER_START_STOP_RESP")){

            getActivity().runOnUiThread(() -> {
                if(dialog!=null&&dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }

                if(m.getProperty("RESPONSE").equals("START")){
                    status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                    status.setText("Ongoing");
                    startend.setText("Stop Timer");
                    startend.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                    osi.status = Order.Status.ONGOING;
                }
                else if(m.getProperty("RESPONSE").equals("STOP")){
                    status.setTextColor(Color.GREEN);
                    status.setText("Completed");
                    part2.setVisibility(View.INVISIBLE);
                    part4.setVisibility(View.GONE);
                    mapCL.setVisibility(View.GONE);
                    startend.setVisibility(View.GONE);
                    osi.status = Order.Status.COMPLETED;
                }

                orders.updateList(this);
            });
        }
    }

    View.OnClickListener approve_decline = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"ORDER_APPROVE_DECLINE");
            newm.putProperty("ORDER",osi.orderID);
            newm.putProperty("RESPONSE",(v.getId()==approve.getId())?Order.Status.CHEF_APPROVED:Order.Status.CHEF_DECLINED);
            newm.putProperty("CART",osi.ordereditems);

            try{
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
                AsyncExecutor executor = new AsyncExecutor("Please wait");
                executor.execute(ep);
            }catch (Exception e){}
        }
    };

    public void disableControl(){
        controls.setVisibility(View.GONE);
    }

    private void show(OrderSummaryItem osi){
        orderID.setText(osi.orderID);
        name.setText(osi.name);
        mobno.setText(osi.mob);
        date.setText(osi.datetime);
        switch(osi.status){
            case PENDING:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#F9A834")));
                status.setText("Pending Approval");
                mapCL.setVisibility(View.GONE);
                startend.setVisibility(View.GONE);
                break;

            case CHEF_APPROVED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                status.setText("Approved");
                disableControl();
                startend.setText("Start Timer");
                startend.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                break;

            case ONGOING:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                status.setText("Ongoing");
                disableControl();
                startend.setText("Stop Timer");
                startend.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                break;
        }
        if(osi.dp!=null && osi.dp.length!=0){
            dp.setImageBitmap(BitmapFactory.decodeByteArray(osi.dp,0,osi.dp.length));
        }

        currentLoc = ((ThisApplication)getActivity().getApplication()).locationEngine.getLastLocation();
        chefLoc = new LatLng(osi.bookingLat,osi.bookingLng);
        refreshMap();
        cartItems = osi.ordereditems;
        cartListAdapter.notifyDataSetChanged();
        address.setText(osi.address);

        pricebasetotal.setText(String.format("%.2f",osi.price));
    }

    View.OnClickListener startendclicked = v -> {
        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"ORDER_START_STOP");
        newm.putProperty("ORDER",osi.orderID);

        if(osi.status.equals(Order.Status.ONGOING))
            newm.putProperty("RESPONSE","STOP");

        else if(osi.status.equals(Order.Status.CHEF_APPROVED))
            newm.putProperty("RESPONSE","START");

        try{
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication)getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor("Please wait...");
            executor.execute(ep);
        }catch (Exception e){}


    };


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


    //------------------------------------------MAP------------------------------------------------

    GoogleMap gMap=null;
    LatLng currentLoc,chefLoc;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        trackmap.onSaveInstanceState(mapViewBundle);
    }
    @Override
    public void onResume() {
        super.onResume();
        trackmap.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        trackmap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        trackmap.onStop();
    }
    @Override
    public void onPause() {
        trackmap.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        trackmap.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        trackmap.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
    }

    MarkerOptions chefMarker,currentMarker;
    public void refreshMap(){
        if(currentLoc!=null&&currentMarker==null){
            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.cheflocation);
            icon = Bitmap.createScaledBitmap(icon,icon.getWidth()/3,icon.getHeight()/3,false);
            currentMarker = new MarkerOptions().flat(false).position(currentLoc).draggable(false).title("CHEF").icon(BitmapDescriptorFactory.fromBitmap(icon));
            gMap.addMarker(currentMarker);
            gMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
        if(chefLoc!=null) {
            if(chefMarker==null) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.homelocation);
                icon = Bitmap.createScaledBitmap(icon,icon.getWidth()/4,icon.getHeight()/4,false);
                chefMarker = new MarkerOptions().flat(false).position(chefLoc).draggable(false).title("HOME").icon(BitmapDescriptorFactory.fromBitmap(icon));
                gMap.addMarker(chefMarker);
            }
            else
                chefMarker.position(chefLoc);
            gMap.moveCamera(CameraUpdateFactory.newLatLng(chefLoc));
        }
    }

//------------------------------------------------------------------------------------------------------------------


    class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.chef_cart_item_design,parent,false);
            return new ViewHolder(v);
        }

        private void disableScaling(TextView t){
            t.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem cartI = cartItems.get(position);
            holder.fName.setText(cartI.getFoodName());
            holder.qty.setText(cartI.getQuantity());
            holder.price.setText(String.format("%.2f",cartI.getBasePrice()));
            holder.category.setText(cartI.getCategory());

            StringBuilder customization= new StringBuilder();
            for(Map.Entry<String,String> e : cartI.getCustomization().entrySet()){
                customization.append(e.getKey());
                customization.append(" : ");
                customization.append(e.getValue());
                customization.append("\n");
            }

            holder.customization.setText(customization.toString());
            holder.itemView.setTranslationZ(0);

            if(osi.status.equals(Order.Status.CHEF_APPROVED)||osi.status.equals(Order.Status.ONGOING))
                disableScaling(holder.scale);

            if(osi.status.equals(Order.Status.PENDING)) {
                holder.scale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        ScaleIngredient si = new ScaleIngredient(holder.fName.getText().toString(), holder.qty.getText().toString(), holder.customization.getText().toString(), cartI.getIngredients());
                        si.show(fm, null);
                        cartI.getIngredients();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView fName;
            TextView category;
            TextView price;
            TextView qty;
            TextView customization;
            TextView scale;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                fName = itemView.findViewById(R.id.foodname);
                category = itemView.findViewById(R.id.category);
                price = itemView.findViewById(R.id.price);
                qty = itemView.findViewById(R.id.qty);
                customization = itemView.findViewById(R.id.customization);
                scale = itemView.findViewById(R.id.scale);
            }
        }
    }
}
