# API Specification — Transformer Digital Twin

**Status:** Approved API baseline — initial read endpoints implemented  
**Basis:** Approved BRD, PRD and Technical Architecture Proposal  
**Backend recommendation:** Java 21 + Spring Boot  
**API style:** REST over HTTPS, with Server-Sent Events (SSE) proposed for live updates

---

## 1. Scope and conventions

The API supports the approved product capabilities:

1. Live transformer-health view from simulated sensor data.
2. Visible alerts when health degrades.
3. Reviewable historical information.

The assignment does not prescribe endpoint paths, HTTP methods, payload shapes, authentication, pagination, status codes or live-update technology. This document specifies the endpoints proposed in the approved architecture and identifies unresolved contract details.

### 1.1 Base path

```text
/api
```

The deployed base URL is environment-specific and is not fixed here.

### 1.2 General conventions

- Use JSON for REST request and response bodies.
- Use ISO 8601 timestamps.
- Store timestamps consistently in UTC; display in the user's local timezone.
- Use DTOs rather than exposing persistence entities.
- Use numeric values with explicit units.
- Identify readings as simulated, for example with `source: "SIMULATED"`.
- Return clear errors for invalid input, missing resources and unavailable data.

### 1.3 Authentication

Authentication and role-based access control are not required by the approved requirements. The proposed initial API does not require JWT or user/role endpoints.

If authentication is added later, it must be documented as an optional extension and must not be confused with an assignment requirement.

## 2. Endpoint inventory

The following endpoints come from the approved architecture proposal. Simulation-control endpoints are conditional: include them only if the UI exposes simulator controls.

| Method | Endpoint | Purpose | Status |
|---|---|---|---|
| GET | `/api/transformers` | List configured transformers | Proposed |
| GET | `/api/transformers/{id}` | Retrieve transformer details | Proposed |
| GET | `/api/transformers/{id}/dashboard` | Retrieve latest readings and health state | Core supporting API |
| GET | `/api/transformers/{id}/readings/latest` | Retrieve latest sensor measurements | Proposed |
| GET | `/api/transformers/{id}/history` | Retrieve historical sensor readings | Core supporting API |
| GET | `/api/transformers/{id}/health/history` | Retrieve historical health assessments | Proposed |
| GET | `/api/transformers/{id}/alerts` | Retrieve alerts for a transformer | Core supporting API |
| GET | `/api/transformers/{id}/events` | Open an SSE connection | Optional live-update mechanism |
| POST | `/api/simulation/start` | Start the simulator | Conditional |
| POST | `/api/simulation/stop` | Stop the simulator | Conditional |
| POST | `/api/simulation/reset` | Reset demo simulation state | Conditional |

The mandatory user-visible capabilities are the live view, degradation alerts and history. The endpoint decomposition above is an implementation design, not an endpoint list supplied by the assignment.

## 3. Resource contracts

### 3.1 Transformer

A transformer resource identifies the asset represented by the Digital Twin.

Illustrative response:

```json
{
  "id": 1,
  "name": "Transformer T-01",
  "assetCode": "DEMO-T01",
  "rating": null
}
```

Fields:

| Field | Type | Notes |
|---|---|---|
| `id` | integer | Identifier |
| `name` | string | Display name |
| `assetCode` | string or null | Optional demo identifier |
| `rating` | string or null | Optional descriptive information; actual transformer specification is TBD |

The example values are illustrative demo data, not facts about a real transformer.

### 3.2 Sensor reading

Illustrative reading object:

```json
{
  "id": 101,
  "transformerId": 1,
  "measurementType": "OIL_TEMPERATURE",
  "value": 68.5,
  "unit": "°C",
  "recordedAt": "2026-11-22T10:30:00Z",
  "source": "SIMULATED"
}
```

| Field | Type | Notes |
|---|---|---|
| `id` | integer | Reading identifier |
| `transformerId` | integer | Associated transformer |
| `measurementType` | string | Supported measurement category |
| `value` | number | Measurement value |
| `unit` | string | Explicit unit |
| `recordedAt` | ISO 8601 timestamp | Measurement timestamp |
| `source` | string | Proposed provenance marker; initial simulated value is `SIMULATED` |

The final measurement types and units remain TBD until sensor selection and domain research are completed.

### 3.3 Health assessment

Illustrative assessment object:

```json
{
  "id": 201,
  "transformerId": 1,
  "healthStatus": "NORMAL",
  "healthScore": null,
  "explanation": "No configured degradation condition is currently met.",
  "assessedAt": "2026-11-22T10:30:00Z"
}
```

