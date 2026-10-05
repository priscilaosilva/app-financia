CREATE TABLE recurrence_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    frequency ENUM('DAILY', 'MONTHLY', 'YEARLY') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL
);

ALTER TABLE income_entries
    ADD CONSTRAINT fk_income_recurrence FOREIGN KEY (recurrence_rule_id) REFERENCES recurrence_rules(id);

ALTER TABLE expense_entries
    ADD CONSTRAINT fk_expense_recurrence FOREIGN KEY (recurrence_rule_id) REFERENCES recurrence_rules(id);
