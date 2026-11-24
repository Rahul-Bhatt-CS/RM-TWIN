# GridPulse — Transformer Digital Twin

A transformer-monitoring demonstration built with **Java 21 + Spring Boot + MySQL + React + TypeScript**. It supports multiple transformers, simulated live measurements, degradation alerts, historical records, imported diagnostic-test records, and an explainable demonstration health index.

> **Safety notice:** This is an educational software demonstration, not a protection system, operational safety assessment, or substitute for transformer manufacturer guidance, applicable standards, site procedures, or a qualified engineer. The demo thresholds and health-index scoring must not be used to make real operating decisions.

## What is implemented

- Add transformers through the frontend; asset codes are unique.
- Select a transformer from the fleet and open its dashboard.
- Backend-owned simulator generates top-oil temperature, winding hot-spot temperature, load percentage and ambient temperature.
- Independent per-transformer simulation cycle; demo data is marked `SIMULATED`.
- Rule-triggered demonstration alerts for configured hot-spot/load triggers.
- Add imported diagnostic records (e.g. DGA gases, moisture, BDV, tan delta) with date, unit, source notes and lab.
- Health index shows component groups, data coverage, missing data and explanations. DGA gases are stored but not automatically diagnosed.
- Server-side pagination for readings, alerts, diagnostics and maintenance records. The dashboard retains only the latest health assessment.
- SSE dashboard updates.
- Flyway database migration; Hibernate validates rather than silently changing production schema.

## Architecture

```text
React + TypeScript
  ├── Fleet / add transformer
  ├── Transformer dashboard
  ├── Raw/aggregated readings, diagnostic workspaces, alerts, maintenance
  └── SSE client
          │ REST + SSE
Spring Boot modular monolith
  ├── Asset API
  ├── Simulation scheduler
  ├── Health assessment + alert rules
  ├── Diagnostic sample / maintenance record APIs
  └── Realtime event service
          │ JPA / Flyway
        MySQL
```

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+
- MySQL 8+

## 1. Create the database

```sql
CREATE DATABASE transformer_twin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'transformer_app'@'%' IDENTIFIED BY 'use-a-strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
ON transformer_twin.* TO 'transformer_app'@'%';
FLUSH PRIVILEGES;
```

Use a dedicated database account. Do not use the MySQL root account in deployment.

## 2. Configure backend environment

Copy `backend/.env.example` to your deployment environment (or export the variables in your shell). Do not commit `.env` files.

Required:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `CORS_ALLOWED_ORIGINS` — exact frontend origin(s), comma-separated.

Optional:
- `SIMULATION_ENABLED=true`
- `SIMULATION_INTERVAL_MS=5000`
- `WARNING_HOTSPOT_C=95`
- `CRITICAL_HOTSPOT_C=110`
- `WARNING_LOAD_PERCENT=100`

The example thresholds are software-demo triggers only. They are not universal transformer operating limits.

## 3. Run backend

```bash
cd backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`. Flyway applies the schema migration on startup. The application expects the database to exist first.

## 4. Run frontend

```bash
cd frontend
npm ci
cp .env.example .env
npm run dev
```

Open the Vite URL, normally `http://localhost:5173`. Set `VITE_API_BASE_URL` if the backend is hosted elsewhere.

## 5. First-use workflow

1. Select **Add transformer**.
2. Enter a name, unique asset code, rating and optional metadata.
3. Create the asset. If the simulator is enabled, wait for readings.
4. Select the transformer card or dropdown to open its dashboard.
5. Add offline diagnostic records under **Diagnostics**. Use `IMPORTED` provenance for actual lab or inspection data and record the actual test date/source. Live readings and alarms are generated automatically by the simulator. Protection events are read-only in this demo.
6. Review alerts, history and health coverage.

No transformer is silently seeded. This makes asset creation an explicit part of the demo.

## API overview

