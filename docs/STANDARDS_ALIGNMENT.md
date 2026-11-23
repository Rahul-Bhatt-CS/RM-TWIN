# Backend Standards Alignment and Limits Policy

## 1. Governing references

| Reference | Backend use |
|---|---|
| CEA, *Standard Specifications and Technical Parameters for Transformers and Reactors (66 kV & above voltage class)*, Dec 2020 + Amendment 01 | Indian reference for 100 MVA 220/33 kV three-phase rating; OTI/WTI, cooling controls, DGA context, FRA, bushing tests, PD, moisture and condition-monitoring categories |
| IEC 60076-2:2011 | Temperature-rise requirements and tests for liquid-immersed transformers; not a universal online alarm setpoint |
| IEC 60076-7:2018 | Mineral-oil-immersed transformer loading guide, operating temperatures and thermal ageing; actual model needs transformer-specific thermal parameters |
| IEC 60599:2022 | Interpretation guidance for dissolved/free gases in mineral-oil-filled equipment; retain sampling context and trend |
| IEEE C57.104-2019 | Complementary DGA interpretation guidance; use approved edition and utility procedure |
| IEC 60422:2024 | Supervision and maintenance guidance for mineral insulating oils |
| IEC 60156:2025 | Test method for breakdown voltage of insulating liquids |
| IEC 60137:2017 | Characteristics and tests for insulated bushings for AC voltages above 1 kV |
| IEC 60214-1:2014 | Tap-changer performance requirements and test methods |
| IEC 60076-18:2012 | Frequency-response measurement technique |
| IEC 60076-22-1:2019 | Protective devices mounted on liquid-immersed transformers/reactors |
| CIGRE TB 761 (2019) | Condition-assessment matrix and fleet-prioritisation reference; does not prescribe this application's weights |

Check the currently applicable BIS adoption, amendments and utility specification for the actual asset procurement/operation. A standard's test method or diagnostic guide is not automatically a legal alarm/trip setting.

## 2. What the backend now does

- Transformer records carry HV voltage, LV voltage, rated MVA, frequency and reference-profile ID.
- The reference profile is fixed to 100 MVA, 220/33 kV, three-phase, 50 Hz.
- Simulator-derived MVA and balanced HV/LV line currents use the nameplate rating and three-phase current equation.
- Temperature, load and fan sequences remain explicitly synthetic.
- Diagnostic tests are imported records, separate from continuous telemetry.
- DGA gas values are not automatically converted to a diagnosis or score. IEC 60599 / IEEE C57.104 interpretation requires trend, sampling and asset context.
- Moisture records are displayed but are not assigned an arbitrary automatic score.
- Protection events are recorded/displayed; no breaker trip, relay control or safety interlock is implemented.
- Readings preserve `SIMULATED` or `IMPORTED` provenance.

## 3. Demonstration alert thresholds (not standards limits)

`app.demo.warning-hotspot-c`, `app.demo.critical-hotspot-c` and `app.demo.warning-load-percent` remain configurable solely to exercise the assignment's alert workflow. The defaults 95 °C, 110 °C and 100% are **not asserted to be CEA, IEC, IEEE or OEM alarm/trip limits**. They are not suitable for operating decisions.

Before using an actual transformer, replace them with alarm settings approved for that asset by the OEM/utility engineer, based on the nameplate, guaranteed technical particulars, cooling arrangement, insulation system, site ambient and approved operating policy. Do not infer trip settings from these application properties.

## 4. Thermal-model limitation

IEC 60076-7 provides a thermal-loading framework for mineral-oil-immersed transformers. The current simulator does not claim compliance with its equations. A future IEC-guided thermal model needs validated asset-specific inputs such as top-oil and winding temperature-rise test values, rated losses, load-loss/no-load-loss data, cooling configuration and stages, time constants, winding hot-spot factor and model exponents. The system must refuse to label the model standards-based if required parameters are missing.

## 5. Health index policy

The project's weighted health index remains a software demonstration construct, not an official CEA/IEC/IEEE/CIGRE health-index formula. It only scores the thermal group using the explicitly labelled demo triggers. Other groups are `UNKNOWN` or `RECORDED_NOT_INTERPRETED` until a validated method and asset baseline are configured. Missing diagnostic evidence is never scored as healthy. A real deployment requires engineering-approved weights, thresholds, freshness rules, baseline comparisons and validation against fleet history.

## 6. Security / operational boundary

No administrator token is present, as requested. The API's write endpoints are therefore unauthenticated. Keep this version local or on a trusted demo network. Do not connect it to SCADA, protection relays or field equipment. Production requires authentication/authorization, audit logging, TLS, network segmentation, rate limiting and engineering validation.

## Official publication pages
- IEC 60076-7:2018: https://webstore.iec.ch/en/publication/34351
- IEC 60076-2:2011: https://webstore.iec.ch/en/publication/599
- IEC 60599:2022: https://webstore.iec.ch/en/publication/66491
- IEC 60422:2024: https://webstore.iec.ch/en/publication/66421
- IEC 60137:2017: https://webstore.iec.ch/en/publication/29183
- IEC 60214-1:2014: https://webstore.iec.ch/en/publication/1032
- IEC 60076-18:2012: https://webstore.iec.ch/en/publication/597
