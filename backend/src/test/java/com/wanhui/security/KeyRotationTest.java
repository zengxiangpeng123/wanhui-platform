package com.wanhui.security;

import com.wanhui.security.crypto.CryptoException;
import com.wanhui.security.crypto.FieldEncryptionService;
import com.wanhui.security.crypto.KmsKeyProvider;
import com.wanhui.security.crypto.KeyRotationService;
import com.wanhui.security.crypto.KeyVersionProvider;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeyRotationTest {

    private static byte[] fill(byte value) {
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, value);
        return bytes;
    }

    private static KmsKeyProvider.KeyMaterial material(byte encryption, byte hmac) {
        return new KmsKeyProvider.KeyMaterial(fill(encryption), fill(hmac));
    }

    private static KmsKeyProvider provider(int current) {
        return new KmsKeyProvider(current, Map.of(
                1, material((byte) 1, (byte) 11),
                2, material((byte) 2, (byte) 12),
                3, material((byte) 3, (byte) 13)
        ));
    }

    @Test
    void oldVersionCiphertextStillDecryptsAfterRotation() {
        FieldEncryptionService v1 = new FieldEncryptionService(new KeyVersionProvider(provider(1)));
        FieldEncryptionService.EncryptedField ciphertext = v1.encrypt("user@example.com");
        assertThat(ciphertext.keyVersion()).isEqualTo(1);

        FieldEncryptionService v2 = new FieldEncryptionService(new KeyVersionProvider(provider(2)));
        assertThat(v2.decrypt(ciphertext)).isEqualTo("user@example.com");
    }

    @Test
    void newWritesUseCurrentVersion() {
        FieldEncryptionService v2 = new FieldEncryptionService(new KeyVersionProvider(provider(2)));
        FieldEncryptionService.EncryptedField ciphertext = v2.encrypt("new@example.com");
        assertThat(ciphertext.keyVersion()).isEqualTo(2);
        assertThat(v2.decrypt(ciphertext)).isEqualTo("new@example.com");
    }

    @Test
    void unknownKeyVersionIsRejected() {
        KeyVersionProvider provider = new KeyVersionProvider(provider(2));
        assertThatThrownBy(() -> provider.encryptionKey(9))
                .isInstanceOf(CryptoException.class);
        assertThatThrownBy(() -> provider.hmacKey(9))
                .isInstanceOf(CryptoException.class);
    }

    @Test
    void rotationPlanRequiresAvailableIncreasingVersion() {
        KeyRotationService rotation = new KeyRotationService(provider(2));

        assertThat(rotation.plan(3)).isEqualTo(new KeyRotationService.RotationPlan(2, 3));
        assertThatThrownBy(() -> rotation.plan(2)).isInstanceOf(CryptoException.class);
        assertThatThrownBy(() -> rotation.plan(4)).isInstanceOf(CryptoException.class);
    }
}
