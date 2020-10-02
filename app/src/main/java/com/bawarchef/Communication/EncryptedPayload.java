package com.bawarchef.Communication;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.*;

public class EncryptedPayload implements Serializable {
    byte[] payload=null;
    byte[] iV=null;

    public static class WrongKeyException extends Exception{
        WrongKeyException(){
            super("Wrong Key!");
        }
    }

    public EncryptedPayload(byte[] payload,byte[] key){
        SecureRandom sr = new SecureRandom();
        sr.setSeed(System.currentTimeMillis());
        iV = new byte[128/8];
        sr.nextBytes(iV);
        this.payload = encrypt(payload,key);
    }

    private byte[] encrypt(byte[] payload, byte[] key){
        try {
            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hash(key), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iV);
            ci.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] result = ci.doFinal(payload);
            return result;
        }catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){return null;}
    }

    private byte[] decrypt(byte[] key) throws Exception{
        try {
            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hash(key), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iV);
            ci.init(Cipher.DECRYPT_MODE,secretKeySpec,ivParameterSpec);
            byte[] result = ci.doFinal(payload);
            return result;
        }catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){throw new Exception();}
    }

    public Message getDecryptedPayload(byte[] key) throws WrongKeyException{
        try {
            return ObjectByteCode.getMessage(decrypt(key));
        }catch (Exception e){throw new WrongKeyException();}
    }

    private byte[] hash(byte[] a){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(a);
        }catch (NoSuchAlgorithmException e){}
        return new byte[0];

    }
}
