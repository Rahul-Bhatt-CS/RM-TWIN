# GridPulse engineering improvement: research, plan and implementation

## Scope

This implementation improves the engineering model, simulation, diagnostic evidence, health assessment, telemetry history, maintenance records and automated verification. Authentication, authorization and the existing security configuration are intentionally unchanged.

## Research basis and boundaries

- IEC 60076-7:2018: loading, operating temperatures, thermal ageing and generally applicable thermal models for mineral-oil-immersed transformers. The simulator uses the structure of a first-order thermal response with load-dependent steady-state rises. Its default thermal constants are illustrative; OEM heat-run/nameplate parameters must replace them for an actual asset.
- IEC 60599:2022: interpretation of dissolved/free gases as diagnostic guidance; findings require engineering judgement. GridPulse therefore exposes gas evidence and ratios but does not call a single-gas threshold a definitive diagnosis.
- IEEE C57.104-2019: DGA quality, interpretation and diagnostic methods. Coherent samples, detection limits, sample metadata and baseline are needed for a defensible interpretation.
- IEC 60422:2024: mineral insulating-oil supervision, maintenance and test evaluation. Its scope excludes DGA/furan-based equipment condition monitoring; GridPulse keeps those diagnostic paths distinct.
- CEA transformer technical specifications (66 kV and above): Indian reference context. It does not supply the missing OEM heat-run/GTP data for this reference asset.

The application does not claim IEC/IEEE/CEA certification. The Health Index weights and scoring anchors remain explicitly labelled project demonstration assumptions.

## Implemented work

### 1. Asset operation and thermal model
- Replaced the 48-step temperature script with load and ambient scenarios that change smoothly.
- Added a first-order dynamic thermal response for top-oil rise and winding hot-spot gradient, including thermal time constants and load exponents.
- Added configurable parameters in `application.properties`: rated top-oil rise, rated hot-spot gradient, oil/winding time constants, loss ratio and exponents.
- Forced cooling is only modelled for asset cooling descriptions containing ONAF/OFAF/ODAF. Pump telemetry is not emitted for ONAN/ONAF.
- A fan-bank unavailability scenario is only enabled for forced-cooling assets.
- OLTC tap position, operation count and motor-current pulse now change together on a discrete simulated tap operation rather than a counter derived from every tick.

### 2. Health Index and alert integrity
- Excluded ambient temperature from direct asset-health penalty; it remains operating context for the thermal model.
- Excluded conservator oil level from the thermal category; it is monitored by its own alert.
- The initial iteration removed synthetic protection scoring; the follow-up implementation removed the protection-event feature, entity, repository and API entirely.
- DGA concentrations are not independently averaged into an overall scalar DGA score. Oil quality scoring is limited to the illustrative moisture, BDV and acidity anchors. DGA is marked for engineering review.
- Furan, partial discharge, OLTC resistance/time and other measurements without enough contextual metadata are not assigned unsupported scores.
- Added freshness checks for key live telemetry and stale-sensor alerts. Stale readings are excluded from thermal scoring.
- Offline diagnostic evidence is freshness-gated (DGA 30 days, oil tests 180 days, other periodic tests 730 days by configurable defaults).
- Phase-specific bushing results are collapsed to a parameter/component group before category averaging so three phases do not multiply the category weight.
- Health assessment writes are throttled to every 12 simulation ticks (one minute at the default 5-second interval), not every telemetry tick.

### 3. DGA evidence view
- Added `/api/transformers/{id}/dga-analysis`. The follow-up groups gases by sample ID, component and sample point, calculates within-sample ratios and returns sample-level trend points. Rates are calculated only for comparable sample point/component pairs with a positive time interval. DGA is not converted into fault labels.

### 4. Diagnostic record quality
- Added optional test method, sample point, test temperature, test voltage, test duration and baseline reference fields.
- Added Flyway V6 migration; existing records remain valid with null optional metadata.
- Added server-side diagnostic search across all pages, rather than filtering only the currently loaded page.

### 5. Historical data and operations
- Added 24-hour, 7-day, 30-day and all-available sensor history filters.
- Added hourly sensor aggregation table and an idempotent daily roll-up. Raw sensor records are retained for 90 days; hourly summaries are retained. The roll-up runs at 02:15 server time by default and can be configured.
- Added maintenance history records with component, activity, date, performer, findings and optional next-due date (Flyway V8).

### 6. Tests
- Added unit tests for thermal response direction, cooling effect and invalid elapsed time.
- Existing electrical calculation test remains.

## Configuration defaults (demonstration only)

