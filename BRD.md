# Business Requirements — Transformer Digital Twin v2

## Objective
Deliver the assignment's required end-to-end transformer digital twin: simulated live asset-health view, degradation alerts and reviewable history. Extend it with user-managed transformer registration, diagnostic-test records and an explainable demonstration health index.

## User capabilities
1. Register a transformer with identity, nameplate/rating, cooling, insulation, commissioning and location metadata.
2. Select a registered transformer and inspect its dashboard.
3. View live simulated top-oil temperature, winding hot-spot, load and ambient temperature.
4. Review alerts and historical readings.
5. Add imported diagnostic results (DGA/oil tests) and protection/auxiliary events.
6. Review health-component scores, evidence coverage and missing data.
7. Browse large datasets through server-side pagination.

## Non-functional requirements
- MySQL persistence and schema migrations.
- Typed React frontend and REST/SSE backend.
- Input validation, protected write endpoints, exact-origin CORS and bounded pagination.
- Clearly label simulated and imported data.
- No missing-data-as-normal behavior.
- No control of protective devices.

## Boundaries
This is a demonstration application, not an operational safety system. It does not connect to physical sensors, SCADA or protection relays. It does not provide validated fault diagnosis, remaining-life prediction or a certified health index. 3D model selection is a documented future extension, not part of this implementation.

## Acceptance criteria
- A user can create a transformer from the UI using authorized write access.
- The new transformer appears in the fleet and can be selected.
- The dashboard updates from backend-generated readings when simulation is enabled.
- Alerts are generated for explicitly configured demo conditions.
- Diagnostic/protection records can be entered and reviewed.
- Readings, diagnostics, alerts, health history and protection events are paginated by the backend.
- Health index includes component explanation and evidence coverage; unsupported groups remain unknown.
