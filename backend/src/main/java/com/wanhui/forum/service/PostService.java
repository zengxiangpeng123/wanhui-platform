package com.wanhui.forum.service;

import com.wanhui.common.exception.BusinessException;
import com.wanhui.forum.dto.CreatePostRequest;
import com.wanhui.forum.dto.PostAuthorResponse;
import com.wanhui.forum.dto.PostCategoryResponse;
import com.wanhui.forum.dto.PostDetailResponse;
import com.wanhui.forum.dto.PostPage;
import com.wanhui.forum.dto.PostSummaryResponse;
import com.wanhui.forum.entity.Post;
import com.wanhui.forum.entity.PostCategory;
import com.wanhui.forum.entity.Tag;
import com.wanhui.forum.mapper.PostCategoryMapper;
import com.wanhui.forum.mapper.PostFavoriteMapper;
import com.wanhui.forum.mapper.PostLikeMapper;
import com.wanhui.forum.mapper.PostMapper;
import com.wanhui.forum.mapper.PostTagMapper;
import com.wanhui.forum.mapper.TagMapper;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
public class PostService {
    private static final int MAX_PAGE_SIZE = 50;
    private static final String PUBLISHED = "PUBLISHED";
    private static final String DELETED = "DELETED";

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostCategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final UserMapper userMapper;

    public PostService(
            PostMapper postMapper,
            PostLikeMapper postLikeMapper,
            PostFavoriteMapper postFavoriteMapper,
            PostCategoryMapper categoryMapper,
            TagMapper tagMapper,
            PostTagMapper postTagMapper,
            UserMapper userMapper
    ) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.postTagMapper = postTagMapper;
        this.userMapper = userMapper;
    }

    public List<PostCategoryResponse> categories() {
        return categoryMapper.selectEnabled().stream()
                .map(category -> new PostCategoryResponse(category.getCode(), category.getName()))
                .toList();
    }

    public PostPage list(String categoryCode, int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "分页参数非法");
        }
        String category = normalizeCategory(categoryCode);
        long total = postMapper.countPublished(category);
        int offset = (page - 1) * size;
        List<PostSummaryResponse> items = postMapper.selectPublished(category, offset, size).stream()
                .map(this::summary)
                .toList();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PostPage(items, page, size, total, totalPages);
    }

    public PostDetailResponse detail(Long postId, Long viewerId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "帖子不存在");
        }
        if (DELETED.equals(post.getStatus()) || post.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.GONE, "POST_DELETED", "帖子已删除");
        }
        if (!PUBLISHED.equals(post.getStatus())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "帖子不存在");
        }
        return detail(post, viewerId);
    }

    @Transactional
    public PostDetailResponse create(Long authorId, CreatePostRequest request) {
        String categoryCode = normalizeCategory(request.categoryCode());
        PostCategory category = categoryMapper.selectByCode(categoryCode);
        if (category == null || !Boolean.TRUE.equals(category.getEnabled())) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "分类不存在或未启用");
        }

        Post post = new Post();
        post.setAuthorId(authorId);
        post.setCategoryId(category.getId());
        post.setTitle(request.title().trim());
        post.setContent(request.content().trim());
        post.setStatus(PUBLISHED);
        post.setViewCount(0L);
        post.setCommentCount(0L);
        post.setLikeCount(0L);
        post.setFavoriteCount(0L);
        post.setCreatedAt(LocalDateTime.now());
        postMapper.insert(post);

        associateTags(post.getId(), request.tags());
        return detail(post, null);
    }

    @Transactional
    public void delete(Long userId, Long postId, boolean admin) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "帖子不存在");
        }
        if (DELETED.equals(post.getStatus()) || post.getDeletedAt() != null) {
            return;
        }
        if (!admin && !userId.equals(post.getAuthorId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "没有权限删除此帖子");
        }
        post.setStatus(DELETED);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(userId);
        postMapper.updateById(post);
    }

    private void associateTags(Long postId, List<String> requestedTags) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String requested : requestedTags) {
            String value = requested == null ? "" : requested.trim();
            if (!value.isBlank()) unique.add(value);
        }
        if (unique.size() > 5) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "标签最多 5 个");
        }
        for (String name : unique) {
            String normalized = name.toLowerCase(Locale.ROOT);
            Tag tag = new Tag();
            tag.setName(name);
            tag.setNormalizedName(normalized);
            tagMapper.insertIgnore(tag);
            Tag stored = tagMapper.selectByNormalizedName(normalized);
            if (stored != null) postTagMapper.insert(postId, stored.getId());
        }
    }

    private PostSummaryResponse summary(Post post) {
        User author = userMapper.selectById(post.getAuthorId());
        PostCategory category = categoryMapper.selectById(post.getCategoryId());
        String content = post.getContent() == null ? "" : post.getContent().replaceAll("\\s+", " ").trim();
        String excerpt = content.length() > 180 ? content.substring(0, 180) + "…" : content;
        return new PostSummaryResponse(
                String.valueOf(post.getId()),
                post.getTitle(),
                excerpt,
                author(author),
                category(category),
                tags(post.getId()),
                value(post.getViewCount()),
                value(post.getCommentCount()),
                value(post.getLikeCount()),
                value(post.getFavoriteCount()),
                post.getCreatedAt()
        );
    }

    private PostDetailResponse detail(Post post, Long viewerId) {
        User author = userMapper.selectById(post.getAuthorId());
        PostCategory category = categoryMapper.selectById(post.getCategoryId());
        return new PostDetailResponse(
                String.valueOf(post.getId()),
                post.getTitle(),
                post.getContent(),
                author(author),
                category(category),
                tags(post.getId()),
                value(post.getViewCount()),
                value(post.getCommentCount()),
                value(post.getLikeCount()),
                value(post.getFavoriteCount()),
                liked(post, viewerId),
                favorited(post, viewerId),
                post.getCreatedAt()
        );
    }

    private boolean liked(Post post, Long viewerId) {
        return viewerId != null && postLikeMapper.countByPostAndUser(post.getId(), viewerId) > 0;
    }

    private boolean favorited(Post post, Long viewerId) {
        return viewerId != null && postFavoriteMapper.countByPostAndUser(post.getId(), viewerId) > 0;
    }

    private PostAuthorResponse author(User user) {
        if (user == null) return new PostAuthorResponse("0", "unknown", "未知用户");
        return new PostAuthorResponse(String.valueOf(user.getId()), user.getUsername(), user.getNickname());
    }

    private PostCategoryResponse category(PostCategory category) {
        return category == null
                ? new PostCategoryResponse("unknown", "未分类")
                : new PostCategoryResponse(category.getCode(), category.getName());
    }

    private List<String> tags(Long postId) {
        return tagMapper.selectByPostId(postId).stream().map(Tag::getName).toList();
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }

    private String normalizeCategory(String categoryCode) {
        return categoryCode == null || categoryCode.isBlank()
                ? null
                : categoryCode.trim().toLowerCase(Locale.ROOT);
    }
}


