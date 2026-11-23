# Research Note — Transformer Condition Monitoring

This note documents the domain categories reflected in the software. It is a design summary, not a substitute for reading the referenced standards or manufacturer documentation.

## Condition-monitoring categories

1. **Operating/thermal:** top-oil temperature, winding hot-spot, load, ambient temperature, cooling status, oil level.
2. **Oil/DGA:** H2, CH4, C2H6, C2H4, C2H2, CO, CO2, moisture, breakdown voltage, acidity, dielectric dissipation/tan delta.
3. **Insulation/winding:** insulation resistance, polarization index, winding resistance, bushing capacitance/tan delta, furan/DP.
4. **Mechanical/electrical:** partial discharge, frequency response analysis, bushing condition, OLTC condition.
5. **Protection/auxiliary:** Buchholz, pressure relief, sudden pressure, cooling fans/pumps and oil-level alarms.
6. **External inspection:** thermal imaging of tank, bushings, cable/jumper connections and bolted joints.

Some are continuous measurements; others are periodic offline tests or discrete protection events. The application models these separately.

## Standards / guidance to verify for each asset

- CEA technical specification / condition monitoring guidance for 66 kV and above (scope-specific; do not apply indiscriminately to distribution transformers).
- Applicable CEA regulations and utility procedures for the asset's voltage class and jurisdiction.
- Applicable BIS standards, including the relevant IS 2026 parts for power transformers and temperature-rise testing.
- IEC 60599 for interpretation of dissolved gases in mineral-oil-filled electrical equipment.
- IEEE C57.104 for interpretation of gases generated in oil-immersed transformers.
- IEEE C57.91 for loading guidance and thermal ageing context.
- Manufacturer nameplate, guaranteed technical particulars, factory/commissioning baseline and approved alarm settings.

The project must record which edition and clauses are applicable before operational limits are configured. CEA technical guidance is not automatically a universal government alarm setting.

## Real-world data examples

`samples/cea_veeranam_dga_example.csv` is a small transcription of a CEA-reported Indian transformer case with two bottom-oil DGA sample dates. It is useful for demonstrating gas trend charts and imported records. Verify the original report's exact page/table and use conditions before formal citation or redistribution.

Other research datasets identified during project research:
- UK power-station transformer DGA time-series records (Zenodo).
- Open DGA fault-diagnosis records with labels.
- Transformer oil-quality laboratory measurements.
- Public multivariate transformer fault-diagnosis research data.

Keep each dataset isolated by source and asset identity. Do not combine unrelated datasets into one fictitious transformer history. Retain the source URL, licence, sample date, sample point, unit and transformation steps.

## Health-index approach

Condition assessment should combine test evidence, trends, baseline comparisons, data freshness and engineering context. A weighted condition index can support prioritisation, but the weights and score bands must be validated against the target fleet and known maintenance/failure history. This project currently uses a transparent demonstration index and does not claim to implement an official CEA/IEC/IEEE/CIGRE formula.

## Selected reference asset
The CEA December 2020 manual's standard-rating table lists a 100 MVA, 220/33 kV, three-phase power transformer. GridPulse uses that one profile. A conservator-type mineral-oil visual model and ONAN/ONAF-style radiators are chosen for the conceptual 3D geometry; those construction/cooling details are not inferred from the rating table. See `REFERENCE_TRANSFORMER_SPEC.md` and `STANDARDS_ALIGNMENT.md`.
