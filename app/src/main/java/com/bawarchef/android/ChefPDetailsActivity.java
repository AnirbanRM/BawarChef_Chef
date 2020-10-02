package com.bawarchef.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefIdentity;

import java.util.Calendar;

public class ChefPDetailsActivity extends AppCompatActivity {

    EditText regNo,fname,lname,dob,resAddr,resCity,resState,resPin,mailAddr,mailCity,mailState,mailPin,mob,email,altmob,emermob,aadhar;
    char gender = 'M';
    ImageView male,female;
    CheckBox mailAddrCB;
    boolean mailAddrisResAddr=false;
    MobileClient.MessageProcessor defaultMessageProcessor;
    ConstraintLayout next;
    ChefIdentity ci = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_p_details);

        next = findViewById(R.id.pdetNext);
        next.setOnClickListener(nextClicked);

        regNo = findViewById(R.id.regNoBox);
        fname = findViewById(R.id.fnamebox);
        lname = findViewById(R.id.lnamebox);
        dob = findViewById(R.id.dobbox);
        resAddr = findViewById(R.id.resaddrbox);
        resCity = findViewById(R.id.rescitybox);
        resState = findViewById(R.id.resstatebox);
        resPin = findViewById(R.id.respinbox);
        mailAddr = findViewById(R.id.mailaddrbox);
        mailCity = findViewById(R.id.mailcitybox);
        mailState = findViewById(R.id.mailstatebox);
        mailPin = findViewById(R.id.mailpinbox);
        mob = findViewById(R.id.mobbox);
        email = findViewById(R.id.emailbox);
        altmob = findViewById(R.id.altmobbox);
        emermob = findViewById(R.id.emermobbox);
        male = findViewById(R.id.malebut);
        female = findViewById(R.id.femalebut);
        aadhar = findViewById(R.id.aadharbox);
        mailAddrCB = findViewById(R.id.mailAddr_CB);
        mailAddrCB.setOnCheckedChangeListener(mailaddrCheckedChanged);

        TextView[] resAddrArr = new TextView[]{resAddr,resState,resCity,resPin};
        for(TextView i : resAddrArr)
            i.addTextChangedListener(resAddrTextWatcher);

        male.setImageTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
        female.setImageTintList(ColorStateList.valueOf(Color.parseColor("#b5b5b5")));

        Intent i = getIntent();
        ci = (ChefIdentity) i.getSerializableExtra("DATA");
        setFields(ci);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((ThisApplication)getApplication()).setMessageProcessor(defaultMessageProcessor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        defaultMessageProcessor = ((ThisApplication)getApplication()).getMessageProcessor();
        ((ThisApplication)getApplication()).setMessageProcessor(processor);
        ((ThisApplication)getApplication()).setCurrentContext(this);
    }

    CheckBox.OnCheckedChangeListener mailaddrCheckedChanged = new CheckBox.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            View[] mailAddrArr = new View[]{mailAddr,mailCity,mailState,mailPin};
            mailAddrisResAddr = isChecked;
            for(View i: mailAddrArr)
                i.setEnabled(!isChecked);
            if(isChecked)
                resAddrTextWatcher.afterTextChanged(null);
        }
    };

    TextWatcher resAddrTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if(mailAddrisResAddr){
                mailAddr.setText(resAddr.getText());
                mailCity.setText(resCity.getText());
                mailState.setText(resState.getText());
                mailPin.setText(resPin.getText());
            }

        }
    };

    View.OnClickListener calendar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(ChefPDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    dob.setText(dayOfMonth+"/"+(month+1)+"/"+year);
                }
            },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        }
    };

    View.OnClickListener genderChange = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId()==male.getId()) {
                male.setImageTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
                female.setImageTintList(ColorStateList.valueOf(Color.parseColor("#b5b5b5")));
                gender = 'M';
            }
            else if(v.getId()==female.getId()) {
                female.setImageTintList(ColorStateList.valueOf(Color.parseColor("#009688")));
                male.setImageTintList(ColorStateList.valueOf(Color.parseColor("#b5b5b5")));
                gender = 'F';
            }
        }
    };

    private void setFields(ChefIdentity ci) {
        regNo.setText(ci.regNo);
        fname.setText(ci.fname);
        lname.setText(ci.lname);
        dob.setText(ci.dob);
        resAddr.setText(ci.resAddr.address);
        resCity.setText(ci.resAddr.city);
        resState.setText(ci.resAddr.state);
        resPin.setText(ci.resAddr.pinNo);
        mailAddr.setText(ci.mailAddr.address);
        mailCity.setText(ci.mailAddr.city);
        mailState.setText(ci.mailAddr.state);
        mailPin.setText(ci.mailAddr.pinNo);
        mob.setText(ci.mob);
        email.setText(ci.email);
        altmob.setText(ci.altmob);
        emermob.setText(ci.emermob);
        aadhar.setText(ci.aadhar);
        gender = ci.gender;
        genderChange.onClick(gender=='M'?male:female);

        View[] permanentFields = new View[]{regNo,fname,lname,dob,aadhar};
        for(View v : permanentFields)
            v.setEnabled(false);
    }

    MobileClient.MessageProcessor processor = new MobileClient.MessageProcessor() {
        @Override
        public void process(Message m) {
            if(m.getMsg_type().equals("UPD_PDET_RESP")){
                if(m.getProperty("RESULT").equals("SUCCESS")){
                    Intent i = new Intent(ChefPDetailsActivity.this,ChefLoginDetails.class);
                    startActivity(i);
                }else
                {
                    runOnUiThread(()->{
                        Toast.makeText(((ThisApplication)getApplication()).getCurrentContext(),"Invalid entry !",Toast.LENGTH_SHORT).show();
                    });
                }
            }

        }
    };

    View.OnClickListener nextClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            ci.resAddr.address = resAddr.getText().toString();
            ci.resAddr.city = resCity.getText().toString();
            ci.resAddr.state = resState.getText().toString();
            ci.resAddr.pinNo = resPin.getText().toString();

            ci.mailAddr.address = mailAddr.getText().toString();
            ci.mailAddr.city = mailCity.getText().toString();
            ci.mailAddr.state = mailState.getText().toString();
            ci.mailAddr.pinNo = mailPin.getText().toString();

            ci.mob = mob.getText().toString();
            ci.email = email.getText().toString();
            ci.altmob = altmob.getText().toString();
            ci.emermob = emermob.getText().toString();

            Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"UPD_PDET");
            m.putProperty("IDENTITY",ci);

            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getApplication()).mobileClient.getCrypto_key());
                new Thread(() -> {
                    ((ThisApplication)getApplication()).mobileClient.send(ep);
                }).start();
            }catch (Exception e){}
        }
    };



}