# GridPulse Reference Transformer — Engineering Profile

## Selected asset (one model for this project)

**CEA-listed 100 MVA, 220/33 kV, three-phase power transformer**, using a mineral-oil-immersed, conservator-type visual reference model.

The CEA December 2020 *Standard Specifications and Technical Parameters for Transformers and Reactors (66 kV & above voltage class)* lists 100 MVA, 220/33 kV (and 230/33 kV variant), three-phase power transformer as a standard rating (Chapter 1, standard ratings table). This is the reason for choosing this one profile: it is a documented Indian utility rating, it has a useful combination of active-part, bushing, cooling, oil, OLTC and protection monitoring categories, and it is a practical reference for a substation digital twin.

## Reference values used by the software

| Field | Value | Status |
|---|---:|---|
| Rated apparent power | 100 MVA | CEA-listed standard rating |
| HV nominal voltage | 220 kV | CEA-listed standard rating |
| LV nominal voltage | 33 kV | CEA-listed standard rating |
| Phases | 3 | CEA-listed standard rating |
| Frequency | 50 Hz | India reference assumption; verify on an actual nameplate |
| Insulating medium | Mineral insulating oil / oil-paper reference | Chosen model scope; confirm actual unit |
| Oil preservation | Conservator-type | Conceptual visual assumption, not established by rating alone |
| Cooling | ONAN/ONAF visual reference | Proposed modelling choice only; not specified by the selected rating entry |
| Tap changer | OLTC compartment shown | Common monitored component; actual presence/location/range must be confirmed from GTP |
| Manufacturer / model / serial | Not specified | No OEM identity is claimed |
| Vector group, impedance, tap range, losses, insulation levels, BIL, temperature-rise guarantees, oil volume, dimensions, masses | Not specified | Must come from OEM nameplate, GTP and approved drawings |

The frontend registers multiple asset records, but the default and only detailed 3D reference profile is `CEA-100MVA-220-33KV-3PH`. Its four nameplate values are locked in the default form so that the profile cannot accidentally be relabelled with a different rating. Manufacturer and serial-number fields remain user-entered because no OEM model is claimed.

## Current calculations

For a balanced three-phase system, the simulator derives apparent power and nominal line current from the simulated load percentage and nameplate values:

- `S_MVA = rated_MVA × load_percent / 100`
- `I_line_A = S_MVA × 1000 / (sqrt(3) × V_line_kV)`

It stores apparent power, HV line current and LV line current as derived simulated readings. These are ideal balanced-system calculations using nominal line voltage and do not model power factor, losses, voltage variation, unbalance, harmonics or CT measurement error.

## Simulation boundary

The backend creates synthetic operating scenarios. It does not solve the IEC 60076-7 thermal differential equations. A valid IEC thermal model needs transformer-specific thermal constants and design data (for example, rated losses, oil/winding time constants, exponents, hot-spot factor, cooling stages and relevant temperature-rise data). Those are not available from the CEA rating entry and are not fabricated here.

The current 5-second simulator's temperature and load sequences are test scenarios, not a physical electro-thermal simulation. The fan state is also scripted for demonstration; it is not controlled from a WTI contact model. All readings are marked `SIMULATED`.

## 3D model boundary

The model is a detailed procedural, interactive conceptual illustration. It includes tank, conservator, radiators, three HV and three LV bushings, OLTC housing/drive, fans, marshalling cabinet, pipes, valves, base rails and rollers. It is not a licensed OEM CAD model, not dimensionally accurate, and not evidence of exact internal sensor locations.

## Research source register

- CEA official manual landing page: https://cea.nic.in/pse___td/standard-specifications-and-technical-parameters-for-transformers-and-reactors-66-kv-above-voltage-class/?lang=en
- CEA manual PDF (December 2020, Amendment 01): https://cea.nic.in/wp-content/uploads/pse___td/2021/09/Transformer_Manual__Amendment_01.pdf
- GE Vernova MS 3000: https://www.gevernova.com/electrification/automation/transformer-monitoring/ms-3000
- Siemens Energy Noedra transformer monitoring: https://www.siemens-energy.com/global/en/home/products-services/product-offerings/substations/substation-digitalization.html
- Hitachi Energy TXpert monitoring systems: https://www.hitachienergy.com/in/en/products-and-solutions/insulation-and-components/transformer-insulation-components/measurement-and-safety-devices/monitoring-systems
