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
| GET | `/transformers/{id}/protection-events` | Public read | List simulated/recorded protection events |
| GET | `/transformers/{id}/alerts?page=0&size=25` | Public read | Paginated alerts |
| POST | `/alerts/{alertId}/acknowledge?by=Operator` | Unauthenticated demo write | Acknowledge an active alert without resolving it |
| GET | `/transformers/{id}/health-history?page=0&size=25` | Public read | Paginated assessment history |
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
- Protection-event history is read-only in this demo; there is no manual protection-event or alarm creation endpoint.
