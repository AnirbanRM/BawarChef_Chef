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
import com.bawarchef.Containers.UserIdentity;

import java.security.MessageDigest;

public class UserLoginDetails extends AppCompatActivity {

    ProgressBar strength;
    EditText uName,pwd,cpwd;
    TextView strScore;
    PasswordComplexityMeter meter;
    ImageButton pwdS,cpwdS;
    ConstraintLayout nextB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_login_details);

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
            if (uName.getText().toString().length() == 0 || pwd.getText().toString().length() == 0 || cpwd.getText().toString().length() == 0) {
                runOnUiThread(() -> Toast.makeText(((ThisApplication) getApplication()).getCurrentContext(), "One or more fields are empty !", Toast.LENGTH_SHORT).show());
                return;
            }
            if (!pwd.getText().toString().equals(cpwd.getText().toString())) {
                runOnUiThread(() -> Toast.makeText(((ThisApplication) getApplication()).getCurrentContext(), "Passwords don't match !", Toast.LENGTH_SHORT).show());
                return;
            }

            ThisApplication.currentUserProfile.setUserUName(uName.getText().toString());
            ThisApplication.currentUserProfile.setPassword(pwd.getText().toString());
            ((ThisApplication)getApplication()).mobileClient.connectAsCusto();
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
