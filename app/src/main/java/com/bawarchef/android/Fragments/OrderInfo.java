package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.OrderSummaryItem;
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

import static android.graphics.BitmapFactory.decodeByteArray;

public class OrderInfo extends Fragment implements OnMapReadyCallback, MessageReceiver{

    View v;
    String orderNo;

    public OrderInfo(String orderNo){
        this.orderNo = orderNo;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.order_info_fragment,container,false);
        v.setVisibility(View.INVISIBLE);
        return v;
    }

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    TextView orderID,name,date,status,address,pricetotal;
    RecyclerView cartist;
    ImageView dp;
    MapView trackmap;
    Button cancel;

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
        address = view.findViewById(R.id.address);
        pricetotal = view.findViewById(R.id.totalPrice);
        cartist = view.findViewById(R.id.cartlist);
        trackmap = view.findViewById(R.id.location);

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

        cancel  = view.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(canceclicked);
    }

    View.OnClickListener canceclicked = v -> {
        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"CANCEL_ORDER");
        newm.putProperty("ORDERID", orderNo);
        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor("Cancelling your order... Please wait!");
            executor.execute(ep);
        }catch (Exception e){}
    };

    private ProgressDialog dialog;

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_ORDERID_INFO")){
            OrderSummaryItem osi = (OrderSummaryItem) m.getProperty("OrderDetail");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(dialog!=null&&dialog.isShowing()) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    show(osi);
                    v.setVisibility(View.VISIBLE);
                }
            });
        }

        if(m.getMsg_type().equals("RESP_ORDER_CANCEL")){
            getActivity().runOnUiThread(() -> {
                if(m.getProperty("RESULT").equals("SUCCESS"))
                    Toast.makeText(getActivity(),"Successfully cancelled your order !",Toast.LENGTH_SHORT).show();
                else if(m.getProperty("RESULT").equals("FAILURE"))
                    Toast.makeText(getActivity(),"Couldn't cancel your order. Please try again !",Toast.LENGTH_SHORT).show();

                if(dialog!=null&&dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.detach(this);
                fragmentTransaction.attach(this);
                fragmentTransaction.commit();
            });
        }
    }

    private void show(OrderSummaryItem osi){
        orderID.setText(osi.orderID);
        name.setText(osi.name);
        date.setText(osi.datetime);
        switch(osi.status){
            case PENDING:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#F9A834")));
                status.setText("Pending");
                break;
            case COMPLETED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#00FF00")));
                status.setText("Completed");
                break;
            case CHEF_APPROVED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                status.setText("Approved");
                break;
            case CHEF_DECLINED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                status.setText("Declined");
                break;
            case USER_CANCELLED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                status.setText("Cancelled");
                break;
        }
        if(osi.dp!=null && osi.dp.length!=0){
            dp.setImageBitmap(BitmapFactory.decodeByteArray(osi.dp,0,osi.dp.length));
        }
        cartItems = osi.ordereditems;
        cartListAdapter.notifyDataSetChanged();
        address.setText(osi.address);
        pricetotal.setText(String.format("%.2f",osi.price));
    }

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

    LatLng currentLocation;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        /*
        gMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        currentLocation = new LatLng(ThisApplication.currentUserProfile.getUserIdentity().lati,ThisApplication.currentUserProfile.getUserIdentity().longi);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        MarkerOptions options = new MarkerOptions().flat(false).position(currentLocation).draggable(false);
        gMap.addMarker(options);

         */
    }

//------------------------------------------------------------------------------------------------------------------


    class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder>{

        @NonNull
        @Override
        public CartListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.cart_item_design_no_del_design,parent,false);
            return new CartListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CartListAdapter.ViewHolder holder, int position) {
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
            ImageButton delBut;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                fName = itemView.findViewById(R.id.foodname);
                category = itemView.findViewById(R.id.category);
                price = itemView.findViewById(R.id.price);
                qty = itemView.findViewById(R.id.qty);
                customization = itemView.findViewById(R.id.customization);
            }
        }
    }
}
