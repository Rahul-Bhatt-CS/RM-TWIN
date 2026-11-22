# Product Requirements Document (PRD)
## Transformer Digital Twin

**Document status:** Draft implementation baseline  
**Source:** *Rugged Monitoring | GBPUAT Pantnagar campus hiring 2026–27 | Take-home assignment*, page 1, Software (CS and AI).  
**Product option:** Digital Twin for electrical assets.  
**Asset focus for this document:** Transformer.  
**Requirement policy:** Mandatory requirements below are taken from the assignment. Where it is silent, this PRD labels the matter as *TBD* or an *implementation consideration*. No unspecified behaviour should be presented as a company requirement.

---

## 1. Product summary

Build a Digital Twin for a transformer that provides a live view of asset health using simulated sensor data, generates alerts when health degrades, and provides historical information that can be reviewed.

The assignment gives examples of sensor data: temperatures, load, dissolved gas or partial discharge. It does not require all examples to be implemented.

## 2. Goals

- Deliver all three mandatory Digital Twin capabilities:
  1. Live asset-health view from simulated sensor data.
  2. Alerts when health degrades.
  3. Reviewable history.
- Demonstrate a working end-to-end flow.
- Research the domain and document sources and design rationale.
- Explain implementation decisions and trade-offs.

## 3. Non-goals

Unless deliberately added and justified by the project author, the product is not required to provide:

- Physical sensor connectivity.
- SCADA/DCS or industrial-protocol integration.
- Predictive failure probabilities or remaining useful life.
- A physics-based thermal model or machine-learning model.
- 3D visualization.
- Maintenance management.
- Mobile/offline/PWA functionality.
- Multi-tenant enterprise administration.
- Authentication or a prescribed role/permission system.
- Any specific cloud, database, language, framework or deployment architecture.

## 4. Functional requirements

Priority labels:
- **MUST** – directly required by the assignment.
- **TBD** – detail is not specified by the assignment and must be decided/documented during design.
- **OPTIONAL** – permitted scope expansion, not required for assignment compliance.

### FR-01 – Digital Twin for a transformer
**Priority:** MUST

The product shall present a Digital Twin for the selected electrical asset, which in this PRD is a transformer.

**Acceptance criteria**
- The application identifies the monitored asset as a transformer.
- The application presents the transformer’s asset-health information as the Digital Twin experience.
- The demonstration makes clear that readings are simulated, not live measurements from physical equipment.

The assignment requires a Digital Twin for an electrical asset and gives transformer as an example. Choosing transformer is the scope selection for this document.

### FR-02 – Simulated sensor data
**Priority:** MUST

The product shall use simulated sensor data to provide the live asset-health view.

The assignment gives these example measurement categories:
- Temperature(s)
- Load
- Dissolved gas
- Partial discharge

**Acceptance criteria**
- The application obtains or generates simulated sensor readings.
- The readings are used by the asset-health view.
- The data shown in the demonstration can be identified as simulated.
- The implementation documents which of the example measurement categories it supports.

**TBD:** Which measurement categories are implemented, their units, ranges, generation method, update frequency and simulation assumptions.

### FR-03 – Live asset-health view
**Priority:** MUST

The product shall display a live view of the transformer’s health based on simulated sensor data.

**Acceptance criteria**
- The user can open the transformer’s health view.
- The view presents current/latest simulated sensor information relevant to the health view.
- When new simulated data is produced, the live view can reflect the updated health state.
- The display does not imply physical sensor connectivity.

**TBD:** Meaning of “live” in the prototype, refresh/update interval, health representation and exact fields displayed.

### FR-04 – Degradation alert generation
**Priority:** MUST

The product shall generate alerts when the transformer’s health degrades.

**Acceptance criteria**
- The implementation defines and documents what condition constitutes health degradation.
- When the simulated data causes the defined degradation condition, an alert is generated.
- The alert is visible to the user.
- The alert can be associated with the transformer and the condition that caused it, to the extent supported by the chosen design.
- A demonstration can show the transition from non-degraded to degraded health and the corresponding alert.

**TBD:** Thresholds, rules, severity levels, persistence, duplicate handling, acknowledgement, resolution and notification channels. The assignment requires alerts but does not prescribe these details.

### FR-05 – Historical information
**Priority:** MUST

The product shall retain history that a user can look back through.

**Acceptance criteria**
- Previously generated simulated sensor information is available after newer readings are produced.
- The user can access historical information for the transformer.
- The historical view makes it possible to review earlier readings or health states.
- The demonstration shows both current/live information and previously recorded information.

**TBD:** Retention period, query filters, pagination, aggregation, export and exact history fields.

### FR-06 – Research note
**Priority:** MUST (submission deliverable)

Provide a short research note stating what was studied, the sources and why the design was chosen.

**Acceptance criteria**
- The note identifies the research performed.
- Sources are listed.
- Design choices are connected to the research.
- Assumptions and limitations are not presented as established facts.

