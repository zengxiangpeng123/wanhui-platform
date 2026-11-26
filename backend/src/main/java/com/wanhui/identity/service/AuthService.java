package com.wanhui.identity.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.identity.dto.CurrentUserResponse;
import com.wanhui.identity.dto.LoginRequest;
import com.wanhui.identity.dto.RegisterRequest;
import com.wanhui.identity.dto.UserSummary;
import com.wanhui.identity.entity.Role;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.RoleMapper;
import com.wanhui.identity.mapper.UserMapper;
import com.wanhui.security.crypto.FieldEncryptionService;
import com.wanhui.security.crypto.FieldHmacService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {
    private static final String ACTIVE = "ACTIVE";
    private static final String USER_ROLE = "USER";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final FieldEncryptionService encryptionService;
    private final FieldHmacService hmacService;
    private final String dummyPasswordHash;

    public AuthService(
            UserMapper userMapper,
            RoleMapper roleMapper,
            PasswordEncoder passwordEncoder,
            FieldEncryptionService encryptionService,
            FieldHmacService hmacService
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
        this.hmacService = hmacService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public UserSummary register(RegisterRequest request) {
        String username = request.username().trim();
        String usernameNormalized = hmacService.normalizeUsername(username);
        String emailNormalized = hmacService.normalizeEmail(request.email());
        if (userMapper.selectByUsernameNormalized(usernameNormalized) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "CONFLICT", "用户名或邮箱已注册");
        }

        byte[] emailHmac = hmacService.hmac(emailNormalized);
        if (userMapper.selectByEmailHmac(emailHmac) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "CONFLICT", "用户名或邮箱已注册");
        }

        FieldEncryptionService.EncryptedField encryptedEmail = encryptionService.encrypt(emailNormalized);
        User user = new User();
        user.setUsername(username);
        user.setUsernameNormalized(usernameNormalized);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailCiphertext(encryptedEmail.ciphertext());
        user.setEmailIv(encryptedEmail.iv());
        user.setEmailKeyVersion(encryptedEmail.keyVersion());
        user.setEmailHmac(emailHmac);
        user.setEmailHmacVersion(hmacService.currentVersion());
        user.setEmailMasked(encryptionService.maskEmail(emailNormalized));
        user.setNickname(username);
        user.setAvatarKey("avatar_01");
        user.setStatus(ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "CONFLICT", "用户名或邮箱已注册");
        }

        Role userRole = roleMapper.selectOne(new QueryWrapper<Role>().eq("code", USER_ROLE));
        if (userRole == null) {
            throw new IllegalStateException("USER role seed is missing");
        }
        userMapper.assignRole(user.getId(), userRole.getId());
        return new UserSummary(String.valueOf(user.getId()), user.getUsername(), "LOGIN");
    }

    public CurrentUserResponse login(LoginRequest request) {
        String account = request.account().trim();
        String normalized = account.contains("@")
                ? hmacService.normalizeEmail(account)
                : hmacService.normalizeUsername(account);
        User user = account.contains("@")
                ? userMapper.selectByEmailHmac(hmacService.hmac(normalized))
                : userMapper.selectByUsernameNormalized(normalized);

        String passwordHash = user == null || user.getPasswordHash() == null
                ? dummyPasswordHash
                : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);
        if (user == null || !passwordMatches || !ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "账号或密码不正确");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        return currentUser(user.getId());
    }

    public CurrentUserResponse currentUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null || !ACTIVE.equals(user.getStatus())) {
            return null;
        }
        List<String> roles = roleMapper.selectByUserId(userId).stream()
                .map(Role::getCode)
                .toList();
        List<String> permissions = roleMapper.selectPermissionCodesByUserId(userId);
        return new CurrentUserResponse(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarKey(),
                user.getEmailMasked(),
                user.getPhoneMasked(),
                roles,
                permissions
        );
    }

}
