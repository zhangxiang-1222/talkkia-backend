package com.talkkia.api.config;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*this config does not implement Jwt token strategy but we generate our own 
SHA-256 token from User Object for each user.*/
@Component
public class JwtTokenUtil implements Serializable {

    // salt
    private String secret = "lg!u*dWsCr?Wi1-NM)09Z_";
    
    public String generateToken(Long userId) {
        return toHexString(getSHA256(userId + secret));
    }
    
    public static byte[] getSHA256(String data) {
    try{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data.getBytes(StandardCharsets.UTF_8));
    }catch(NoSuchAlgorithmException e){
        return e.getMessage().getBytes();
    }
    }
    
    public String toHexString(byte[] hash){
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        
        while(hexString.length() < 32){
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
    
}
