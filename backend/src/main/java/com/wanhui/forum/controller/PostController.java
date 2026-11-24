package com.wanhui.forum.controller;

import com.wanhui.common.api.ApiResponse;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.forum.dto.CreatePostRequest;
import com.wanhui.forum.dto.PostCategoryResponse;
import com.wanhui.forum.dto.PostDetailResponse;
import com.wanhui.forum.dto.PostPage;
import com.wanhui.forum.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/forum")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<PostCategoryResponse>> categories() {
        return ApiResponse.success(postService.categories());
    }

    @GetMapping("/posts")
    public ApiResponse<PostPage> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(postService.list(category, page, size));
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable Long postId) {
        return ApiResponse.success(postService.detail(postId));
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostDetailResponse>> create(
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(postService.create(currentUserId(authentication), request)));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId, Authentication authentication) {
        postService.delete(currentUserId(authentication), postId, isAdmin(authentication));
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

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "POST_DELETE_ANY".equals(authority.getAuthority()));
    }
}
