# Architecture and Product Decision Log

This log records choices made where the source documents leave implementation details open. The source requirements remain the BRD/PRD; entries below are implementation decisions, not Rugged Monitoring requirements.

| ID | Decision | Reason | Trade-off / limitation | Status |
|---|---|---|---|---|
| ADR-001 | React + Vite + TypeScript frontend | Matches approved architecture; fast dashboard development | Adds frontend toolchain | Accepted |
| ADR-002 | Java 21 + Spring Boot modular monolith | Matches approved architecture and keeps domain logic in one deployable backend | Not independently scalable by module | Accepted |
| ADR-003 | MySQL with Spring Data JPA | Matches approved architecture and relational schema | Prototype uses Hibernate schema update; production should use versioned migrations | Accepted |
| ADR-004 | Backend-owned simulator | One authoritative stream feeds persistence, health, alerts and UI | Simulation is not physical telemetry | Accepted |
| ADR-005 | Three simulated measurements: oil temperature, winding hot-spot temperature, load | Minimal sensor set proposed by architecture; all support a clear dashboard | DGA and partial discharge omitted from first increment | Accepted for prototype |
| ADR-006 | Multi-input health assessment has no single `sensor_reading_id` FK | Health is evaluated from a snapshot of the latest readings; schema explicitly warns against forcing a one-reading relation | Assessment does not persist a full immutable copy of all input readings in v1 | Accepted |
| ADR-007 | Health states: `NORMAL`, `WARNING`, `CRITICAL`, `UNAVAILABLE` | Candidate vocabulary in architecture plus explicit unavailable state required by PRD/UI | Exact rule thresholds are demo-only assumptions and are configurable | Accepted for demo |
| ADR-008 | Rule-based thresholds: hot-spot ≥ 95°C => WARNING; ≥ 110°C => CRITICAL; load ≥ 100% => at least WARNING | Produces a repeatable visible degradation demonstration | These are illustrative software-demo trigger values, not operating limits, protection settings, or a claim of standards compliance | Accepted for demo; review before public use |
| ADR-009 | Simulator cycle advances through NORMAL, STRESS, DEGRADED, RECOVERY | Deterministic demonstration of healthy-to-degraded transition and recovery | Synthetic profile is not a physical thermal model | Accepted for demo |
| ADR-010 | Simulator tick interval defaults to 5 seconds | Makes live updates observable in a short demo | Not specified by PRD; configurable | Accepted for demo |
| ADR-011 | SSE for live updates; REST for initial snapshots/history | Matches approved architecture recommendation | Browser reconnects; frontend refreshes snapshot on reconnect | Accepted |
| ADR-012 | Alert lifecycle uses `ACTIVE` and `RESOLVED`; repeated same-condition events do not create duplicates while active | Minimal deduplication policy makes alert list readable | Acknowledgement/severity workflow omitted; active alert resolves when rule clears | Accepted for demo |
| ADR-013 | UTC timestamps persisted; browser displays local time | Matches API specification | User timezone is browser-local | Accepted |
| ADR-014 | No authentication or roles | Not required by PRD and explicitly excluded by UI/UX spec | Demo is not suitable for public deployment without access controls | Accepted |
| ADR-015 | One seeded demo transformer | One asset is sufficient for approved scope | No fleet management | Accepted |
| ADR-016 | Retain history for prototype; no automatic purge | Assignment requires reviewable history and gives no retention period | Database grows until manually cleared | Accepted |
| ADR-017 | `ddl-auto=update` for local prototype | Low setup friction during incremental implementation | Not a production-grade schema migration approach | Accepted for prototype |

## Revisit before production
- Validate sensor types, units, operating ranges, and thresholds with authoritative transformer-domain sources.
- Replace illustrative health rules if a researched model is selected.
- Consider Flyway/Liquibase migrations.
- Define data retention and access control.
- Decide whether the simulator should expose start/stop/reset controls.

## Implementation progress

### Increment 1 — project bootstrap and core monitoring slice
- Created Spring Boot backend package structure, JPA entities/repositories, REST read endpoints, scheduled simulator, rule-based assessment, alert generation/deduplication, and SSE event publisher.
- Created React/Vite/TypeScript application shell with Overview, Alerts, and History screens.
- Added dashboard cards, historical readings chart/table, health assessment history table, loading/error/empty states, and SSE connection status.
- Verification limitation: Maven is not available in the current execution environment, and dependency installation/build verification has not yet been completed. Treat compilation/runtime as unverified until the commands in README are run locally.
