package com.wanhui.security.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

public final class KeyVersionProvider {
    private final KmsKeyProvider kmsKeyProvider;

    public KeyVersionProvider(KmsKeyProvider kmsKeyProvider) {
        if (kmsKeyProvider == null) {
            throw new IllegalArgumentException("kmsKeyProvider must not be null");
        }
        this.kmsKeyProvider = kmsKeyProvider;
    }

    /**
     * 兼容单版本配置的构造器。新代码应使用 KmsKeyProvider 注入多版本密钥。
     */
    public KeyVersionProvider(int currentVersion, byte[] encryptionKey, byte[] hmacKey) {
        this(new KmsKeyProvider(
                currentVersion,
                Map.of(currentVersion, new KmsKeyProvider.KeyMaterial(encryptionKey, hmacKey))
        ));
    }

    public int currentVersion() {
        return kmsKeyProvider.currentVersion();
    }

    public boolean supports(int version) {
        return kmsKeyProvider.supports(version);
    }

    public SecretKey encryptionKey(int version) {
        return new SecretKeySpec(kmsKeyProvider.keyMaterial(version).encryptionKey(), "AES");
    }

    public SecretKey hmacKey(int version) {
        return new SecretKeySpec(kmsKeyProvider.keyMaterial(version).hmacKey(), "HmacSHA256");
    }
}
