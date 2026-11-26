package com.wanhui.security.crypto;

import java.util.Arrays;
import java.util.Map;

/**
 * 密钥材料来源抽象。
 * 生产环境应由 KMS（AWS KMS / 阿里云 KMS / Vault 等）实现本类，仅在内存中暴露指定版本的密钥材料；
 * 开发与测试环境使用构造注入的静态密钥。此类本身不生成或持久化密钥。
 */
public class KmsKeyProvider {
    private static final int AES_KEY_BYTES = 32;
    private static final int HMAC_KEY_BYTES = 32;

    private final int currentVersion;
    private final Map<Integer, KeyMaterial> keys;

    public KmsKeyProvider(int currentVersion, Map<Integer, KeyMaterial> keys) {
        if (currentVersion < 1) {
            throw new IllegalArgumentException("currentVersion must be positive");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys must not be empty");
        }
        if (!keys.containsKey(currentVersion)) {
            throw new IllegalArgumentException("currentVersion must exist in keys");
        }
        this.currentVersion = currentVersion;
        this.keys = Map.copyOf(keys);
    }

    public int currentVersion() {
        return currentVersion;
    }

    public KeyMaterial keyMaterial(int version) {
        KeyMaterial material = keys.get(version);
        if (material == null) {
            throw new CryptoException("Unsupported key version: " + version);
        }
        return material;
    }

    public boolean supports(int version) {
        return keys.containsKey(version);
    }

    public record KeyMaterial(byte[] encryptionKey, byte[] hmacKey) {
        public KeyMaterial {
            if (encryptionKey == null || encryptionKey.length != AES_KEY_BYTES) {
                throw new IllegalArgumentException("AES-GCM key must be 32 bytes");
            }
            if (hmacKey == null || hmacKey.length != HMAC_KEY_BYTES) {
                throw new IllegalArgumentException("HMAC key must be 32 bytes");
            }
            encryptionKey = Arrays.copyOf(encryptionKey, encryptionKey.length);
            hmacKey = Arrays.copyOf(hmacKey, hmacKey.length);
        }

        @Override
        public byte[] encryptionKey() {
            return Arrays.copyOf(encryptionKey, encryptionKey.length);
        }

        @Override
        public byte[] hmacKey() {
            return Arrays.copyOf(hmacKey, hmacKey.length);
        }
    }
}
