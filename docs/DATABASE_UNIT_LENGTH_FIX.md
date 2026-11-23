# Database unit-length fix

## Symptom
MySQL error 1406 / SQLState 22001:
`Data too long for column 'unit' at row 1`

The stack trace points to `SimulationService.addTest()` while inserting synthetic diagnostic history.

## Root cause
The original Flyway schema defined `diagnostic_test.unit` and `sensor_reading.unit` as `VARCHAR(20)`. The simulator inserts descriptive unit strings longer than 20 characters, for example `% · synthetic indication` and `operations · demo counter`.

## Fix
A new forward-only Flyway migration `V4__widen_measurement_units.sql` widens both columns to `VARCHAR(40)`. `DiagnosticTest` and `SensorReading` entity mappings were updated to `length=40`.

Existing V1-V3 migrations were not edited. On restart, Flyway should automatically apply V4 to an existing database already at V3.

## Apply
1. Stop the backend.
2. Replace the project files with this fixed package (or copy the changed files).
3. Start the backend again.
4. Confirm the startup log says Flyway migrated the schema to version 4.
5. Wait for the next simulator tick and check that diagnostic records are inserted.

No database drop or manual deletion is required.

## Separate Hibernate warning
`HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory` is a performance warning, not the SQL failure above. It means pagination may happen in memory for that query. It should be addressed separately by paginating parent IDs first or avoiding collection fetch joins in paginated queries.
