package com.bawarchef.android.Fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.android.Hierarchy.DataStructure.Node;
import com.bawarchef.android.Hierarchy.DataStructure.Tree;
import com.bawarchef.android.R;
import com.bawarchef.android.ThisApplication;

import java.util.ArrayList;

public class UserChefMenu extends Fragment implements MessageReceiver{

    View v;
    String chefID;
    String name;
    Bitmap dp_img;

    public UserChefMenu(String id, String name, Bitmap dp) {
        this.chefID = id;
        this.name = name;
        this.dp_img = dp;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.user_chefmenu,container,false);
        return v;
    }

    TextView name_box;
    ImageView dp;
    ConstraintLayout body;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name_box = view.findViewById(R.id.name);
        dp = view.findViewById(R.id.dp);
        body = view.findViewById(R.id.body);

        name_box.setText(name);
        dp.setImageBitmap(dp_img);

        Message newm = new Message(Message.Direction.CLIENT_TO_SERVER,"FETCH_CHEF_MENU");
        newm.putProperty("chefID", chefID);

        try {
            EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(newm), ((ThisApplication) getActivity().getApplication()).mobileClient.getCrypto_key());
            AsyncExecutor executor = new AsyncExecutor();
            executor.execute(ep);
        }catch (Exception e){}
    }

    @Override
    public void process(Message m) {
        if(m.getMsg_type().equals("RESP_CHEF_MENU")){
            getActivity().runOnUiThread(() -> {
                setMenuPreview((Tree)m.getProperty("CHEF_MENU"));

                if(dialog!=null && dialog.isShowing()){
                    dialog.dismiss();
                    dialog = null;
                }
            });
        }
    }

    private void setMenuPreview(Tree menuTree){
        ArrayList<Node> categories = menuTree.getRoot().getChildren();
        ArrayList<TextView> categoriesBox = new ArrayList<TextView>();

        for(int i = 0; i<categories.size(); i++)
            categoriesBox.add(getCategoryBox(categories.get(i).getNodeText()));

        View lastNode=null;
        for(int i = 0; i<categoriesBox.size(); i++){
            body.addView(categoriesBox.get(i));
            ConstraintSet cs = new ConstraintSet();
            cs.clone(body);

            cs.constrainWidth(categoriesBox.get(i).getId(), ConstraintSet.MATCH_CONSTRAINT);
            cs.connect(categoriesBox.get(i).getId(), ConstraintSet.START, body.getId(), ConstraintSet.START, 70);
            cs.connect(categoriesBox.get(i).getId(), ConstraintSet.END, body.getId(), ConstraintSet.END, 70);

            if(i==0)
                cs.connect(categoriesBox.get(i).getId(), ConstraintSet.TOP, body.getId(), ConstraintSet.TOP, 50);

            else
                cs.connect(categoriesBox.get(i).getId(), ConstraintSet.TOP, lastNode.getId(), ConstraintSet.BOTTOM, 100);

            cs.applyTo(body);
            lastNode = setFoodItemsUnderCategory(categoriesBox.get(i),categories.get(i).getChildren());
        }
    }

    private View setFoodItemsUnderCategory(View textView, ArrayList<Node> foods) {
        ArrayList<TextView> foodsBox = new ArrayList<TextView>();

        for(int i = 0; i<foods.size(); i++)
            foodsBox.add(getFoodItemBox(foods.get(i).getNodeText()));

        View lastNode=textView;
        for(int i = 0; i<foodsBox.size(); i++){
            body.addView(foodsBox.get(i));
            ConstraintSet cs = new ConstraintSet();
            cs.clone(body);

            cs.constrainWidth(foodsBox.get(i).getId(), ConstraintSet.MATCH_CONSTRAINT);
            cs.connect(foodsBox.get(i).getId(), ConstraintSet.START, body.getId(), ConstraintSet.START, 150);
            cs.connect(foodsBox.get(i).getId(), ConstraintSet.END, body.getId(), ConstraintSet.END, 70);

            cs.connect(foodsBox.get(i).getId(), ConstraintSet.TOP, lastNode.getId(), ConstraintSet.BOTTOM, 70);
            cs.applyTo(body);
            lastNode = foodsBox.get(i);
        }
        return lastNode;
    }

    private TextView getCategoryBox(String content){
        TextView tv = new TextView(this.getActivity());
        tv.setText(content);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(24);
        tv.setTypeface(ResourcesCompat.getFont(getActivity(),R.font.raleway_bold));
        tv.setId(View.generateViewId());
        return tv;
    }

    private TextView getFoodItemBox(String content){
        TextView tv = new TextView(this.getActivity());
        tv.setText(content);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(18);
        tv.setTypeface(ResourcesCompat.getFont(getActivity(),R.font.raleway_regular));
        tv.setId(View.generateViewId());
        return tv;
    }















    private ProgressDialog dialog;
    class AsyncExecutor extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading Chef's menu... Please wait !");
            dialog.show();
        }


        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getActivity().getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }


}
