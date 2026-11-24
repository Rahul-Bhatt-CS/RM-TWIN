UPDATE sensor_reading SET sensor_key='' WHERE sensor_key IS NULL;
UPDATE sensor_reading_hourly SET sensor_key='' WHERE sensor_key IS NULL;
ALTER TABLE sensor_reading MODIFY COLUMN sensor_key VARCHAR(120) NOT NULL DEFAULT '';
ALTER TABLE sensor_reading_hourly MODIFY COLUMN sensor_key VARCHAR(120) NOT NULL DEFAULT '';
ALTER TABLE sensor_reading_hourly DROP INDEX uk_hourly_metric_bucket;
CREATE UNIQUE INDEX uk_hourly_metric_bucket_sensor ON sensor_reading_hourly(transformer_id,measurement_type,bucket_start,source,sensor_key);