### FR-07 – Repository, deployment or demo
**Priority:** MUST (submission deliverable)

Provide one of the following:
- Repository link, or
- Deployed link, or
- Short demo video.

**Acceptance criteria**
- At least one of the permitted artifacts is supplied.
- The supplied link is accessible to the evaluators.

### FR-08 – README
**Priority:** MUST (submission deliverable)

Provide a README that covers how to run the project.

**Acceptance criteria**
- The README includes the instructions needed to run the delivered implementation.
- Instructions reflect the actual chosen stack and setup; placeholders must be replaced before submission.

### FR-09 – AI-use note
**Priority:** MUST when AI tools are used

The assignment welcomes AI tools and requires a short AI note.

**Acceptance criteria**
- The note identifies which parts AI produced.
- The note explains how the AI-produced work was checked.
- The note identifies one place where the candidate overruled AI output.
- The note is truthful and specific.

## 5. User stories

The assignment does not define formal user roles. These stories use the generic user implied by the required monitoring experience and do not impose an authorization model.

| ID | User story | Requirement |
|---|---|---|
| US-01 | As a monitoring user, I want to view the transformer’s current health so that I can see its current condition. | FR-03 |
| US-02 | As a monitoring user, I want the health view to use simulated sensor data so that the prototype demonstrates asset monitoring without physical sensor integration. | FR-02, FR-03 |
| US-03 | As a monitoring user, I want to see an alert when the asset’s health degrades so that the degradation is brought to my attention. | FR-04 |
| US-04 | As a monitoring user, I want to review historical information so that I can look back at earlier readings or health states. | FR-05 |
| US-05 | As an evaluator, I want to access a repository, deployment or demo video so that I can review the working submission. | FR-07 |
| US-06 | As an evaluator, I want run instructions so that I can understand how to start the project. | FR-08 |

## 6. User flows

### Flow A – View live transformer health
1. User opens the application.
2. User opens the transformer Digital Twin view.
3. Application presents the latest simulated sensor information used for health monitoring.
4. Application presents the current asset-health view.
5. As simulated readings update, the live view reflects the updated information.

**Required outcome:** A live asset-health view based on simulated sensor data.

### Flow B – Detect degraded health and show an alert
1. Simulated sensor data is produced.
2. The product evaluates the data using its documented degradation logic.
3. When the defined degradation condition occurs, the product generates an alert.
4. The alert is displayed to the user.

**Required outcome:** An alert is generated when asset health degrades.

### Flow C – Review history
1. User opens the transformer’s history view.
2. Application presents previously recorded information.
3. User reviews earlier readings and/or health states.

**Required outcome:** History is available for the user to look back through.

### Flow D – Evaluator runs or reviews the submission
1. Evaluator opens the provided repository, deployed application or demo video.
2. Evaluator follows the README run instructions where applicable.
3. Evaluator reviews the end-to-end flow and the research/design explanation.

**Required outcome:** The submission can be evaluated against the assignment criteria.

## 7. Permissions and roles

**Source finding:** The assignment does not specify roles, authentication, authorization or permission rules.

For the minimum assignment-compliant product:
- The required monitoring flow must be accessible in the demonstration.
- No particular login or role model is mandated by the assignment.

If the implementation introduces roles or authentication, document them as optional design choices. Do not make them prerequisites for the assignment unless the project author consciously chooses to do so.

## 8. Edge cases and implementation considerations

The assignment does not enumerate edge cases. The following are prudent cases to consider while implementing the mandatory capabilities; they are not additional business requirements.

| Area | Case to consider | Expected handling to define |
|---|---|---|
| Simulation | No simulated reading has been generated yet | Show an empty/not-yet-available state rather than implying a valid measurement |
| Simulation | Simulator is paused or stops producing readings | Make the absence of new data apparent |
| Simulation | A reading is missing or unusable | Do not silently treat it as a valid measurement |
| Health | Data is insufficient to assess health | Avoid asserting a definitive health state without sufficient inputs |
| Degradation | A reading moves back across a degradation boundary | Define how alert state behaves; assignment does not prescribe it |
| Alerts | Repeated readings satisfy the same degradation condition | Decide whether repeated alerts are grouped or generated separately |
| History | No historical readings exist | Show an empty history state |
| History | Requested period has no data | Communicate that no records are available |
| Time | Readings arrive with equal or out-of-order timestamps | Define deterministic ordering and handling |
| Data source | Historical/sample data is mixed with generated data | Clearly distinguish provenance to avoid misrepresenting simulated data |

## 9. Validation requirements

The assignment does not prescribe field-level validation. The following are implementation considerations:

- Validate that a simulated reading has a usable measurement value and timestamp before using it in the health view.
- Validate that the measurement category and unit are known to the implementation.
- Do not represent missing or invalid measurements as normal measurements.
- Validate user-supplied history filters, if such filters are implemented.
- Document the rules used to classify health degradation.
- Ensure an alert is generated only when the implementation's documented degradation condition is met.

