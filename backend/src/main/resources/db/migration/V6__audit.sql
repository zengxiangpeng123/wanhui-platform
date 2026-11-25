CREATE TABLE audit_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    operator_id BIGINT UNSIGNED NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    result VARCHAR(20) NOT NULL,
    occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_audit_logs_time (occurred_at DESC, id DESC),
    KEY idx_audit_logs_operator (operator_id, occurred_at DESC),
    CONSTRAINT fk_audit_logs_operator FOREIGN KEY (operator_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
