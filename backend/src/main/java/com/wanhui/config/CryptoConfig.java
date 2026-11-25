package com.wanhui.config;

import com.wanhui.security.crypto.FieldEncryptionService;
import com.wanhui.security.crypto.FieldHmacService;
import com.wanhui.security.crypto.KeyVersionProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Bean
    public KeyVersionProvider keyVersionProvider(
            @Value("${wanhui.security.crypto.key-version}") int keyVersion,
            @Value("${wanhui.security.crypto.encryption-key}") String encryptionKey,
            @Value("${wanhui.security.crypto.hmac-key}") String hmacKey
    ) {
        return new KeyVersionProvider(
                keyVersion,
                Base64.getDecoder().decode(encryptionKey),
                Base64.getDecoder().decode(hmacKey)
        );
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
