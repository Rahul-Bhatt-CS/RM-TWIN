# API Specification — v2

Base path: `/api`. JSON REST endpoints plus SSE. All page responses include `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`. Page size is capped at 100.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/transformers?page=0&size=50` | Public read | Paginated fleet |
| POST | `/transformers` | Unauthenticated demo write | Create transformer |
| GET | `/transformers/{id}` | Public read | Asset details |
| GET | `/transformers/{id}/dashboard` | Public read | Latest readings, health and active-alert count |
| GET | `/transformers/{id}/readings?metric=...&page=0&size=25` | Public read | Paginated readings |
| GET/POST | `/transformers/{id}/diagnostics` | Unauthenticated demo write | List/add diagnostic results |
| GET | `/transformers/{id}/alerts?page=0&size=25` | Public read | Paginated alerts |
| POST | `/alerts/{alertId}/acknowledge?by=Operator` | Unauthenticated demo write | Acknowledge an active alert without resolving it |
| GET | `/transformers/{id}/events` | Public read | SSE dashboard events |

## Write access
There is no token or authentication check in this prototype. POST endpoints are available to any client that can reach the API. Do not expose this configuration to the public internet. Add proper authentication and role-based authorization before deployment.

## Validation
- Asset code: 2–60 characters, `[A-Za-z0-9._-]`, unique.
- Names and text fields have explicit maximum lengths.
- Numeric nameplate values are bounded.
- Diagnostic value/date/unit are required. Optional `componentKey` associates a test with a stable 3D inspection area; optional `phase` supports HV A/B/C and LV a/b/c bushing records. There is no endpoint to manually create an alarm or protection event.
- Page index must be non-negative; size 1–100.
- Unknown transformer returns 404; validation errors return 400; duplicate/conflict returns 409.
- Unexpected errors return generic 500 response without stack traces or SQL details.

## Future API
`componentKey` values currently supported on diagnostic records: `MAIN_TANK`, `CONSERVATOR`, `HV_BUSHING`, `LV_BUSHING`, `RADIATOR`, `COOLING_FAN`, `OLTC`, `CONTROL_CABINET`. Phase is optional and limited to A/B/C/a/b/c. A future `GET /transformers/{id}/parts` can return the same mapping plus model-node metadata.

## Transformer nameplate profile additions

Transformer create/read contracts include `lvVoltageKv`, `ratedFrequencyHz` and `referenceProfile`. The fixed default profile `CEA-100MVA-220-33KV-3PH` requires 100 MVA, 220 kV HV, 33 kV LV and 50 Hz. The simulator derives `APPARENT_POWER_MVA`, `HV_LINE_CURRENT_A`, `LV_LINE_CURRENT_A`, cooling fan/pump state, oil level, radiator outlet temperature, OLTC tap position, demonstration operation count and motor-current pulse readings. These derived/simulated values are labelled `SIMULATED` and are not live CT/SCADA measurements.


### Component-linked diagnostic example
`POST /api/transformers/{id}/diagnostics` accepts optional `componentKey` and `phase`, for example `{"testType":"BUSHING_TEST","parameterName":"TAN_DELTA","value":0.0042,"unit":"pu","testDate":"2026-11-23T10:00:00Z","componentKey":"HV_BUSHING","phase":"A"}`. The record is an imported test result; it is not automatically judged against a universal limit.

## Alert acknowledgement
- `POST /api/alerts/{alertId}/acknowledge?by=Operator` records acknowledgement for an active alert.
- Acknowledgement does not resolve or suppress the alert condition. The alert is resolved only when the backend assessment no longer detects the condition.
- Protection-event functionality has been removed. There is no protection-event endpoint or persistence entity.


## Engineering update endpoints

- `GET /api/transformers/{id}/dga-analysis`: latest DGA gas evidence, sample counts, selected ratios and rate-of-rise when two time-separated samples are available. The endpoint deliberately does not return a definitive fault label.
- `GET /api/transformers/{id}/diagnostics?page=&size=&q=`: paginated diagnostic records; `q` searches parameter, test type, component, phase, laboratory and notes across the asset's records.
- `POST /api/transformers/{id}/diagnostics`: accepts optional `testMethod`, `samplePoint`, `testTemperatureC`, `testVoltage`, `testDurationSeconds` and `baselineReference` context fields.
- `GET /api/transformers/{id}/maintenance?page=&size=`: paginated maintenance history.
- `POST /api/transformers/{id}/maintenance`: creates an inspection, test, service or repair log entry.
- `GET /api/transformers/{id}/readings?metric=&from=&to=&page=&size=`: optional ISO-8601 time-window filtering.

## Operational notes

- Health assessment is persisted once per 12 simulation ticks (one minute with the default five-second tick).
- Thermal constants and freshness windows are configurable in `application.properties`. Defaults are demonstration assumptions.
- Flyway V6-V16 add diagnostic context, hourly sensor aggregates, maintenance history, persistent simulator state, DGA sample identity, alert lifecycle fields, aggregate units and sensor-channel identity. V9 drops the legacy protection-event table.


## Current workflow additions
- `GET /transformers/{id}/readings/aggregated?metric=...&from=...&to=...` returns hourly average/minimum/maximum/sample-count points for long-range history.
- `POST /alerts/{alertId}/notes` accepts `{ "notes": "..." }` for operator investigation notes.
- DGA diagnostic creation requires `sampleId`; sample-level analysis is returned by `GET /transformers/{id}/dga-analysis`.
- Maintenance creation accepts optional `relatedAlertId` and `relatedDiagnosticId`; each linked record must belong to the same transformer.
- Protection event and health-history endpoints have been removed. The latest health assessment remains available in the dashboard only.

## Transformer engineering configuration
`POST /transformers` also accepts optional `ratedTopOilRiseK`, `ratedHotspotGradientK`, `oilTimeConstantMin`, `windingTimeConstantMin`, `lossRatio`, `coolingStages` and `sensorMapping`. Thermal values should be taken from OEM heat-run/GTP data; omitted values use demonstration defaults. `coolingStages` is a comma-separated configuration such as `ONAN,ONAF` or `OFAF`. `sensorMapping` is a JSON object keyed by `MeasurementType`, for example `{ "TOP_OIL_TEMPERATURE": "OTI-01", "WINDING_HOTSPOT_TEMPERATURE": "WTI-01" }`. The mapped sensor key is attached to generated raw readings and hourly aggregates; unmapped channels are labelled `Unmapped` in the UI.
