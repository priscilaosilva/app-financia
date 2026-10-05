CREATE TABLE recurrence_exceptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recurrence_rule_id BIGINT NOT NULL,
    original_date DATE NOT NULL,
    exception_type ENUM('CANCELLED', 'MODIFIED') NOT NULL,
    modified_amount DECIMAL(15, 2) NULL,
    modified_description TEXT NULL,
    CONSTRAINT fk_exceptions_rule FOREIGN KEY (recurrence_rule_id) REFERENCES recurrence_rules(id) ON DELETE CASCADE
);

CREATE INDEX idx_recurrence_exceptions_rule ON recurrence_exceptions(recurrence_rule_id, original_date);
