# Numbered 3D Inspection Map

The 3D view is a procedural interactive model of the selected CEA-listed 100 MVA, 220/33 kV three-phase reference transformer. The numbered circles are clickable inspection-area placeholders.

| Number | Physical area | Health / data group | Linked evidence |
|---:|---|---|---|
| 1 | Main tank / active part | Thermal and loading; oil and DGA | Top oil, winding hot spot, load, ambient, DGA/oil tests |
| 2 | Conservator and oil system | Oil and DGA | Moisture, oil level/leakage records when added, oil tests and DGA |
| 3 | HV bushings A/B/C | Insulation | Component-linked, phase-specific capacitance, tan delta/power factor, leakage and PD records (A/B/C) when entered |
| 4 | LV bushings a/b/c | Insulation | Component-linked, phase-specific bushing test records (a/b/c) when entered |
| 5 | Radiator banks | Thermal and loading | Temperature, ambient and cooling evidence |
| 6 | Cooling fans and motors | Protection and auxiliary; thermal | Simulated fan state and recorded cooling alarms |
| 7 | OLTC compartment / drive | Mechanical and electrical | Simulated tap position, demonstration operation count and motor-current pulse; imported timing, contact resistance and compartment DGA tests |
| 8 | Marshalling / control cabinet | Protection and auxiliary | Buchholz, PRD, temperature/oil-level alarms and auxiliary events |

The API now stores optional component keys and phase labels on diagnostic records. A diagnostic appears on its linked 3D inspection area; legacy records without a component key remain asset-level and may appear in matching group views. These links identify the record subject, not a verified physical sensor location.

Future extension: add `transformer_part` and `component_measurement_mapping` tables, stable part IDs, phase IDs, GLB node names and sensor/test-to-part links. Preserve the current numbered IDs as stable UI identifiers.
