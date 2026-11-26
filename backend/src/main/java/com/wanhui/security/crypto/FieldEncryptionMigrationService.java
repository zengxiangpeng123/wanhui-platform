package com.wanhui.security.crypto;

import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 将用户邮箱/手机号从旧密钥版本重加密到当前版本。
 * 按批处理、幂等（只处理低于当前版本的行）、单行失败不影响其他行、可重复执行续跑。
 * 日志仅记录 userId 和计数，不记录邮箱、手机号、密钥或明文样本。
 */
@Service
public class FieldEncryptionMigrationService {
    private static final Logger log = LoggerFactory.getLogger(FieldEncryptionMigrationService.class);
    private static final int MAX_BATCH_SIZE = 200;

    private final UserMapper userMapper;
    private final FieldEncryptionService encryptionService;
    private final FieldHmacService hmacService;
    private final KeyVersionProvider keyVersionProvider;

    public FieldEncryptionMigrationService(
            UserMapper userMapper,
            FieldEncryptionService encryptionService,
            FieldHmacService hmacService,
            KeyVersionProvider keyVersionProvider
    ) {
        this.userMapper = userMapper;
        this.encryptionService = encryptionService;
        this.hmacService = hmacService;
        this.keyVersionProvider = keyVersionProvider;
    }

    public MigrationReport migrate(int batchSize) {
        int currentVersion = keyVersionProvider.currentVersion();
        int safeBatch = Math.min(Math.max(batchSize, 1), MAX_BATCH_SIZE);
        int migrated = 0;
        int failed = 0;
        List<Long> failedUserIds = new ArrayList<>();

        long lastId = 0L;
        while (true) {
            List<User> batch = userMapper.selectOutdatedKeyVersionAfter(currentVersion, lastId, safeBatch);
            if (batch.isEmpty()) {
                break;
            }
            for (User user : batch) {
                lastId = user.getId();
                try {
                    reencrypt(user, currentVersion);
                    userMapper.updateById(user);
                    migrated++;
                } catch (RuntimeException exception) {
                    failed++;
                    failedUserIds.add(user.getId());
                    log.warn("Field re-encryption failed userId={} targetVersion={} cause={}",
                            user.getId(), currentVersion, exception.getClass().getSimpleName());
                }
            }
        }

        log.info("Field re-encryption finished targetVersion={} migrated={} failed={}",
                currentVersion, migrated, failed);
        return new MigrationReport(currentVersion, migrated, failed, List.copyOf(failedUserIds));
    }

    private void reencrypt(User user, int currentVersion) {
        String email = encryptionService.decrypt(new FieldEncryptionService.EncryptedField(
                user.getEmailCiphertext(),
                user.getEmailIv(),
                user.getEmailKeyVersion()
        ));
        FieldEncryptionService.EncryptedField newEmail = encryptionService.encrypt(email);
        user.setEmailCiphertext(newEmail.ciphertext());
        user.setEmailIv(newEmail.iv());
        user.setEmailKeyVersion(newEmail.keyVersion());
        user.setEmailHmac(hmacService.hmac(email));
        user.setEmailHmacVersion(hmacService.currentVersion());

        if (user.getPhoneCiphertext() != null && user.getPhoneIv() != null && user.getPhoneKeyVersion() != null) {
            String phone = encryptionService.decrypt(new FieldEncryptionService.EncryptedField(
                    user.getPhoneCiphertext(),
                    user.getPhoneIv(),
                    user.getPhoneKeyVersion()
            ));
            FieldEncryptionService.EncryptedField newPhone = encryptionService.encrypt(phone);
            user.setPhoneCiphertext(newPhone.ciphertext());
            user.setPhoneIv(newPhone.iv());
            user.setPhoneKeyVersion(newPhone.keyVersion());
            user.setPhoneHmac(hmacService.hmac(phone));
            user.setPhoneHmacVersion(hmacService.currentVersion());
        }

        user.setUpdatedAt(LocalDateTime.now());
    }

    public record MigrationReport(int currentVersion, int migrated, int failed, List<Long> failedUserIds) {
    }
}


