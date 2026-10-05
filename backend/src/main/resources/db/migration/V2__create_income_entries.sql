CREATE TABLE income_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    income_source_id BIGINT NULL,
    source_name_snapshot VARCHAR(100) NULL,
    description TEXT NULL,
    recurrence_rule_id BIGINT NULL,
    is_base_recurrence BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_income_amount_positive CHECK (amount > 0),
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_income_user_date ON income_entries(user_id, date);
