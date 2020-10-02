package com.bawarchef.Communication;

import com.bawarchef.Communication.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue extends ConcurrentLinkedQueue<Message> {

    public static abstract class OnMessageArrivalListener{
        public abstract void OnArrival();
    }

    private OnMessageArrivalListener listener=null;

    public void addToQueue(Message m){
        add(m);
        if(listener!=null)
            if(size()==1)
                new Thread(()->{listener.OnArrival();}).start();
    }

    public long getQueueLength(){
        return size();
    }

    public Message getLastMessage(){
        return remove();
    }

    public void setOnMessageArrival(OnMessageArrivalListener listener){
        this.listener = listener;
    }

    public void removeOnMessageArrival(){
        this.listener = null;
    }

    public OnMessageArrivalListener getOnMessageArrivalListener(){
        return listener;
    }



}
