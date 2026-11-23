ALTER TABLE diagnostic_test
  ADD COLUMN component_key VARCHAR(60) NULL AFTER notes,
  ADD COLUMN phase VARCHAR(8) NULL AFTER component_key;

CREATE INDEX idx_diagnostic_component_phase
  ON diagnostic_test(transformer_id, component_key, phase);
