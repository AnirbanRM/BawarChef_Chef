package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.Order;
import com.bawarchef.Containers.OrderSummaryItem;
import com.bawarchef.android.DashboardUserActivity;
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.R;
import com.bawarchef.android.ScrollableMap;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    TextView orderID,name,date,status,mob,address,pricebasetotal,pricetotal;
    RecyclerView cartist;
    ImageView dp;
    ScrollableMap trackmap;
    Button cancel,ingredients;
    ConstraintLayout mapCL,rating;
    ImageView[] rating_star;


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
        mob = view.findViewById(R.id.mob);
        address = view.findViewById(R.id.address);
        pricebasetotal = view.findViewById(R.id.totalbasePrice);
        pricetotal = view.findViewById(R.id.totalPrice);
        cartist = view.findViewById(R.id.cartlist);
        trackmap = view.findViewById(R.id.location);
        mapCL = view.findViewById(R.id.part3);

        rating = view.findViewById(R.id.rating);
        rating_star = new ImageView[]{view.findViewById(R.id.s1),view.findViewById(R.id.s2),view.findViewById(R.id.s3),view.findViewById(R.id.s4),view.findViewById(R.id.s5)};

        for(ImageView i : rating_star)
            i.setOnClickListener(ratingchanged);

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

        ingredients = view.findViewById(R.id.ing_button);
        ingredients.setOnClickListener(ingredientsClicked);
    }

    View.OnClickListener ratingchanged = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for(int i = 0; i< rating_star.length; i++)
                rating_star[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
            int i = 0;
            do
                rating_star[i++].setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            while(v.getId()!=rating_star[i-1].getId());

            try {
                Message m = new Message(Message.Direction.CLIENT_TO_SERVER, "ORDER_RATING");
                m.putProperty("ORDER",osi.orderID);
                m.putProperty("RATING",i);
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                AsyncExecutor executor = new AsyncExecutor("Rating your order... Please wait !");
                executor.execute(ep);
            }catch (Exception e){}
        }
    };

    View.OnClickListener ingredientsClicked = v -> {
        DashboardUserActivity.activeFragment = new Ingredients(cartItems);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        DashboardUserActivity.activeFragment.setTargetFragment(getActivity().getSupportFragmentManager().getFragments().get(0),9999);
        ft.add(R.id.fragmentViewPort,DashboardUserActivity.activeFragment);
        ft.addToBackStack(null);

        ft.commit();
    };

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

        if(m.getMsg_type().equals("RESP_CHEF_LOC")){
            double lat = (double)m.getProperty("LAT");
            double lng = (double)m.getProperty("LNG");
            if(lat==0&&lng==0)
                return;
            chefLoc = new LatLng(lat,lng);
            Log.e(String.valueOf(chefLoc.latitude),String.valueOf(chefLoc.longitude));
            getActivity().runOnUiThread(() -> refreshMap());
        }

        if(m.getMsg_type().equals("RESP_ORDER_RATING")){
            getActivity().runOnUiThread(() -> {
                if(m.getProperty("RESULT").equals("SUCCESS"))
                    Toast.makeText(getActivity(),"Successfully rated !",Toast.LENGTH_SHORT).show();
                else if(m.getProperty("RESULT").equals("FAILURE"))
                    Toast.makeText(getActivity(),"Couldn't rate your order. Please try again !",Toast.LENGTH_SHORT).show();

                if(dialog!=null&&dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
            });
        }
    }

    boolean tracking = false;
    public void disableTracking(){
        tracking = false;
        ConstraintSet cs = new ConstraintSet();

        cs.clone(mapCL);
        cs.setDimensionRatio(R.id.location,null);
        cs.applyTo(mapCL);
        TextView t = v.findViewById(R.id.tracking_text);
        t.setText("Live Tracking is not available.");
    }

    private void disableCancel(){
        cancel.setVisibility(View.GONE);
    }

    private void show(OrderSummaryItem osi){
        orderID.setText(osi.orderID);
        name.setText(osi.name);
        currentLoc = new LatLng(osi.bookingLat,osi.bookingLng);
        mob.setText(osi.mob);
        refreshMap();
        date.setText(osi.datetime);
        switch(osi.status){
            case PENDING:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#F9A834")));
                status.setText("Pending");
                disableTracking();
                rating.setVisibility(View.GONE);
                ingredients.setVisibility(View.GONE);
                break;
            case COMPLETED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#00FF00")));
                status.setText("Completed");
                disableTracking();
                disableCancel();

                if(osi.rating!=-1){
                    for(int i = 0;i< osi.rating; i++)
                        rating_star[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                }
                break;
            case CHEF_APPROVED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                status.setText("Approved");
                tracking = true;
                rating.setVisibility(View.GONE);
                ingredients.setVisibility(View.GONE);
                break;
            case CHEF_DECLINED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                status.setText("Declined");
                disableTracking();
                cancel.setVisibility(View.GONE);
                rating.setVisibility(View.GONE);
                ingredients.setVisibility(View.GONE);
                break;
            case ONGOING:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#0000FF")));
                status.setText("Ongoing");
                disableTracking();
                disableCancel();
                rating.setVisibility(View.GONE);
                break;
            case USER_CANCELLED:
                status.setTextColor(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                status.setText("Cancelled");
                disableTracking();
                cancel.setVisibility(View.GONE);
                rating.setVisibility(View.GONE);
                ingredients.setVisibility(View.GONE);
                break;
        }
        if(osi.dp!=null && osi.dp.length!=0){
            dp.setImageBitmap(BitmapFactory.decodeByteArray(osi.dp,0,osi.dp.length));
        }
        cartItems = osi.ordereditems;
        cartListAdapter.notifyDataSetChanged();
        address.setText(osi.address);

        double sum = 0;
        for(CartItem c : cartItems)
            sum+=c.getBasePrice();
        pricebasetotal.setText(String.format("%.2f",sum));

        if(osi.status== Order.Status.COMPLETED)
            pricetotal.setText(String.format("%.2f",osi.price));
        else
            pricetotal.setText("--");

        if(tracking)
            getContinuousLocationUpdates();
    }

    public void getContinuousLocationUpdates(){
        if(osi==null)return;

        String datepart[] = osi.datetime.split(" ")[0].split("-");
        String timepart[] = osi.datetime.split(" ")[1].split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(datepart[0]),Integer.parseInt(datepart[1]),Integer.parseInt(datepart[2]),Integer.parseInt(timepart[0]),Integer.parseInt(timepart[1]));

        Date bookDt = calendar.getTime();
        Date nowDt = Calendar.getInstance().getTime();

        if(bookDt.getTime()-nowDt.getTime()<30*60)
            disableTracking();

        new Thread(() -> {
            while(true){
                try {
                    Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"GET_CHEF_LOC");
                    m.putProperty("CHEF",osi.chefID);
                    EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                    ((ThisApplication)getActivity().getApplication()).mobileClient.send(ep);

                    Thread.sleep(5000);
                }catch (Exception e){}
            }
        }).start();
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
            Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.homelocation);
            icon = Bitmap.createScaledBitmap(icon,icon.getWidth()/4,icon.getHeight()/4,false);
            currentMarker = new MarkerOptions().flat(false).position(currentLoc).draggable(false).title("HOME").icon(BitmapDescriptorFactory.fromBitmap(icon));
            gMap.addMarker(currentMarker);
            gMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
        if(chefLoc!=null) {
            if(chefMarker==null) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.cheflocation);
                icon = Bitmap.createScaledBitmap(icon,icon.getWidth()/3,icon.getHeight()/3,false);
                chefMarker = new MarkerOptions().flat(false).position(chefLoc).draggable(false).title("CHEF").icon(BitmapDescriptorFactory.fromBitmap(icon));
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
