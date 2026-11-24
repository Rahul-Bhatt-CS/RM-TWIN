CREATE TABLE maintenance_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 activity_type VARCHAR(60) NOT NULL,
 component_key VARCHAR(80) NOT NULL,
 description VARCHAR(1000) NOT NULL,
 performed_at TIMESTAMP(6) NOT NULL,
 performed_by VARCHAR(120),
 findings VARCHAR(1000),
 next_due_at TIMESTAMP(6) NULL,
 CONSTRAINT fk_maintenance_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 INDEX idx_maintenance_asset_date (transformer_id,performed_at)
);
