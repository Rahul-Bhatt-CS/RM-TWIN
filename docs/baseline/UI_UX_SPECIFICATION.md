# UI/UX Specification — Transformer Digital Twin

**Document type:** Product design specification  
**Project:** Transformer Digital Twin  
**Baseline:** Approved PRD and technical architecture proposal  
**Status:** Design specification only — no implementation  
**Version:** 1.0

---

## 1. Purpose and scope

This document defines the user experience for the Transformer Digital Twin application. It translates the PRD's three required outcomes into a small, coherent interface:

1. A live view of transformer health using simulated sensor data.
2. Alerts when asset health degrades.
3. Historical data that users can review.

The chosen asset is a **transformer**. The interface must make it easy to understand its current condition, notice degradation, and inspect what happened over time.

### 1.1 Source requirements versus design decisions

The assignment/PRD does not prescribe screens, user roles, authentication, sensor selection, units, health formula, alert thresholds, alert workflow, refresh interval, chart types, or visual style. This specification therefore separates:

- **PRD requirement:** necessary to satisfy the stated assignment.
- **UX design decision:** a proposed interface behavior to make the requirement usable.
- **TBD:** a product or domain decision that must be confirmed before implementation.

Do not present UX decisions or TBD values as requirements from Rugged Monitoring.

### 1.2 In scope

- Transformer overview and live health.
- Current simulated sensor readings.
- Degradation alert visibility.
- Historical sensor and health information.
- Navigation between overview, alerts, and history.
- Loading, empty, error, and stale-data states.
- Responsive layouts and accessibility basics.

### 1.3 Out of scope unless separately approved

- Physical sensor or gateway connectivity.
- SCADA integrations.
- User registration, login, permissions, or role administration.
- Multiple asset types or a fleet-wide asset inventory.
- Maintenance work orders, technician assignment, or service scheduling.
- Predictive remaining-life calculations or machine-learning predictions.
- 3D transformer rendering.
- Configuration of engineering thresholds from the UI.
- Reports/export, notifications by email/SMS, and audit logs.
- Multi-tenant administration.

---

## 2. UX principles

1. **Current condition first.** The first screen answers: “What is the transformer's condition now?”
2. **Show evidence.** Health status must be accompanied by the readings and/or recent trend that explain it. Do not show a score without a defined calculation.
3. **Make degradation noticeable.** Active degradation alerts should be visually prominent without relying on color alone.
4. **Keep time context explicit.** Every reading and chart must show its timestamp and time range.
5. **Be honest about simulation.** Clearly label the data as simulated; never imply that it is coming from a physical transformer sensor.
6. **Graceful failure.** If live updates stop, preserve the last known values but mark them stale and show when they were last updated.
7. **Small, finished scope.** Prefer a clear overview, alert list, and history view over a large set of unfinished enterprise features.

---

## 3. Information architecture

### 3.1 Primary navigation

Use a persistent application shell with:

- **Overview** — current transformer health and live readings.
- **Alerts** — degradation alerts.
- **History** — historical readings and health trend.

A compact header contains:

- Product name/wordmark (working product name may be selected separately).
- Current transformer identity and a selector to switch between configured transformers.
- Simulation/data-source indicator: `SIMULATED`.
- Last-updated timestamp or live connection state.

The implemented demo seeds multiple transformers. Provide a transformer selector in the application header. The selected transformer scopes Overview, Alerts, History and the live event connection. Keep one selected transformer visible at a time; a fleet comparison dashboard is not included in this increment.

### 3.2 Navigation map

```text
Application
├── Overview
│   ├── Current health summary
│   ├── Sensor readings
│   ├── Recent trend
│   └── Recent alerts preview
├── Alerts
│   ├── Alert list
│   └── Alert detail (inline row expansion or side panel)
└── History
    ├── Time-range controls
    ├── Sensor history chart/table
    └── Health history chart/table
```

### 3.3 Navigation behavior

