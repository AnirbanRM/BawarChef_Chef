package com.bawarchef.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;

import com.bawarchef.Containers.UserIdentity;
import com.bawarchef.android.Fragments.Cart;
import com.bawarchef.android.Hierarchy.DataStructure.CartItem;
import com.bawarchef.android.Hierarchy.DataStructure.FoodNode;
import com.bawarchef.android.Hierarchy.DataStructure.Node;

import java.io.InputStream;
import java.util.ArrayList;

public class FoodCustomize_dialog extends Activity {

    Node foodnode;

    TextView foodName,category;
    EditText qty;
    ConstraintLayout custoBody;
    ImageButton addButton;
    ArrayList<CustomRadioGroup> radioGroups;
    Spinner unitSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_food_custo);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        foodName = findViewById(R.id.foodname);
        category = findViewById(R.id.category);
        qty = findViewById(R.id.qty);
        custoBody = findViewById(R.id.custoBody);

        addButton = findViewById(R.id.addCart);
        addButton.setOnClickListener(addClicked);

        radioGroups = new ArrayList<CustomRadioGroup>();

        Intent i = getIntent();
        foodnode = (Node) i.getSerializableExtra("DATA");

        unitSpinner = findViewById(R.id.unit_text);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.ingredient_units,R.layout.customspinner1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        show();
    }


    View.OnClickListener addClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CartItem cartItem = new CartItem(category.getText().toString(),foodName.getText().toString(),((FoodNode)foodnode).getBaseP(),((FoodNode)foodnode).getSubP(),qty.getText().toString()+" "+unitSpinner.getSelectedItem().toString());
            for(CustomRadioGroup customRadioGroup : radioGroups)
                cartItem.addCustomization(customRadioGroup.getgName(),customRadioGroup.getSelectedText());

            ThisApplication.currentUserProfile.getCart().getCartItems().add(cartItem);
            ThisApplication.currentUserProfile.getCart().refresh();
            FoodCustomize_dialog.this.finish();
        }
    };

    private void show(){
        foodName.setText(foodnode.getNodeText());
        category.setText(foodnode.getParent().getNodeText());

        ArrayList<Node> customizations = foodnode.getChildren();
        ArrayList<TextView> customizationsBox = new ArrayList<TextView>();

        for(int i = 0; i<customizations.size(); i++)
            customizationsBox.add(getCustomizationBox(customizations.get(i).getNodeText()));

        View lastNode=null;
        for(int i = 0; i<customizationsBox.size(); i++){
            custoBody.addView(customizationsBox.get(i));
            ConstraintSet cs = new ConstraintSet();
            cs.clone(custoBody);

            cs.constrainWidth(customizationsBox.get(i).getId(), ConstraintSet.MATCH_CONSTRAINT);
            cs.connect(customizationsBox.get(i).getId(), ConstraintSet.START, custoBody.getId(), ConstraintSet.START, 70);
            cs.connect(customizationsBox.get(i).getId(), ConstraintSet.END, custoBody.getId(), ConstraintSet.END, 70);

            if(i==0)
                cs.connect(customizationsBox.get(i).getId(), ConstraintSet.TOP, custoBody.getId(), ConstraintSet.TOP, 50);

            else
                cs.connect(customizationsBox.get(i).getId(), ConstraintSet.TOP, lastNode.getId(), ConstraintSet.BOTTOM, 100);

            cs.applyTo(custoBody);
            lastNode = setOptionsUnderCustomization(customizationsBox.get(i),customizations.get(i).getChildren());
        }
    }

    private View setOptionsUnderCustomization(View textView, ArrayList<Node> options) {
        CustomRadioGroup radioGroup = new CustomRadioGroup(this,((TextView)textView).getText().toString());
        radioGroups.add(radioGroup);
        radioGroup.setId(View.generateViewId());
        custoBody.addView(radioGroup);

        for(int i = 0; i<options.size(); i++)
            radioGroup.addView(getOptionItemRadio(options.get(i).getNodeText()));

        ConstraintSet cs = new ConstraintSet();
        cs.clone(custoBody);

        cs.constrainWidth(radioGroup.getId(), ConstraintSet.MATCH_CONSTRAINT);

        cs.connect(radioGroup.getId(),ConstraintSet.START,custoBody.getId(), ConstraintSet.START,100);
        cs.connect(radioGroup.getId(),ConstraintSet.END,custoBody.getId(), ConstraintSet.END, 50);
        cs.connect(radioGroup.getId(),ConstraintSet.TOP,textView.getId(), ConstraintSet.BOTTOM, 20);

        cs.applyTo(custoBody);

        return radioGroup;
    }

    private TextView getCustomizationBox(String content){
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(24);
        tv.setTypeface(ResourcesCompat.getFont(this,R.font.raleway_bold));
        tv.setId(View.generateViewId());
        return tv;
    }

    private RadioButton getOptionItemRadio(String content){
        RadioButton rb = new RadioButton(this);
        rb.setText(content);
        rb.setTextColor(Color.BLACK);
        rb.setTextSize(18);
        rb.setTypeface(ResourcesCompat.getFont(this,R.font.raleway_regular));
        rb.setId(View.generateViewId());

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,15,0,15);

        rb.setLayoutParams(layoutParams);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[] {
                        Color.BLACK,
                        getColor(R.color.button_color)
                }
        );

        rb.setButtonTintList(colorStateList);
        return rb;
    }

    class CustomRadioGroup extends RadioGroup{

        private String gName;
        private ArrayList<RadioButton> radioButtons;

        public CustomRadioGroup(Context context,String gName) {
            super(context);
            this.gName = gName;
            radioButtons = new ArrayList<RadioButton>();
        }

        @Override
        public void addView(View child) {
            super.addView(child);
            radioButtons.add((RadioButton) child);
        }

        public String getgName(){
            return gName;
        }

        public String getSelectedText(){
            for(RadioButton b : radioButtons){
                if(b.isChecked())
                    return b.getText().toString();
            }
            return null;
        }
    }

}
