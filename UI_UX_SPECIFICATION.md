# UI/UX Specification — v2

## Main workspace
1. Fleet overview with Add Transformer.
2. Transformer cards and selector.
3. Selected asset dashboard.
4. Tabs for Overview, Readings, Diagnostics, Alerts, Health and Protection.

## Add transformer
Required: name and unique asset code. Optional: manufacturer, model, serial number, voltage class, MVA rating, cooling, insulation, commissioning date, location and description. No login or administrator token is required in this demo build.

## Overview
- Transformer identity, rating and last updated.
- Health index or unavailable state, condition status and coverage.
- Component-group scores and explanations.
- Latest sensor readings and source labels.
- Links to paginated detailed tables.

## Tables
Readings, diagnostic tests, alerts, health history and protection events are paginated by the server. Default page size 25; maximum 100. Tables show units, timestamps and provenance. No unbounded "load everything" behavior.

## Diagnostic entry
Allows recording a test type, parameter, value, unit, test date, laboratory, reference/method note and comments. It does not claim that a result is real unless the user enters it with its provenance.

## Protection events
Allows recording a reported device/event status. The application is read/record-only; it cannot configure or operate protection equipment.

## States and accessibility
- Distinguish unavailable, missing, stale, normal and warning states.
- Do not use color alone.
- Keep keyboard focus visible, inputs labelled, tables horizontally scrollable on small screens.
- Errors are inline and retryable; no-data states do not show zero values.

## Future 3D interaction
Not implemented. Future component selection will map glTF mesh IDs to stable backend component keys and open the same diagnostics/readings/alerts panel for that component. Do not render unselectable decorative 3D art.

## Interactive 3D transformer explorer

The Overview screen includes an interactive conceptual transformer model. Users can drag to rotate, zoom with the mouse wheel or controls, reset the view, hover to highlight a component, and click a component to inspect linked condition information. An adjacent component list provides an alternative selection method.

The inspector displays the selected component's mapped health group, score/state where available, and relevant readings or diagnostic/protection records. If component-level telemetry is unavailable, the UI explicitly says so rather than fabricating a component-specific measurement.

The current geometry is illustrative and is not a manufacturer-specific CAD drawing. Stable component IDs and health-group keys are retained so a future GLB/glTF model can replace the renderer without changing the backend data model.