- Overview is the default route when the application opens.
- Selecting a primary navigation item changes the main content while retaining the application header.
- Selecting an alert from the overview's recent-alert preview opens Alerts and focuses the selected alert.
- Selecting an alert in the Alerts list opens its detail panel or expanded row; it does not navigate to an unrelated workflow.
- Selecting a time range in History updates the displayed history in place.
- Browser back/forward should work for primary routes if the chosen frontend routing approach supports URL-based navigation.
- Preserve a selected time range when moving between sensor and health history within the History screen, where practical.

---

## 4. Roles and access behavior

### 4.1 PRD position

The PRD does not define user roles, login, authentication, or authorization. The initial UI therefore uses a **single, role-neutral operator experience**.

### 4.2 Role behavior matrix

| Capability | Initial behavior | Rationale |
|---|---|---|
| View transformer overview | Available to the application user | Required live health view |
| View simulated readings | Available | Required source of live health |
| View alerts | Available | Required degradation alerts |
| View history | Available | Required historical review |
| Acknowledge/resolve alerts | Not included by default | Alert lifecycle is not defined in the PRD |
| Edit thresholds or health formula | Not included | Engineering rules are TBD |
| Manage users or roles | Not included | Authentication and RBAC are not required |
| Create maintenance work | Not included | Outside selected project scope |

Do not display role names, permission-denied screens, user-management navigation, or role-specific buttons in the first version.

If roles are added later, define them as a separate approved requirement and update this matrix before designing role-specific UI.

---

## 5. Global layout and visual behavior

### 5.1 Desktop layout

Recommended structure:

- Header across the top.
- Primary navigation directly below the header or in a narrow left sidebar.
- Main content in a centered, responsive content area.
- Cards arranged in a clear grid with consistent spacing.
- Charts and tables use the available width and avoid horizontal overflow.

### 5.2 Tablet and mobile layout

- Collapse the primary navigation into a menu or compact tab bar.
- Stack dashboard cards vertically.
- Keep the health status and latest update near the top.
- Allow tables to become horizontally scrollable or switch to compact reading rows.
- Keep filter controls usable without requiring precision pointer interaction.
- Charts must resize to the viewport; chart labels may simplify on narrow screens.

### 5.3 Visual hierarchy

Use a restrained industrial monitoring visual language:

- Neutral background and surfaces.
- High-contrast text.
- One primary accent color for navigation and interactive controls.
- Status colors for condition states only, accompanied by text and/or icons.
- Consistent typography and spacing.
- Avoid decorative gauges or animations that obscure actual readings.

Exact brand colors, typography, logo, and design tokens are **TBD**. Do not imply that the design is an official Rugged Monitoring interface.

### 5.4 Status presentation

Candidate status labels from the architecture proposal are `Normal`, `Warning`, and `Critical`. They remain provisional until the health model and thresholds are approved.

For every status:
- Show the text label.
- Use a distinct visual treatment.
- Do not communicate status through color alone.
- Provide the reason or supporting reading where available.
- If no valid assessment exists, show `Unavailable` rather than guessing a status.

---

## 6. Screen specifications

## 6.1 Screen S-01 — Transformer Overview

**Purpose:** Give the user an at-a-glance view of current transformer condition and simulated sensor activity.

**Entry point:** Default screen after application load; Overview navigation item.

### Layout regions

1. **Page heading**
   - Title: `Transformer Overview`
   - Transformer name/identifier.
   - Data-source badge: `SIMULATED`.
   - Last-updated timestamp and connection/staleness indicator.

2. **Health summary**
   - Current health status.
   - Health score only if a score and calculation have been approved.
   - Assessment timestamp.
   - Short explanation of the status, based on the actual rule/assessment result.
   - If no score exists, omit the score area rather than inventing one.

3. **Sensor reading cards**
   - One card for each configured simulated sensor.
   - Each card displays sensor name, latest value, unit, reading timestamp, and data state.
   - Initial candidate sensor categories from the architecture proposal: oil temperature, winding hot-spot temperature, and load.
   - These are candidate choices, not fixed PRD requirements. Final sensor set and units are TBD.
   - DGA or partial-discharge readings may be shown only if selected and supported by the implemented simulator.

