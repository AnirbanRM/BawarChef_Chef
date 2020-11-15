package com.bawarchef.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bawarchef.Communication.EncryptedPayload;
import com.bawarchef.Communication.Message;
import com.bawarchef.Communication.MessageQueue;
import com.bawarchef.Containers.ChefIdentity;
import com.bawarchef.Containers.UserIdentity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.MessageDigest;

public class MobileClient {

    private ThisApplication appRef;
    private static Socket sock;
    private ObjectInputStream iStream;
    private ObjectOutputStream oStream;

    private byte[] crypto_key=null;
    private MessageQueue messageQueue;
    private MessageProcessor messageProcessor;

    public static abstract class MessageProcessor{
        public abstract void process(Message m);
    }

    MobileClient(ThisApplication appRef){
        this.appRef = appRef;
        messageQueue = new MessageQueue();
        messageQueue.setOnMessageArrival(defaultMessageListener);
        messageProcessor = new MessageProcessor() {
            @Override
            public void process(Message m) {
                Log.e("HANDLED",m.getMsg_type());
            }
        };
        setDefaultCryptoKey();
    }

    public void setDefaultCryptoKey(){
        MessageDigest md5= null;
        try{
            md5 = MessageDigest.getInstance("SHA-256");
        }catch (Exception e){}
        crypto_key =  md5.digest(appRef.getString(R.string.D_KEY_0).getBytes());
    }


    public void initialSocketSetup() throws IOException {
        sock = new Socket(appRef.getString(R.string.SERVER_ADDRESS),Integer.parseInt(appRef.getString(R.string.COMMUNICATION_PORT)));
        oStream = new ObjectOutputStream(sock.getOutputStream());
        iStream = new ObjectInputStream(sock.getInputStream());
    }

    void connect(String regNo){
        params = null;
        setDefaultCryptoKey();
        ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.CHEF);
        ThisApplication.currentUserProfile.setType(CurrentUserProfile.Type.UNREGISTERED);
        ThisApplication.currentUserProfile.setRegNo(regNo);
        new Thread(()->{
            try {
                initialSocketSetup();
            } catch (Exception e) {}

            AuthenticationManager authenticationManager = new AuthenticationManager(MobileClient.this) {
                @Override
                public void onSuccessResponse(Message m) {
                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{
                        Intent i = new Intent(appRef.currentContext,ChefPDetailsActivity.class);
                        i.putExtra("DATA",(Serializable) m.getProperty("CHEF_IDENTITY"));
                        (appRef.currentContext).startActivity(i);
                    });
                }

                @Override
                public void onFailureResponse(Message m) {
                    setDefaultCryptoKey();
                    ((RegistrationActivity)appRef.currentContext).failedAuth();
                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{Toast.makeText(appRef.currentContext,"Invalid Credential !",Toast.LENGTH_SHORT).show();});
                }
            };