All endpoints use `/api`.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/transformers?page=0&size=50` | Paginated asset list |
| POST | `/transformers` | Create transformer |
| GET | `/transformers/{id}/dashboard` | Current dashboard |
| GET | `/transformers/{id}/readings?metric=...&page=0&size=25` | Paginated readings |
| GET/POST | `/transformers/{id}/diagnostics` | List/add diagnostic results |
| GET | `/transformers/{id}/alerts?page=0&size=25` | Paginated alerts |
| POST | `/alerts/{alertId}/acknowledge?by=Operator` | Record operator acknowledgement; does not resolve the alert |
| GET | `/transformers/{id}/events` | Server-sent dashboard events |

Maximum page size is 100. POST endpoints do not require a token in this demo build. Alert acknowledgement records operator review; it does not clear the underlying condition.

## Health-index model and limitations

The health index is a **transparent demonstration score**, not a CEA/BIS/IEC/IEEE/CIGRE formula. Parameter scores use piecewise-linear 0–100 normalization; category scores average available parameter scores; the overall index is the weighted average normalized by assessed category coverage. After removing protection/auxiliary, the original 25:30:25:15 relative proportions are normalized across Thermal, Oil/DGA, Insulation and Mechanical/Electrical. See `docs/HEALTH_INDEX_AND_DOMAIN_MODEL.md` for the exact method and limitations.

Category weights and score availability:
- Thermal and loading (26.32%): requires fresh top-oil and winding hot-spot readings; load is an operating-stress/alert input, not a direct health score.
- Oil quality and DGA (31.58%): fresh moisture, BDV and acidity evidence may be scored; DGA is grouped by sample and displayed for engineering review, not averaged as independent gas scores.
- Insulation (26.32%): selected IR, PI and winding/bushing tan-delta evidence is scored only where a project scoring rule is defined.
- Mechanical and electrical (15.78%): selected FRA deviation and fan-vibration demonstration anchors are scored; other diagnostic results remain evidence-only without test context and baselines.

Within a category, the latest available record per parameter/component/phase is scored and the available scores are averaged. Overall HI is `sum(category score × category weight) / sum(assessed category weights)`. Coverage is the sum of assessed category weights. Minimum coverage defaults to 60%; missing data is never assigned a score of 100. Critical conditions override the status label, not the numeric HI. See `docs/HEALTH_INDEX_AND_DOMAIN_MODEL.md` for the interpolation anchors and explicit limitations. These are project demonstration assumptions, not universal operating thresholds.

## Security and known limitations

Implemented:
- CORS is restricted to configured exact origins.
- Request DTO validation, field length/range constraints, unique asset code, and bounded pagination.
- JPA repository queries are parameterized; no user-controlled SQL or dynamic sort expression.
- `spring.jpa.open-in-view=false`; API returns DTOs rather than entities.
- Flyway migrations and `ddl-auto=validate`.
- Generic server errors; stack traces and SQL details are not returned to the browser.
- No actuator environment/config endpoints are exposed.

Known limitations before public/production deployment:
- **There is no authentication or authorization in this demo build.** Anyone who can reach the backend can create transformers and diagnostic records, and acknowledge alerts. Keep it on localhost or a trusted private network. Do not expose it publicly. Add proper identity, role-based authorization and audit logging before deployment.
- Use HTTPS/TLS for all deployed traffic.
- Add reverse-proxy rate limiting, request-size limits, security headers, dependency scanning, backup/restore and monitoring before public deployment.
- SSE client counts and retention policies need operational limits.
- No CSV upload endpoint; diagnostic records are entered through the form. Validate source licences and retain dataset provenance before importing bulk records.
- The simulator is synthetic demonstration data and uses a simplified first-order thermal response. Its simulation tick, thermal state, tap position and operation count are persisted on the transformer record and resume after backend restart. This is not a complete IEC 60076-7 model.
- No real sensor gateway, SCADA, IEC 61850, MQTT or field device is connected.
- No real-time protection control, trip command, remaining-life prediction or ML fault diagnosis.
- The demo health rules/weights require engineering review and transformer-specific manufacturer data before any real-world use.

## Future 3D extension

The backend uses stable health-component keys (`THERMAL`, `OIL_DGA`, `INSULATION`, `MECHANICAL`) rather than UI labels as identifiers. A later `TransformerPart` model can map a 3D asset's glTF node/mesh IDs to component keys, measurement channels and diagnostic records. The future flow can be:

`select 3D part → resolve component key → show related measurements/tests/alerts/history`

No 3D model, mesh mapping, WebGL renderer or part-selection API is implemented in this release.

## Verification

- Backend: run `mvn test` from `backend/`.
- Frontend: run `npm ci && npm run build` from `frontend/`.
- Test with a disposable MySQL database before deployment.

## AI-use note

AI assistance was used to inspect the earlier project, propose the expanded schema and architecture, draft implementation files and identify security concerns. The developer must run the build/tests, review generated code, verify standards and dataset citations, and be able to explain all scoring and security decisions. The health-index weights and thresholds are explicit demo assumptions, not externally validated rules.

## Interactive transformer model

The Overview screen includes a detailed procedural GLB model with eight numbered clickable inspection hotspots. It uses model-viewer 4.0.0 loaded from a pinned Google CDN URL; browser internet access is required for the viewer library. The GLB model file is stored locally in `frontend/public/models/`.

### Model interactions

- Drag on the model to orbit/rotate the transformer.
- Use the mouse wheel/pinch gesture to zoom and drag to orbit the model.
- Hover over a numbered hotspot to highlight it.
- Click/tap a numbered hotspot to select the inspection area and open its condition inspector.
- The component list beside the model is an accessible alternative for selecting the same parts.

### Component-to-data mapping

| Model component | Health group / records |
|---|---|
| 1 Main tank | Thermal and loading readings; oil/DGA records are listed separately |
| 5 Radiator banks | Thermal and loading readings |
| 2 Conservator | Oil and DGA diagnostic records |
| 3 HV bushings / 4 LV bushings | Insulation diagnostic records |
| 7 Tap-changer housing | Mechanical / electrical diagnostic records |
| 6 Cooling fans | Thermal readings and cooling status |
| 8 Marshalling / monitoring cabinet | Configured sensor mapping metadata |

The current model is conceptual geometry, not manufacturer-specific CAD, a dimensionally accurate transformer, or a claim about physical sensor locations. The inspector maps components to asset-level health groups and currently available records. For a future detailed model, replace the conceptual geometry with a licensed GLB/glTF CAD export and retain the stable component identifiers / group keys used by the inspector.

## Selected reference transformer and 3D inspection

The default project profile is the CEA-listed 100 MVA, 220/33 kV, three-phase power-transformer rating (`CEA-100MVA-220-33KV-3PH`). The 3D model is a detailed procedural conceptual illustration with eight numbered clickable inspection areas. It is not an OEM CAD model. See:

- `docs/REFERENCE_TRANSFORMER_SPEC.md`
- `docs/NUMBERED_3D_INSPECTION_MAP.md`
- `docs/STANDARDS_ALIGNMENT.md`

The simulator now derives apparent MVA and balanced three-phase HV/LV line currents from the registered nameplate. Temperature/load/fan sequences remain synthetic scenarios. The backend does not claim to implement IEC 60076-7 thermal equations; transformer-specific thermal constants and approved OEM alarm settings are not available in the reference rating alone.

### 3D viewer runtime dependency
The 3D view uses the Web Component `model-viewer` 4.0.0 loaded from the Google AJAX CDN in `frontend/index.html`. The model file is local at `frontend/public/models/transformer_100mva_220_33kv.glb`. A network connection is required for the viewer library unless you vendor that library locally.


## Component-level inspection additions
- Diagnostic entries support `componentKey` and optional `phase`; use `HV_BUSHING` with phase `A`, `B`, or `C`, or `LV_BUSHING` with phase `a`, `b`, or `c` for bushing test history.
- Supported component keys: `MAIN_TANK`, `CONSERVATOR`, `HV_BUSHING`, `LV_BUSHING`, `RADIATOR`, `COOLING_FAN`, `OLTC`, `CONTROL_CABINET`.
- Simulated auxiliary telemetry includes cooling fan/pump status, oil level, radiator outlet temperature, OLTC tap position, demonstration operation count and motor-current pulse.
- These auxiliary signals are scripted demo values. They are not manufacturer-calibrated control behaviour.
- A genuine IEC 60076-7 thermal model remains gated on OEM thermal constants and engineering validation; the current scripted thermal cycle must not be represented as standards-compliant thermal prediction.


## Important security boundary
This demonstration intentionally has no administrator token or authentication because that was requested. Write endpoints are unauthenticated. Keep the backend on localhost or a trusted private network; do not expose it publicly. Before public deployment, add proper identity, role-based authorization, rate limiting, audit trails and TLS. CORS is not authentication.


## Synthetic diagnostic demonstration data

When simulation is enabled, the backend seeds one historical set of synthetic diagnostic records per transformer. It includes DGA gases and oil tests, insulation/ dielectric tests, phase-specific HV/LV bushing examples, FRA, winding resistance, partial-discharge, OLTC and radiator records, with a shared DGA sample identifier. No protection-event records are generated or stored. The records use `source=SIMULATED` and include notes stating they are not actual measurements or standards-based pass/fail conclusions. The dashboard normalizes supported parameters to 0–100 using project-defined good/warning/poor anchors, averages them within category, and combines assessed categories using documented weights. Parameters without a defensible project scoring rule remain unscored and reduce coverage. These scores are not IEC/CEA/OEM health ratings.

Synthetic diagnostic history is seeded once per transformer. Continuous simulated telemetry continues at `SIMULATION_INTERVAL_MS` (default 5000 ms). A newly created transformer receives its synthetic diagnostic history on its first simulator tick.

## Engineering improvement update (November 2026)

See `docs/EXECUTION_PLAN_AND_IMPLEMENTATION.md` for the research basis, implementation phases, migration notes, configuration defaults, limitations and verification status. The current simulator uses a simplified load-responsive first-order thermal model; its default parameters are demonstration assumptions, not OEM data. DGA values and ratios are presented as evidence for review and are not automatically classified into definitive faults. Protection-event functionality and protection scoring have been removed. Diagnostic records support optional method/sample/test-condition/baseline metadata. Maintenance history and hourly telemetry roll-up are available.

New database migrations V6-V16 are applied automatically by Flyway. They add diagnostic context, hourly aggregates, maintenance links, persisted simulation state, DGA sample identity and alert lifecycle fields, sensor-channel mapping and remove the legacy protection-event table. Keep the existing database and do not edit earlier migrations. Authentication, authorization and security configuration have intentionally not been changed in this update.

## Transformer engineering configuration
The asset-registration form accepts OEM thermal constants, cooling-stage configuration and sensor-channel mappings. Sensor mapping JSON keys must match the backend `MeasurementType` enum; for example `{"TOP_OIL_TEMPERATURE":"OTI-01","WINDING_HOTSPOT_TEMPERATURE":"WTI-01"}`. Mapped channel IDs are stored with raw readings and hourly aggregates. These fields configure the demonstration model; they do not represent an actual connected sensor gateway.
