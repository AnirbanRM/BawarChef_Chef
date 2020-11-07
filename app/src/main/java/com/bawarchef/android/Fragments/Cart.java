package com.bawarchef.android.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.Communication.Message;
import com.bawarchef.Containers.UserIdentity;
import com.bawarchef.android.Hierarchy.DataStructure.CartContainer;
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class Cart extends Fragment implements MessageReceiver{

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_cart_fragment,container,false);
        return v;
    }

    RecyclerView listRecyclerView;
    CartListAdapter adapter;
    CartContainer cartContainer;
    Button bookButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listRecyclerView = view.findViewById(R.id.cartlist);
        bookButton = view.findViewById(R.id.bookButton);
        bookButton.setOnClickListener(bookButtonClicked);

        cartContainer = ThisApplication.currentUserProfile.getCart();

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        listRecyclerView.setLayoutManager(recyMngr);
        listRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new CartListAdapter();
        listRecyclerView.setAdapter(adapter);
    }

    View.OnClickListener bookButtonClicked = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(cartContainer.getCartItems().size()==0){
                Toast.makeText(getActivity(),"Empty Cart !", Toast.LENGTH_SHORT).show();
                return;
            }

            activeFragment = new UserBookingDetails();

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            activeFragment.setTargetFragment(Cart.this, 9999);
            ft.add(R.id.fragmentViewPort,activeFragment);
            ft.addToBackStack(null);
            ft.commit();;
        }
    };

    Fragment activeFragment=null;
    @Override
    public void process(Message m) {
        if(activeFragment!=null)
            ((MessageReceiver)activeFragment).process(m);
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

    class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder>{

        @NonNull
        @Override
        public CartListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.cart_item_design,parent,false);
            return new ViewHolder(v);
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