| Field | Type | Notes |
|---|---|---|
| `id` | integer | Assessment identifier |
| `transformerId` | integer | Associated transformer |
| `healthStatus` | string | Health state produced by the configured assessment logic |
| `healthScore` | number or null | Include only if a defined score is implemented |
| `explanation` | string or null | Optional explanation |
| `assessedAt` | ISO 8601 timestamp | Assessment time |

`NORMAL`, `WARNING` and `CRITICAL` are candidate status labels from the architecture proposal, not source-mandated values. Their definitions and thresholds must be finalized and documented.

### 3.4 Alert

Illustrative alert object:

```json
{
  "id": 301,
  "transformerId": 1,
  "condition": "THERMAL_DEGRADATION",
  "severity": "WARNING",
  "observedValue": 98.0,
  "thresholdValue": 95.0,
  "unit": "°C",
  "status": "ACTIVE",
  "createdAt": "2026-11-22T10:31:00Z"
}
```

| Field | Type | Notes |
|---|---|---|
| `id` | integer | Alert identifier |
| `transformerId` | integer | Associated transformer |
| `condition` | string | Rule or degradation condition that triggered |
| `severity` | string or null | Optional classification |
| `observedValue` | number or null | Value associated with the trigger |
| `thresholdValue` | number or null | Configured threshold, if applicable |
| `unit` | string or null | Unit for the associated measurement |
| `status` | string or null | Optional alert lifecycle state |
| `createdAt` | ISO 8601 timestamp | Alert creation time |

The values above are examples only. They do not establish actual transformer operating limits. Severity labels, alert lifecycle and numeric thresholds remain TBD.

## 4. Endpoint specifications

### 4.1 List transformers

**Request**

```http
GET /api/transformers
Accept: application/json
```

**Purpose**

Returns configured transformer assets.

**Illustrative response — `200 OK`**

```json
[
  {
    "id": 1,
    "name": "Transformer T-01",
    "assetCode": "DEMO-T01",
    "rating": null
  }
]
```

A single preconfigured transformer is sufficient for the proposed prototype. The list endpoint keeps the frontend independent of a hard-coded identifier.

### 4.2 Get transformer details

**Request**

```http
GET /api/transformers/1
Accept: application/json
```

**Purpose**

Returns descriptive information for one transformer.

**Illustrative response — `200 OK`**

```json
{
  "id": 1,
  "name": "Transformer T-01",
  "assetCode": "DEMO-T01",
  "rating": null
}
```

### 4.3 Get dashboard snapshot

**Request**

```http
GET /api/transformers/1/dashboard
Accept: application/json
```

**Purpose**

Returns the latest available readings and current health state in one request. This is the frontend's initial dashboard load.

**Illustrative response — `200 OK`**

```json
{
  "transformerId": 1,
  "assetName": "Transformer T-01",
  "source": "SIMULATED",
  "health": {
    "status": "NORMAL",
    "score": null,
    "explanation": "No configured degradation condition is currently met.",
    "assessedAt": "2026-11-22T10:30:00Z"
  },
  "lastUpdated": "2026-11-22T10:30:00Z",
  "readings": [
    {
      "measurementType": "OIL_TEMPERATURE",
      "value": 68.5,
      "unit": "°C",
      "recordedAt": "2026-11-22T10:30:00Z"
    },
    {
      "measurementType": "LOAD",
      "value": 75.0,
      "unit": "%",
      "recordedAt": "2026-11-22T10:30:00Z"
    }
  ],
  "activeAlertCount": 0
}
```

**Notes**

- `health.score` is nullable and should remain absent or null unless a meaningful score is defined.
- `activeAlertCount` is only meaningful if alert status/lifecycle is implemented. Otherwise omit it.
- If no readings exist, return an explicit empty or unavailable state; do not fabricate a normal reading.
- The dashboard must clearly identify data as simulated.

### 4.4 Get latest readings

**Request**

```http
GET /api/transformers/1/readings/latest
Accept: application/json
```

**Purpose**

Returns the latest available reading for each supported measurement type.

**Illustrative response — `200 OK`**

```json
{
  "transformerId": 1,
  "source": "SIMULATED",
  "readings": [
    {
      "measurementType": "OIL_TEMPERATURE",
      "value": 68.5,
      "unit": "°C",
      "recordedAt": "2026-11-22T10:30:00Z"
    },
    {
      "measurementType": "LOAD",
      "value": 75.0,
      "unit": "%",
      "recordedAt": "2026-11-22T10:30:00Z"
    }
  ]
}
```

The endpoint should not assume all measurement types update at exactly the same time. Each reading carries its own timestamp.

### 4.5 Get historical sensor readings

**Request**

