# Database Schema — v2

Flyway migration: `backend/src/main/resources/db/migration/V1__initial_schema.sql`.

| Table | Purpose | Important indexes / relations |
|---|---|---|
| transformer | Asset identity and nameplate metadata | Unique asset_code |
| sensor_reading | Timestamped continuous measurements | `(transformer_id, measurement_type, recorded_at)` |
| diagnostic_test | Periodic laboratory/field test results | `(transformer_id, test_date)` |
| health_assessment | One persisted health score and evidence snapshot per transformer per UTC hour | Unique `(transformer_id, assessment_hour)` |
| health_component_score | Per-group score, weight, state and explanation | FK to health_assessment |
| alert | Degradation alerts with recurrence, acknowledgement, notes and resolution evidence | `(transformer_id, created_at)`, `(transformer_id, status)` |
| alert_notification | Durable in-app/browser notification for each new alert episode | Unique `alert_id`; `(transformer_id, created_at)` |

## Data principles
- Readings and tests are append-oriented historical records.
- Each reading/test carries unit, timestamp and provenance.
- `SIMULATED`, `IMPORTED` and `LIVE` identify source types; `LIVE` is reserved and not yet connected.
- Health assessment is not linked to a single reading because multiple records can contribute.
- Health component keys are stable domain identifiers intended for future 3D mapping.
- Sensor telemetry remains as timestamped raw rows in `sensor_reading`; no sensor-hourly aggregate table or scheduled raw-data purge is used.
- Health assessments are retained at hourly granularity. The row for the current UTC hour is refreshed as new evaluations arrive; older hourly snapshots remain available.
- Every newly raised alert episode creates a persistent notification row. Marking a notification read updates its `read_at` timestamp.

## Future schema additions
- `transformer_part`: asset-specific part identity, glTF node/mesh ID, component key and display metadata.
- `model_asset`: object-storage reference, model version and checksum.
- Optional mapping table from a part to sensor channels/diagnostic parameters.

## V2 reference-profile fields

Flyway migration `V2__reference_transformer_profile.sql` adds `lv_voltage_kv`, `rated_frequency_hz` and `reference_profile` to `transformer`. The default profile ID is `CEA-100MVA-220-33KV-3PH`.


## V3 migration — component-linked diagnostic records
`diagnostic_test.component_key` and `diagnostic_test.phase` are nullable so older records remain valid. `component_key` identifies the selected inspection area; `phase` identifies a bushing phase when relevant. The index `(transformer_id, component_key, phase)` supports component/phase lookups.

New `sensor_reading.measurement_type` values include `COOLING_PUMP_STATUS`, `RADIATOR_OUTLET_TEMPERATURE`, `OIL_LEVEL_PERCENT`, `OLTC_TAP_POSITION`, `OLTC_OPERATION_COUNT` and `OLTC_MOTOR_CURRENT_A`. Values remain in the existing sensor_reading table with `SIMULATED` provenance.

## Current schema changes (V9–V13)
- V9 drops the legacy protection-event table.
- V10 adds OEM thermal/cooling/sensor configuration and persisted simulator/OLTC state to transformer.
- V11 adds diagnostic sample identity.
- V12 adds alert recurrence/resolution/operator-note fields and maintenance links to alert/diagnostic records.
- V13–V15 were historical sensor-hourly aggregation migrations; V17 drops that table.
- V14 also adds `sensor_key` to raw readings so transformer sensor mappings are traceable.
- V16 retained only the latest assessment as an intermediate state; V17 introduces one unique assessment per UTC hour.
- V17 drops `sensor_reading_hourly` and adds the hourly health uniqueness key.
- V18 creates persistent alert notifications.
