# GridPulse — Remaining feature implementation

## Added
- Component-linked diagnostic records with optional phase metadata.
- HV bushing phases A/B/C and LV bushing phases a/b/c can be stored independently.
- 3D inspector filters diagnostics by selected component and displays phase labels.
- Expanded simulated telemetry: cooling pump state, oil level, radiator outlet temperature, OLTC tap position, demo operation count and motor-current pulse.
- Scripted cooling-assist warning scenario during elevated hot-spot conditions.
- Extracted and unit-tested balanced three-phase apparent-power/current calculations.
- Updated API, schema, standards-alignment, 3D map and implementation-status documentation.

## Not fabricated / requires OEM data
A true transformer-specific IEC 60076-7 thermal model is not activated. The selected CEA rating alone does not provide the OEM thermal constants and cooling parameters needed to implement and validate it. Obtain the actual GTP and approved thermal model inputs before adding that calculation. DGA, bushing, PD, FRA and oil-test records are stored but are not automatically diagnosed from invented thresholds.

## Verification
- TypeScript/TSX syntax transpilation checked successfully with the globally available TypeScript transpiler.
- Full `tsc -b` and Vite production build not completed: `npm ci` timed out and dependency tree was incomplete.
- Java/Maven tests were not run: Maven and cached Spring dependencies were unavailable in the authoring environment. A JUnit test for the three-phase current calculation is included.
- ZIP integrity checked after packaging.

## Security
Administrator token remains removed as requested. Write APIs remain unauthenticated; run only on localhost/trusted network until real authentication and authorization are implemented.


## Database unit-length fix

- Added Flyway migration `V4__widen_measurement_units.sql`.
- Increased `diagnostic_test.unit` and `sensor_reading.unit` from VARCHAR(20) to VARCHAR(40).
- Updated both JPA entity mappings to length 40.
- This resolves MySQL error 1406 (`Data too long for column 'unit'`) when synthetic diagnostic labels such as `°C · synthetic indication` are inserted.