4. **Recent trend**
   - Compact time-series chart for a selected key reading or health history.
   - Clearly label the metric, unit, time range, and sample timestamps.
   - If there is no approved health score, do not create a synthetic health-score line.

5. **Recent alerts**
   - Show the newest few alerts, if any.
   - Each item shows severity/status (if defined), alert title/reason, and time.
   - `View all alerts` navigates to S-02.
   - If there are no alerts, show a calm empty state rather than a warning.

### Primary interactions

- Select a sensor card to open History with that sensor preselected, if sensor-specific history is supported.
- Select a recent alert to open its detail in Alerts.
- Select `View history` to open S-03.
- The live display updates when a new simulated reading/assessment arrives.
- Do not make dashboard cards editable in the initial scope.

### Loading state

- Show the page shell and skeleton placeholders for health, sensor cards, trend, and alerts.
- Do not show zero values as placeholders.
- Label the content as loading until the first successful response is received.

### Empty state

If the transformer exists but no readings have been generated:
- Health area: `Waiting for the first simulated reading`.
- Sensor area: `No readings available yet`.
- Trend: `History will appear after readings are generated`.
- Alerts: `No alerts yet`.
- Do not infer `Normal` from missing data.

### Error state

- If the overview request fails, show an inline page-level error with a retry action.
- If only one section fails, keep other successful sections visible and show an error in the affected section.
- If live updates fail after initial load, keep last known values and show `Live updates interrupted` with the last successful update time.
- If data is older than the approved freshness window, show `Stale data`; the freshness window is TBD.

---

## 6.2 Screen S-02 — Alerts

**Purpose:** Let the user review degradation alerts generated by the health/alert processing flow.

**Entry point:** Alerts navigation item or alert preview on Overview.

### Page header

- Title: `Alerts`.
- Optional result count, if returned by the API.
- No create-alert action; alerts are generated by the system based on degradation logic.

### Alert list/table

Recommended columns:

| Column | Display |
|---|---|
| Time | Alert creation/trigger timestamp |
| Alert | Short title or triggering condition |
| Sensor/metric | Related sensor or metric, when known |
| Observed value | Value and unit, when applicable |
| Severity | Only if severity is defined |
| Status | Only if alert lifecycle is defined |

The table must not show empty or fabricated severity/status values. If severity is not implemented, omit the column. If lifecycle is not implemented, omit the status column.

### Filters

Initial filter controls:

- Time range: preset range or start/end date-time.
- Metric/sensor: only if the API returns a sensor association.
- Severity: only if severity levels are approved and stored.
- Status: only if an alert lifecycle is approved and stored.

For the first minimal version, time range is sufficient. Do not show disabled or non-functional filters.

### Alert detail

Selecting an alert opens an inline expansion or side panel containing:
- Alert title/reason.
- Trigger timestamp.
- Related metric/sensor, if available.
- Observed value and unit, if available.
- Threshold/rule reference, only if the rule is defined and safe to expose.
- Related time context or link to History.

Do not invent recommended actions, diagnosis, or maintenance instructions.

### Forms and actions

No form is required in the initial scope. No acknowledge, resolve, assign, comment, or delete actions should be shown unless those capabilities are separately approved.

### Loading state

- Show skeleton rows.
- Keep filters visible but disabled until filter metadata is available, if applicable.

### Empty state

- No alerts in selected range: `No degradation alerts found for this period`.
- Provide a clear `Clear filters` action when filters are active.
- Distinguish “no alerts” from “alerts could not be loaded.”

### Error state

- Show an inline alert-list error and retry.
- Preserve filter selections after retry.
- If alert detail fails, keep the list visible and show an error in the detail panel.

---

## 6.3 Screen S-03 — History

**Purpose:** Allow the user to look back through historical simulated readings and health assessments.

**Entry point:** History navigation item, Overview's `View history`, or a sensor card.

### Page header

