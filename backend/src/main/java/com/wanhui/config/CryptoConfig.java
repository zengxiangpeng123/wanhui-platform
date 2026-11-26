package com.wanhui.config;

import com.wanhui.security.crypto.FieldEncryptionService;
import com.wanhui.security.crypto.FieldHmacService;
import com.wanhui.security.crypto.KmsKeyProvider;
import com.wanhui.security.crypto.KeyRotationService;
import com.wanhui.security.crypto.KeyVersionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.Map;

@Configuration
public class CryptoConfig {

    /**
     * 开发/测试环境从环境变量注入单版本密钥。
     * 生产环境应将此类替换为 KMS 实现（如 AWS KMS / 阿里云 KMS / Vault），
     * 由 KMS 按 version 返回密钥材料，应用侧不持久化明文密钥。
     */
    @Bean
    public KmsKeyProvider kmsKeyProvider(
            @Value("${wanhui.security.crypto.key-version}") int keyVersion,
            @Value("${wanhui.security.crypto.encryption-key}") String encryptionKey,
            @Value("${wanhui.security.crypto.hmac-key}") String hmacKey
    ) {
        KmsKeyProvider.KeyMaterial material = new KmsKeyProvider.KeyMaterial(
                Base64.getDecoder().decode(encryptionKey),
                Base64.getDecoder().decode(hmacKey)
        );
        return new KmsKeyProvider(keyVersion, Map.of(keyVersion, material));
    }

    @Bean
    public KeyVersionProvider keyVersionProvider(KmsKeyProvider kmsKeyProvider) {
        return new KeyVersionProvider(kmsKeyProvider);
    }

    @Bean
    public KeyRotationService keyRotationService(KmsKeyProvider kmsKeyProvider) {
        return new KeyRotationService(kmsKeyProvider);
    }

    @Bean
    public FieldEncryptionService fieldEncryptionService(KeyVersionProvider keyVersionProvider) {
        return new FieldEncryptionService(keyVersionProvider);
    }

    @Bean
    public FieldHmacService fieldHmacService(KeyVersionProvider keyVersionProvider) {
        return new FieldHmacService(keyVersionProvider);
    }
}
