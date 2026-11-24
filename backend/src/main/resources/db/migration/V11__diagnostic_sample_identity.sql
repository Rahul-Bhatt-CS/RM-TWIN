ALTER TABLE diagnostic_test ADD COLUMN sample_id VARCHAR(100) NULL;
CREATE INDEX idx_diagnostic_asset_sample ON diagnostic_test(transformer_id,sample_id,test_date);
