package com.wanhui.admin.controller;

import com.wanhui.admin.dto.AdminPostPage;
import com.wanhui.admin.dto.AuditLogResponse;
import com.wanhui.admin.service.AdminPostService;
import com.wanhui.common.api.ApiResponse;
import com.wanhui.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminPostController {
    private static final Set<String> ALLOWED_AUTHORITIES = Set.of("ROLE_ADMIN", "POST_DELETE_ANY", "ADMIN_CONTENT_VIEW");

    private final AdminPostService adminPostService;

    public AdminPostController(AdminPostService adminPostService) {
        this.adminPostService = adminPostService;
    }

    @GetMapping("/posts")
    public ApiResponse<AdminPostPage> posts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        return ApiResponse.success(adminPostService.list(page, size));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId, Authentication authentication) {
        adminPostService.delete(requireAdmin(authentication), postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AuditLogResponse>> auditLogs(
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        return ApiResponse.success(adminPostService.auditLogs(limit));
    }

    private Long requireAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "请先登录");
        }
        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ALLOWED_AUTHORITIES::contains);
        if (!admin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "没有权限执行此操作");
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "登录状态无效");
        }
    }
}
