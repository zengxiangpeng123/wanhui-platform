package com.wanhui.security;

import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import com.wanhui.security.crypto.FieldEncryptionMigrationService;
import com.wanhui.security.crypto.FieldEncryptionService;
import com.wanhui.security.crypto.FieldHmacService;
import com.wanhui.security.crypto.KmsKeyProvider;
import com.wanhui.security.crypto.KeyVersionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FieldEncryptionMigrationTest extends com.wanhui.identity.IdentityDatabaseTestSupport {

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void cleanUsers() {
        userMapper.deleteAllUserRoles();
        userMapper.delete(null);
    }

    @Test
    void migratesLegacyUserToCurrentVersionAndVerifiesPlaintext() {
        userMapper.insert(legacyUser(1L, "legacy_user", "legacy@example.com"));

        FieldEncryptionMigrationService.MigrationReport report = migrationService().migrate(10);

        assertThat(report.currentVersion()).isEqualTo(2);
        assertThat(report.migrated()).isEqualTo(1);
        assertThat(report.failed()).isZero();

        User migrated = userMapper.selectById(1L);
        assertThat(migrated.getEmailKeyVersion()).isEqualTo(2);
        assertThat(migrated.getEmailHmacVersion()).isEqualTo(2);
        FieldEncryptionService v2 = encryptionService(new KeyVersionProvider(currentProvider()));
        String plaintext = v2.decrypt(new FieldEncryptionService.EncryptedField(
                migrated.getEmailCiphertext(),
                migrated.getEmailIv(),
                migrated.getEmailKeyVersion()
        ));
        assertThat(plaintext).isEqualTo("legacy@example.com");
    }

    @Test
    void repeatedMigrationIsIdempotent() {
        userMapper.insert(legacyUser(1L, "legacy_user", "legacy@example.com"));
        FieldEncryptionMigrationService service = migrationService();

        assertThat(service.migrate(10).migrated()).isEqualTo(1);
        FieldEncryptionMigrationService.MigrationReport second = service.migrate(10);

        assertThat(second.migrated()).isZero();
        assertThat(second.failed()).isZero();
    }

    @Test
    void corruptRowIsReportedAndOtherRowsStillMigrate() {
        userMapper.insert(legacyUser(1L, "valid_user", "valid@example.com"));
        User corrupt = legacyUser(2L, "corrupt_user", "corrupt@example.com");
        corrupt.setEmailCiphertext("not-a-valid-ciphertext".getBytes(StandardCharsets.UTF_8));
        userMapper.insert(corrupt);

        FieldEncryptionMigrationService.MigrationReport report = migrationService().migrate(10);

        assertThat(report.migrated()).isEqualTo(1);
        assertThat(report.failed()).isEqualTo(1);
        assertThat(report.failedUserIds()).containsExactly(2L);

        assertThat(userMapper.selectById(1L).getEmailKeyVersion()).isEqualTo(2);
        assertThat(userMapper.selectById(2L).getEmailKeyVersion()).isEqualTo(1);
    }

    private static byte[] fill(byte value) {
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, value);
        return bytes;
    }

    private static KmsKeyProvider currentProvider() {
        return new KmsKeyProvider(2, Map.of(
                1, new KmsKeyProvider.KeyMaterial(fill((byte) 1), fill((byte) 11)),
                2, new KmsKeyProvider.KeyMaterial(fill((byte) 2), fill((byte) 12))
        ));
    }

    private static KeyVersionProvider legacyProvider() {
        return new KeyVersionProvider(1, fill((byte) 1), fill((byte) 11));
    }

    private static FieldEncryptionService encryptionService(KeyVersionProvider provider) {
        return new FieldEncryptionService(provider);
    }

    private FieldEncryptionMigrationService migrationService() {
        KeyVersionProvider provider = new KeyVersionProvider(currentProvider());
        return new FieldEncryptionMigrationService(
                userMapper,
                new FieldEncryptionService(provider),
                new FieldHmacService(provider),
                provider
        );
    }

    private User legacyUser(long id, String username, String email) {
        KeyVersionProvider provider = legacyProvider();
        FieldEncryptionService encryption = new FieldEncryptionService(provider);
        FieldHmacService hmac = new FieldHmacService(provider);
        FieldEncryptionService.EncryptedField encrypted = encryption.encrypt(email);

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setUsernameNormalized(username);
        user.setPasswordHash("$2a$12$test.hash");
        user.setEmailCiphertext(encrypted.ciphertext());
        user.setEmailIv(encrypted.iv());
        user.setEmailKeyVersion(encrypted.keyVersion());
        user.setEmailHmac(hmac.hmac(email));
        user.setEmailHmacVersion(hmac.currentVersion());
        user.setEmailMasked(encryption.maskEmail(email));
        user.setNickname(username);
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
