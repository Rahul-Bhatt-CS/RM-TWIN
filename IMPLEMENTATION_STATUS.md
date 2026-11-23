# Implementation Status — Reference Transformer + Interactive 3D

## Implemented in source
- Multi-transformer fleet; each new asset defaults to the same selected reference profile.
- Selected reference profile: CEA-listed 100 MVA, 220/33 kV, three-phase power transformer; 50 Hz is an India project assumption.
- Transformer records include HV voltage, LV voltage, MVA rating, frequency and reference profile.
- Backend simulator emits synthetic top-oil, winding hot-spot, load, ambient, apparent MVA, HV line current, LV line current and cooling-fan-state readings.
- Apparent MVA and balanced line currents are derived from nameplate values using the three-phase equation; these are idealized simulated quantities.
- Detailed procedural GLB model with 165 geometries: tank, conservator, radiator fins, HV/LV bushings, OLTC housing/drive, fans, marshalling cabinet, pipes, valves, rollers and accessories.
- Model-viewer integration with orbit, zoom, pan/touch and eight numbered clickable inspection hotspots.
- Numbered areas map to existing health groups and show available transformer-level evidence; not individual sensor-placement claims.
- SSE dashboard events, demo alerts, diagnostic record entry, protection-event entry and server-side pagination remain in place.
- Diagnostic records now carry optional stable component keys and phase identifiers (HV A/B/C, LV a/b/c), so bushing test results can be tied to a selected 3D inspection area and phase.
- Simulator now emits cooling pump state, oil level, radiator outlet temperature, OLTC tap position, demonstration operation count and OLTC motor-current pulse, in addition to existing measurements.
- 3D inspector now loads paginated diagnostic/protection records and filters component-linked diagnostic results; bushing phase is shown when recorded.
- DGA and moisture results are stored but not automatically diagnosed/scored without an approved interpretation method and context.
- Flyway V2 adds LV voltage, rated frequency and reference-profile fields.
- Administrator-token system remains removed; write APIs are unauthenticated in this demo.

## Important engineering limitations
- The selected CEA table specifies the rating, not a complete OEM transformer design.
- ONAN/ONAF-style cooling, mineral-oil/conservator visual arrangement and component dimensions are modelling assumptions; confirm against a real nameplate/GTP/drawing.
- The simulator is a scripted demonstration scenario, not a physical thermal simulation and not an IEC 60076-7 implementation.
- Demo alert thresholds are not CEA/IEC/IEEE/OEM alarm or trip limits.
- Health index is not an official standards formula. Unknown diagnostic groups remain unknown; no DGA diagnosis is inferred.
- No SCADA, live sensors, protection relay control, breaker control or safety certification.
- OLTC/cooling auxiliary signals are scripted demonstration telemetry, not calibrated device models or manufacturer-specific controller logic.
- A standards-conformant IEC 60076-7 thermal model is not enabled because OEM thermal constants (rated top-oil rise, winding hot-spot gradient, time constants, exponent and cooling data) are not supplied. The scripted thermal scenario remains explicitly illustrative.
- GLB is procedural conceptual geometry, not OEM CAD and not a verified sensor-location drawing.
- Google model-viewer 4.0.0 is loaded from a pinned CDN URL; browser internet is required for that library unless vendored locally.

## Verification
- Java 21 and Node 22 are available in the authoring environment; Maven is not installed. npm dependency installation timed out; the extracted dependency tree is incomplete.
- Frontend dependencies in the archived environment are incomplete; `npx tsc -b` cannot be used here because type packages are missing. `npm install` timed out.
- Backend compilation/tests were not run because Maven is unavailable.
- GLB file was reopened with trimesh: 165 geometries; bounds approximately X [-2.34, 2.74], Y [0.13, 3.41], Z [-1.00, 1.33] metres.
- ZIP integrity is checked after packaging.

Before submission, run `npm ci && npm run build`, `mvn test`, and an end-to-end test against a disposable MySQL database. Verify model-viewer CDN loading and all eight hotspots in the target browser.


## Latest diagnostic-data update
- Backend seeds synthetic historical DGA/oil, insulation, phase-specific bushing, mechanical/electrical, OLTC, cooling and protection/auxiliary records for each simulation-enabled transformer.
- Seeded records are explicitly `SIMULATED`; they are not physical measurements.
- Health components now show illustrative scenario scores for synthetic groups instead of UNKNOWN. Explanations state that these are not standard-derived pass/fail ratings.
- Imported diagnostic data without an approved interpretation method remains recorded-but-unscored.
- Protection status examples are synthetic indications, not relay self-tests or proof of protection health.


## Synthetic data coverage by 3D inspection point
- MAIN_TANK: DGA gas panel, oil tests, IR/PI, winding dielectric, FRA, winding resistance and PD sample records.
- CONSERVATOR: synthetic moisture and oil-level gauge check records.
- HV_BUSHING / LV_BUSHING: phase-specific A/B/C capacitance and tan-delta sample records.
- RADIATOR: radiator temperature-difference test record.
- COOLING_FAN: vibration example plus live simulated fan state.
- OLTC: contact-resistance and operating-time sample records plus live tap/motor telemetry.
- CONTROL_CABINET: synthetic protection/auxiliary indication events.
All sample test records are tagged SIMULATED and include non-operational-use disclaimers.
