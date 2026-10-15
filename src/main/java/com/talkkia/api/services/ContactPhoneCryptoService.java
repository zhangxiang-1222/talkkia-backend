package com.talkkia.api.services;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.talkkia.api.model.contact.ContactHashRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContactPhoneCryptoService {

    private static final int AES_KEY_BYTES = 32;
    private static final int HMAC_SECRET_BYTES = 32;
    private static final int GCM_NONCE_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final String encryptionKeyBase64;
    private final String hmacSecretBase64;

    public ContactPhoneCryptoService(
            @Value("${contacts.phone-encryption-key:}") String encryptionKeyBase64,
            @Value("${contacts.phone-hmac-secret:}") String hmacSecretBase64
    ) {
        this.encryptionKeyBase64 = encryptionKeyBase64;
        this.hmacSecretBase64 = hmacSecretBase64;
    }

    public ContactHashRecord protect(String normalizedPhone) {
        try {
            byte[] encryptionKey = decodeRequiredKey(encryptionKeyBase64, AES_KEY_BYTES);
            byte[] hmacSecret = decodeRequiredKey(hmacSecretBase64, HMAC_SECRET_BYTES);
            byte[] nonce = new byte[GCM_NONCE_BYTES];
            secureRandom.nextBytes(nonce);
            byte[] phoneBytes = normalizedPhone.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(phoneBytes);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
            String phoneHmac = toHex(mac.doFinal(phoneBytes));
            return new ContactHashRecord(phoneHmac, ciphertext, nonce, UUID.randomUUID());
        } catch (GeneralSecurityException e) {
            throw new ContactSyncConfigurationException("Contact sync encryption is unavailable.", e);
        }
    }

    public String decrypt(byte[] ciphertext, byte[] nonce) {
        if (ciphertext == null || nonce == null || nonce.length != GCM_NONCE_BYTES) {
            throw new IllegalArgumentException("Contact ciphertext is invalid.");
        }
        try {
            byte[] encryptionKey = decodeRequiredKey(encryptionKeyBase64, AES_KEY_BYTES);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, nonce));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Contact phone decryption failed.", e);
        }
    }

    private byte[] decodeRequiredKey(String value, int expectedBytes) {
        if (value == null || value.trim().isEmpty()) {
            throw new ContactSyncConfigurationException("Contact sync encryption is unavailable.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value.trim());
            if (decoded.length != expectedBytes) {
                throw new ContactSyncConfigurationException("Contact sync encryption is unavailable.");
            }
            return decoded;
        } catch (IllegalArgumentException e) {
            throw new ContactSyncConfigurationException("Contact sync encryption is unavailable.", e);
        }
    }

    private String toHex(byte[] value) {
        StringBuilder result = new StringBuilder(value.length * 2);
        for (byte current : value) {
            result.append(Character.forDigit((current >>> 4) & 0xF, 16));
            result.append(Character.forDigit(current & 0xF, 16));
        }
        return result.toString();
    }
}
