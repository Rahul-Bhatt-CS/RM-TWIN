package com.transformertwin.service;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
/** Idempotent hourly roll-up of all raw data available before the current UTC day. */
@Service public class TelemetryRetentionService{
 private final JdbcTemplate jdbc;public TelemetryRetentionService(JdbcTemplate j){jdbc=j;}
 @Scheduled(cron="${app.telemetry.rollup-cron:0 15 2 * * *}") @Transactional public void rollup(){
  jdbc.update("INSERT INTO sensor_reading_hourly(transformer_id,measurement_type,bucket_start,average_value,minimum_value,maximum_value,sample_count,source,unit,sensor_key) SELECT transformer_id,measurement_type,DATE_FORMAT(recorded_at,'%Y-%m-%d %H:00:00'),AVG(value),MIN(value),MAX(value),COUNT(*),source,MAX(unit),sensor_key FROM sensor_reading WHERE recorded_at < UTC_DATE() GROUP BY transformer_id,measurement_type,DATE_FORMAT(recorded_at,'%Y-%m-%d %H:00:00'),source,sensor_key ON DUPLICATE KEY UPDATE average_value=VALUES(average_value),minimum_value=VALUES(minimum_value),maximum_value=VALUES(maximum_value),sample_count=VALUES(sample_count),unit=VALUES(unit),sensor_key=VALUES(sensor_key)");
  jdbc.update("DELETE FROM sensor_reading WHERE recorded_at < UTC_TIMESTAMP()-INTERVAL 90 DAY");
 }
}
