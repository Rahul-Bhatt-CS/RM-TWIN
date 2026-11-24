ALTER TABLE alert
 ADD COLUMN occurrence_count INT NOT NULL DEFAULT 1,
 ADD COLUMN last_occurred_at TIMESTAMP(6) NULL,
 ADD COLUMN resolution_evidence VARCHAR(1000) NULL,
 ADD COLUMN operator_notes VARCHAR(1500) NULL;
UPDATE alert SET last_occurred_at=created_at WHERE last_occurred_at IS NULL;
ALTER TABLE maintenance_record
 ADD COLUMN related_alert_id BIGINT NULL,
 ADD COLUMN related_diagnostic_id BIGINT NULL,
 ADD INDEX idx_maintenance_alert (related_alert_id),
 ADD INDEX idx_maintenance_diagnostic (related_diagnostic_id);
