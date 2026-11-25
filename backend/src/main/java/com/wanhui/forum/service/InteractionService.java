package com.wanhui.forum.service;

import com.wanhui.common.exception.BusinessException;
import com.wanhui.forum.dto.CommentPage;
import com.wanhui.forum.dto.CommentResponse;
import com.wanhui.forum.dto.PostAuthorResponse;
import com.wanhui.forum.dto.PostFavoriteState;
import com.wanhui.forum.dto.PostLikeState;
import com.wanhui.forum.entity.Comment;
import com.wanhui.forum.entity.Post;
import com.wanhui.forum.mapper.CommentMapper;
import com.wanhui.forum.mapper.PostFavoriteMapper;
import com.wanhui.forum.mapper.PostLikeMapper;
import com.wanhui.forum.mapper.PostMapper;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteractionService {
    private static final int MAX_PAGE_SIZE = 50;
    private static final String PUBLISHED = "PUBLISHED";
    private static final String DELETED = "DELETED";

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public InteractionService(
            PostMapper postMapper,
            PostLikeMapper postLikeMapper,
            PostFavoriteMapper postFavoriteMapper,
            CommentMapper commentMapper,
            UserMapper userMapper
    ) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public PostLikeState like(Long userId, Long postId) {
        requirePublishedPost(postId);
        if (postLikeMapper.insertIgnore(postId, userId) > 0) {
            postMapper.incrementLikeCount(postId);
        }
        return likeState(postId, true);
    }

    @Transactional
    public PostLikeState unlike(Long userId, Long postId) {
        requirePublishedPost(postId);
        int removed = postLikeMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wanhui.forum.entity.PostLike>()
                        .eq(com.wanhui.forum.entity.PostLike::getPostId, postId)
                        .eq(com.wanhui.forum.entity.PostLike::getUserId, userId));
        if (removed > 0) {
            postMapper.decrementLikeCount(postId);
        }
        return likeState(postId, false);
    }

    @Transactional
    public PostFavoriteState favorite(Long userId, Long postId) {
        requirePublishedPost(postId);
        if (postFavoriteMapper.insertIgnore(postId, userId) > 0) {
            postMapper.incrementFavoriteCount(postId);
        }
        return favoriteState(postId, true);
    }

    @Transactional
    public PostFavoriteState unfavorite(Long userId, Long postId) {
        requirePublishedPost(postId);
        int removed = postFavoriteMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.wanhui.forum.entity.PostFavorite>()
                        .eq(com.wanhui.forum.entity.PostFavorite::getPostId, postId)
                        .eq(com.wanhui.forum.entity.PostFavorite::getUserId, userId));
        if (removed > 0) {
            postMapper.decrementFavoriteCount(postId);
        }
        return favoriteState(postId, false);
    }

    public CommentPage listComments(Long postId, int page, int size) {
        requirePublishedPost(postId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        long total = commentMapper.countPublishedByPost(postId);
        List<CommentResponse> items = commentMapper.selectPublishedByPost(postId, (safePage - 1) * safeSize, safeSize)
                .stream()
                .map(this::toResponse)
                .toList();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new CommentPage(items, safePage, safeSize, total, totalPages);
    }

    @Transactional
    public CommentResponse addComment(Long userId, Long postId, String content) {
        requirePublishedPost(postId);
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setStatus(PUBLISHED);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        postMapper.incrementCommentCount(postId);
        return toResponse(comment);
    }

    private PostLikeState likeState(Long postId, boolean liked) {
        Post post = postMapper.selectById(postId);
        return new PostLikeState(liked, value(post == null ? null : post.getLikeCount()));
    }

    private PostFavoriteState favoriteState(Long postId, boolean favorited) {
        Post post = postMapper.selectById(postId);
        return new PostFavoriteState(favorited, value(post == null ? null : post.getFavoriteCount()));
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }

    private void requirePublishedPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || DELETED.equals(post.getStatus()) || post.getDeletedAt() != null
                || !PUBLISHED.equals(post.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "帖子不存在");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        User author = userMapper.selectById(comment.getUserId());
        PostAuthorResponse authorResponse = author == null
                ? new PostAuthorResponse("0", "unknown", "未知用户")
                : new PostAuthorResponse(String.valueOf(author.getId()), author.getUsername(), author.getNickname());
        return new CommentResponse(String.valueOf(comment.getId()), comment.getContent(), authorResponse, comment.getCreatedAt());
    }
}

