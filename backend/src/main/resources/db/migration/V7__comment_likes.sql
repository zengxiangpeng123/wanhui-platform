ALTER TABLE post_comments
    ADD COLUMN like_count BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER status;

CREATE TABLE comment_likes (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    comment_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_comment_likes_user_comment (user_id, comment_id),
    KEY idx_comment_likes_comment (comment_id, created_at),
    CONSTRAINT fk_cl_comment FOREIGN KEY (comment_id) REFERENCES post_comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_cl_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
