# GridPulse deployment: Aiven + Render + Vercel

## Deployment order
1. Create Aiven for MySQL and create a database.
2. Deploy `backend/` to Render as a Docker web service.
3. Deploy `frontend/` to Vercel as a Vite project.
4. Set the final Vercel origin in Render's `CORS_ALLOWED_ORIGINS`.

## Render backend settings
- Root directory: `backend`
- Runtime: Docker
- Dockerfile path: `./Dockerfile`
- Health check path: `/actuator/health`

Required environment variables:
- `DB_URL=jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/<DATABASE>?sslMode=REQUIRED&serverTimezone=UTC`
- `DB_USERNAME=<AIVEN_DATABASE_USERNAME>`
- `DB_PASSWORD=<AIVEN_DATABASE_PASSWORD>`
- `CORS_ALLOWED_ORIGINS=https://<YOUR_VERCEL_DOMAIN>`
- `SIMULATION_ENABLED=true`
- `SIMULATION_INTERVAL_MS=5000`
- `SIMULATION_SEED=false`

## Vercel frontend settings
- Root directory: `frontend`
- Framework preset: Vite
- Build command: `npm run build`
- Output directory: `dist`
- Environment variable: `VITE_API_BASE_URL=https://<YOUR_RENDER_SERVICE>.onrender.com/api`

Redeploy the frontend after changing `VITE_API_BASE_URL`; Vite embeds it at build time.

## Database notes
- Flyway is enabled and migrations are in `backend/src/main/resources/db/migration`.
- Hibernate is configured with `ddl-auto=validate`; do not change it to `update`.
- Back up any existing database before applying migrations.
- Use Aiven's connection details exactly as shown in its console. TLS is enabled with `sslMode=REQUIRED` in the JDBC URL. Prefer certificate/hostname verification once the Java trust configuration is set up for Aiven's CA.
- Never commit database credentials or production `.env` files.

## Operational notes
- The application currently has no authentication/authorization layer. Do not expose it publicly with sensitive operational data until access control and write endpoints have been reviewed.
- Simulated telemetry is written every 5 seconds while the Render service is running. If the selected Render plan sleeps or restarts, simulation ticks pause during that period. Use an always-on service for continuous simulation.
- Existing API endpoints are retained; this setup does not invent or rename APIs.
