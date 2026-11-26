package com.wanhui.security.crypto;

/**
 * 密钥轮换编排：校验目标版本可用性并给出轮换计划。
 * 实际密钥生成与保管由 KMS 负责，本服务不生成、不记录密钥材料。
 */
public class KeyRotationService {
    private final KmsKeyProvider kmsKeyProvider;

    public KeyRotationService(KmsKeyProvider kmsKeyProvider) {
        if (kmsKeyProvider == null) {
            throw new IllegalArgumentException("kmsKeyProvider must not be null");
        }
        this.kmsKeyProvider = kmsKeyProvider;
    }

    public int currentVersion() {
        return kmsKeyProvider.currentVersion();
    }

    /**
     * 生成轮换计划。目标版本必须大于当前版本，且其密钥材料已由 KMS 提供。
     */
    public RotationPlan plan(int targetVersion) {
        int current = kmsKeyProvider.currentVersion();
        if (targetVersion <= current) {
            throw new CryptoException("targetVersion must be greater than currentVersion");
        }
        if (!kmsKeyProvider.supports(targetVersion)) {
            throw new CryptoException("target key version is not available in KMS: " + targetVersion);
        }
        return new RotationPlan(current, targetVersion);
    }

    public record RotationPlan(int fromVersion, int toVersion) {
    }
}
