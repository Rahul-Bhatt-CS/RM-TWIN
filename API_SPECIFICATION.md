# API Specification — v2

Base path: `/api`. JSON REST endpoints plus SSE. All page responses include `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`. Page size is capped at 100.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/transformers?page=0&size=50` | Public read | Paginated fleet |
| POST | `/transformers` | Unauthenticated demo write | Create transformer |
| GET | `/transformers/{id}` | Public read | Asset details |
| GET | `/transformers/{id}/dashboard` | Public read | Latest readings, health and active-alert count |
| GET | `/transformers/{id}/readings?metric=...&page=0&size=25` | Public read | Paginated readings |
| GET/POST | `/transformers/{id}/diagnostics` | Unauthenticated demo write | List/add diagnostic results |
| GET/POST | `/transformers/{id}/protection-events` | Unauthenticated demo write | List/add protection events |
| GET | `/transformers/{id}/alerts?page=0&size=25` | Public read | Paginated alerts |
| GET | `/transformers/{id}/health-history?page=0&size=25` | Public read | Paginated assessment history |
| GET | `/transformers/{id}/events` | Public read | SSE dashboard events |

## Write access
There is no token or authentication check in this prototype. POST endpoints are available to any client that can reach the API. Do not expose this configuration to the public internet. Add proper authentication and role-based authorization before deployment.

## Validation
- Asset code: 2–60 characters, `[A-Za-z0-9._-]`, unique.
- Names and text fields have explicit maximum lengths.
- Numeric nameplate values are bounded.
- Diagnostic value/date/unit are required.
- Page index must be non-negative; size 1–100.
- Unknown transformer returns 404; validation errors return 400; duplicate/conflict returns 409.
- Unexpected errors return generic 500 response without stack traces or SQL details.

## Future API
`GET /transformers/{id}/parts` may return 3D part metadata and stable component keys. It is not implemented in v2.
