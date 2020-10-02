package com.bawarchef.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button sin;
    TextView reg;
    EditText username,password;

    ImageView chefI,custoI;
    TextView chefT,custoT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        sin = findViewById(R.id.signin);
        reg = findViewById(R.id.signup);
        sin.setOnClickListener(signinClicked);
        reg.setOnClickListener(regclicked);

        chefI = findViewById(R.id.chefbut);
        custoI = findViewById(R.id.userbut);
        chefT = findViewById(R.id.chefText);
        custoT = findViewById(R.id.custoText);

        chefI.setOnClickListener(v -> selectType(CurrentUserProfile.ClientType.CHEF));
        custoI.setOnClickListener(v -> selectType(CurrentUserProfile.ClientType.USER));
    }

    void selectType(CurrentUserProfile.ClientType userType){
        if(userType== CurrentUserProfile.ClientType.CHEF){
            chefI.setImageTintList(ColorStateList.valueOf(getColor(R.color.button_color)));
            chefT.setTextColor(getColor(R.color.button_color));
            custoI.setImageTintList(ColorStateList.valueOf(Color.parseColor("#80000000")));
            custoT.setTextColor(Color.parseColor("#80000000"));
            ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.CHEF);
        }
        else if(userType== CurrentUserProfile.ClientType.USER){
            custoI.setImageTintList(ColorStateList.valueOf(getColor(R.color.button_color)));
            custoT.setTextColor(getColor(R.color.button_color));
            chefI.setImageTintList(ColorStateList.valueOf(Color.parseColor("#80000000")));
            chefT.setTextColor(Color.parseColor("#80000000"));
            ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.USER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((ThisApplication)getApplication()).setCurrentContext(this);
    }

    View.OnClickListener regclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.CHEF) {
                Intent i = new Intent(MainActivity.this, RegistrationActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.USER) {
                Intent i = new Intent(MainActivity.this, RegistrationUserActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }
    };

    View.OnClickListener signinClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.X){
                Toast.makeText(MainActivity.this,"Please select account type!",Toast.LENGTH_SHORT).show();
                return;
            }

            sin.setText("Please Wait...");
            sin.setEnabled(false);
            new Thread(() -> {
                if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.CHEF)
                    ((ThisApplication) getApplication()).mobileClient.connect(username.getText().toString(),password.getText().toString(),false);
                if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.USER)
                    ((ThisApplication) getApplication()).mobileClient.connectAsCusto(username.getText().toString(),password.getText().toString(),false);
            }).start();
        }
    };

    public void failedAuth() {
        runOnUiThread(()->{
            sin.setText("Sign in");
            sin.setEnabled(true);
        });
    }
}
