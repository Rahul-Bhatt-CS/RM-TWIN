# Transformer Digital Twin

A transformer monitoring prototype for Rugged Monitoring's GBPUAT Pantnagar 2026–27 Software (CS and AI) take-home assignment.

## Mandatory product flow
- Live transformer health view driven by simulated sensor readings.
- Degradation alerts generated from documented demo rules.
- Historical readings and health assessments.

All readings are labelled `SIMULATED`. This project does not connect to physical sensors or SCADA.

## Technology
- Frontend: React + Vite + TypeScript + Recharts
- Backend: Java 21 + Spring Boot + Spring Data JPA
- Database: MySQL
- Live updates: Server-Sent Events (SSE), with REST snapshot refresh after reconnect

## Repository structure
```text
.
├── backend/
├── frontend/
├── docs/
│   └── DECISION_LOG.md
├── BRD.md
├── PRD.md
├── TECHNICAL_ARCHITECTURE.md
├── DATABASE_SCHEMA.md
├── API_SPECIFICATION.md
└── UI_UX_SPECIFICATION.md
```

## Prerequisites
- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- MySQL 8+

## Configure the database
Create a MySQL database and configure the backend environment variables:
- `DB_URL` — JDBC URL, e.g. `jdbc:mysql://localhost:3306/transformer_twin`
- `DB_USERNAME`
- `DB_PASSWORD`
- `CORS_ALLOWED_ORIGINS` — frontend origin, default `http://localhost:5173`

The backend uses `spring.jpa.hibernate.ddl-auto=update` for this prototype. This is a documented prototype choice, not a production migration strategy.

## Run backend
```bash
cd backend
mvn spring-boot:run
```
Backend base URL: `http://localhost:8080/api`

## Run frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`.

Set `VITE_API_BASE_URL` if the backend is not running at `http://localhost:8080/api`.

## Demo flow
1. Start MySQL.
2. Start the backend. It creates a demo transformer if one does not exist and starts the simulated data cycle.
3. Start the frontend.
4. Open Overview to inspect the latest readings and health status.
5. Open Alerts to inspect generated degradation alerts.
6. Open History to review readings and health assessments.
7. The simulator follows a deterministic normal → stress → degraded → recovery cycle. The values and rule thresholds are illustrative demonstration assumptions, not transformer operating limits or standards-compliant protection settings.

## API
See `API_SPECIFICATION.md`. Core routes:
- `GET /api/transformers`
- `GET /api/transformers/{id}`
- `GET /api/transformers/{id}/dashboard`
- `GET /api/transformers/{id}/readings/latest`
- `GET /api/transformers/{id}/history`
- `GET /api/transformers/{id}/health/history`
- `GET /api/transformers/{id}/alerts`
- `GET /api/transformers/{id}/events` (SSE)

## Tests
```bash
cd backend
mvn test
```
```bash
cd frontend
npm test
```

## Explicit limitations
- Sensor measurements and thresholds are simulated demo assumptions.
- The health status is rule-based and explainable; it is not a validated transformer diagnostic, prediction, or remaining-life model.
- No authentication, roles, maintenance workflows, physical sensor integrations, or SCADA protocols are included.
- The initial project supports one seeded transformer.

## AI-use note
Complete this truthfully before submission. Record which parts were AI-assisted, how they were checked, and one AI suggestion that was overruled. Do not claim verification that was not performed.


## Implementation status
See [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) for what is implemented, what remains, and verification limitations. Source files are scaffolded, but compilation and runtime verification have not yet been completed in this environment.

## Design decisions
See [docs/DECISION_LOG.md](docs/DECISION_LOG.md). In particular, the simulator profile and health thresholds are explicit demo assumptions, not real transformer operating limits.
