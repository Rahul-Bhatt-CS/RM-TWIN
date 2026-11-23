# Changelog — backend synthetic data coverage

- Added one-time synthetic historical diagnostic records for oil/DGA, insulation, phase-specific HV/LV bushings, FRA, winding resistance, PD, OLTC, radiator and fan categories.
- Added synthetic protection/auxiliary indication records.
- Continuous simulator continues to produce thermal, load, electrical-current, cooling, oil-level, radiator and OLTC telemetry.
- Health assessment now returns illustrative scenario scores for all five component groups when synthetic data is present, instead of showing every group as UNKNOWN.
- Imported diagnostic records remain unscored until a validated interpretation method and transformer-specific baseline are configured.
- All synthetic test rows are labelled `SIMULATED` with explicit non-operational-use notes.
- Fixed seed idempotency to check for the simulated DGA marker record, preventing diagnostic rows from being inserted on every five-second tick.
