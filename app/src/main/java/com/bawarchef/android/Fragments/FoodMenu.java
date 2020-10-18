package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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
import com.bawarchef.android.Hierarchy.DataStructure.FoodNode;
import com.bawarchef.android.Hierarchy.DataStructure.Node;
import com.bawarchef.android.Hierarchy.DataStructure.Tree;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FoodMenu extends Fragment {

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.food_menu,container,false);
        return v;
    }

    Tree t = new Tree("Menu");
    Node root;
    RecyclerView list;
    Button addButton;
    EditText head_name;
    ImageButton upload;

    ArrayList<Node> items;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = v.findViewById(R.id.list);
        addButton = v.findViewById(R.id.add_button);

        root = t.getRoot();
        items = root.getChildren();

        upload = v.findViewById(R.id.upload_menu);
        upload.setOnClickListener(update_menu);

        head_name = v.findViewById(R.id.head_text);
        head_name.setEnabled(false);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(recyMngr);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(new RecyclerViewFoodAdapter());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add(new Node(root,"",true));
                list.removeAllViews();
                list.getAdapter().notifyDataSetChanged();
            }
        });
    }

    View.OnClickListener update_menu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"UPD_CHEF_MENU");
            m.putProperty("MENU_DATA",FoodMenu.this.t);
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                FoodMenu.UpdateMenuAsyncTask updateMenuAsyncTask = new FoodMenu.UpdateMenuAsyncTask();
                updateMenuAsyncTask.execute(ep);
            }catch (Exception e){}
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

    class RecyclerViewFoodAdapter extends RecyclerView.Adapter<RecyclerViewFoodAdapter.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.list_item_design,parent,false);
            return new RecyclerViewFoodAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            holder.text.setHint("Untitled Category");
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
            holder.nextL.setOnClickListener(v1 -> edit(position));
        }

        void delete(int position) {
            items.remove(position);
            list.removeAllViews();
            RecyclerViewFoodAdapter.this.notifyDataSetChanged();
        }

        void edit(int position){
            Fragment fragment = new FoodMenu_2("Food",items.get(position),t);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            fragment.setTargetFragment(FoodMenu.this, 9999);
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
            }
        }
    }

    private ProgressDialog dialog;
    class UpdateMenuAsyncTask extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Updating your menu. Please wait !");
            dialog.show();
        }

        public UpdateMenuAsyncTask() {
            dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

}
