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
- Record protection/auxiliary events such as Buchholz or pressure-relief indications. The app does not control equipment.
- Health index shows component groups, data coverage, missing data and explanations. DGA gases are stored but not automatically diagnosed.
- Server-side pagination for readings, alerts, health assessments, diagnostics and protection events.
- SSE dashboard updates.
- Flyway database migration; Hibernate validates rather than silently changing production schema.

## Architecture

```text
React + TypeScript
  ├── Fleet / add transformer
  ├── Transformer dashboard
  ├── Paginated readings, diagnostics, alerts, health, protection
  └── SSE client
          │ REST + SSE
Spring Boot modular monolith
  ├── Asset API
  ├── Simulation scheduler
  ├── Health assessment + alert rules
  ├── Diagnostic / protection record APIs
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
5. Add diagnostic records under **Diagnostics**. Use `IMPORTED` provenance for lab or research data and record the actual test date/source.
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
| GET/POST | `/transformers/{id}/protection-events` | List/add protection events |
| GET | `/transformers/{id}/alerts?page=0&size=25` | Paginated alerts |
| GET | `/transformers/{id}/health-history?page=0&size=25` | Paginated health history |
| GET | `/transformers/{id}/events` | Server-sent dashboard events |

Maximum page size is 100. POST endpoints do not require a token in this demo build.

## Health-index model and limitations

The health index is a **transparent demonstration score**, not a CEA/BIS/IEC/IEEE/CIGRE formula.

Current groups:
- Thermal and loading (25%): uses available top-oil, hot-spot and load values.
- Oil and DGA (30%): currently uses a moisture record for the demo score. DGA gas values are stored but not automatically diagnosed.
- Insulation (25%): unknown until a validated method/baseline is configured.
- Mechanical and electrical (15%): unknown until FRA, winding-resistance, bushing or PD methods are configured.
- Protection and auxiliary (5%): event visibility only; no health score is inferred from missing events.

The index averages only groups with supported inputs. `coverage` shows the fraction of configured group weights represented by available evidence. Missing groups are excluded, not assigned a healthy score. A critical hot-spot demo trigger overrides the average status. This is not an engineering assessment.

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
- **There is no authentication or authorization in this demo build.** Anyone who can reach the backend can create transformers, diagnostic records and protection events. Keep it on localhost or a trusted private network. Do not expose it publicly. Add proper identity, role-based authorization and audit logging before deployment.
- Use HTTPS/TLS for all deployed traffic.
- Add reverse-proxy rate limiting, request-size limits, security headers, dependency scanning, backup/restore and monitoring before public deployment.
- SSE client counts and retention policies need operational limits.
- No CSV upload endpoint; diagnostic records are entered through the form. Validate source licences and retain dataset provenance before importing bulk records.
- The simulator is synthetic, deterministic demonstration data; it is not a transformer thermal/physics model and resets its cycle after backend restart.
- No real sensor gateway, SCADA, IEC 61850, MQTT or field device is connected.
- No real-time protection control, trip command, remaining-life prediction or ML fault diagnosis.
- The demo health rules/weights require engineering review and transformer-specific manufacturer data before any real-world use.

## Future 3D extension

The backend uses stable health-component keys (`THERMAL`, `OIL_DGA`, `INSULATION`, `MECHANICAL`, `PROTECTION`) rather than UI labels as identifiers. A later `TransformerPart` model can map a 3D asset's glTF node/mesh IDs to component keys, measurement channels and diagnostic records. The future flow can be:

`select 3D part → resolve component key → show related measurements/tests/alerts/history`

No 3D model, mesh mapping, WebGL renderer or part-selection API is implemented in this release.

## Verification

- Backend: run `mvn test` from `backend/`.
- Frontend: run `npm ci && npm run build` from `frontend/`.
- Test with a disposable MySQL database before deployment.

## AI-use note

AI assistance was used to inspect the earlier project, propose the expanded schema and architecture, draft implementation files and identify security concerns. The developer must run the build/tests, review generated code, verify standards and dataset citations, and be able to explain all scoring and security decisions. The health-index weights and thresholds are explicit demo assumptions, not externally validated rules.
