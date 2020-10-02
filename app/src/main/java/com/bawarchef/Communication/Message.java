package com.bawarchef.Communication;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {

    public enum Direction{SERVER_TO_CLIENT,CLIENT_TO_SERVER};
    private String msg_type;
    private Direction direction;
    private HashMap<String,Object> container;

    public Message(Direction direction,String msg_type){
        this.direction = direction;
        this.msg_type = msg_type;
        container = new HashMap<String,Object>();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            container.put("#RANDOM#",digest.digest(Long.toString(System.currentTimeMillis()).getBytes()));
        }catch (Exception e){}
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void putProperty(String key, Object value){
        container.put(key,value);
    }

    public boolean removeProperty(String key){
        if(container.containsKey(key)) {
            container.remove(key);
            return true;
        }
        return false;
    }

    public Object getProperty(String key){
        if(container.containsKey(key))
            return container.get(key);
        return null;
    }

    public String[] getPropertyList(){
        ArrayList<String> list = new ArrayList<String>(container.keySet());
        return (String[]) list.toArray();
    }

    public void deleteAllProperty(){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            container.put("#RANDOM#", digest.digest(Long.toString(System.currentTimeMillis()).getBytes()));
        }catch (Exception e){}
        container.clear();
    }
}
