package com.bawarchef.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bawarchef.Containers.UserIdentity;

public class RegistrationUserActivity extends AppCompatActivity {

    Button signupButton;
    EditText fNameBox,lNameBox,mobBox,emailBox;
    TextView loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        ((ThisApplication)getApplication()).setCurrentContext(this);
        signupButton = findViewById(R.id.signupb);
        loginButton = findViewById(R.id.signinb);
        loginButton.setOnClickListener(loginclicked);
        signupButton.setOnClickListener(signupClicked);

        fNameBox = findViewById(R.id.fnameBox);
        lNameBox = findViewById(R.id.lnameBox);
        mobBox = findViewById(R.id.mobBox);
        emailBox = findViewById(R.id.emailBox);
    }

    View.OnClickListener signupClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ThisApplication.currentUserProfile.setUserIdentity(new UserIdentity());
            ThisApplication.currentUserProfile.getUserIdentity().fname = fNameBox.getText().toString();
            ThisApplication.currentUserProfile.getUserIdentity().lname = lNameBox.getText().toString();
            ThisApplication.currentUserProfile.getUserIdentity().mob = mobBox.getText().toString();
            ThisApplication.currentUserProfile.getUserIdentity().email = emailBox.getText().toString();

            Intent i = new Intent(RegistrationUserActivity.this,UserLoginDetails.class);
            startActivity(i);
        }
    };

    View.OnClickListener loginclicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(RegistrationUserActivity.this,MainActivity.class);
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
