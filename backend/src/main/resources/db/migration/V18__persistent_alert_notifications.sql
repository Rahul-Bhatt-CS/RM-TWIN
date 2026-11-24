CREATE TABLE alert_notification (
 id BIGINT NOT NULL AUTO_INCREMENT,
 transformer_id BIGINT NOT NULL,
 alert_id BIGINT NOT NULL,
 title VARCHAR(180) NOT NULL,
 message VARCHAR(1000) NOT NULL,
 severity VARCHAR(20) NOT NULL,
 created_at TIMESTAMP(6) NOT NULL,
 read_at TIMESTAMP(6) NULL,
 PRIMARY KEY (id),
 CONSTRAINT fk_notification_transformer FOREIGN KEY (transformer_id) REFERENCES transformer(id),
 CONSTRAINT fk_notification_alert FOREIGN KEY (alert_id) REFERENCES alert(id),
 UNIQUE KEY uk_notification_alert (alert_id),
 INDEX idx_notification_asset_time (transformer_id, created_at),
 INDEX idx_notification_unread (transformer_id, read_at)
);
