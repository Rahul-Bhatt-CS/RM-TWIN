CREATE TABLE transformer (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(120) NOT NULL,
 asset_code VARCHAR(60) NOT NULL UNIQUE,
 manufacturer VARCHAR(120),
 model VARCHAR(120),
 serial_number VARCHAR(100),
 voltage_class_kv DECIMAL(10,2),
 rated_power_mva DECIMAL(10,2),
 cooling_type VARCHAR(40),
 insulation_type VARCHAR(60),
 commissioning_date DATE,
 location VARCHAR(180),
 description VARCHAR(1000),
 simulation_enabled BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP(6) NOT NULL,
 updated_at TIMESTAMP(6) NOT NULL
);
CREATE TABLE sensor_reading (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 measurement_type VARCHAR(60) NOT NULL,
 value DECIMAL(16,5) NOT NULL,
 unit VARCHAR(20) NOT NULL,
 source VARCHAR(20) NOT NULL,
 recorded_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT fk_reading_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 INDEX idx_reading_asset_metric_time (transformer_id, measurement_type, recorded_at),
 INDEX idx_reading_time (recorded_at)
);
CREATE TABLE diagnostic_test (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 test_type VARCHAR(60) NOT NULL,
 parameter_name VARCHAR(80) NOT NULL,
 value DECIMAL(16,5) NOT NULL,
 unit VARCHAR(20) NOT NULL,
 test_date TIMESTAMP(6) NOT NULL,
 source VARCHAR(20) NOT NULL,
 laboratory VARCHAR(160),
 reference_note VARCHAR(500),
 notes VARCHAR(1000),
 CONSTRAINT fk_diagnostic_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 INDEX idx_diagnostic_asset_date (transformer_id, test_date)
);
CREATE TABLE protection_event (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 device_name VARCHAR(80) NOT NULL,
 event_type VARCHAR(40) NOT NULL,
 active BOOLEAN NOT NULL,
 occurred_at TIMESTAMP(6) NOT NULL,
 source VARCHAR(20) NOT NULL,
 notes VARCHAR(500),
 CONSTRAINT fk_protection_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 INDEX idx_protection_asset_time (transformer_id, occurred_at)
);
CREATE TABLE health_assessment (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 health_status VARCHAR(20) NOT NULL,
 health_index DECIMAL(6,2),
 coverage DECIMAL(5,4) NOT NULL,
 explanation VARCHAR(1500) NOT NULL,
 assessed_at TIMESTAMP(6) NOT NULL,
 CONSTRAINT fk_health_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 INDEX idx_health_asset_time (transformer_id, assessed_at)
);
CREATE TABLE health_component_score (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 assessment_id BIGINT NOT NULL,
 component_key VARCHAR(60) NOT NULL,
 component_label VARCHAR(100) NOT NULL,
 score DECIMAL(6,2),
 weight DECIMAL(5,4) NOT NULL,
 state VARCHAR(20) NOT NULL,
 explanation VARCHAR(1000) NOT NULL,
 CONSTRAINT fk_component_assessment FOREIGN KEY (assessment_id) REFERENCES health_assessment(id) ON DELETE CASCADE
);
CREATE TABLE alert (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 condition_key VARCHAR(100) NOT NULL,
 title VARCHAR(180) NOT NULL,
 message VARCHAR(1000) NOT NULL,
 severity VARCHAR(20) NOT NULL,
 status VARCHAR(20) NOT NULL,
 observed_value DECIMAL(16,5),
 unit VARCHAR(20),
 created_at TIMESTAMP(6) NOT NULL,
 resolved_at TIMESTAMP(6),
 CONSTRAINT fk_alert_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 INDEX idx_alert_asset_time (transformer_id, created_at),
 INDEX idx_alert_asset_status (transformer_id, status)
);
