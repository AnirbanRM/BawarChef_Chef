package com.bawarchef.android;

import android.app.Application;

import com.bawarchef.Containers.ChefIdentity;
import com.bawarchef.Containers.UserIdentity;

import java.util.HashMap;

public class CurrentUserProfile {

    enum Type{REGISTERED,UNREGISTERED};
    public enum ClientType{X,CHEF,USER};
    private Application appRef;
    private String regNo;
    private String chefUName;
    private String userUName;
    private String password;
    private Type type;
    private ChefIdentity chefIdentity;
    private UserIdentity userIdentity;
    private byte crypto_Key[];
    private HashMap<String,Object> extras;

    ClientType clientType=ClientType.X;

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getChefUName() {
        return chefUName;
    }

    public void setChefUName(String chefUName) {
        this.chefUName = chefUName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public HashMap<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(HashMap<String, Object> extras) {
        this.extras = extras;
    }

    public Application getAppRef() {
        return appRef;
    }

    public void setAppRef(Application appRef) {
        this.appRef = appRef;
    }

    public byte[] getCrypto_Key() {
        return crypto_Key;
    }

    public void setCrypto_Key(byte[] crypto_Key) {
        this.crypto_Key = crypto_Key;
    }

    CurrentUserProfile(Application appRef){
        this.appRef = appRef;
        extras = new HashMap<String, Object>();
    }

    public ChefIdentity getChefIdentity() {
        return chefIdentity;
    }

    public void setChefIdentity(ChefIdentity chefIdentity) {
        this.chefIdentity = chefIdentity;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getUserUName() {
        return userUName;
    }

    public void setUserUName(String userUName) {
        this.userUName = userUName;
    }

    public UserIdentity getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(UserIdentity userIdentity) {
        this.userIdentity = userIdentity;
    }
}
