package com.wanhui.user.controller;

import com.wanhui.common.api.ApiResponse;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.user.dto.ChangePasswordRequest;
import com.wanhui.user.dto.UpdateProfileRequest;
import com.wanhui.user.dto.UserProfileResponse;
import com.wanhui.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {
    private final UserProfileService profileService;

    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> profile(Authentication authentication) {
        return ApiResponse.success(profileService.profile(currentUserId(authentication)));
    }

    @PatchMapping("/profile")
    public ApiResponse<UserProfileResponse> update(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(profileService.update(currentUserId(authentication), request));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        profileService.changePassword(currentUserId(authentication), request);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "请先登录");
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "登录状态无效");
        }
    }
}
