# Security Review — v2

## Threats considered

- Unauthorized asset creation, diagnostic/maintenance record injection or alert acknowledgement.
- SQL injection through search/filter inputs.
- Excessive pagination or oversized user input.
- Cross-origin browser access.
- Leakage of database credentials.
- Accidental exposure of stack traces, SQL errors or actuator internals.
- XSS through imported notes or asset metadata.
- Unbounded history queries and frontend memory usage.

## Controls implemented

| Risk | Control |
|---|---|
| Anonymous writes | POST APIs are intentionally unauthenticated in this demo; any reachable client can create records |
| Cross-origin access | Exact configurable CORS origin list |
| SQL injection | Spring Data JPA parameterized repository methods; no concatenated SQL |
| Oversized pages | Maximum page size 100; negative page rejected |
| Invalid fields | Jakarta validation, length and numeric bounds |
| Duplicate asset code | Unique database constraint and service validation |
| Schema drift | Flyway migration + Hibernate `ddl-auto=validate` |
| ORM serialization leaks | DTO response objects; `open-in-view=false` |
| Error leakage | Generic unexpected error response; stack traces/SQL details disabled |
| Stored XSS | React renders text as text; no `dangerouslySetInnerHTML` |
| Unnecessary admin endpoints | No actuator env/config exposure |

## Residual risks / deployment requirements

- There is no authentication or authorization in this demo. Any client that can reach the API can create transformers, diagnostic records, maintenance records and acknowledge alerts. Add OIDC/OAuth2/JWT and role-based authorization before public deployment.
- HTTPS/TLS must be enabled for deployed traffic.
- Add reverse-proxy rate limiting, WAF rules if public, security headers, CSRF review for any future cookie auth, and request/connection quotas.
- Add dependency vulnerability scanning (OWASP Dependency-Check or equivalent), secret scanning, SAST, DAST and regular patching.
- Add database backups, restore drills, retention/partitioning and monitoring.
- Limit SSE connections and implement heartbeat/backpressure.
- Add audit log for asset creation and diagnostic imports.
- Restrict CORS to the exact deployed frontend origin; never use `*` in production.
- Use managed secrets, least-privilege DB credentials and network isolation.
- Validate imported file types/size and CSV parsing if bulk import is added.

This is a prototype security baseline, not a security certification.