- Rated top-oil rise: 55 K
- Rated hot-spot gradient: 25 K
- Oil time constant: 180 minutes
- Winding time constant: 7 minutes
- Loss ratio: 5
- Oil exponent: 0.8
- Winding exponent: 1.6
- Virtual time per 5-second tick: 1 minute
- Telemetry stale threshold: 30 seconds
- DGA evidence freshness: 30 days
- Oil test freshness: 180 days
- Other offline diagnostic freshness: 730 days

These are not values guaranteed for the 100 MVA reference transformer. Replace them using actual OEM GTP, heat-run test data, sensor specifications and site operating policy.

## Migration and upgrade notes

New Flyway migrations V6–V16 are included. V6 adds diagnostic context; V7 creates hourly aggregates; V8 creates maintenance history; V9 removes protection events; V10 persists simulation state and asset thermal configuration; V11 adds DGA sample identity; V12 adds alert lifecycle and maintenance links; V13 adds aggregate units; V14 adds sensor-channel identity; V15 keys hourly aggregates by sensor channel; V16 retains only the latest health assessment. Do not edit or delete V1–V8. Keep the existing database and allow Flyway to apply V9–V16 in order. The roll-up is idempotent and backfills all raw periods still available before the 90-day raw-data purge.

## Explicitly not implemented / remaining validation

- Real SCADA/IEC 61850/Modbus or OEM sensor ingestion.
- A complete IEC 60599 / IEEE C57.104 DGA classification engine with all method-specific constraints, sample validation, detection limits and expert review workflow. The new view is evidence presentation only.
- Full IEC 60076-7 implementation including all loading cases, winding/oil exponents and OEM calibration. Current dynamic model is a simplified first-order demonstration model inspired by the standard's thermal framework.
- Bushing, FRA, PD, OLTC and protection expert diagnosis using OEM baselines and test waveforms.
- Automatic maintenance work-order integration, notification escalation and multi-user audit attribution.
- Production security, authentication and authorization changes (explicitly excluded by request).
- Production-scale multi-instance SSE fanout, HA, disaster recovery and load testing.

## Validation status

Source archive and migration sequence were inspected. The frontend TS/TSX files passed TypeScript transpilation syntax checks. Full frontend semantic type-check/build was not completed because dependency installation did not complete. Maven is not installed, so Java compilation, automated tests and MySQL migration/integration were not run. A local Maven test/build, frontend `npm ci && npm run build`, and MySQL migration/integration run are required before treating this archive as runtime-verified.


## Follow-up implementation — asset workflow alignment (24 Nov 2026)

- Removed the Protection Events API and persistence entity/repository. Flyway V9 drops the legacy `protection_event` table.
- Removed protection/auxiliary from Health Index categories. The four remaining relative weights are normalized from 25:30:25:15.
- Removed the health-history endpoint and frontend tab. Only the latest assessment is retained per transformer; prior snapshots are deleted when a new assessment is saved.
- Persisted simulation tick, thermal state, OLTC tap position and operation count on the transformer row. State resumes after application restart.
- Added OEM thermal parameters, cooling-stage text and sensor-mapping JSON to transformer registration; mapped channel keys are now attached to simulated raw readings and carried into hourly aggregates.
- Added raw short-window and hourly aggregate long-window history API. Roll-up is idempotent and reprocesses all raw periods still retained before the 90-day purge.
- Added DGA sample identity. DGA ratios are calculated inside a sample; gas trend points and rates are returned only where sampling point/component and interval are comparable. Manual DGA entry requires a sample ID.
- Added alert recurrence count, last occurrence, resolution evidence and operator notes. Recurrence count increases across distinct alert episodes, not repeated polling ticks.
- Added maintenance links to alert and diagnostic record IDs, with same-transformer validation. Component linkage remains via component key.
- Replaced the diagnostics browser with dedicated DGA, oil-quality, insulation, bushing, OLTC and FRA/mechanical views.

### Migrations
V9 removes protection events; V10 adds persistent simulation and asset engineering configuration; V11 adds diagnostic sample identity; V12 adds alert lifecycle and maintenance references; V13 adds units to hourly aggregates; V14 adds sensor-channel identity to raw and hourly readings; V15 preserves sensor-channel identity in hourly aggregation keys; V16 removes old health snapshots. Preserve the database and allow Flyway to apply these migrations in order.

### Verification caveat
The source changes and ZIP packaging were checked. A complete Maven build/database integration test and frontend production build were not completed in this environment because Maven is unavailable and npm dependency installation did not complete. The new migrations and API contracts therefore still require execution against the user's MySQL database before operational use.
