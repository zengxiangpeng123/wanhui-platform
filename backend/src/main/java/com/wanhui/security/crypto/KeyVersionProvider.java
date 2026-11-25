package com.wanhui.security.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public final class KeyVersionProvider {
    private static final int AES_KEY_BYTES = 32;
    private static final int HMAC_KEY_BYTES = 32;
    private final int currentVersion;
    private final SecretKey encryptionKey;
    private final SecretKey hmacKey;

    public KeyVersionProvider(int currentVersion, byte[] encryptionKey, byte[] hmacKey) {
        if (currentVersion < 1) {
            throw new IllegalArgumentException("currentVersion must be positive");
        }
        if (encryptionKey == null || encryptionKey.length != AES_KEY_BYTES) {
            throw new IllegalArgumentException("AES-GCM key must be 32 bytes");
        }
        if (hmacKey == null || hmacKey.length != HMAC_KEY_BYTES) {
            throw new IllegalArgumentException("HMAC key must be 32 bytes");
        }
        this.currentVersion = currentVersion;
        this.encryptionKey = new SecretKeySpec(Arrays.copyOf(encryptionKey, encryptionKey.length), "AES");
        this.hmacKey = new SecretKeySpec(Arrays.copyOf(hmacKey, hmacKey.length), "HmacSHA256");
    }

    public int currentVersion() {
        return currentVersion;
    }

    public SecretKey encryptionKey(int version) {
        if (version != currentVersion) {
            throw new CryptoException("Unsupported encryption key version: " + version);
        }
        return encryptionKey;
    }

    public SecretKey hmacKey(int version) {
        if (version != currentVersion) {
            throw new CryptoException("Unsupported HMAC key version: " + version);
        }
        return hmacKey;
    }
}