```http
GET /api/transformers/1/history?from=2026-11-22T00:00:00Z&to=2026-11-22T12:00:00Z
Accept: application/json
```

**Purpose**

Returns previously persisted sensor readings for the selected transformer and time range.

**Illustrative response — `200 OK`**

```json
{
  "transformerId": 1,
  "from": "2026-11-22T00:00:00Z",
  "to": "2026-11-22T12:00:00Z",
  "readings": [
    {
      "id": 101,
      "measurementType": "OIL_TEMPERATURE",
      "value": 68.5,
      "unit": "°C",
      "recordedAt": "2026-11-22T10:30:00Z",
      "source": "SIMULATED"
    }
  ]
}
```

**Query parameters**

| Parameter | Type | Required? | Description |
|---|---|---:|---|
| `from` | ISO 8601 timestamp | TBD | Start of requested period |
| `to` | ISO 8601 timestamp | TBD | End of requested period |
| `measurementType` | string | Optional | Filter by measurement type, if implemented |

The architecture proposes time-range filtering. Exact parameter requirements, maximum range, pagination and default range must be finalized before implementation.

### 4.6 Get historical health assessments

**Request**

```http
GET /api/transformers/1/health/history
Accept: application/json
```

**Purpose**

Returns previous health assessments for the transformer.

**Illustrative response — `200 OK`**

```json
{
  "transformerId": 1,
  "assessments": [
    {
      "id": 201,
      "healthStatus": "NORMAL",
      "healthScore": null,
      "explanation": "No configured degradation condition is currently met.",
      "assessedAt": "2026-11-22T10:30:00Z"
    }
  ]
}
```

Optional time-range filtering may follow the same convention as the readings-history endpoint. It is not a mandatory requirement.

### 4.7 Get alerts

**Request**

```http
GET /api/transformers/1/alerts
Accept: application/json
```

**Purpose**

Returns alerts associated with the transformer.

**Illustrative response — `200 OK`**

```json
{
  "transformerId": 1,
  "alerts": [
    {
      "id": 301,
      "condition": "THERMAL_DEGRADATION",
      "severity": "WARNING",
      "observedValue": 98.0,
      "thresholdValue": 95.0,
      "unit": "°C",
      "status": "ACTIVE",
      "createdAt": "2026-11-22T10:31:00Z"
    }
  ]
}
```

The API must make the degradation condition visible to the user. Severity and lifecycle fields are optional design decisions.

### 4.8 Open live event stream (SSE)

**Request**

```http
GET /api/transformers/1/events
Accept: text/event-stream
```

**Purpose**

Opens a server-to-client event stream for live dashboard updates.

**Illustrative event**

```text
event: dashboard-update
data: {
  "transformerId": 1,
  "source": "SIMULATED",
  "lastUpdated": "2026-11-22T10:30:05Z",
  "health": {
    "status": "WARNING",
    "explanation": "A configured degradation condition was met."
  },
  "readings": [
    {
      "measurementType": "OIL_TEMPERATURE",
      "value": 72.0,
      "unit": "°C",
      "recordedAt": "2026-11-22T10:30:05Z"
    }
  ],
  "newAlerts": []
}
```

**Notes**

- This is an optional transport choice from the architecture, not an assignment requirement.
- Publish an update only after the corresponding data and assessment have been processed successfully.
- The client should reconnect if the connection drops and fetch a fresh dashboard snapshot after reconnecting.
- If SSE proves difficult to deploy, periodic REST polling is the approved fallback in the architecture proposal.

### 4.9 Start simulation — conditional

**Request**

```http
POST /api/simulation/start
Content-Type: application/json
```

**Purpose**

Starts the simulator only if the frontend includes a simulation control.

**Illustrative response**

```json
{
  "status": "RUNNING",
  "message": "Simulation started."
}
```

This endpoint is not necessary if the simulator starts automatically with the backend.

### 4.10 Stop simulation — conditional

**Request**

```http
POST /api/simulation/stop
Content-Type: application/json
```

**Purpose**

Stops the simulator only if a stop control is included.

**Illustrative response**

```json
{
  "status": "STOPPED",
  "message": "Simulation stopped."
}
```

### 4.11 Reset simulation — conditional

**Request**

```http
POST /api/simulation/reset
Content-Type: application/json
```

**Purpose**

Resets demo simulation state only if a reset control is deliberately included.

The reset contract must explicitly state whether it resets only the simulator's in-memory phase or also deletes stored readings and alerts. Do not silently delete historical data. This endpoint can be omitted entirely from the first implementation.

## 5. Error response convention

A consistent error response is recommended.

**Illustrative format**

