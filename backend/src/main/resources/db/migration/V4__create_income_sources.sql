CREATE TABLE income_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_income_sources_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_income_sources_user ON income_sources(user_id, active);

ALTER TABLE income_entries
    ADD CONSTRAINT fk_income_source FOREIGN KEY (income_source_id) REFERENCES income_sources(id);
