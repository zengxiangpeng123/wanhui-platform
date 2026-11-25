package com.wanhui.admin.service;

import com.wanhui.admin.dto.AdminPostPage;
import com.wanhui.admin.dto.AuditLogResponse;
import com.wanhui.admin.entity.AuditLog;
import com.wanhui.admin.mapper.AuditLogMapper;
import com.wanhui.common.exception.BusinessException;
import com.wanhui.forum.dto.PostAuthorResponse;
import com.wanhui.forum.dto.PostCategoryResponse;
import com.wanhui.forum.dto.PostSummaryResponse;
import com.wanhui.forum.entity.Post;
import com.wanhui.forum.entity.PostCategory;
import com.wanhui.forum.entity.Tag;
import com.wanhui.forum.mapper.PostCategoryMapper;
import com.wanhui.forum.mapper.PostMapper;
import com.wanhui.forum.mapper.TagMapper;
import com.wanhui.identity.entity.User;
import com.wanhui.identity.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPostService {
    private static final int MAX_PAGE_SIZE = 50;
    private static final String DELETED = "DELETED";

    private final PostMapper postMapper;
    private final PostCategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;

    public AdminPostService(
            PostMapper postMapper,
            PostCategoryMapper categoryMapper,
            TagMapper tagMapper,
            UserMapper userMapper,
            AuditLogMapper auditLogMapper
    ) {
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.auditLogMapper = auditLogMapper;
    }

    public AdminPostPage list(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "分页参数非法");
        }
        long total = postMapper.countForAdmin();
        List<PostSummaryResponse> items = postMapper.selectForAdmin((page - 1) * size, size).stream()
                .map(this::summary)
                .toList();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new AdminPostPage(items, page, size, total, totalPages);
    }

    @Transactional
    public void delete(Long operatorId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || DELETED.equals(post.getStatus()) || post.getDeletedAt() != null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "帖子不存在");
        }
        post.setStatus(DELETED);
        post.setDeletedAt(LocalDateTime.now());
        post.setDeletedBy(operatorId);
        postMapper.updateById(post);
        writeAudit(operatorId, postId, "POST_DELETE", "SUCCESS");
    }

    public List<AuditLogResponse> auditLogs(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return auditLogMapper.selectRecent(safeLimit).stream()
                .map(log -> new AuditLogResponse(
                        String.valueOf(log.getOperatorId()),
                        log.getTargetType(),
                        log.getTargetId(),
                        log.getAction(),
                        log.getResult(),
                        log.getOccurredAt()
                ))
                .toList();
    }

    private void writeAudit(Long operatorId, Long postId, String action, String result) {
        AuditLog audit = new AuditLog();
        audit.setOperatorId(operatorId);
        audit.setTargetType("POST");
        audit.setTargetId(String.valueOf(postId));
        audit.setAction(action);
        audit.setResult(result);
        audit.setOccurredAt(LocalDateTime.now());
        auditLogMapper.insert(audit);
    }

    private PostSummaryResponse summary(Post post) {
        User author = userMapper.selectById(post.getAuthorId());
        PostCategory category = categoryMapper.selectById(post.getCategoryId());
        String content = post.getContent() == null ? "" : post.getContent().replaceAll("\\s+", " ").trim();
        String excerpt = content.length() > 180 ? content.substring(0, 180) + "…" : content;
        List<String> tags = tagMapper.selectByPostId(post.getId()).stream().map(Tag::getName).toList();
        PostAuthorResponse authorResponse = author == null
                ? new PostAuthorResponse("0", "unknown", "未知用户")
                : new PostAuthorResponse(String.valueOf(author.getId()), author.getUsername(), author.getNickname());
        PostCategoryResponse categoryResponse = category == null
                ? new PostCategoryResponse("unknown", "未分类")
                : new PostCategoryResponse(category.getCode(), category.getName());
        return new PostSummaryResponse(
                String.valueOf(post.getId()),
                post.getTitle(),
                excerpt,
                authorResponse,
                categoryResponse,
                tags,
                value(post.getViewCount()),
                value(post.getCommentCount()),
                value(post.getLikeCount()),
                value(post.getFavoriteCount()),
                post.getCreatedAt()
        );
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
