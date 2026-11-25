package com.wanhui.security.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldEncryptionTest {

    private static final KeyVersionProvider PROVIDER = provider((byte) 0x11);
    private static final KeyVersionProvider WRONG_PROVIDER = provider((byte) 0x55);

    private final FieldEncryptionService encryptionService = new FieldEncryptionService(PROVIDER);

    @Test
    void encryptsSameValueWithDifferentCiphertextAndDecryptsConsistently() {
        var first = encryptionService.encrypt("demo@example.com");
        var second = encryptionService.encrypt("demo@example.com");

        assertThat(first.ciphertext()).isNotEqualTo(second.ciphertext());
        assertThat(first.iv()).isNotEqualTo(second.iv());
        assertThat(encryptionService.decrypt(first)).isEqualTo("demo@example.com");
        assertThat(encryptionService.decrypt(second)).isEqualTo("demo@example.com");
    }

    @Test
    void failsToDecryptWithWrongKey() {
        var encrypted = encryptionService.encrypt("demo@example.com");
        FieldEncryptionService wrongService = new FieldEncryptionService(WRONG_PROVIDER);

        assertThatThrownBy(() -> wrongService.decrypt(encrypted))
                .isInstanceOf(CryptoException.class);
    }

    @Test
    void masksSensitiveFieldsForDisplay() {
        assertThat(encryptionService.maskEmail("secret@example.com")).isEqualTo("sec***@example.com");
        assertThat(encryptionService.maskPhone("13812345678")).isEqualTo("138****5678");
    }

    @Test
    void rejectsPlaintextThatDoesNotRoundTrip() {
        var encrypted = encryptionService.encrypt("中文与 emoji 😀");

        assertThat(encryptionService.decrypt(encrypted)).isEqualTo("中文与 emoji 😀");
        assertThat(encrypted.ciphertext()).isNotEqualTo("中文与 emoji 😀".getBytes(StandardCharsets.UTF_8));
    }

    private static KeyVersionProvider provider(byte seed) {
        byte[] encryptionKey = new byte[32];
        byte[] hmacKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            encryptionKey[i] = (byte) (seed + i);
            hmacKey[i] = (byte) (seed + i + 32);
        }
        return new KeyVersionProvider(1, encryptionKey, hmacKey);
    }
}
