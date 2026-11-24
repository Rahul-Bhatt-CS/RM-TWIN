ALTER TABLE diagnostic_test
    ADD COLUMN test_method VARCHAR(120) NULL,
    ADD COLUMN sample_point VARCHAR(80) NULL,
    ADD COLUMN test_temperature_c DECIMAL(8,3) NULL,
    ADD COLUMN test_voltage VARCHAR(40) NULL,
    ADD COLUMN test_duration_seconds INT NULL,
    ADD COLUMN baseline_reference VARCHAR(180) NULL;

CREATE INDEX idx_diagnostic_asset_parameter_date
    ON diagnostic_test (transformer_id, parameter_name, test_date);
