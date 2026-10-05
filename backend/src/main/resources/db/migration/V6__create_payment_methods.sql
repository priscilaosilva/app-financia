CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type ENUM('CHECKING', 'SAVINGS', 'CREDIT_CARD') NOT NULL,
    due_day TINYINT NULL,
    billing_start_day TINYINT NULL,
    billing_end_day TINYINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payment_methods_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_methods_user ON payment_methods(user_id, active);

ALTER TABLE expense_entries
    ADD CONSTRAINT fk_expense_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id);
