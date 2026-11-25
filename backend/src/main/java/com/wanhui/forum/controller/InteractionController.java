package com.wanhui.forum.controller;

import com.wanhui.common.api.ApiResponse;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.forum.dto.CommentPage;
import com.wanhui.forum.dto.CommentResponse;
import com.wanhui.forum.dto.CreateCommentRequest;
import com.wanhui.forum.dto.PostFavoriteState;
import com.wanhui.forum.dto.PostLikeState;
import com.wanhui.forum.service.InteractionService;
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

@RestController
@RequestMapping("/api/v1/forum/posts")
public class InteractionController {
    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PutMapping("/{postId}/like")
    public ApiResponse<PostLikeState> like(@PathVariable Long postId, Authentication authentication) {
        return ApiResponse.success(interactionService.like(currentUserId(authentication), postId));
    }

    @DeleteMapping("/{postId}/like")
    public ApiResponse<PostLikeState> unlike(@PathVariable Long postId, Authentication authentication) {
        return ApiResponse.success(interactionService.unlike(currentUserId(authentication), postId));
    }

    @PutMapping("/{postId}/favorite")
    public ApiResponse<PostFavoriteState> favorite(@PathVariable Long postId, Authentication authentication) {
        return ApiResponse.success(interactionService.favorite(currentUserId(authentication), postId));
    }

    @DeleteMapping("/{postId}/favorite")
    public ApiResponse<PostFavoriteState> unfavorite(@PathVariable Long postId, Authentication authentication) {
        return ApiResponse.success(interactionService.unfavorite(currentUserId(authentication), postId));
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<CommentPage> comments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(interactionService.listComments(postId, page, size));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> comment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(interactionService.addComment(
                        currentUserId(authentication), postId, request.content())));
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