- Title: `History`.
- Transformer identifier.
- Data-source label: `SIMULATED`.

### Controls

1. **Metric selector**
   - Options are limited to configured and available metrics.
   - Candidate examples: oil temperature, winding hot-spot temperature, load.
   - Add DGA or partial discharge only if present in the simulator and data model.
   - Default selection: a product decision; recommend the most useful configured metric, but confirm before implementation.

2. **Time range**
   - Provide a small set of practical presets (for example, last hour, last 24 hours, last 7 days) only if the stored history supports them.
   - Provide custom start/end date-time only if the API supports arbitrary ranges.
   - Exact presets and maximum range are TBD.

3. **View mode**
   - Chart view for trend interpretation.
   - Table view for exact values and timestamps.
   - Both modes represent the same selected metric and time range.

### Chart

Display:
- Metric name and unit.
- Time on the horizontal axis.
- Reading value on the vertical axis.
- Tooltip with exact timestamp and value.
- Clear indication of missing intervals, if known.
- Threshold lines only when thresholds are approved and returned by the system.
- Health history may be shown as a separate series/panel only when a defined health assessment is stored.

Avoid smoothing or interpolating values in a way that implies measurements that do not exist.

### History table

Recommended columns:

| Column | Display |
|---|---|
| Timestamp | Time the reading was recorded |
| Metric | Sensor/metric name |
| Value | Numeric reading |
| Unit | Unit for that metric |
| Source | `SIMULATED` |
| Assessment/status | Only if a linked assessment exists and the relationship is defined |

Table behavior:
- Sort newest first by default.
- Use pagination or bounded result windows if the dataset is large.
- Display the active time range above the table.
- Do not silently truncate data without indicating that more results are available.

### Filters

Required interaction:
- Metric selection.
- Time range.

Optional only if supported by data/API:
- Sensor identifier.
- Assessment status.
- Alert-associated readings.

### Empty state

- No readings for the selected metric/range: `No historical readings found for this selection`.
- Offer `Clear filters` or a wider time range if the current selection is restrictive.
- If no readings have ever been generated, explain that history appears after simulated readings are generated.

### Error state

- Show an inline chart/table error with retry.
- Keep the selected metric and time range.
- If chart rendering fails but table data is available, show the table rather than a blank page.
- If history is unavailable, do not substitute current readings as historical data.

---

## 7. Forms and input specifications

The initial product is primarily a monitoring interface. It does not require data-entry forms.

| Form/control | Screen | Required? | Behavior |
|---|---|---:|---|
| Metric selector | History | Yes | Selects an available metric and refreshes chart/table |
| Time-range preset | History, Alerts | Yes | Filters records to the selected period |
| Custom date-time range | History, Alerts | Conditional | Show only if arbitrary date-time filtering is supported |
| Chart/table toggle | History | Recommended | Changes presentation without changing query scope |
| Retry action | Any failed section | Yes | Repeats the failed request without discarding current context |

### Input validation

For custom date-time ranges, if implemented:
- Start and end must both be valid date-time values.
- Start must be earlier than end.
- Do not allow future end times unless future simulation data is intentionally supported.
- Show validation inline beside the relevant field.
- Preserve the user's entered values after validation errors.

Exact timezone display and storage behavior are TBD. The UI should use one consistent timezone and label it explicitly.

### No forms in initial scope

Do not add:
- Transformer creation/edit form.
- Sensor configuration form.
- Threshold configuration form.
- Alert creation form.
- User profile or account form.
- Login/registration forms.
- Maintenance/work-order forms.

These are not required by the approved PRD.

---

## 8. Tables, sorting, filtering, and data presentation

### 8.1 General table behavior

- Use clear column labels and units.
- Keep timestamps consistently formatted.
- Default historical records to newest first.
- Use a visible empty state, not an empty grid.
- Show loading skeletons while data is fetched.
- Keep filters visible when results are empty or fail.
- Provide pagination or a bounded range for large result sets.
- On narrow screens, allow horizontal scrolling or use a compact row layout.
- Do not show columns for fields that are not actually available.

