package com.wanhui.security.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class FieldEncryptionService {
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final KeyVersionProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public FieldEncryptionService(KeyVersionProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public EncryptedField encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    keyProvider.encryptionKey(keyProvider.currentVersion()),
                    new GCMParameterSpec(TAG_BITS, iv)
            );
            return new EncryptedField(
                    cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)),
                    iv,
                    keyProvider.currentVersion()
            );
        } catch (Exception exception) {
            throw new CryptoException("Unable to encrypt field", exception);
        }
    }

    public String decrypt(EncryptedField encrypted) {
        if (encrypted == null || encrypted.ciphertext() == null || encrypted.iv() == null) {
            throw new IllegalArgumentException("encrypted field must not be null");
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keyProvider.encryptionKey(encrypted.keyVersion()),
                    new GCMParameterSpec(TAG_BITS, encrypted.iv())
            );
            return new String(
                    cipher.doFinal(encrypted.ciphertext()),
                    StandardCharsets.UTF_8
            );
        } catch (CryptoException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CryptoException("Unable to decrypt field", exception);
        }
    }

    public String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        String local = email.substring(0, at);
        int visible = Math.min(3, local.length());
        return local.substring(0, visible) + "***" + email.substring(at);
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public record EncryptedField(byte[] ciphertext, byte[] iv, int keyVersion) {
    }
}
