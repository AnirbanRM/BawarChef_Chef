package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class FoodMenu extends Fragment implements MessageReceiver{

    View v;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.food_menu,container,false);
        return v;
    }

    ArrayList<Tree> menus;

    Tree currentTree;

    RecyclerView list;
    Button addButton;
    EditText head_name;
    ImageButton upload, addMenu,deleteMenu;

    RecyclerView menulist;
    MenuRecyclerAdapter menuAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = v.findViewById(R.id.list);
        addButton = v.findViewById(R.id.add_button);
        menulist = v.findViewById(R.id.menuname);

        addMenu = v.findViewById(R.id.addmenu);
        addMenu.setOnClickListener(addMenuclicked);
        deleteMenu = v.findViewById(R.id.deletemenu);
        deleteMenu.setOnClickListener(deleteMenuclicked);

        menus = new ArrayList<Tree>();

        LinearLayoutManager recyMngr2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        menulist.setLayoutManager(recyMngr2);
        menulist.setItemAnimator(new DefaultItemAnimator());
        menuAdapter = new MenuRecyclerAdapter();
        menulist.setAdapter(menuAdapter);

        upload = v.findViewById(R.id.upload_menu);
        upload.setOnClickListener(update_menu);

        head_name = v.findViewById(R.id.head_text);
        head_name.setEnabled(false);

        head_name.addTextChangedListener(head_text_changed);

        LinearLayoutManager recyMngr = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(recyMngr);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(new RecyclerViewFoodAdapter());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentTree==null)return;
                currentTree.getRoot().add(new Node(currentTree.getRoot(),"",true));
                list.removeAllViews();
                list.getAdapter().notifyDataSetChanged();
            }
        });
        fetch();
    }

    TextWatcher head_text_changed = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if(currentTree==null)return;
            currentTree.getRoot().setNodeText(head_name.getText().toString());
            menuAdapter.setActiveElementText(head_name.getText().toString());
        }
    };

    View.OnClickListener addMenuclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menus.add(new Tree("Untitled Menu"));
            menuAdapter.notifyDataSetChanged();
        }
    };

    View.OnClickListener deleteMenuclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(currentTree!=null){
                menus.remove(currentTree);
                currentTree = null;
                menulist.removeAllViews();
                menuAdapter.notifyDataSetChanged();
                list.removeAllViews();
                list.getAdapter().notifyDataSetChanged();
                head_name.setEnabled(false);
                head_name.setText("Select a menu to edit...");
            }
        }
    };


    private void fetch(){
        Message m = new Message(Message.Direction.CLIENT_TO_SERVER, "FETCH_CHEF_MENU");
        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            FoodMenu.FetchMenuAsyncTask fetchMenuAsyncTask = new FoodMenu.FetchMenuAsyncTask();
            fetchMenuAsyncTask.execute(ep);
        } catch (Exception e) {}
    }

    View.OnClickListener update_menu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message m = new Message(Message.Direction.CLIENT_TO_SERVER, "UPD_CHEF_MENU");
            m.putProperty("MENU_DATA", FoodMenu.this.menus);
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
                FoodMenu.UpdateMenuAsyncTask updateMenuAsyncTask = new FoodMenu.UpdateMenuAsyncTask();
                updateMenuAsyncTask.execute(ep);
            } catch (Exception e) {}
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

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("UPD_MENU_RESP")){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(dialog!=null || dialog.isShowing()){
                        dialog.hide();
                        dialog=null;
                    }
                    Toast.makeText(getActivity(),"Food menu updated succcessfully",Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(m.getMsg_type().equals("RESP_CHEF_MENU")){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(dialog!=null || dialog.isShowing()){
                        dialog.hide();
                        dialog=null;
                    }
                    FoodMenu.this.menus = (ArrayList<Tree>) m.getProperty("MENU_TREE");
                    if(FoodMenu.this.menus==null) {
                        FoodMenu.this.menus = new ArrayList<Tree>();
                    }
                    menulist.removeAllViews();
                    menuAdapter.notifyDataSetChanged();
                }
            });
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
            holder.text.setText(currentTree.getRoot().getChildren().get(position).getNodeText());

            holder.text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    currentTree.getRoot().getChildren().get(position).setNodeText(holder.text.getText().toString());
                }
            });

            holder.delB.setOnClickListener(v1 -> delete(position) );
            holder.nextL.setOnClickListener(v1 -> edit(position));
        }

        void delete(int position) {
            currentTree.getRoot().getChildren().remove(position);
            list.removeAllViews();
            RecyclerViewFoodAdapter.this.notifyDataSetChanged();
        }

        void edit(int position){
            Fragment fragment = new FoodMenu_2("Food",currentTree.getRoot().getChildren().get(position),menus);

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            fragment.setTargetFragment(FoodMenu.this, 9999);
            ft.add(R.id.fragmentViewPort,fragment);
            ft.addToBackStack(null);
            ft.commit();;
        }

        @Override
        public int getItemCount() {
            if(currentTree==null)return 0;
            return currentTree.getRoot().getChildren().size();
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

    public static ProgressDialog dialog;
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

    class FetchMenuAsyncTask extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Getting your menu. Please wait !");
            dialog.show();
        }

        public FetchMenuAsyncTask() {
            dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }

    class MenuRecyclerAdapter extends RecyclerView.Adapter<MenuRecyclerAdapter.ViewHolder>{

        ViewHolder activeElement;

        public void setActiveElementText(String text){
            if(activeElement!=null)
                activeElement.menu_name.setText(text);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.menuname_list_item_design,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.menu_name.setText(menus.get(position).getRoot().getNodeText());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(activeElement!=null) {
                        activeElement.menu_name.setTypeface(Typeface.DEFAULT);
                        activeElement.menu_name.setTextColor(Color.parseColor("#878787"));
                    }

                    holder.menu_name.setTypeface(Typeface.DEFAULT_BOLD);
                    holder.menu_name.setTextColor(Color.BLACK);
                    activeElement = holder;
                    currentTree=menus.get(position);
                    head_name.setText(menus.get(position).getRoot().getNodeText());
                    head_name.setEnabled(true);
                    list.removeAllViews();
                    list.getAdapter().notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return menus.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView menu_name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                menu_name = itemView.findViewById(R.id.text);
            }
        }

    }

}
