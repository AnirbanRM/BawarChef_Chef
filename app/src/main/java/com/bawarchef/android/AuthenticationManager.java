package com.bawarchef.android;

import android.util.Log;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.ObjectByteCode;
import com.bawarchef.Containers.UserIdentity;

public abstract class AuthenticationManager {

    public abstract void onSuccessResponse(Message m);
    public abstract void onFailureResponse(Message m);

    MobileClient currentClient;
    MobileClient.MessageProcessor defaultMessageProcessor;

    public AuthenticationManager(MobileClient mobileClient){
        this.currentClient = mobileClient;
    }


    public void waitAndWork() {
        defaultMessageProcessor = currentClient.getMessageProcessor();
        currentClient.setMessageProcessor(authMessageProcessor);
    }

    MobileClient.MessageProcessor authMessageProcessor = new MobileClient.MessageProcessor(){

        @Override
        public void process(Message m) {
            if(m.getMsg_type().equals("CHALLENGE->CLIENT"))
                step1(m);
            if(m.getMsg_type().equals("AUTH_ACK")){
                step2(m);
            }
        }

        private void step1(Message m) {
            Message t = new Message(Message.Direction.CLIENT_TO_SERVER,"AUTH->RESPONSE");
            t.putProperty("IDENTITY",ThisApplication.currentUserProfile.getCrypto_Key());

            if(ThisApplication.currentUserProfile.getType()== CurrentUserProfile.Type.UNREGISTERED)
                handleUnRegistered(t);
            else
                handleRegistered(t);
            try {
                EncryptedPayload ep = new EncryptedPayload(ObjectByteCode.getBytes(t), currentClient.getCrypto_key());
                currentClient.send(ep);
            }catch (Exception e){Log.e("SERIALIZATION ERROR",e.getMessage());}
            currentClient.setCrypto_key(arrayxor(currentClient.getCrypto_key(),ThisApplication.currentUserProfile.getCrypto_Key()));
        }

        private void step2(Message m) {
            restore();
            if(m.getProperty("RESULT").toString().equals("SUCCESS"))
                success(m);

            else if(m.getProperty("RESULT").toString().equals("FAILURE"))
                fail(m);

        }

        private byte[] arrayxor(byte[] a,byte[] b){
            byte[] t = new byte[a.length];
            for(int i = 0 ; i< t.length; i++)
                t[i] = (byte) (a[i] ^ b[i]);
            return t;
        }

        private void handleRegistered(Message m) {
            if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.CHEF) {
                m.putProperty("CLIENT_TYPE", "CHEF");
                m.putProperty("UNAME",ThisApplication.currentUserProfile.getChefUName());
            }
            else if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.USER) {
                m.putProperty("CLIENT_TYPE", "USER");
                m.putProperty("UNAME",ThisApplication.currentUserProfile.getUserUName());
            }
            m.putProperty("TYPE","REGISTERED");
            m.putProperty("PWD",ThisApplication.currentUserProfile.getPassword());
        }

        private void handleUnRegistered(Message m) {
            m.putProperty("TYPE","UNREGISTERED");
            if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.CHEF) {
                m.putProperty("RegNo", ThisApplication.currentUserProfile.getRegNo());
                m.putProperty("CLIENT_TYPE","CHEF");
            }

            else if(ThisApplication.currentUserProfile.getClientType()== CurrentUserProfile.ClientType.USER){
                UserIdentity ui = ThisApplication.currentUserProfile.getUserIdentity();
                m.putProperty("FNAME",ui.fname);
                m.putProperty("LNAME",ui.lname);
                m.putProperty("MOB",ui.mob);
                m.putProperty("EMAIL",ui.email);
                m.putProperty("UNAME",ThisApplication.currentUserProfile.getUserUName());
                m.putProperty("PWD",ThisApplication.currentUserProfile.getPassword());
                m.putProperty("CLIENT_TYPE","USER");
            }
        }

        private void restore(){
            currentClient.setMessageProcessor(defaultMessageProcessor);
        }

        private void fail(Message m){
            onFailureResponse(m);
        }

        private void success(Message m){
            onSuccessResponse(m);
        }

    };






}
