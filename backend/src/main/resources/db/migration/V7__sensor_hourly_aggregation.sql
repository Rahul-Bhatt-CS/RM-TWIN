CREATE TABLE sensor_reading_hourly (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 measurement_type VARCHAR(60) NOT NULL,
 bucket_start TIMESTAMP(6) NOT NULL,
 average_value DECIMAL(16,5) NOT NULL,
 minimum_value DECIMAL(16,5) NOT NULL,
 maximum_value DECIMAL(16,5) NOT NULL,
 sample_count BIGINT NOT NULL,
 source VARCHAR(20) NOT NULL,
 CONSTRAINT fk_hourly_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 UNIQUE KEY uk_hourly_metric_bucket (transformer_id,measurement_type,bucket_start,source),
 INDEX idx_hourly_asset_metric_bucket (transformer_id,measurement_type,bucket_start)
);
