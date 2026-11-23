# Decision Log — v2

- Asset creation is a frontend workflow; no hidden seed fleet.
- Simulation is backend-owned and opt-in per asset.
- Sensor records and diagnostic tests are separate entities because they have different sampling cadence and provenance.
- DGA values can be recorded and graphed, but no automatic DGA diagnosis is implemented without a chosen interpretation method and validated thresholds.
- Health score is a prototype composite with explicit weights and evidence coverage; it is not an official standard formula.
- Critical demo hot-spot alert overrides the composite status.
- All large tables use server-side pagination, max 100 rows per request.
- Write operations are unauthenticated in the local/demo prototype for ease of evaluation; add proper authentication/RBAC before public deployment.
- Future 3D selection will resolve mesh identifiers to stable component keys; no 3D renderer is included.
- MySQL + Flyway used; Hibernate validates schema.
- SSE updates are scoped by transformer ID.
- Time values are stored as UTC instants and rendered in browser local time.

- ADR-021: Use one reference transformer profile: CEA-listed 100 MVA, 220/33 kV, three-phase. Frequency 50 Hz is the India project assumption; mineral-oil/conservator/ONAN-ONAF are conceptual visual choices requiring OEM confirmation.
- ADR-022: Generate a detailed local GLB procedural model and use model-viewer for real 3D orbit/zoom plus eight numbered inspection hotspots. Hotspots map to condition groups, not asserted sensor locations.
- ADR-023: Extend nameplate with LV voltage, rated frequency and reference profile; derive apparent MVA and balanced HV/LV line current from S=√3VI using nominal voltage. These remain simulated/idealized values.
- ADR-024: Keep demo alert thresholds explicitly labelled as software test triggers, not CEA/IEC/IEEE/OEM settings. Do not score DGA or moisture automatically without approved interpretation and context.
