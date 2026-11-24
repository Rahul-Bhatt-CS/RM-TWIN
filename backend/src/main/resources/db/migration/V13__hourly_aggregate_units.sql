ALTER TABLE sensor_reading_hourly ADD COLUMN unit VARCHAR(40) NOT NULL DEFAULT '';
UPDATE sensor_reading_hourly h
JOIN (SELECT transformer_id,measurement_type,MAX(unit) unit FROM sensor_reading GROUP BY transformer_id,measurement_type) s
 ON s.transformer_id=h.transformer_id AND s.measurement_type=h.measurement_type
SET h.unit=s.unit WHERE h.unit='';
