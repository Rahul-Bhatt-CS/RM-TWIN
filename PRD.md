# Product Requirements — Transformer Digital Twin v2

## Product summary
A web application for registering transformers and reviewing their simulated operating condition, diagnostic evidence, alerts and history.

## Core flows
### A. Register and select an asset
- User opens Add Transformer.
- Enters unique asset code, name and optional nameplate/installation metadata.
- Submits the request directly; this prototype does not require a login or token.
- Created asset appears in the fleet; user selects it to open the dashboard.

### B. Monitor live condition
- Backend scheduler generates timestamped simulated readings.
- Readings are persisted and assessed.
- Dashboard snapshot is returned by REST; subsequent snapshots are published over SSE.
- Every reading identifies its source.

### C. Review diagnostics
- User records a dated diagnostic result such as H2, CH4, C2H2, moisture, BDV, acidity or tan delta.
- Record includes parameter, value, unit, test date, lab/reference and notes.
- Imported values are not represented as live sensor readings.
- DGA results are displayed as evidence; the app does not automatically diagnose faults.

### D. Review alerts and history
- Alerts are created from configured demonstration triggers and diagnostic evidence; protection-event functionality is out of scope and removed.
- Readings, diagnostics, alerts and maintenance records are returned in pages. Only the latest health assessment is retained and shown on the dashboard.
- Page size is bounded to 100.

### E. Review health index
- Dashboard displays health status, optional numeric index, weighted evidence coverage and group explanations.
- Unsupported/missing groups are marked unknown.
- Critical individual demo triggers can override the aggregate status.
- The index is explicitly not a real-world operating safety determination.

## Data model
- Transformer
- SensorReading
- DiagnosticTest
- HealthAssessment
- HealthComponentScore
- Alert

## User experience
- Fleet overview with add-asset action.
- Asset selector and overview dashboard.
- Tabs for readings, diagnostics, alerts and maintenance.
- Paginated tables with empty states.
- Responsive layout and accessible labels.

## Security
- Read endpoints are public in this prototype.
- Write endpoints are unauthenticated in this local/demo prototype.
- No administrator token is collected or sent by the frontend.
- CORS is exact-origin configurable.
- This is not a substitute for user identity, RBAC or production-grade authentication.

## Out of scope
- Physical sensors, gateway and SCADA protocols.
- Relay configuration, trip control or switching.
- Certified health index, automatic DGA diagnosis, ML/RUL.
- 3D model rendering and mesh selection.
- User accounts, per-user permissions, maintenance work orders and notifications.

## Future extension: 3D
Stable health component keys are reserved for a future `TransformerPart` model mapping glTF node identifiers to health groups and measurement/test channels. The future UI can select a component and retrieve associated readings, tests, alerts and history. No 3D functionality is shipped now.
