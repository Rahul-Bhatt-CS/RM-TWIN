-- Diagnostic and sensor units may include descriptive engineering notation.
-- Widen existing columns without modifying already-applied migrations.
ALTER TABLE diagnostic_test
    MODIFY COLUMN unit VARCHAR(40) NOT NULL;

ALTER TABLE sensor_reading
    MODIFY COLUMN unit VARCHAR(40) NOT NULL;
