package com.bawarchef.android.Fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.bawarchef.Containers.ChefIdentity;
import com.bawarchef.Containers.Order;
import com.bawarchef.Containers.UserIdentity;
import com.bawarchef.android.ChefPDetailsActivity;
import com.bawarchef.android.DashboardUserActivity;
import com.bawarchef.android.Hierarchy.DataStructure.CartContainer;
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class UserBookingDetails extends Fragment implements OnMapReadyCallback,MessageReceiver {

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_book_activity,container,false);
        return v;
    }

    RecyclerView listRecyclerView;
    CartListAdapter adapter;
    CartContainer cartContainer;
    ImageView chefdp;
    TextView chefname,datebox,timebox,totalPice;
    EditText adddress;
    MapView mapView;
    Button bookButton;

    Bitmap chefImg;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listRecyclerView = view.findViewById(R.id.cartlist);

        chefdp = view.findViewById(R.id.dp);
        chefname = view.findViewById(R.id.name);
        adddress = view.findViewById(R.id.address_box);
        datebox = view.findViewById(R.id.dateBox);
        timebox = view.findViewById(R.id.timeBox);
        mapView = view.findViewById(R.id.location);
        totalPice = view.findViewById(R.id.totalPrice);
        bookButton = view.findViewById(R.id.bookButton);

        bookButton.setOnClickListener(bookclicked);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        cartContainer = ThisApplication.currentUserProfile.getCart();
        chefname.setText(cartContainer.getChefname());
        UserIdentity.Address addr = ThisApplication.currentUserProfile.getUserIdentity().addr;
        adddress.setText(addr.address+"\n"+addr.city+" "+addr.state+" "+addr.pinNo);

        if(cartContainer.getChefDP()!=null) {
            chefImg = BitmapFactory.decodeByteArray(cartContainer.getChefDP(), 0, cartContainer.getChefDP().length);
            chefdp.setImageBitmap(chefImg);
        }

        totalPice.setText(String.format("%.2f",cartContainer.getCartPrice()));

        datebox.setOnClickListener(dateboxcl);
        timebox.setOnClickListener(timeboxcl);

        Calendar c = Calendar.getInstance();
        datebox.setText(c.get(Calendar.DATE)+"/"+c.get(Calendar.MONTH)+"/"+c.get(Calendar.YEAR));
        timebox.setText(c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        listRecyclerView.setLayoutManager(recyMngr);
        listRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CartListAdapter();
        listRecyclerView.setAdapter(adapter);
    }

    View.OnClickListener bookclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String datecompo[] = datebox.getText().toString().split("/");
            String date = datecompo[2]+"-"+datecompo[1]+"-"+datecompo[0];
            Order o = new Order(cartContainer.getChefID(),adddress.getText().toString(),ThisApplication.currentUserProfile.getUserIdentity().userID,currentLocation.latitude,currentLocation.longitude,date,timebox.getText().toString());
            o.setOrdereditems(cartContainer.getCartItems());

            Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"ORDER_BOOK");
            newm.putProperty("ORDER", o);
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                AsyncExecutor executor = new AsyncExecutor();
                executor.execute(ep);
            }catch (Exception e){}
        }
    };

    View.OnClickListener dateboxcl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    datebox.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                }
            },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        }
    };

    View.OnClickListener timeboxcl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            TimePickerDialog dpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    timebox.setText(hourOfDay+":"+minute);
                }
            },Calendar.HOUR_OF_DAY,Calendar.MINUTE,true);
            dpd.show();
        }
    };


    private ProgressDialog dialog;

    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Booking your Order ! Please wait...");
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

    LatLng currentLocation;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        gMap.setOnMapClickListener(mapclicked);
        gMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        currentLocation = new LatLng(ThisApplication.currentUserProfile.getUserIdentity().lati,ThisApplication.currentUserProfile.getUserIdentity().longi);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        MarkerOptions options = new MarkerOptions().flat(false).position(currentLocation).draggable(false);
        gMap.addMarker(options);


    }

    GoogleMap.OnMapClickListener mapclicked = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            currentLocation = latLng;
            gMap.clear();
            MarkerOptions options = new MarkerOptions().flat(false).position(currentLocation).draggable(false);
            gMap.addMarker(options);
        }
    };

//------------------------------------------------------------------------------------------------------------------

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_BOOK_ORDER")){
            getActivity().runOnUiThread(() -> {
                if(dialog!=null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                Toast.makeText(getActivity(),"Order successfully booked",Toast.LENGTH_SHORT).show();

                int backStackCount = getActivity().getSupportFragmentManager().getBackStackEntryCount()-1;
                for(int i = 0; i< backStackCount; i++)
                    getActivity().getSupportFragmentManager().popBackStack();

                DashboardUserActivity.activeFragment = new OrderInfo((String) m.getProperty("OrderID"));

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                DashboardUserActivity.activeFragment.setTargetFragment(getActivity().getSupportFragmentManager().getFragments().get(0),9999);
                ft.add(R.id.fragmentViewPort,DashboardUserActivity.activeFragment);
                ft.addToBackStack(null);

                ft.commit();
            });
        }
    }


    class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.cart_item_design,parent,false);
            return new CartListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CartListAdapter.ViewHolder holder, int position) {
            CartItem cartI = cartContainer.getCartItems().get(position);
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
            holder.delBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartContainer.getCartItems().remove(position);
                    adapter.notifyDataSetChanged();
                    ThisApplication.currentUserProfile.getCart().refresh();
                }
            });
            holder.itemView.setTranslationZ(0);
        }

        @Override
        public int getItemCount() {
            return cartContainer.getCartItems().size();
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
                delBut = itemView.findViewById(R.id.del_but);
            }
        }
    }


}
