package com.wanhui.user.service;

import com.wanhui.common.exception.BusinessException;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import com.wanhui.user.dto.ChangePasswordRequest;
import com.wanhui.user.dto.UpdateProfileRequest;
import com.wanhui.user.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserProfileService {
    private static final String ACTIVE = "ACTIVE";
    private static final String DEFAULT_AVATAR = "avatar_01";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse profile(Long userId) {
        return toResponse(requireActiveUser(userId));
    }

    @Transactional
    public UserProfileResponse update(Long userId, UpdateProfileRequest request) {
        User user = requireActiveUser(userId);

        if (request.nickname() != null) {
            String nickname = request.nickname().trim();
            if (nickname.isEmpty()) {
                throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "昵称不能为空");
            }
            user.setNickname(nickname);
        }
        if (request.bio() != null) {
            user.setBio(blankToNull(request.bio()));
        }
        if (request.interest() != null) {
            user.setInterest(blankToNull(request.interest()));
        }
        if (request.avatarKey() != null) {
            user.setAvatarKey(request.avatarKey().isBlank() ? DEFAULT_AVATAR : request.avatarKey());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = requireActiveUser(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "INVALID_OLD_PASSWORD",
                    "旧密码不正确"
            );
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "VALIDATION_FAILED",
                    "新密码不能与旧密码相同"
            );
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private User requireActiveUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !ACTIVE.equals(user.getStatus())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "登录状态无效");
        }
        return user;
    }

    private String blankToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarKey(),
                user.getEmailMasked(),
                user.getPhoneMasked(),
                user.getInterest(),
                user.getBio(),
                user.getCreatedAt()
        );
    }
}
