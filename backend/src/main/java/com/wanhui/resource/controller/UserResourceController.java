package com.wanhui.resource.controller;

import com.wanhui.common.api.ApiResponse;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.resource.dto.FavoritePage;
import com.wanhui.resource.dto.RecentVisitRequest;
import com.wanhui.resource.dto.RecentVisitResponse;
import com.wanhui.resource.dto.UserSiteRequest;
import com.wanhui.resource.dto.UserSiteResponse;
import com.wanhui.resource.service.UserResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserResourceController {
    private final UserResourceService resourceService;

    public UserResourceController(UserResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PutMapping("/resources/{resourceKey}/favorite")
    public ResponseEntity<Void> favorite(@PathVariable String resourceKey, Authentication authentication) {
        resourceService.addFavorite(currentUserId(authentication), resourceKey);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/resources/{resourceKey}/favorite")
    public ResponseEntity<Void> unfavorite(@PathVariable String resourceKey, Authentication authentication) {
        resourceService.removeFavorite(currentUserId(authentication), resourceKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me/favorites/resources")
    public ApiResponse<FavoritePage> favorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return ApiResponse.success(resourceService.favorites(currentUserId(authentication), page, size));
    }

    @GetMapping("/users/me/recent-visits")
    public ApiResponse<List<RecentVisitResponse>> recentVisits(Authentication authentication) {
        return ApiResponse.success(resourceService.recentVisits(currentUserId(authentication)));
    }

    @PostMapping("/users/me/recent-visits")
    public ApiResponse<RecentVisitResponse> recordVisit(
            @Valid @RequestBody RecentVisitRequest request,
            Authentication authentication
    ) {
        Long userId = currentUserId(authentication);
        resourceService.recordVisit(userId, request.resourceKey());
        return ApiResponse.success(resourceService.recentVisits(userId).stream()
                .filter(item -> item.resourceKey().equals(request.resourceKey()))
                .findFirst()
                .orElseThrow());
    }

    @DeleteMapping("/users/me/recent-visits")
    public ResponseEntity<Void> clearRecentVisits(Authentication authentication) {
        resourceService.clearRecentVisits(currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me/sites")
    public ApiResponse<List<UserSiteResponse>> sites(Authentication authentication) {
        return ApiResponse.success(resourceService.sites(currentUserId(authentication)));
    }

    @PostMapping("/users/me/sites")
    public ResponseEntity<ApiResponse<UserSiteResponse>> addSite(
            @Valid @RequestBody UserSiteRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resourceService.addSite(currentUserId(authentication), request)));
    }

    @DeleteMapping("/users/me/sites/{siteId}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long siteId, Authentication authentication) {
        resourceService.deleteSite(currentUserId(authentication), siteId);
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
