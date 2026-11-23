# Numbered 3D Inspection Map

The 3D view is a procedural interactive model of the selected CEA-listed 100 MVA, 220/33 kV three-phase reference transformer. The numbered circles are clickable inspection-area placeholders.

| Number | Physical area | Health / data group | Linked evidence |
|---:|---|---|---|
| 1 | Main tank / active part | Thermal and loading; oil and DGA | Top oil, winding hot spot, load, ambient, DGA/oil tests |
| 2 | Conservator and oil system | Oil and DGA | Moisture, oil level/leakage records when added, oil tests and DGA |
| 3 | HV bushings A/B/C | Insulation | Phase-specific capacitance, tan delta/power factor, leakage and PD records when available |
| 4 | LV bushings a/b/c | Insulation | Phase-specific bushing test records when available |
| 5 | Radiator banks | Thermal and loading | Temperature, ambient and cooling evidence |
| 6 | Cooling fans and motors | Protection and auxiliary; thermal | Simulated fan state and recorded cooling alarms |
| 7 | OLTC compartment / drive | Mechanical and electrical | OLTC operation count, motor current, timing, contact resistance and compartment DGA when added |
| 8 | Marshalling / control cabinet | Protection and auxiliary | Buchholz, PRD, temperature/oil-level alarms and auxiliary events |

The current API stores many diagnostics at transformer level rather than individual component/phase level. The UI therefore displays group-level records and clearly labels the mapping as an inspection placeholder. It must not imply that a specific sensor is physically installed at the selected point.

Future extension: add `transformer_part` and `component_measurement_mapping` tables, stable part IDs, phase IDs, GLB node names and sensor/test-to-part links. Preserve the current numbered IDs as stable UI identifiers.
