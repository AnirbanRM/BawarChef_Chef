package com.bawarchef.Containers;

import java.io.Serializable;

public class BroadcastReply implements Serializable {
    public String chefID;
    public String chefName;
    public String message;

    public BroadcastReply(String chefID, String chefName, String message){
        this.chefID = chefID;
        this.message = message;
        this.chefName = chefName;
    }
}
