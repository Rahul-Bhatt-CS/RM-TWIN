# Database Schema — v2

Flyway migration: `backend/src/main/resources/db/migration/V1__initial_schema.sql`.

| Table | Purpose | Important indexes / relations |
|---|---|---|
| transformer | Asset identity and nameplate metadata | Unique asset_code |
| sensor_reading | Timestamped continuous measurements | `(transformer_id, measurement_type, recorded_at)` |
| diagnostic_test | Periodic laboratory/field test results | `(transformer_id, test_date)` |
| protection_event | Discrete protection/auxiliary status events | `(transformer_id, occurred_at)` |
| health_assessment | Point-in-time status/index/coverage/explanation | `(transformer_id, assessed_at)` |
| health_component_score | Per-group score, weight, state and explanation | FK to health_assessment |
| alert | Degradation and protection-event alerts | `(transformer_id, created_at)`, `(transformer_id, status)` |

## Data principles
- Readings and tests are append-oriented historical records.
- Each reading/test carries unit, timestamp and provenance.
- `SIMULATED`, `IMPORTED` and `LIVE` identify source types; `LIVE` is reserved and not yet connected.
- Health assessment is not linked to a single reading because multiple records can contribute.
- Health component keys are stable domain identifiers intended for future 3D mapping.
- No data-retention/purge policy is implemented yet.

## Future schema additions
- `transformer_part`: asset-specific part identity, glTF node/mesh ID, component key and display metadata.
- `model_asset`: object-storage reference, model version and checksum.
- Optional mapping table from a part to sensor channels/diagnostic parameters.

## V2 reference-profile fields

Flyway migration `V2__reference_transformer_profile.sql` adds `lv_voltage_kv`, `rated_frequency_hz` and `reference_profile` to `transformer`. The default profile ID is `CEA-100MVA-220-33KV-3PH`.


## V3 migration — component-linked diagnostic records
`diagnostic_test.component_key` and `diagnostic_test.phase` are nullable so older records remain valid. `component_key` identifies the selected inspection area; `phase` identifies a bushing phase when relevant. The index `(transformer_id, component_key, phase)` supports component/phase lookups.

New `sensor_reading.measurement_type` values include `COOLING_PUMP_STATUS`, `RADIATOR_OUTLET_TEMPERATURE`, `OIL_LEVEL_PERCENT`, `OLTC_TAP_POSITION`, `OLTC_OPERATION_COUNT` and `OLTC_MOTOR_CURRENT_A`. Values remain in the existing sensor_reading table with `SIMULATED` provenance.
