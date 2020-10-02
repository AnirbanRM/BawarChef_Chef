package com.bawarchef.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.EventLog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.ChefLogin;

import java.security.MessageDigest;

public class ChefLoginDetails extends AppCompatActivity {

    ProgressBar strength;
    EditText uName,pwd,cpwd;
    TextView strScore;
    PasswordComplexityMeter meter;
    ImageButton pwdS,cpwdS;
    ConstraintLayout nextB;
    MobileClient.MessageProcessor defaultMessageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_details);

        uName = findViewById(R.id.usernameBox);
        pwd = findViewById(R.id.passwordBox);
        cpwd = findViewById(R.id.cpasswordBox);
        pwdS = findViewById(R.id.pwdview);
        cpwdS = findViewById(R.id.cpwdview);
        nextB = findViewById(R.id.ldetNext);

        nextB.setOnClickListener(nextClicked);

        pwdS.setOnTouchListener(pwdST);
        cpwdS.setOnTouchListener(cpwdST);

        strength = findViewById(R.id.strength);
        strScore = findViewById(R.id.strNum);
        strength.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#909090")));
        strength.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));

        pwd.addTextChangedListener(pwdChanged);
        cpwd.addTextChangedListener(pwdChanged);

        strength.setProgress(0);
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

    MobileClient.MessageProcessor processor = new MobileClient.MessageProcessor() {
        @Override
        public void process(Message m) {
            if(m.getMsg_type().equals("UPD_L_DET_RESP")){
                if(m.getProperty("RESULT").equals("SUCCESS"))
                    runOnUiThread(()->{
                        try {
                            ((ThisApplication) getApplication()).mobileClient.setDefaultCryptoKey();
                            ((ThisApplication) getApplication()).mobileClient.closeConnection();
                        }catch (Exception e){}

                        Toast.makeText(((ThisApplication)getApplication()).getCurrentContext(),"Login to continue !",Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(((ThisApplication)getApplication()).getCurrentContext(),MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        ((ThisApplication)getApplication()).getCurrentContext().startActivity(i);
                    });
                if(m.getProperty("RESULT").equals("FAILURE"))
                    runOnUiThread(()->Toast.makeText(((ThisApplication)getApplication()).getCurrentContext(),"Invalid entry !",Toast.LENGTH_SHORT).show());
            }
        }
    };

    private int strength(){
        return 50;

    }

    TextWatcher pwdChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if(!pwd.getText().toString().equals(cpwd.getText().toString()))
                cpwd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffff0000")));
            else
                cpwd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e0e0e0")));

            meter=new PasswordComplexityMeter(pwd.getText().toString());
            strength.setProgress(meter.getStrength());
            if(meter.getStrength()==-1)
                strScore.setText("Nil.");
            else
                strScore.setText(meter.getStrength()+"/100");
        }
    };

    View.OnTouchListener pwdST = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()== MotionEvent.ACTION_DOWN)
                pwd.setTransformationMethod(SingleLineTransformationMethod.getInstance());
            if(event.getAction()== MotionEvent.ACTION_UP)
                pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            return true;
        }
    };

    View.OnTouchListener cpwdST = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()== MotionEvent.ACTION_DOWN)
                cpwd.setTransformationMethod(SingleLineTransformationMethod.getInstance());
            if(event.getAction()== MotionEvent.ACTION_UP)
                cpwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            return true;
        }
    };

    View.OnClickListener nextClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(uName.getText().toString().length()==0||pwd.getText().toString().length()==0||cpwd.getText().toString().length()==0) {
                runOnUiThread(() -> Toast.makeText(((ThisApplication) getApplication()).getCurrentContext(), "One or more fields are empty !", Toast.LENGTH_SHORT).show());
                return;
            }
            if(!pwd.getText().toString().equals(cpwd.getText().toString())){
                runOnUiThread(() -> Toast.makeText(((ThisApplication) getApplication()).getCurrentContext(), "Passwords don't match !", Toast.LENGTH_SHORT).show());
                return;
            }

            ChefLogin cl = new ChefLogin();
            cl.regNo = ThisApplication.currentUserProfile.getRegNo();
            cl.uName = uName.getText().toString();
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] arr = md.digest(pwd.getText().toString().getBytes());
                cl.pwd = getHex(arr);
            }catch (Exception e){
                Log.e("LOL",e.getMessage());}

            Message new_m = new Message(Message.Direction.CLIENT_TO_SERVER,"UPD_L_DET");
            new_m.putProperty("IDENTITY",cl);
            new Thread(() -> {
                try {
                    EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(new_m), ((ThisApplication) getApplication()).mobileClient.getCrypto_key());
                    ((ThisApplication)getApplication()).mobileClient.send(ep);
                }catch (Exception e){}
            }).start();
        }
    };

    private static String getHex(byte[] arr) {
        String t = "";
        String[] codes = new String[]{"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        for(int i = 0; i< arr.length; i++) {
            String d="";
            int temp = arr[i];
            if(temp<0)temp+=256;
            d += codes[temp % 16];
            d = codes[temp/16] + d;
            t=t+d;
        }
        return t;
    }


}