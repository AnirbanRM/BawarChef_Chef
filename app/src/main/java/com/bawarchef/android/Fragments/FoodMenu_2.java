package com.bawarchef.android.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bawarchef.android.Hierarchy.DataStructure.FoodNode;
import com.bawarchef.android.Hierarchy.DataStructure.Node;
import com.bawarchef.android.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FoodMenu_2 extends Fragment {

    View v;
    String type = "";
    String head_name="";
    Node currentNode = null;
    Button backButton;
    EditText headingView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.food_menu2,container,false);
        return v;
    }

    RecyclerView list;
    Button addButton;
    TextView addStrBox;

    ArrayList<Node> items;

    FoodMenu_2(String type,Node current_node){
        this.type = type;
        this.head_name = current_node.getNodeText();
        this.currentNode =current_node;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        items = currentNode.getChildren();

        list = v.findViewById(R.id.list);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(recyMngr);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(new FoodMenu_2.RecyclerViewFoodAdapter());

        addButton = v.findViewById(R.id.add_button);
        backButton = v.findViewById(R.id.orders_back_button);
        addStrBox = v.findViewById(R.id.add_str);
        addStrBox.setText("Add new "+type);
        headingView = v.findViewById(R.id.head_text);
        headingView.setText(currentNode.getNodeText());

        addButton.setOnClickListener(addClicked);
        backButton.setOnClickListener(backClicked);
        headingView.addTextChangedListener(textChanged);
    }

    View.OnClickListener backClicked = v -> {

        Intent intent = new Intent(getContext(), FoodMenu_2.class);
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
        getFragmentManager().popBackStack();
    };

    View.OnClickListener addClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(type.equals("Food"))
                items.add(new FoodNode(currentNode,"",true));
            else
                items.add(new Node(currentNode,"",true));
            list.removeAllViews();
            list.getAdapter().notifyDataSetChanged();
        }
    };

    TextWatcher textChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            currentNode.setNodeText(headingView.getText().toString());
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode==9999){
                list.removeAllViews();
                list.getAdapter().notifyDataSetChanged();
            }
        }
    }

    class RecyclerViewFoodAdapter extends RecyclerView.Adapter<FoodMenu_2.RecyclerViewFoodAdapter.ViewHolder>{

        @NonNull
        @Override
        public FoodMenu_2.RecyclerViewFoodAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.list_item_design,parent,false);
            return new RecyclerViewFoodAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodMenu_2.RecyclerViewFoodAdapter.ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            holder.text.setHint("Untitled "+type);
            holder.text.setText(items.get(position).getNodeText());

            holder.text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    items.get(position).setNodeText(holder.text.getText().toString());
                }
            });

            holder.delB.setOnClickListener(v1 -> delete(position) );

            if(!FoodMenu_2.this.type.equals("Customization Option"))
                holder.nextL.setOnClickListener(v1 -> edit(position));
        }

        void delete(int position) {
            items.remove(position);
            list.removeAllViews();
            RecyclerViewFoodAdapter.this.notifyDataSetChanged();
        }

        void edit(int position){

            String type = "";
            if(FoodMenu_2.this.type.equals("Food"))
                type = "Customization";
            if(FoodMenu_2.this.type.equals("Customization"))
                type = "Customization Option";

            Fragment fragment;
            if(FoodMenu_2.this.type.equals("Food"))
                fragment = new FoodIngredientMenu(type,items.get(position));
            else
                fragment = new FoodMenu_2(type,items.get(position));

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            fragment.setTargetFragment(FoodMenu_2.this, 9999);
            ft.add(R.id.fragmentViewPort,fragment);
            ft.addToBackStack(null);
            ft.commit();;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            EditText text;
            Button nextL,delB;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                text = itemView.findViewById(R.id.text);
                nextL = itemView.findViewById(R.id.nextL_but);
                delB = itemView.findViewById(R.id.del_but);

                if(type.equals(FoodMenu_2.this.type.equals("Customization Option")))
                    nextL.setWidth(0);
            }
        }
    }
}
