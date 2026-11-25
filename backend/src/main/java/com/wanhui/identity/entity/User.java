package com.wanhui.identity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String usernameNormalized;
    private String passwordHash;
    private byte[] emailCiphertext;
    private byte[] emailIv;
    private Integer emailKeyVersion;
    private byte[] emailHmac;
    private Integer emailHmacVersion;
    private String emailMasked;
    private byte[] phoneCiphertext;
    private byte[] phoneIv;
    private Integer phoneKeyVersion;
    private byte[] phoneHmac;
    private Integer phoneHmacVersion;
    private String phoneMasked;
    private String nickname;
    private String avatarKey;
    private String interest;
    private String bio;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUsernameNormalized() { return usernameNormalized; }
    public void setUsernameNormalized(String usernameNormalized) { this.usernameNormalized = usernameNormalized; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public byte[] getEmailCiphertext() { return emailCiphertext; }
    public void setEmailCiphertext(byte[] emailCiphertext) { this.emailCiphertext = emailCiphertext; }
    public byte[] getEmailIv() { return emailIv; }
    public void setEmailIv(byte[] emailIv) { this.emailIv = emailIv; }
    public Integer getEmailKeyVersion() { return emailKeyVersion; }
    public void setEmailKeyVersion(Integer emailKeyVersion) { this.emailKeyVersion = emailKeyVersion; }
    public byte[] getEmailHmac() { return emailHmac; }
    public void setEmailHmac(byte[] emailHmac) { this.emailHmac = emailHmac; }
    public Integer getEmailHmacVersion() { return emailHmacVersion; }
    public void setEmailHmacVersion(Integer emailHmacVersion) { this.emailHmacVersion = emailHmacVersion; }
    public String getEmailMasked() { return emailMasked; }
    public void setEmailMasked(String emailMasked) { this.emailMasked = emailMasked; }
    public byte[] getPhoneCiphertext() { return phoneCiphertext; }
    public void setPhoneCiphertext(byte[] phoneCiphertext) { this.phoneCiphertext = phoneCiphertext; }
    public byte[] getPhoneIv() { return phoneIv; }
    public void setPhoneIv(byte[] phoneIv) { this.phoneIv = phoneIv; }
    public Integer getPhoneKeyVersion() { return phoneKeyVersion; }
    public void setPhoneKeyVersion(Integer phoneKeyVersion) { this.phoneKeyVersion = phoneKeyVersion; }
    public byte[] getPhoneHmac() { return phoneHmac; }
    public void setPhoneHmac(byte[] phoneHmac) { this.phoneHmac = phoneHmac; }
    public Integer getPhoneHmacVersion() { return phoneHmacVersion; }
    public void setPhoneHmacVersion(Integer phoneHmacVersion) { this.phoneHmacVersion = phoneHmacVersion; }
    public String getPhoneMasked() { return phoneMasked; }
    public void setPhoneMasked(String phoneMasked) { this.phoneMasked = phoneMasked; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarKey() { return avatarKey; }
    public void setAvatarKey(String avatarKey) { this.avatarKey = avatarKey; }
    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
