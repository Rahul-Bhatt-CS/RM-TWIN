ALTER TABLE transformer
  ADD COLUMN lv_voltage_kv DECIMAL(10,2) NULL AFTER voltage_class_kv,
  ADD COLUMN rated_frequency_hz DECIMAL(6,2) NULL AFTER rated_power_mva,
  ADD COLUMN reference_profile VARCHAR(100) NULL AFTER rated_frequency_hz;

UPDATE transformer
SET lv_voltage_kv = 33.00,
    rated_frequency_hz = 50.00,
    reference_profile = 'CEA-100MVA-220-33KV-3PH'
WHERE rated_power_mva = 100.00 AND voltage_class_kv = 220.00;
