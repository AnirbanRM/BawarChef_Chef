package com.bawarchef.android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;

public class CircleChange extends Activity {

    TextView circle;
    Button accept,decline;
    String idC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.circ_change);

        Intent i = getIntent();

        String newCircle = i.getStringExtra("newC");
        idC = i.getStringExtra("newI");

        circle = findViewById(R.id.circle);
        circle.setText(newCircle);

        accept = findViewById(R.id.accept);
        decline = findViewById(R.id.decline);

        accept.setOnClickListener(accept_clicked);
        decline.setOnClickListener(decline_clicked);
    }

    View.OnClickListener accept_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Message m = new Message(Message.Direction.CLIENT_TO_SERVER,"CIRCLE_REGISTRATION");
            m.putProperty("CIRCLE_ID",idC);
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(m), ((ThisApplication) getApplication()).mobileClient.getCrypto_key());
                AsyncSender asyncSender = new AsyncSender();
                asyncSender.execute(ep);
                finish();
            }catch(Exception e){}
        }
    };

    View.OnClickListener decline_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DashboardActivity.changeCircle = false;
            finish();
        }
    };

    class AsyncSender extends AsyncTask<EncryptedPayload,Void,Void> {

        @Override
        protected Void doInBackground(EncryptedPayload... encryptedPayloads) {
            ((ThisApplication)getApplication()).mobileClient.send(encryptedPayloads[0]);
            return null;
        }
    }
}
