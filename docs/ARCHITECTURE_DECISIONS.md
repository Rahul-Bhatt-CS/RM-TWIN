# Architecture Decisions — v2

| ID | Decision | Rationale / trade-off |
|---|---|---|
| ADR-01 | React + Vite + TypeScript | Fast frontend iteration with typed API contracts |
| ADR-02 | Java 21 + Spring Boot modular monolith | Familiar maintainable backend; avoids premature microservices |
| ADR-03 | MySQL + Spring Data JPA | Relational integrity for assets, measurements, diagnostics, alerts and assessments |
| ADR-04 | Flyway migrations; Hibernate validates schema | Avoid automatic production schema mutation |
| ADR-05 | Backend owns simulation | Single source of truth; UI cannot fabricate readings |
| ADR-06 | Independent tick counter per transformer | One asset's simulator phase does not control another |
| ADR-07 | REST for queries and SSE for dashboard updates | Simple request/response plus server push |
| ADR-08 | Timestamped immutable reading rows | Enables trend and historical review |
| ADR-09 | Separate `diagnostic_test` and `protection_event` records | Periodic test results and discrete events are not live sensor readings |
| ADR-10 | Stable health component keys | Future 3D mesh selection can resolve to a health component without coupling to labels |
| ADR-11 | Health score uses available evidence and reports coverage | Missing tests must not be treated as healthy |
| ADR-12 | DGA values are stored but not auto-diagnosed | Avoid invented thresholds or unsupported fault claims |
| ADR-13 | Server-side pagination, max page size 100 | Prevent unbounded table responses and browser memory use |
| ADR-14 | No authentication in local/demo prototype | Simplifies evaluator workflow; never expose publicly; add identity and authorization before production |
| ADR-15 | Exact configurable CORS origins | Avoid wildcard browser access |
| ADR-16 | No automatic seed fleet | User explicitly creates assets through the frontend |
| ADR-17 | No 3D renderer in this increment | Preserve clean domain extension point without adding unvalidated visual complexity |

## Rejected / deferred

- Kafka/microservices: not justified for a single demo backend and low event rate.
- Time-series database: MySQL is sufficient for this bounded prototype; evaluate retention/query load before migration.
- ML remaining-life prediction: no validated training data or evaluation protocol.
- 3D model: deferred until a reliable part-to-diagnostic mapping and model asset exist.
- Real sensor protocols: no hardware/gateway supplied.

## ADR — Interactive 3D model navigation

**Decision:** Add a dependency-free Canvas-based projected 3D transformer model to the Overview screen. It supports pointer drag rotation, wheel and button zoom, hover highlighting, click selection, reset view, and an accessible component list.

**Component identifiers:** `main-tank`, `conservator`, `radiator-left`, `radiator-right`, `hv-bushing-a/b/c`, `lv-bushing-a/b/c`, `tap-changer`, `control-cabinet`, and `cooling-fans`.

**Data mapping:** Model parts map to the existing health component keys `THERMAL`, `OIL_DGA`, `INSULATION`, `MECHANICAL`, and `PROTECTION`. The inspector reads latest thermal values from the dashboard and fetches diagnostic / protection records for the selected group.

**Trade-off:** The model is an illustrative 3D navigation model, not manufacturer CAD or a physics simulation. No Three.js dependency is required for this version. A future GLB/glTF viewer can replace the renderer while preserving component IDs and data-group mappings.
