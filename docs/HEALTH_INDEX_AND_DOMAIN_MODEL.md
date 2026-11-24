# GridPulse Health Index and scoring model

## Purpose and limitation

The Health Index is an explainable **project demonstration model** for a simulated 100 MVA, 220/33 kV transformer. It is not a formula prescribed by IEC, IEEE, CEA or CIGRE, is not a condition certification, and must not be used for operational decisions. All scoring anchors and category weights below are explicit project assumptions that require asset-specific engineering review before any real deployment.

## Category weights

| Category | Weight |
|---|---:|
| Thermal and loading | 25% |
| Oil and DGA | 30% |
| Insulation | 25% |
| Mechanical and electrical | 15% |
| Protection and auxiliary | 5% |
| **Total** | **100%** |

For each category, available parameter scores are averaged. For this version each available parameter record has equal influence within its category. Phase-specific bushing readings therefore contribute as individual records. Parameters without a scoring rule are displayed as evidence but are not scored.

## Parameter normalization

Each supported parameter is normalized to 0–100 using a piecewise linear function between three project anchors:

- Better-is-higher parameters: score 100 at or above the `good` anchor, 60 at the `warning` anchor, and 0 at or below the `poor` anchor.
- Lower-is-better parameters: score 100 at or below the `good` anchor, 60 at the `warning` anchor, and 0 at or above the `poor` anchor.
- Between anchors, linear interpolation is used. Values are clamped to [0,100].

The actual anchors are defined in `HealthService.java` and are demonstration screening values, not universal operating limits. DGA interpretation in particular requires trend, rate-of-rise, gas ratios, sampling context, and asset baseline; this model is not an IEC 60599 / IEEE C57.104 diagnostic implementation.

## Category and overall formulas

For category `c`, with `n` supported and available parameter scores:

`categoryScore(c) = sum(parameterScore_i) / n`

For categories with an assessed score, overall index is normalized to the weight coverage actually assessed:

`HI = sum(categoryScore_c * categoryWeight_c) / sum(categoryWeight_c for assessed categories)`

Coverage is `sum(assessed category weights)`, expressed as a fraction of 1. Missing or unscored data is excluded and reduces coverage; it is never assigned 100. The HI is unavailable below configured minimum coverage (`app.health.minimum-coverage`, default 0.60).

## Alarm separation

Alarms are generated independently from the numeric index using configured demo triggers and diagnostic screening anchors. An active critical protection event or configured critical hot-spot trigger overrides the displayed status to CRITICAL, but does not artificially change the numeric HI. Acknowledgement records operator review; it does not resolve the condition. Alerts resolve only when the associated condition is no longer present in the latest assessment.

## Sources and data provenance

Continuous telemetry and seeded diagnostic history are marked `SIMULATED`. User-entered offline diagnostic results are marked `IMPORTED`. The interface labels the simulator and includes source fields in history tables. Synthetic records are not physical measurements, laboratory certificates, or proof of relay operation.

## Current normalization anchors

The columns below are `(good, warning, poor)`. Lower-is-better parameters receive 100 at/below good, 60 at warning, and 0 at/above poor. Higher-is-better parameters use the inverse direction.

| Category | Parameter | Direction | Good | Warning | Poor | Unit |
|---|---|---|---:|---:|---:|---|
| Thermal | Winding hot-spot | Lower | 80 | 95 | 110 | °C |
| Thermal | Top-oil | Lower | 65 | 80 | 95 | °C |
| Thermal | Load | Lower | 80 | 100 | 120 | % |
| Thermal | Ambient | Lower | 35 | 45 | 55 | °C |
| Thermal | Fan/pump status | Binary | ON=100 | OFF=55 | — | — |
| Thermal | Oil level | Higher | 90 | 80 | 65 | % |
| Oil/DGA | Hydrogen | Lower | 50 | 150 | 300 | ppm |
| Oil/DGA | Methane | Lower | 30 | 130 | 250 | ppm |
| Oil/DGA | Ethane | Lower | 20 | 90 | 180 | ppm |
| Oil/DGA | Ethylene | Lower | 20 | 100 | 280 | ppm |
| Oil/DGA | Acetylene | Lower | 0.5 | 2 | 10 | ppm |
| Oil/DGA | Carbon monoxide | Lower | 350 | 600 | 1000 | ppm |
| Oil/DGA | Carbon dioxide | Lower | 2500 | 5000 | 10000 | ppm |
| Oil/DGA | Moisture | Lower | 10 | 20 | 35 | ppm |
| Oil/DGA | Breakdown voltage | Higher | 60 | 50 | 40 | kV |
| Oil/DGA | Acidity | Lower | 0.10 | 0.20 | 0.40 | mgKOH/g |
| Insulation | Insulation resistance | Higher | 5000 | 2000 | 500 | MΩ |
| Insulation | Polarization index | Higher | 2.0 | 1.5 | 1.0 | ratio |
| Insulation | Winding/bushing tan delta | Lower | 0.003 | 0.007 | 0.015 | ratio |
| Insulation | Furan 2-FAL | Lower | 0.1 | 1.0 | 5.0 | ppm |
| Mechanical | FRA deviation | Lower | 1 | 3 | 8 | % |
| Mechanical | PD apparent charge | Lower | 50 | 100 | 300 | pC |
| Mechanical | OLTC contact resistance | Lower | 80 | 100 | 150 | µΩ |
| Mechanical | OLTC operating time | Lower | 60 | 80 | 120 | ms |
| Mechanical | Radiator temperature difference | Lower | 10 | 20 | 30 | °C |
| Mechanical | Fan vibration | Lower | 2 | 4 | 8 | mm/s |

These anchors are only demo values. Some parameters intentionally have no score (for example bushing capacitance and winding resistance) because an absolute value without the transformer-specific baseline, phase comparison, test method and temperature correction is not enough to create a defensible score. They remain visible in the diagnostic history.
