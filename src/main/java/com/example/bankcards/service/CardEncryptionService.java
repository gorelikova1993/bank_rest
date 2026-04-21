package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class CardEncryptionService {
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    private static final int IV_LENGTH = 16;
    
    private String key;
    
    public CardEncryptionService(@Value("${app.encryption.key}") String key) {
        this.key = key;
        validateKey(key);
    }
    
    public String encrypt(String rawValue) {
        try {
            byte[] iv = generateIv();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            
            byte[] encryptedBytes =
                    cipher.doFinal(rawValue.getBytes(StandardCharsets.UTF_8));
            
            byte[] ivAndEncrypted = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, ivAndEncrypted, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, ivAndEncrypted, IV_LENGTH, encryptedBytes.length);
            
            return Base64.getEncoder().encodeToString(ivAndEncrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: ", e);
        }
    }
    
    public String decrypt(String encryptedValue){
        try {
            byte[] ivAndEncrypted = Base64.getDecoder().decode(encryptedValue);
            
            byte[] iv = Arrays.copyOfRange(ivAndEncrypted, 0, IV_LENGTH);
            byte[] encryptedBytes = Arrays.copyOfRange(ivAndEncrypted, IV_LENGTH, ivAndEncrypted.length);
            
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            
            byte[] decryptedBytes =
                    cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: ", e);
        }
    }
    
    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    
    private void validateKey(String key) {
        int length = key.getBytes(StandardCharsets.UTF_8).length;
        if (length != 16 && length != 24 && length != 32) {
            throw new IllegalArgumentException(
                    "AES key must be 16, 24, or 32 bytes long"
            );
        }
    }
}
