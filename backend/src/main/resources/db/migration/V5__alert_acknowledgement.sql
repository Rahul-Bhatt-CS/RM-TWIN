ALTER TABLE alert
    ADD COLUMN acknowledged_at TIMESTAMP(6) NULL,
    ADD COLUMN acknowledged_by VARCHAR(100) NULL;
