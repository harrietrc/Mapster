package com.mapster.webutils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Harriet on 7/25/2015.
 */
public class Md5Hash {

    private MessageDigest _messageDigest;

    public Md5Hash() throws NoSuchAlgorithmException {
        _messageDigest = MessageDigest.getInstance("MD5");
    }

    public String calculateExpediaSignature(String apiKey, String secretKey, long timeStamp) {
        String plainText = apiKey + secretKey + timeStamp;
        return calculateHashFromPlaintext(plainText);
    }

    public String calculateExpediaSignature(String apiKey, String secretKey) {
        long timeStamp = System.currentTimeMillis() / 1000;
        return calculateExpediaSignature(apiKey, secretKey, timeStamp);
    }

    public String calculateHashFromPlaintext(String plainText) {
        _messageDigest.reset();
        _messageDigest.update(plainText.getBytes());
        byte[] digest = _messageDigest.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String sig = bigInt.toString(16);
        while(sig.length() < 32 )
            sig = "0"+ sig;
        return sig;
    }
}
