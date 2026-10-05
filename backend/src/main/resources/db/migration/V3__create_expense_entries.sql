CREATE TABLE expense_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    expense_category_id BIGINT NULL,
    category_name_snapshot VARCHAR(100) NULL,
    payment_method_id BIGINT NULL,
    payment_method_name_snapshot VARCHAR(100) NULL,
    payment_status ENUM('CONFIRMED', 'PENDING_CREDIT') NOT NULL DEFAULT 'CONFIRMED',
    description TEXT NULL,
    recurrence_rule_id BIGINT NULL,
    is_base_recurrence BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_expense_amount_positive CHECK (amount > 0),
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_expense_user_date ON expense_entries(user_id, date);
CREATE INDEX idx_expense_status ON expense_entries(user_id, payment_status, date);