### 8.2 Filter behavior

- Filters apply only after valid selection.
- A visible indication should show when filters are active.
- `Clear filters` resets to the screen's documented default range and metric.
- Preserve filter values when a request fails.
- Avoid filters that cannot be honored by the API.
- A selector is sufficient for the three seeded demo assets; a separate fleet-wide comparison or asset search is not included in this increment.

### 8.3 Numeric values and units

- Always display a unit with a sensor reading.
- Use consistent decimal precision per metric.
- Do not compare readings from different units on the same axis without normalization.
- If a value or unit is missing/invalid, show `Unavailable` and do not plot it as zero.
- Sensor names, supported units, precision, and valid ranges are TBD.

---

## 9. Live update and freshness behavior

The architecture proposes server-sent events (SSE) for live dashboard updates, with polling as a fallback. This is an architecture proposal, not a PRD requirement.

UI behavior:
- Show a compact live indicator when updates are being received.
- Show `Last updated: <timestamp>` using the latest successfully received reading/assessment.
- On reconnect, indicate `Reconnecting` without clearing the current view.
- If updates stop, retain last known data and mark it stale after an approved freshness interval.
- When a new reading arrives, update only affected values and charts; avoid a full-page flash.
- Do not animate numeric changes in a way that makes values difficult to read.
- If the connection is unavailable, provide a manual retry/reconnect action if supported.

TBD:
- Update cadence.
- Staleness threshold.
- Whether SSE or polling is used in the final implementation.
- Whether live status is driven by reading arrival, assessment arrival, or both.

---

## 10. Dashboard and chart requirements

### 10.1 Overview dashboard composition

Recommended order:

1. Transformer identity and data freshness.
2. Current health status.
3. Sensor reading cards.
4. Recent trend.
5. Recent alerts.

This order prioritizes current condition, then evidence, then context.

### 10.2 Chart behavior

- Every chart has a title, metric, unit, time range, and accessible text equivalent.
- Tooltips expose exact values and timestamps.
- Use consistent axes and units.
- Missing data is represented as a gap, not zero.
- Do not add trend predictions, forecast bands, or remaining-life estimates.
- Threshold/reference lines appear only after domain rules are approved.
- On mobile, preserve readability and allow horizontal time navigation only if necessary.

### 10.3 Health visualization

A simple status label is sufficient for the initial release. A gauge or numeric score is optional and should be used only if:
- A health formula is defined.
- The score range and interpretation are documented.
- The score can be traced to actual inputs.
- The UI explains what the score means.

Until then, prefer `Normal`, `Warning`, `Critical`, or `Unavailable` only if the backend health model supports those states. Do not manufacture a percentage score.

---

## 11. System-wide loading, empty, error, and stale states

| State | Required UI behavior |
|---|---|
| Initial loading | Render shell and skeletons; do not show fake values |
| Section loading | Keep other loaded sections visible; show local skeleton |
| No data yet | Explain that simulated readings have not yet been generated |
| No matching results | Explain that no records match the selected filters; offer clear filters |
| Request error | Show a concise message and retry action |
| Partial failure | Preserve successful sections and isolate the failed section |
| Live connection interrupted | Retain last known values and show connection interruption |
| Stale data | Mark values stale and show last successful timestamp |
| Invalid/missing value | Show `Unavailable`; never coerce to zero |
| Unknown health status | Show `Unavailable` or `Not assessed`; never assume normal |

Error messages should be understandable to a monitoring user and should not expose stack traces, SQL details, internal hostnames, or implementation exceptions.

---

## 12. Accessibility and usability requirements

- Use semantic page headings and meaningful labels.
- All navigation and controls must be keyboard accessible.
- Provide visible focus indicators.
- Status must not be communicated by color alone.
- Ensure text and control contrast is sufficient.
- Tables must have clear headers.
- Charts must have a text summary or accessible data table.
- Tooltips must not be the only way to access important values.
- Form validation, if custom date-time controls are included, must be announced and associated with the relevant input.
- Use plain language: distinguish `No data`, `Unavailable`, `Stale`, and `Normal`.

