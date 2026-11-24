# Implementation status

## Implemented in this update
- Load-responsive first-order thermal simulation with configurable illustrative constants.
- Cooling-type-aware fan/pump simulation; no pump assumed for ONAN/ONAF.
- Discrete OLTC tap-operation sequence.
- Health assessment freshness checks and stale telemetry alerts.
- Ambient temperature removed as direct asset penalty; oil level separated from thermal score.
- Synthetic protection score removed; protection category remains not assessed.
- DGA evidence endpoint with latest gas values, selected ratios and rate calculation when two samples exist.
- Diagnostic test context fields and server-side search.
- Sensor history time-window filters.
- Removed sensor-hourly roll-up table and automatic raw-reading deletion.
- Persistent hourly health-index snapshots with coverage and component evidence.
- Persistent alert notifications, in-app feed and opt-in browser notifications.
- Database-backed simulated readings, diagnostic seed records and transformer simulation state.
- Maintenance history API and UI.
- Thermal model unit tests.

## Not implemented
- Physical sensor/SCADA ingestion and protocol adapters.
- Full standard-specific DGA diagnosis or a full IEC 60076-7 implementation.
- OEM-calibrated thresholds, thermal constants or asset baselines.
- Expert diagnosis for FRA, PD, bushings, OLTC and protection.
- External notification channels (email/SMS/push service), maintenance workflow integration, HA and multi-instance SSE.
- Authentication, authorization and security changes (explicitly excluded from this task).

## Verification
- Maven tests/build not run: Maven unavailable in the execution environment.
- Frontend production build not run: npm dependency installation timed out and extracted dependencies are incomplete.
- Flyway V6-V8 scripts added; must be verified against a copy of the user's MySQL database before deployment.