Specific numeric ranges, thresholds, units and validation limits must be researched or explicitly identified as assumptions. They are not provided by the assignment.

## 10. Data and history requirements

The assignment requires simulated sensor data and reviewable history, but does not specify a data model.

The implementation must provide enough information to:
- Associate simulated readings with the transformer.
- Identify the measurement category.
- Preserve readings over time.
- Retrieve historical information for review.
- Support the health view and degradation-alert logic.

**TBD:** Storage technology, schema, retention period, sampling interval, aggregation and import format.

## 11. Non-functional and quality expectations

The assignment evaluates code quality, clarity of explanation, design decisions and trade-offs, and a working end-to-end flow. It does not specify numerical performance, availability, security or scalability targets.

| Area | Source-supported expectation |
|---|---|
| Completeness | A clear, finished, honest submission is preferred to a large unfinished one |
| End-to-end behaviour | Demonstrate a working end-to-end flow |
| Code quality | Code quality is an evaluation criterion |
| Explainability | Candidate must clearly explain decisions and trade-offs |
| Assumptions | Make sensible assumptions where unspecified and write them down |
| Individual ownership | Candidate works alone and can explain every decision |
| Time | About 8–12 hours of real work |

Do not invent SLAs, throughput targets, uptime targets, latency requirements or security certifications.

## 12. Acceptance test scenarios

These scenarios translate the assignment's mandatory capabilities into demonstrable checks.

### AT-01 – Live health view uses simulated data
- **Given** the application has a transformer Digital Twin,
- **When** simulated sensor readings are available,
- **Then** the live asset-health view displays health information based on those readings.

### AT-02 – Live view updates
- **Given** the live transformer-health view is open,
- **When** the simulator produces a subsequent reading,
- **Then** the view can display the updated information and resulting health state.

### AT-03 – Degradation generates an alert
- **Given** the product's documented degradation condition,
- **When** simulated readings satisfy that condition,
- **Then** the product generates and displays an alert.

### AT-04 – Non-degraded state does not falsely demonstrate degradation
- **Given** simulated readings that do not satisfy the documented degradation condition,
- **When** the health assessment is evaluated,
- **Then** the product does not claim that the degradation condition was met.

This is a consistency check for the chosen implementation logic; the assignment does not prescribe the underlying threshold.

### AT-05 – Historical information is reviewable
- **Given** multiple simulated readings have been recorded over time,
- **When** the user opens the history view,
- **Then** earlier information can be reviewed rather than only the latest reading being available.

### AT-06 – End-to-end demonstration
- **Given** the running application,
- **When** the demonstration generates simulated readings that cause degradation,
- **Then** the evaluator can observe the live health view, the generated alert and the historical information.

### AT-07 – Research and design explanation
- **Given** the submission,
- **When** the evaluator reviews its research note and explanation,
- **Then** the note identifies what was studied, sources and why the design was chosen.

### AT-08 – Run instructions
- **Given** the repository,
- **When** a reviewer follows the README,
- **Then** the README explains how to run the project using the actual implementation setup.

## 13. Assignment submission checklist

- [ ] Select the Digital Twin option.
- [ ] Implement a live asset-health view from simulated sensor data.
- [ ] Generate alerts when health degrades.
- [ ] Provide historical information for review.
- [ ] Prepare a short research note with sources and design rationale.
- [ ] Provide a repository link, deployed link or short demo video.
- [ ] Provide a README covering how to run the project.
- [ ] Include an AI note if AI tools are used.
- [ ] Be prepared to explain every decision, including one changed decision.
- [ ] Ensure the shared submission link is view-accessible.
- [ ] Email one link to kartik@ruggedmonitoring.com and nsuresh@ruggedmonitoring.com.
- [ ] Use subject: `GBPUAT take-home | <Track> | <Your name>`.
- [ ] Submit by Sunday, 29 November 2026, 11:59 PM IST.

## 14. Open decisions register

The following must be decided and documented before or during implementation:

| Decision | Assignment status |
|---|---|
| Exact transformer characteristics | Not specified |
| Sensor categories implemented | Examples provided; exact selection not specified |
| Sensor units and simulation model | Not specified |
| Meaning/update interval of “live” | Not specified |
| Health representation and calculation | Not specified |
| Degradation rules and thresholds | Not specified |
| Alert severity and lifecycle | Not specified |
| Historical retention and presentation | Not specified |
| User roles and authentication | Not specified |
| Technology stack | Any stack is allowed |
| Deployment approach | Not prescribed |
| Additional features | Open, must be justified |

This PRD is the requirements baseline. An AI coding agent should not silently convert TBD items into claimed assignment requirements. Any proposed assumptions should be surfaced for review and documented before implementation.
