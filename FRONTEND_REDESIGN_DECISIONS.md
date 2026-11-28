# GridPulse Frontend Redesign — Design Decisions

## Product direction
GridPulse is presented as a transformer reliability operations console, not a generic admin template. The primary user task is to identify the selected asset, understand its current condition, investigate evidence, respond to alerts, and record maintenance.

## Decisions and rationale

1. **Persistent full-height workspace rail**
   - The six existing work areas remain visible and predictable.
   - A fixed rail prevents navigation from moving as users scroll long tables and diagnostic records.
   - The rail collapses into a compact two-column navigation on narrow screens.

2. **Asset-first content hierarchy**
   - Fleet selection and selected-transformer identity remain above the active workspace.
   - This reduces the risk of reading a measurement or acknowledging an alert against the wrong transformer.

3. **Overview as an operational summary**
   - The health assessment, model view, latest measurements, component breakdown and shortcuts remain the first screen.
   - Dense historical records stay in their dedicated workspaces.

4. **Dedicated task workspaces**
   - Sensor history, hourly health history, diagnostics, alerts and maintenance each retain their own screen.
   - Existing backend pagination is retained for pageable records; the design improves hierarchy and readability without fabricating API routes.

5. **Notifications as an overlay**
   - The notification control remains fixed at the viewport corner.
   - The drawer overlays content and does not reserve a permanent column, preserving the available working width.

6. **Progressive disclosure**
   - Diagnostic context and alert notes remain expandable.
   - Users see the most useful summary first and open detailed evidence when needed.

7. **Visual language**
   - Deep teal is reserved for navigation and high-level identity; a restrained green accent denotes condition/positive actions.
   - Neutral surfaces, consistent spacing, readable type sizes and subtle borders prioritize long-session operational use over decorative effects.

## Scope and compatibility
- No backend endpoints, DTOs, database tables, health-index calculations, alert rules or simulation behaviour were changed for this visual redesign.
- Existing pageable API calls and page controls are preserved.
- The 3D transformer model styles are retained.
- Browser-level visual testing and full production build must still be run in an environment with frontend dependencies installed.
