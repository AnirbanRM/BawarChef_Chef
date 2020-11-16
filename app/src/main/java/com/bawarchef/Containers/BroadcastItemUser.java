package com.bawarchef.Containers;

import java.io.Serializable;
import java.util.ArrayList;

public class BroadcastItemUser implements Serializable {

    public String userID;
    public String message;
    public String id;
    public long timestamp=0;

    public ArrayList<BroadcastReply> broadcast_replies;
}