---

## 13. Responsive behavior

| Area | Desktop | Tablet | Mobile |
|---|---|---|---|
| Navigation | Persistent top nav or sidebar | Compact nav | Menu or bottom tab navigation |
| Health summary | Wide summary card | Full-width card | First card, full width |
| Sensor cards | Multi-column grid | Two columns where space allows | Single column |
| Trend chart | Full-width or half-width panel | Full-width | Full-width, simplified tick labels |
| Alerts table | Full table | Compact table | Scrollable table or stacked rows |
| History filters | Inline toolbar | Wrapping toolbar | Stacked controls |
| History chart/table | Side-by-side only if space allows | Stacked | Stacked |

No interaction may depend exclusively on hover.

---

## 14. Screen-to-requirement traceability

| PRD requirement | UI screen/component | Acceptance outcome |
|---|---|---|
| Live view of asset health from simulated sensor data | S-01 Overview | User can see current health state and latest simulated readings with timestamps |
| Alerts when health degrades | S-01 Recent Alerts and S-02 Alerts | Generated degradation alerts are visible and explain the triggering condition where data is available |
| History users can look back through | S-03 History | User can select a metric and time range and inspect historical readings |
| Transformer is the selected asset | Global header and all screens | The interface consistently identifies the monitored transformer |
| Simulated data is the source | Global data-source label and reading source | User is never led to believe physical sensor data is connected |

---

## 15. Acceptance criteria

The UI/UX design is ready for implementation when:

1. The user can open the application and reach the Transformer Overview.
2. The overview clearly identifies the transformer and marks the data as simulated.
3. The overview displays the latest available readings with units and timestamps.
4. The health area shows a valid backend-provided assessment or a clear unavailable state.
5. Degradation alerts are visible on the overview and in the Alerts screen.
6. The user can open an alert and inspect the information actually stored for it.
7. The user can open History, choose an available metric, and select a supported time range.
8. Historical readings are shown in a chart and/or table with timestamps and units.
9. Loading, no-data, request-error, partial-failure, stale-data, and interrupted-live-update states are designed.
10. The UI does not expose unsupported role management, maintenance workflows, physical sensor integrations, or unapproved health calculations.
11. Core screens remain usable on desktop, tablet, and mobile.
12. The UI never presents missing data as zero or missing health assessment as normal.

---

## 16. Open decisions before implementation

These decisions are not answered by the PRD and must be recorded as explicit assumptions if the project proceeds without further research:

| Decision | Why it matters |
|---|---|
| Transformer display name and identifier | Header and asset identity |
| Final sensor set | Determines cards, metric selector, and history series |
| Units and display precision | Prevents misleading measurements |
| Health status model and formula | Determines health summary and chart |
| Degradation rules and thresholds | Determines when alerts appear and their explanation |
| Alert severity and lifecycle | Determines alert columns and actions |
| Simulation cadence | Determines live update expectations |
| Staleness interval | Determines when data is marked stale |
| History retention and maximum query range | Determines filters and pagination |
| Timezone policy | Ensures timestamps are interpreted correctly |
| Whether alert detail is inline or a side panel | Layout implementation detail |
| Whether chart and table both ship in first version | Scope/time trade-off |
| SSE versus polling | Affects live connection indicators |
| Brand/design tokens | Visual identity |
| Whether a numeric health score is justified | Avoids unsupported precision |

---

## 17. Explicit design recommendation

For a focused first release, implement only three primary screens:

- **Overview:** current health, latest simulated sensor readings, recent trend, recent alerts.
- **Alerts:** list and inspect degradation alerts.
- **History:** filter by available metric and time range; inspect chart/table.

Use one role-neutral experience and no authentication. Do not add CRUD screens, maintenance workflows, user roles, or advanced analytics unless the PRD is deliberately expanded. This keeps the interface aligned with the required end-to-end flow and the assignment's stated preference for a clear, finished result over a broad unfinished system.
