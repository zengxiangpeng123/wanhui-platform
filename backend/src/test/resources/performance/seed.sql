-- 万汇网性能基线数据（可重复执行）
-- 资源：202 个（由 catalog/resources.json 提供，无需入库）
-- 帖子：5000；评论：20000
SET SESSION cte_max_recursion_depth = 100000;

-- 1. 基线用户（不存在时创建）
INSERT INTO users (
    username, username_normalized, password_hash,
    email_ciphertext, email_iv, email_key_version,
    email_hmac, email_hmac_version, email_masked,
    nickname, status, created_at
)
SELECT
    'perf_seed_user', 'perf_seed_user', '$2a$12$perf.seed.hash',
    UNHEX('00'), UNHEX(REPEAT('00', 12)), 1,
    UNHEX(REPEAT('AB', 32)), 1, 'perf***@example.com',
    '性能基线用户', 'ACTIVE', NOW(3)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username_normalized = 'perf_seed_user');

SET @uid = (SELECT id FROM users WHERE username_normalized = 'perf_seed_user' LIMIT 1);
SET @cat = (SELECT id FROM post_categories ORDER BY id LIMIT 1);

-- 2. 清理旧基线数据（级联删除评论/点赞/收藏）
DELETE FROM posts WHERE title LIKE 'PERF-%';

-- 3. 5000 个帖子
INSERT INTO posts (
    author_id, category_id, title, content, status,
    view_count, comment_count, like_count, favorite_count, created_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 5000
)
SELECT
    @uid, @cat,
    CONCAT('PERF-', LPAD(n, 5, '0')),
    CONCAT('性能基线帖子正文 ', n, '：用于搜索、列表与详情接口的压测数据。'),
    'PUBLISHED', n * 7, 0, 0, 0,
    DATE_SUB(NOW(3), INTERVAL n SECOND)
FROM seq;

-- 4. 20000 条评论，均匀分布到 5000 个帖子
INSERT INTO post_comments (post_id, user_id, content, status, like_count, created_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 20000
),
numbered AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM posts WHERE title LIKE 'PERF-%'
)
SELECT
    nb.id, @uid,
    CONCAT('PERF-COMMENT-', LPAD(s.n, 5, '0')),
    'PUBLISHED', 0,
    DATE_SUB(NOW(3), INTERVAL s.n SECOND)
FROM seq s
JOIN numbered nb ON nb.rn = ((s.n - 1) % 5000) + 1;

-- 5. 同步评论计数
UPDATE posts p
SET comment_count = (SELECT COUNT(*) FROM post_comments c WHERE c.post_id = p.id)
WHERE p.title LIKE 'PERF-%';

-- 6. 校验
SELECT
    (SELECT COUNT(*) FROM posts WHERE title LIKE 'PERF-%') AS perf_posts,
    (SELECT COUNT(*) FROM post_comments WHERE content LIKE 'PERF-COMMENT-%') AS perf_comments;
