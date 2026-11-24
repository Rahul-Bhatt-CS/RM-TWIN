# Synthetic data catalogue

## Purpose

The backend seeds synthetic historical diagnostic examples for every simulation-enabled transformer and continuously generates telemetry at the configured simulation interval (default 5 seconds). This is to exercise the UI, charts, component inspection, pagination, alerts and health-assessment workflow.

**These values are synthetic demonstration data. They are not physical measurements, not copied from a specific transformer, not calibrated to a manufacturer's guaranteed technical particulars, and must not be used for operation, maintenance decisions or safety certification.**

## Continuous telemetry generated every simulation tick

- Top-oil temperature (°C)
- Winding hot-spot temperature (°C)
- Load (% of entered nameplate rating)
- Ambient temperature (°C)
- Apparent power (MVA)
- Balanced three-phase HV and LV line-current estimates (A)
- Cooling fan and pump state
- Oil-level indication (%)
- Radiator outlet temperature (°C)
- OLTC tap position, demonstration operation counter and motor current (A)

## Synthetic historical diagnostic records seeded once per transformer

| Component | Synthetic records |
|---|---|
| Main tank / active part | H2, CH4, C2H6, C2H4, C2H2, CO, CO2, moisture, BDV, acidity, insulation resistance, PI, winding tan delta, furan, FRA deviation, winding resistance, apparent charge |
| Conservator | Moisture sample and oil-level gauge check |
| HV bushings | Phase A/B/C tan delta and capacitance |
| LV bushings | Phase a/b/c tan delta and capacitance |
| Radiators | Radiator temperature-difference test record |
| Cooling fans | Fan vibration example and continuous fan state |
| OLTC | Contact resistance and operating-time sample records, plus continuous tap/motor telemetry |
| Control cabinet | Sensor mapping metadata only; no protection-event feature is implemented |

All diagnostic rows use `source=SIMULATED`, lab name `GridPulse synthetic data generator`, and notes explaining that they are not actual measured results or standard pass/fail limits. The seed is idempotent per transformer: the presence of a simulated DGA seed record prevents repeated historical inserts on each tick/restart.

## Demonstration health scores

The current health assessment uses illustrative scenario scores when a corresponding synthetic data group exists:

| Group | Weight after normalization | Demonstration treatment |
|---|---:|---|
| Thermal condition | 26.32% | Requires both fresh top-oil and winding hot-spot readings |
| Oil quality and DGA | 31.58% | Selected oil tests can be screened; DGA remains sample-level evidence only |
| Insulation | 26.32% | Selected supported test parameters only |
| Mechanical and electrical | 15.78% | Selected FRA / fan-vibration demonstration anchors only |

These are UI demonstration scores only. The oil/DGA score is **not** derived from IEC 60599 or IEEE C57.104 interpretation. The insulation and mechanical scores are **not** pass/fail results under IEC/IS standards. Protection and auxiliary systems are excluded from the Health Index; no relay or trip-circuit health is inferred.

Imported diagnostic records without an approved, validated interpretation method remain `RECORDED_NOT_INTERPRETED` and do not contribute to the composite score. Missing data is never silently treated as healthy.

## Seed timing

- Existing simulation-enabled assets are seeded on application startup.
- Assets added later are seeded on their first simulation tick.
- Continuous telemetry is generated at `SIMULATION_INTERVAL_MS` (default 5000 ms).
- Historical diagnostic rows are not re-created every tick.
