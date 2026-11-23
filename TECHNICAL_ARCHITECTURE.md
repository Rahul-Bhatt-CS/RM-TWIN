# Technical Architecture — v2

## Stack
- Frontend: React, TypeScript, Vite, Recharts.
- Backend: Java 21, Spring Boot, Spring Web, Spring Data JPA, Jakarta Validation.
- Database: MySQL 8.
- Migrations: Flyway.
- Live updates: Server-Sent Events.

## Runtime flow
1. User creates a transformer through POST `/api/transformers`.
2. A backend scheduler loops over transformers with simulation enabled.
3. Each tick creates timestamped top-oil, winding hot-spot, load and ambient readings.
4. HealthService evaluates supported demo rules, stores component scores and health assessment.
5. AlertService deduplicates active alerts by transformer/condition key and resolves cleared demo conditions.
6. RealtimeService publishes a dashboard DTO to SSE subscribers scoped to that transformer.
7. Frontend receives the event and updates the selected asset dashboard.

## Modules
- `domain`: JPA entities and enums.
- `repository`: parameterized Spring Data repository queries.
- `service`: asset, simulation, health, alert and realtime logic.
- `api`: REST controllers, DTOs and exception mapping.
- `config`: CORS configuration.

## Persistence
Flyway creates schema. Hibernate is configured with `ddl-auto=validate`; open-in-view is disabled. DTOs prevent JPA entities from being serialized directly. Large tables are fetched with server-side pages.

## Health model
Five stable component keys:
- `THERMAL` 25%
- `OIL_DGA` 30%
- `INSULATION` 25%
- `MECHANICAL` 15%
- `PROTECTION` 5%

Only groups with supported evidence contribute to the current demonstration score. Coverage is the sum of their configured weights. Unsupported groups remain unknown. Current demo scoring is deliberately limited: thermal/load triggers and a moisture demonstration rule. DGA gas values are stored but not diagnosed automatically. See `docs/HEALTH_INDEX_AND_DOMAIN_MODEL.md`.

## Security
See `docs/SECURITY_REVIEW.md`. In brief: exact CORS origins, DTO validation, bounded pagination, JPA parameterization, Flyway, generic errors and limited actuator exposure. Write endpoints are unauthenticated in this demo and must not be publicly exposed.

## Future 3D extension
A future glTF viewer can use `componentKey` values to connect selected mesh nodes to health groups. Add `TransformerPart` and `ModelAsset` entities plus a parts API. A 3D click should resolve a part to its component key and then display existing measurements, tests, alerts and history. Keep rendering/model files in object storage; keep mappings and metadata in MySQL. No renderer is included in v2.