```json
{
  "timestamp": "2026-11-22T10:35:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Transformer not found.",
  "path": "/api/transformers/999"
}
```

Suggested HTTP status usage:

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `400 Bad Request` | Invalid query parameter or request data |
| `404 Not Found` | Transformer or requested resource does not exist |
| `500 Internal Server Error` | Unexpected server-side failure |
| `503 Service Unavailable` | Optional response when a required service is unavailable |

The exact error schema and mapping are implementation conventions, not assignment requirements.

## 6. Validation and edge-case behaviour

The PRD identifies several cases the implementation should consider. The API and frontend should handle them explicitly:

| Situation | Expected API/UI behaviour to define |
|---|---|
| No readings generated yet | Return an empty/unavailable state, not a fabricated normal value |
| Simulator stopped or stalled | Make the last update time visible; do not imply that stale data is live |
| Invalid or missing measurement | Do not use it as a valid input to health evaluation |
| Insufficient data for health evaluation | Avoid asserting a definitive health state |
| No history for requested period | Return an empty collection |
| Unknown transformer ID | Return `404 Not Found` |
| Invalid time range | Return `400 Bad Request` |
| SSE disconnect | Reconnect and fetch a fresh dashboard snapshot |
| Repeated degraded readings | Apply the documented duplicate-alert policy |

Exact validation ranges, time-range limits, retry intervals and duplicate-alert rules remain TBD.

## 7. API-to-requirement traceability

| Requirement | API support |
|---|---|
| FR-01 — Transformer Digital Twin | `GET /api/transformers/{id}` and dashboard resource |
| FR-02 — Simulated sensor data | Simulator processing; reading resources identify `SIMULATED` provenance |
| FR-03 — Live asset-health view | `GET /dashboard`, `GET /readings/latest`, optional SSE events |
| FR-04 — Degradation alerts | `GET /alerts`; alert creation occurs in backend processing |
| FR-05 — Historical information | `GET /history` and `GET /health/history` |
| FR-08 — README and run instructions | Documentation deliverable; not an API endpoint |

## 8. Decisions required before implementation

| Decision | Current status |
|---|---|
| Base URL and deployment paths | Environment-specific |
| Final sensor types and units | TBD |
| Dashboard response fields | Proposed; finalize with sensor and health model |
| Health status vocabulary | TBD |
| Health-score field | Omit unless a defined score is implemented |
| Degradation rules and thresholds | Must be researched and documented |
| Alert severity and lifecycle | Optional/TBD |
| History date filters | Proposed; exact validation and defaults TBD |
| Pagination | Not specified; add only if needed |
| SSE versus polling | SSE recommended; polling fallback |
| Simulator control endpoints | Conditional; omit if no UI controls |
| Authentication | Not required by approved baseline |
| Rate limits and performance targets | Not specified |

## 9. Explicit exclusions

Do not add endpoints for the following unless the scope is separately approved:

- User registration, login, JWT refresh or role administration.
- Physical sensor or gateway provisioning.
- SCADA, MQTT, Modbus, IEC 61850 or other industrial-protocol ingestion.
- Maintenance work orders.
- Predictive failure probability or remaining useful life.
- Multi-tenant organization and substation management.
- Exports, notifications by email/SMS or external integrations.

These are not needed for the approved assignment baseline.

---

## 10. Implementation handoff

Before implementation begins:

1. Finalize the supported measurement types and units.
2. Research and document the health-degradation rules and their limitations.
3. Decide whether health assessment consumes one reading or multiple readings.
4. Finalize the health-status vocabulary and whether a numeric score is justified.
5. Decide the minimum alert lifecycle and duplicate-suppression behaviour.
6. Confirm the dashboard and history response contracts.
7. Choose SSE or polling based on available implementation time.
8. Remove conditional simulator-control endpoints if the UI will not expose those controls.

This document specifies the proposed database and API contracts. It does not authorize implementation of unspecified features or convert optional architecture choices into assignment requirements.


## Implementation notes (v1)
- `health_assessment` does not include `sensor_reading_id`: each assessment evaluates a snapshot containing multiple latest measurements. This follows the schema's explicit warning not to force a one-reading relationship.
- The `HealthDto` uses `healthStatus`; numeric score remains null/omitted because no approved health-score formula exists.
- History `from` and `to` parameters are required in the implementation to bound the query; the API document marked their exact requirement status as TBD. This is recorded as a v1 API decision.
- `severity` and `status` are populated for the demo alert experience. Alert lifecycle is limited to `ACTIVE` and `RESOLVED`; acknowledgement is not implemented.
- SSE event stream currently publishes dashboard snapshots after each simulator tick. The client refreshes alerts through REST after receiving an event.
