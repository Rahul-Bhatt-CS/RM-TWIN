ALTER TABLE transformer
 ADD COLUMN rated_top_oil_rise_k DECIMAL(8,3) NULL,
 ADD COLUMN rated_hotspot_gradient_k DECIMAL(8,3) NULL,
 ADD COLUMN oil_time_constant_min DECIMAL(8,3) NULL,
 ADD COLUMN winding_time_constant_min DECIMAL(8,3) NULL,
 ADD COLUMN loss_ratio DECIMAL(8,3) NULL,
 ADD COLUMN cooling_stages VARCHAR(300) NULL,
 ADD COLUMN sensor_mapping TEXT NULL,
 ADD COLUMN simulation_tick BIGINT NOT NULL DEFAULT 0,
 ADD COLUMN top_oil_rise_k_state DECIMAL(10,4) NOT NULL DEFAULT 35,
 ADD COLUMN hotspot_gradient_k_state DECIMAL(10,4) NOT NULL DEFAULT 15,
 ADD COLUMN oltc_tap_position INT NOT NULL DEFAULT 0,
 ADD COLUMN oltc_operation_count BIGINT NOT NULL DEFAULT 0;
