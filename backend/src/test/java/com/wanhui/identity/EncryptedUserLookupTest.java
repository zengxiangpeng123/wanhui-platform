package com.wanhui.identity;

import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import com.wanhui.security.crypto.FieldEncryptionService;
import com.wanhui.security.crypto.FieldHmacService;
import com.wanhui.security.crypto.KeyVersionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EncryptedUserLookupTest extends IdentityDatabaseTestSupport {

    private static final byte[] ENCRYPTION_KEY = key((byte) 0x11);
    private static final byte[] HMAC_KEY = key((byte) 0x22);

    @Autowired
    private UserMapper userMapper;

    private final FieldEncryptionService encryptionService =
            new FieldEncryptionService(new KeyVersionProvider(1, ENCRYPTION_KEY, HMAC_KEY));
    private final FieldHmacService hmacService =
            new FieldHmacService(new KeyVersionProvider(1, ENCRYPTION_KEY, HMAC_KEY));

    @BeforeEach
    void cleanUsers() {
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
    }

    @Test
    void looksUpByHmacWithoutStoringOrReturningPlaintextEmail() {
        String email = "secret@example.com";
        var encrypted = encryptionService.encrypt(email);
        byte[] emailHmac = hmacService.hmac(email);

        User user = new User();
        user.setUsername("secret_user");
        user.setUsernameNormalized("secret_user");
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext(encrypted.ciphertext());
        user.setEmailIv(encrypted.iv());
        user.setEmailKeyVersion(encrypted.keyVersion());
        user.setEmailHmac(emailHmac);
        user.setEmailHmacVersion(hmacService.currentVersion());
        user.setEmailMasked(encryptionService.maskEmail(email));
        user.setNickname("secret_user");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);
        User found = userMapper.selectByEmailHmac(emailHmac);

        assertThat(found).isNotNull();
        assertThat(found.getEmailMasked()).isEqualTo("sec***@example.com");
        assertThat(found.getEmailCiphertext()).isNotEqualTo(email.getBytes(StandardCharsets.UTF_8));
        assertThat(new String(found.getEmailCiphertext(), StandardCharsets.UTF_8)).doesNotContain(email);
        assertThat(encryptionService.decrypt(new FieldEncryptionService.EncryptedField(
                found.getEmailCiphertext(), found.getEmailIv(), found.getEmailKeyVersion()
        ))).isEqualTo(email);
    }

    private static byte[] key(byte seed) {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (seed + i);
        }
        return key;
    }
}