            authenticationManager.waitAndWork();
            startListening();
        }).start();
    }

    void connect(String uname,String pwd,boolean pwdHashed){
        params = new Object[]{uname,pwd,pwdHashed};
        setDefaultCryptoKey();
        ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.CHEF);
        ThisApplication.currentUserProfile.setType(CurrentUserProfile.Type.REGISTERED);
        ThisApplication.currentUserProfile.setChefUName(uname);

        if(!pwdHashed) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                pwd = getHex(sha.digest(pwd.getBytes()));
            } catch (Exception e) {
            }
        }
        ThisApplication.currentUserProfile.setPassword(pwd);

        final String pwd2 = pwd;
        new Thread(()->{
            try {
                initialSocketSetup();
            } catch (IOException e) {}

            AuthenticationManager authenticationManager = new AuthenticationManager(MobileClient.this) {
                @Override
                public void onSuccessResponse(Message m) {
                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{
                        saveLoginCredToLocalPref(uname,pwd2, CurrentUserProfile.ClientType.CHEF);
                        Intent i = new Intent(appRef.currentContext,DashboardActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        ThisApplication.currentUserProfile.setChefIdentity((ChefIdentity) m.getProperty("CHEF_IDENTITY"));
                        (appRef.currentContext).startActivity(i);
                    });
                }

                @Override
                public void onFailureResponse(Message m) {
                    setDefaultCryptoKey();
                    if(appRef.currentContext instanceof MainActivity)
                        ((MainActivity)appRef.currentContext).failedAuth();
                    else if(appRef.currentContext instanceof SplashActivity)
                        ((SplashActivity)appRef.currentContext).failedAuth();

                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{Toast.makeText(appRef.currentContext,"Invalid Credential !",Toast.LENGTH_SHORT).show();});
                }
            };

            authenticationManager.waitAndWork();

            startListening();
            }).start();
    }

    void connectAsCusto(){
        params = null;
        setDefaultCryptoKey();
        ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.USER);
        ThisApplication.currentUserProfile.setType(CurrentUserProfile.Type.UNREGISTERED);

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            ThisApplication.currentUserProfile.setPassword(getHex(sha.digest(ThisApplication.currentUserProfile.getPassword().getBytes())));
        } catch (Exception e) { }

        new Thread(()->{
            try {
                initialSocketSetup();
            } catch (Exception e) {}

            AuthenticationManager authenticationManager = new AuthenticationManager(MobileClient.this) {
                @Override
                public void onSuccessResponse(Message m) {
                    ThisApplication.currentUserProfile.setUserIdentity(null);
                    ThisApplication.currentUserProfile.setUserUName(null);
                    ThisApplication.currentUserProfile.setPassword(null);

                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{
                        Intent i = new Intent(appRef.currentContext,MainActivity.class);
                        Toast.makeText(appRef.currentContext,"Login to continue !",Toast.LENGTH_SHORT).show();
                        (appRef.currentContext).startActivity(i);
                    });
                }

                @Override
                public void onFailureResponse(Message m) {
                    setDefaultCryptoKey();
                    ((RegistrationActivity)appRef.currentContext).failedAuth();
                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{Toast.makeText(appRef.currentContext,"Invalid Credential !",Toast.LENGTH_SHORT).show();});
                }
            };

            authenticationManager.waitAndWork();
            startListening();
        }).start();
    }

    void connectAsCusto(String uname,String pwd,boolean pwdHashed){
        params = new Object[]{uname,pwd,pwdHashed};
        setDefaultCryptoKey();
        ThisApplication.currentUserProfile.setClientType(CurrentUserProfile.ClientType.USER);
        ThisApplication.currentUserProfile.setType(CurrentUserProfile.Type.REGISTERED);
        ThisApplication.currentUserProfile.setUserUName(uname);

        if(!pwdHashed) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                pwd = getHex(sha.digest(pwd.getBytes()));
            } catch (Exception e) {
            }
        }
        ThisApplication.currentUserProfile.setPassword(pwd);

        final String pwd2 = pwd;
        new Thread(()->{
            try {
                initialSocketSetup();
            } catch (IOException e) {}

            AuthenticationManager authenticationManager = new AuthenticationManager(MobileClient.this) {
                @Override
                public void onSuccessResponse(Message m) {
                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{
                        saveLoginCredToLocalPref(uname,pwd2, CurrentUserProfile.ClientType.USER);
                        Intent i = new Intent(appRef.currentContext,DashboardUserActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        ThisApplication.currentUserProfile.setUserIdentity((UserIdentity) m.getProperty("USER_IDENTITY"));
                        (appRef.currentContext).startActivity(i);
                    });
                }

                @Override
                public void onFailureResponse(Message m) {
                    setDefaultCryptoKey();
                    if(appRef.currentContext instanceof MainActivity)
                        ((MainActivity)appRef.currentContext).failedAuth();
                    else if(appRef.currentContext instanceof SplashActivity)
                        ((SplashActivity)appRef.currentContext).failedAuth();

                    ((AppCompatActivity)appRef.currentContext).runOnUiThread(()->{Toast.makeText(appRef.currentContext,"Invalid Credential !",Toast.LENGTH_SHORT).show();});
                }
            };

            authenticationManager.waitAndWork();
            startListening();
        }).start();
    }


    private void saveLoginCredToLocalPref(String uname, String pwd, CurrentUserProfile.ClientType clientType) {
        String prefname = clientType== CurrentUserProfile.ClientType.CHEF?"BawarChef_CHEF_AppData":"BawarChef_USER_AppData";
        SharedPreferences sharedPref = appRef.currentContext.getSharedPreferences(prefname,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("UNAME", uname);
        editor.putString("PWD", pwd);
        editor.apply();
    }

    public void setMessageProcessor(MessageProcessor processor){
        this.messageProcessor = processor;
    }

    public MessageProcessor getMessageProcessor(){
        return messageProcessor;
    }

    Object params[];
    public void startListening() {
        if(sock==null)return;
        while(!sock.isClosed()){
            try {
                EncryptedPayload p = (EncryptedPayload) iStream.readObject();
                Message o = p.getDecryptedPayload(crypto_key);
                messageQueue.addToQueue(o);
            } catch (Exception e) {
                Log.e("ERROR",e.toString());
                try {
                    closeConnection();
                }catch (Exception e2){}
                break;
            }
        }
        appRef.reEstablish();
    }

    public byte[] getCrypto_key() {
        return crypto_key;
    }

    public void setCrypto_key(byte[] crypto_key) {
        this.crypto_key = crypto_key;
    }

    MessageQueue.OnMessageArrivalListener defaultMessageListener = new MessageQueue.OnMessageArrivalListener() {
        @Override
        public void OnArrival() {
            if(messageProcessor==null) {
                try {
                    closeConnection();
                }catch (Exception e){}
                return;
            }
            while(messageQueue.size()>0)
                messageProcessor.process(messageQueue.getLastMessage());
        }
    };

    public void closeConnection() throws Exception{
        MobileClient.this.sock.close();
    }


    public void send(EncryptedPayload encryptedPayload){
        try{
            if(sock.isClosed())return;
            oStream.writeUnshared(encryptedPayload);
            oStream.reset();
        }catch (Exception e){ }
    }

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
