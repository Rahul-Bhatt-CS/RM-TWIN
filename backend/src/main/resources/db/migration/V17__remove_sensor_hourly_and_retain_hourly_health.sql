-- Sensor history remains raw and persistent. Health snapshots are stored separately once per UTC hour.
DROP TABLE IF EXISTS sensor_reading_hourly;
ALTER TABLE health_assessment ADD COLUMN assessment_hour TIMESTAMP NULL;
UPDATE health_assessment SET assessment_hour = DATE_FORMAT(assessed_at, '%Y-%m-%d %H:00:00') WHERE assessment_hour IS NULL;
ALTER TABLE health_assessment MODIFY assessment_hour TIMESTAMP NOT NULL;
CREATE UNIQUE INDEX uk_health_asset_hour ON health_assessment(transformer_id, assessment_hour);
