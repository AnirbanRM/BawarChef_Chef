package com.bawarchef.Communication;

import java.io.*;

public class ObjectByteCode {

    public static class SerializableError extends Exception{
        SerializableError(String a){
            super(a);
        }
    }

    public static byte[] getBytes(Message obj) throws SerializableError{
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }catch (Exception e){throw new SerializableError("Unable to serialize.");}

    }

    public static Message getMessage(byte[] bytes) throws SerializableError{
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Message o = (Message) ois.readObject();
            bais.close();
            return o;
        }catch (Exception e){throw new SerializableError("Unable to deserialize.");}

    }

}
