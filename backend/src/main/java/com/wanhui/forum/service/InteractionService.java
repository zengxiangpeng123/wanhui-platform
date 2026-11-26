package com.wanhui.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.forum.dto.CommentLikeState;
import com.wanhui.forum.dto.CommentPage;
import com.wanhui.forum.dto.CommentResponse;
import com.wanhui.forum.dto.PostAuthorResponse;
import com.wanhui.forum.dto.PostFavoriteState;
import com.wanhui.forum.dto.PostLikeState;
import com.wanhui.forum.entity.Comment;
import com.wanhui.forum.entity.CommentLike;
import com.wanhui.forum.entity.Post;
import com.wanhui.forum.entity.PostFavorite;
import com.wanhui.forum.entity.PostLike;
import com.wanhui.forum.mapper.CommentLikeMapper;
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
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;

    public InteractionService(
            PostMapper postMapper,
            PostLikeMapper postLikeMapper,
            PostFavoriteMapper postFavoriteMapper,
            CommentMapper commentMapper,
            CommentLikeMapper commentLikeMapper,
            UserMapper userMapper
    ) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
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
        int removed = postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));
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
        int removed = postFavoriteMapper.delete(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getUserId, userId));
        if (removed > 0) {
            postMapper.decrementFavoriteCount(postId);
        }
        return favoriteState(postId, false);
    }

    public CommentPage listComments(Long postId, int page, int size, Long viewerId) {
        requirePublishedPost(postId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        long total = commentMapper.countPublishedByPost(postId);
        List<CommentResponse> items = commentMapper.selectPublishedByPost(postId, (safePage - 1) * safeSize, safeSize)
                .stream()
                .map(comment -> toResponse(comment, viewerId))
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
        comment.setLikeCount(0L);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        postMapper.incrementCommentCount(postId);
        return toResponse(comment, userId);
    }

    @Transactional
    public CommentLikeState likeComment(Long userId, Long commentId) {
        Comment comment = requirePublishedComment(commentId);
        if (commentLikeMapper.insertIgnore(commentId, userId) > 0) {
            commentMapper.incrementLikeCount(commentId);
        }
        return commentLikeState(commentId, true);
    }

    @Transactional
    public CommentLikeState unlikeComment(Long userId, Long commentId) {
        requirePublishedComment(commentId);
        int removed = commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId));
        if (removed > 0) {
            commentMapper.decrementLikeCount(commentId);
        }
        return commentLikeState(commentId, false);
    }

    private PostLikeState likeState(Long postId, boolean liked) {
        Post post = postMapper.selectById(postId);
        return new PostLikeState(liked, value(post == null ? null : post.getLikeCount()));
    }

    private PostFavoriteState favoriteState(Long postId, boolean favorited) {
        Post post = postMapper.selectById(postId);
        return new PostFavoriteState(favorited, value(post == null ? null : post.getFavoriteCount()));
    }

    private CommentLikeState commentLikeState(Long commentId, boolean liked) {
        Comment comment = commentMapper.selectById(commentId);
        return new CommentLikeState(liked, value(comment == null ? null : comment.getLikeCount()));
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

    private Comment requirePublishedComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !PUBLISHED.equals(comment.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "评论不存在");
        }
        return comment;
    }

    private CommentResponse toResponse(Comment comment, Long viewerId) {
        User author = userMapper.selectById(comment.getUserId());
        PostAuthorResponse authorResponse = author == null
                ? new PostAuthorResponse("0", "unknown", "未知用户")
                : new PostAuthorResponse(String.valueOf(author.getId()), author.getUsername(), author.getNickname());
        boolean liked = viewerId != null
                && commentLikeMapper.countByCommentAndUser(comment.getId(), viewerId) > 0;
        return new CommentResponse(
                String.valueOf(comment.getId()),
                comment.getContent(),
                authorResponse,
                value(comment.getLikeCount()),
                liked,
                comment.getCreatedAt()
        );
    }
}
