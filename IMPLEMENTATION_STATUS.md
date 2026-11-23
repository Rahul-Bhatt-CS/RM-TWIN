# Implementation Status — v2

## Implemented in source
- Transformer registration from frontend and selection from fleet.
- Transformer nameplate/identity metadata.
- Backend simulator with independent per-transformer cycles.
- Simulated top-oil temperature, winding hot spot, load and ambient temperature.
- Demo health assessments, component groups, coverage and explanations.
- Demo alerts with active-condition deduplication and resolution.
- Diagnostic-test record entry and listing.
- Protection-event record entry and listing.
- SSE dashboard events.
- Paginated readings, diagnostics, alerts, health history and protection events.
- Flyway initial schema migration.
- CORS allowlist, DTO validation, bounded page size and generic error handling. Write endpoints have no authentication in this demo build.
- Future 3D component-key extension documented.

## Not implemented
- Real sensor/gateway/SCADA integration.
- Automatic DGA fault interpretation.
- Validated transformer-specific health index or loading/thermal model.
- CSV bulk import.
- Authentication provider, individual users, RBAC and audit attribution.
- 3D model/mesh rendering and part selection.
- Retention/archival, report export and notifications.

## Verification performed in authoring environment
- Frontend TypeScript check: `npx tsc -b` passed.
- Frontend production bundling: not completed because the available archived node_modules lacked the platform-specific Rollup native package; npm installation timed out in this environment.
- Backend: source generated, but Maven is not installed in this environment; Java 21 is present. Backend compilation/tests have not been run.
- No runtime test against MySQL has been performed.

Before submission, run `mvn test` and `npm ci && npm run build` in a normal development environment, then run the app against a disposable MySQL database and exercise the README demo flow.
