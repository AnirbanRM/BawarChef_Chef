package com.bawarchef.android.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.android.Hierarchy.DataStructure.Ingredient;
import com.bawarchef.android.R;


import java.util.ArrayList;

public class ScaleIngredient extends DialogFragment {

    View v;
    ArrayList<Ingredient> arrayList;
    String fName,qty,custo;
    SeekBar scaler;
    RecyclerView list;

    ScaleIngredient(String fname,String qty, String custo, ArrayList<Ingredient> arrayList){
        this.arrayList = arrayList;
        this.fName = fname;
        this.qty = qty;
        this.custo = custo;
        boxes = new TextView[arrayList.size()];
    }

    TextView foodname,quantity,customization;
    RecyclerListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_scale,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        foodname = v.findViewById(R.id.foodname);
        quantity = v.findViewById(R.id.qty);
        customization = v.findViewById(R.id.customization);
        scaler = v.findViewById(R.id.scaler);
        list = v.findViewById(R.id.recylist);

        foodname.setText(fName);
        quantity.setText(qty);
        customization.setText(custo);
        scaler.setProgress(50);


        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(recyMngr);
        list.setItemAnimator(new DefaultItemAnimator());
        listAdapter = new RecyclerListAdapter();
        list.setAdapter(listAdapter);

        scaler.setOnTouchListener(touchl);
    }

    View.OnTouchListener touchl = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if(event.getAction()==MotionEvent.ACTION_MOVE){
                for(int i = 0; i< boxes.length; i++)
                    boxes[i].setText(String.format("%.2f",arrayList.get(i).getMagnitude()*f(scaler.getProgress())));
            }

            else if(event.getAction()==MotionEvent.ACTION_UP) {

                for(int i = 0; i< arrayList.size(); i++)
                    arrayList.get(i).setMagnitude(Float.parseFloat(boxes[i].getText().toString()));

                new Thread(() -> getActivity().runOnUiThread(() -> scaler.setProgress(50))).start();
            }
            return false;
        }
    };



    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    private double f(double x){
        return Math.exp((x-50)/45);
    }


    TextView[] boxes;

    class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ViewHolder>{


        @NonNull
        @Override
        public RecyclerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scale_ingredient_item_design, parent, false);
            return new RecyclerListAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerListAdapter.ViewHolder holder, int position) {
            Ingredient ingre = arrayList.get(position);
            holder.iname.setText(ingre.getTitle());
            holder.imag.setText(String.valueOf(ingre.getMagnitude()));
            holder.iunit.setText(ingre.getUnit().toString());
            boxes[position] = holder.imag;
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView iname,imag,iunit;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                iname = itemView.findViewById(R.id.ingredient_name);
                iunit = itemView.findViewById(R.id.unit);
                imag = itemView.findViewById(R.id.magnitude);
            }
        }
    }
}
