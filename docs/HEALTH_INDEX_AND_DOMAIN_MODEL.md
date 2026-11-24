# Health Index and domain model

## Scope and disclaimer

GridPulse uses a transparent project demonstration model. It is not an IEC, IEEE, CEA or CIGRE-prescribed Health Index and is not an operational safety decision. Weights and score anchors are illustrative assumptions pending asset-specific OEM GTP, baseline data and engineering approval.

## Category weights

| Category | Weight | Current treatment |
|---|---:|---|
| Thermal and loading | 26.32% | Requires both fresh top-oil and winding hot-spot evidence; loading is an operating-stress input only |
| Oil quality and DGA | 31.58% | Scores fresh moisture, BDV and acidity evidence; DGA concentrations are displayed for engineering review, not averaged as independent health scores |
| Insulation | 26.32% | Scores selected IR, PI and winding/bushing tan-delta evidence; furan remains evidence-only pending paper/oil context |
| Mechanical and electrical | 15.78% | Scores selected FRA deviation and fan vibration demonstration anchors; PD, OLTC and winding resistance remain evidence-only without test context/baselines |

The index is normalized over the weights of categories that have supported, sufficiently recent evidence:

`HI = sum(categoryScore × categoryWeight) / sum(weights for assessed categories)`

Coverage is the sum of assessed category weights. If coverage is below the configured minimum, the numeric index is unavailable. Missing or stale evidence is never assigned 100.

## Parameter score interpolation

For parameters with configured anchors, a monotonic linear interpolation maps a good anchor to 100, warning anchor to 60 and poor anchor to 0. The anchors are project assumptions and are documented in `HealthService.java` and `application.properties`; they are not universal operating limits.

Ambient temperature is model context, not a direct health penalty. Oil level produces a separate alert. Cooling equipment is interpreted only if applicable to the configured cooling type.

## Data quality and freshness

Key live thermal telemetry is stale after the configured freshness interval (default 30 seconds). Stale values are excluded from thermal scoring and trigger a warning. Offline diagnostic evidence has separate freshness windows: DGA 30 days, oil tests 180 days and other periodic tests 730 days by default. Configure these values for the actual maintenance regime.

## DGA

DGA is not treated as seven independent scalar health scores. The evidence endpoint groups gases by sample ID (and component/sampling point), exposes sample-level trends and selected ratios within each sample, and calculates rate-of-change only between comparable records. It does not label a fault type. Proper diagnosis needs coherent sample grouping, sample point, lab/method, detection limits, gas trends, asset baseline and engineering judgement consistent with IEC 60599 / IEEE C57.104 guidance.

## Critical conditions

Critical monitoring conditions can override the displayed status without changing the numeric index. Protection-event monitoring is not part of this application. Acknowledgement records operator review, not resolution. Active conditions remain active until their source condition is cleared by the assessment workflow.

## Thermal model

The simulator uses a simplified first-order dynamic thermal response. Load determines steady-state oil rise and hot-spot gradient using configurable rated rises, exponents, loss ratio and time constants. Thermal inertia prevents instantaneous temperature jumps. Cooling factor changes only when forced cooling is configured and its simulated fan state changes. Defaults are illustrative; use OEM heat-run and nameplate data for a real asset.
