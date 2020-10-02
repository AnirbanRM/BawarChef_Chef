package com.bawarchef.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bawarchef.Communication.Message;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class RegistrationActivity extends AppCompatActivity {

    Button signupButton;
    EditText registrationBox;
    TextView loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ((ThisApplication)getApplication()).setCurrentContext(this);
        registrationBox = findViewById(R.id.reg_no);
        signupButton = findViewById(R.id.signupb);
        loginButton = findViewById(R.id.signinb);
        loginButton.setOnClickListener(loginclicked);
        signupButton.setOnClickListener(signupClicked);
    }

    View.OnClickListener signupClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            signupButton.setText("Please Wait...");
            signupButton.setEnabled(false);
            new Thread(() -> ((ThisApplication) getApplication()).mobileClient.connect(registrationBox.getText().toString())).start();
        }
    };

    View.OnClickListener loginclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(RegistrationActivity.this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    };

    public void failedAuth(){
        runOnUiThread(()->{
            signupButton.setText("Sign Up");
            signupButton.setEnabled(true);
        });
    }
}
