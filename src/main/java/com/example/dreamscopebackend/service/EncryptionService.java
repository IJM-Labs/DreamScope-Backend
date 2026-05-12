package com.example.dreamscopebackend.service;

import com.example.dreamscopebackend.exception.EncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class EncryptionService {
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;

    public EncryptionService(@Value("${dreamscope.encryption-key:}") String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new EncryptionException("Missing dreamscope.encryption-key. Set a 32 byte Base64 key before starting the backend.");
        }
        byte[] key = decodeKey(encryptionKey);
        if (key.length != 32) {
            throw new EncryptionException("dreamscope.encryption-key must decode to exactly 32 bytes for AES-256.");
        }
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new EncryptionException("Could not encrypt data");
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedText);
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH_BYTES, payload.length);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Could not decrypt data");
        }
    }

    private byte[] decodeKey(String encryptionKey) {
        try {
            return Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException ignored) {
            return encryptionKey.getBytes(StandardCharsets.UTF_8);
        }
    }
}
