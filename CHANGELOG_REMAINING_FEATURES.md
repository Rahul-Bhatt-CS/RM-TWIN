# GridPulse — Remaining feature implementation

## Added
- Component-linked diagnostic records with optional phase metadata.
- HV bushing phases A/B/C and LV bushing phases a/b/c can be stored independently.
- 3D inspector filters diagnostics by selected component and displays phase labels.
- Expanded simulated telemetry: cooling pump state, oil level, radiator outlet temperature, OLTC tap position, demo operation count and motor-current pulse.
- Scripted cooling-assist warning scenario during elevated hot-spot conditions.
- Extracted and unit-tested balanced three-phase apparent-power/current calculations.
- Updated API, schema, standards-alignment, 3D map and implementation-status documentation.

## Not fabricated / requires OEM data
A true transformer-specific IEC 60076-7 thermal model is not activated. The selected CEA rating alone does not provide the OEM thermal constants and cooling parameters needed to implement and validate it. Obtain the actual GTP and approved thermal model inputs before adding that calculation. DGA, bushing, PD, FRA and oil-test records are stored but are not automatically diagnosed from invented thresholds.

## Verification
- TypeScript/TSX syntax transpilation checked successfully with the globally available TypeScript transpiler.
- Full `tsc -b` and Vite production build not completed: `npm ci` timed out and dependency tree was incomplete.
- Java/Maven tests were not run: Maven and cached Spring dependencies were unavailable in the authoring environment. A JUnit test for the three-phase current calculation is included.
- ZIP integrity checked after packaging.

## Security
Administrator token remains removed as requested. Write APIs remain unauthenticated; run only on localhost/trusted network until real authentication and authorization are implemented.


## Database unit-length fix

- Added Flyway migration `V4__widen_measurement_units.sql`.
- Increased `diagnostic_test.unit` and `sensor_reading.unit` from VARCHAR(20) to VARCHAR(40).
- Updated both JPA entity mappings to length 40.
- This resolves MySQL error 1406 (`Data too long for column 'unit'`) when synthetic diagnostic labels such as `°C · synthetic indication` are inserted.

## Health/alarm workflow alignment (2026-11-24)
- Replaced fixed demonstration category scores with parameter-level 0–100 piecewise-linear scoring and weighted category aggregation.
- Applied category weights: thermal/loading 25%, oil/DGA 30%, insulation 25%, mechanical/electrical 15%, protection/auxiliary 5%.
- Health Index is normalized over assessed category weights; missing/unscored categories reduce coverage and are never assumed healthy. Minimum coverage defaults to 60%.
- Added automatic diagnostic-condition alerts and retained automatic telemetry alerts. Critical conditions override status separately from the numeric Health Index.
- Removed the manual protection-event entry form and write endpoint; protection-event history is read-only in the UI.
- Kept manual entry for offline diagnostic/laboratory results only.
- Added alert acknowledgement (migration V5). Acknowledgement records review and does not resolve an alert.
- Documented all scoring assumptions and limitations in `docs/HEALTH_INDEX_AND_DOMAIN_MODEL.md`.

## Engineering upgrade (November 2026)
- Replaced scripted thermal values with a configurable first-order dynamic thermal response and load-responsive operating scenario.
- Made forced cooling and pump telemetry conditional on configured cooling type; OLTC count is now event-driven.
- Removed synthetic protection-health scoring and unsupported standalone DGA scoring.
- Added stale telemetry alerts and diagnostic evidence freshness.
- Added DGA evidence/ratio/rate endpoint, diagnostic test-condition metadata and cross-page server-side search.
- Added sensor history time windows, hourly aggregation/retention, and maintenance history.
- Added thermal model unit tests and implementation/research documentation.
- Authentication, authorization and security configuration unchanged.

## Persistence and hourly health updates (2026-11-24)
- Removed the sensor-reading hourly aggregate table and its API/UI path. Sensor history now queries persisted raw readings for every selected time range.
- Removed the scheduled raw-to-hourly sensor rollup and automatic 90-day deletion. Simulated raw readings remain in `sensor_reading` until an explicit retention policy is introduced.
- Added one `health_assessment` snapshot per transformer per UTC hour, protected by a unique database key. Health evaluation updates the current hour's snapshot; the hourly scheduler ensures a snapshot is captured at the start of each hour even when simulation cadence changes.
- Added persistent `alert_notification` records created transactionally when a new alert episode is raised. Added notification and mark-read APIs plus an in-app notification feed and opt-in browser notifications.
- Simulated sensor values and diagnostic seed records continue to be persisted in `sensor_reading` and `diagnostic_test`; simulation thermal/OLTC state remains persisted on `transformer`. No heap collection is used as the source of simulated measurement/state truth. The in-memory SSE registry only tracks connected clients, not simulated data.
