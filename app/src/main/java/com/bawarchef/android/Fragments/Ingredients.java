package com.bawarchef.android.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.Hierarchy.DataStructure.Ingredient;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;
import com.google.android.gms.maps.MapView;

import java.util.ArrayList;

public class Ingredients extends Fragment implements MessageReceiver{

    View v;
    ArrayList<CartItem> items;

    public Ingredients(ArrayList<CartItem> items){
        this.items = items;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_ingredients, container,false);
        return v;
    }

    TextView textView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView = view.findViewById(R.id.ing_list);

        StringBuilder sb = new StringBuilder();
        for(CartItem ci : items){
            ArrayList<Ingredient> ing = ci.getIngredients();
            for(Ingredient i : ing){
                sb.append(i.getTitle());
                sb.append("\t");
                sb.append("=>    ");
                sb.append(i.getMagnitude());
                sb.append(" ");
                sb.append(i.getUnit());
                sb.append("\n");
            }
        }
        textView.setText(sb.toString());
    }

    @Override
    public void process(Message m) {

    }
}
