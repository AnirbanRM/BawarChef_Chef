package com.bawarchef.android.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.android.Hierarchy.DataStructure.FoodNode;
import com.bawarchef.android.Hierarchy.DataStructure.Ingredient;
import com.bawarchef.android.Hierarchy.DataStructure.Node;
import com.bawarchef.android.Hierarchy.DataStructure.Tree;
import com.bawarchef.android.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FoodIngredientMenu extends FoodMenu_2 {

    FoodIngredientMenu(String type, Node current_node) {
        super(type, current_node);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.food_menu_ingredient,container,false);
        return v;
    }

    RecyclerView ingList;
    Button ingAdd;
    FrameLayout custom,ingred;
    BottomNavigationView bottomNavigationView;
    EditText baseP,subP;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ingList = v.findViewById(R.id.ingredientList);
        ingAdd = v.findViewById(R.id.add_ingredient);
        custom = v.findViewById(R.id.custoF);
        ingred = v.findViewById(R.id.ingreF);
        baseP = v.findViewById(R.id.baseBox);
        subP = v.findViewById(R.id.subsqBox);

        bottomNavigationView = v.findViewById(R.id.food_custo_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(itemSelected);

        LinearLayoutManager ingRecyMngr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        ingList.setLayoutManager(ingRecyMngr);
        ingList.setItemAnimator(new DefaultItemAnimator());
        ingList.setAdapter(new IngredientRecyclerViewAdapter());

        ingAdd.setOnClickListener(ingAdded);

        baseP.addTextChangedListener(baseChanged);
        subP.addTextChangedListener(subsqChanged);

        baseP.setText(String.valueOf(((FoodNode)currentNode).getBaseP()));
        subP.setText(String.valueOf(((FoodNode)currentNode).getSubP()));
    }

    TextWatcher baseChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                ((FoodNode)currentNode).setBaseP(Float.valueOf(baseP.getText().toString()));
            }catch (Exception e){
                ((FoodNode)currentNode).setBaseP(0);
            }
        }
    };

    TextWatcher subsqChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                ((FoodNode)currentNode).setSubP(Float.valueOf(subP.getText().toString()));
            }catch (Exception e){
                ((FoodNode)currentNode).setSubP(0);
            }
        }
    };

    BottomNavigationView.OnNavigationItemSelectedListener itemSelected = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(item.getTitle().toString().equals("Customization")){
                item.setChecked(true);
                custom.setVisibility(View.VISIBLE);
                ingred.setVisibility(View.INVISIBLE);
            }

            else if(item.getTitle().toString().equals("Ingredients")){
                item.setChecked(true);
                ingred.setVisibility(View.VISIBLE);
                custom.setVisibility(View.INVISIBLE);
            }
            return true;
        }
    };

    View.OnClickListener ingAdded = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((FoodNode)currentNode).getIngredients().add(new Ingredient());
            ingList.removeAllViews();
            ingList.getAdapter().notifyDataSetChanged();
        }
    };

    class IngredientRecyclerViewAdapter extends RecyclerView.Adapter<IngredientRecyclerViewAdapter.ViewHolder>{

        @NonNull
        @Override
        public IngredientRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.ingredient_item_design,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull IngredientRecyclerViewAdapter.ViewHolder holder, int position) {
            Ingredient i = ((FoodNode)currentNode).getIngredients().get(position);
            holder.ingredientName.setText(i.getTitle());
            holder.amount.setText(String.valueOf(i.getMagnitude()));

            holder.ingredientName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    i.setTitle(holder.ingredientName.getText().toString());
                }
            });

            holder.amount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        i.setMagnitude(Float.valueOf(holder.amount.getText().toString()));
                    }catch (Exception e) {
                        i.setMagnitude(0);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return ((FoodNode)currentNode).getIngredients().size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            EditText ingredientName;
            EditText amount;
            Spinner unitSpinner;
            Button delButton;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                ingredientName = itemView.findViewById(R.id.text);
                delButton = itemView.findViewById(R.id.del);
                amount = itemView.findViewById(R.id.weight);
                unitSpinner = itemView.findViewById(R.id.unitSpinner);

                amount.setInputType(InputType.TYPE_CLASS_NUMBER);

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.ingredient_units,android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                unitSpinner.setAdapter(adapter);
            }
        }
    }
}
