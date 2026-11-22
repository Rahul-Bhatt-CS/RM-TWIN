# Business Requirements Document (BRD)
## Transformer Digital Twin for Rugged Monitoring – GBPUAT Take-Home Assignment

**Document status:** Draft baseline  
**Source of requirements:** *Rugged Monitoring | GBPUAT Pantnagar campus hiring 2026–27 | Take-home assignment*, page 1, Software (CS and AI).  
**Scope discipline:** This document is limited to the Digital Twin option and the instructions stated on page 1. Details not specified by the assignment are marked as *to be decided* or *implementation consideration*, not treated as company requirements.

---

## 1. Business problem

Utilities operate thousands of electrical assets. The assignment states that software used to track asset health, planning and field work is still young. The selected challenge is to create a Digital Twin for an electrical asset, such as a transformer, switchgear or generator.

For this project, the asset under consideration is a **transformer**. The business problem addressed is the need to view an asset's health using simulated sensor data, recognize when its health degrades, and review historical information.

The assignment does not define a particular utility, transformer specification, failure mode, sensor installation, operating standard or commercial business process. Those details must not be represented as facts or requirements unless separately researched and documented by the project team.

## 2. Business objectives

The solution is intended to:

1. Provide a live view of a transformer’s health using simulated sensor data.
2. Represent relevant simulated measurements, with examples in the assignment including temperature, load, dissolved gas and partial discharge.
3. Generate alerts when the asset's health degrades.
4. Preserve history that users can look back through.
5. Demonstrate a working end-to-end flow.
6. Reflect domain research and explain the design choices and trade-offs made.

Objectives 1–4 are the mandatory product capabilities for the selected Digital Twin option. Objectives 5–6 reflect the assignment's evaluation criteria.

## 3. Stakeholders

| Stakeholder | Relationship to the project | Source status |
|---|---|---|
| Utility organization | Context in which electrical assets are operated and monitored | Explicitly mentioned by the assignment |
| People responsible for understanding asset health | Potential users of the asset-health view and history | Implied by the monitoring purpose; exact titles are not specified |
| Project author/developer | Researches, designs and builds the submission | Explicitly required to work alone |
| Rugged Monitoring evaluators/interviewers | Evaluate research, end-to-end flow, design decisions, code quality and explanation | Explicitly described in evaluation/interview instructions |

The assignment does not specify a formal organizational stakeholder map or named end-user job titles.

## 4. Users and roles

The assignment does not prescribe application roles, authentication, authorization or user-specific permissions.

For requirements traceability, the only source-supported user concept is a person using the software to view asset health, receive/inspect degradation alerts and look back through history.

The implementation may define role names if useful, but these are **design choices**, not assignment-mandated roles. Do not imply that the assignment requires administrator, operator, engineer or maintenance roles.

| User concept | Source-supported need |
|---|---|
| Monitoring user (generic) | View live asset health, see degradation alerts and review history |

Any additional roles and permission differences remain **TBD**.

## 5. Scope

### 5.1 In scope – mandatory

- One Digital Twin build for an electrical asset.
- A transformer is the selected subject for this BRD.
- A live view of asset health.
- Simulated sensor data as the source for the live view.
- Sensor-data examples may include:
  - Temperature
  - Load
  - Dissolved gas
  - Partial discharge
- Alerts when asset health degrades.
- Historical information that can be reviewed.

### 5.2 In scope – supporting submission deliverables

- A short research note describing what was studied, sources used and why the design was chosen.
- A repository link, deployed link or short demo video.
- A README explaining how to run the project.
- An explanation of design decisions and trade-offs.
- An AI-use note identifying AI-produced work, how it was checked and one instance where AI output was overruled, if AI tools are used.

### 5.3 Out of scope / not required by the source

The assignment does not require the following. They must not be treated as mandatory requirements:

- Connection to physical sensors or a real transformer.
- Real-time integration with a utility SCADA/DCS system.
- A specific transformer rating, voltage, manufacturer or cooling type.
- A 3D model or spatial visualization.
- Machine learning, predictive failure prediction or remaining-life estimation.
- A prescribed health-score formula or industry-standard compliance.
- User registration, login, role-based access control or a prescribed set of roles.
- Maintenance work orders or maintenance planning.
- Mobile/offline operation or a PWA.
- A specific programming language, framework, database, hosting provider or architecture.
- A specific number of transformers, substations, sensors or historical records.

The assignment allows scope beyond the must-haves, but requires the team to decide what matters and explain why. Optional additions should therefore be consciously justified and must not compromise completion of the mandatory flow.

## 6. Business rules

The following rules are directly supported by the assignment:

1. The project must implement **one** of the three offered builds; this BRD covers the Digital Twin option.
2. The Digital Twin must provide a live view of asset health.
3. The live view must use simulated sensor data.
4. Sensor-data examples named by the assignment are temperatures, load, dissolved gas or partial discharge. The assignment does not say all four are mandatory.
5. The system must generate alerts when asset health degrades.
6. The system must provide history that users can look back through.
7. The technology stack is open; any stack is acceptable.
8. Assumptions made where the assignment is silent must be written down.
9. The work is individual.
10. The stated time budget is about 8–12 hours of real work.
11. A clear, finished and honest submission is preferred over a large unfinished one.

### 6.1 Rules not specified

The assignment does not define numerical sensor thresholds, health-score ranges, alert severities, alert delivery channels, sampling intervals, retention periods, data formats, simulation equations or criteria for deciding that health has degraded. These must be established as documented project assumptions or researched design choices, not attributed to the assignment.

## 7. Success criteria

The submission is successful against the stated requirements when:

| ID | Success criterion | Basis |
|---|---|---|
| SC-01 | A transformer Digital Twin is presented as the selected build | Digital Twin option |
| SC-02 | The application shows a live asset-health view | Mandatory requirement |
| SC-03 | The live view is driven by simulated sensor data | Mandatory requirement |
| SC-04 | The application generates an alert when the simulated asset health degrades | Mandatory requirement |
| SC-05 | A user can look back through historical information | Mandatory requirement |
| SC-06 | The complete core flow can be demonstrated end to end | Evaluation criterion |
| SC-07 | A short research note identifies what was studied, sources and design rationale | Submission requirement |
| SC-08 | A repository link, deployed link or short demo video is supplied | Submission requirement |
| SC-09 | A README explains how to run the project | Submission requirement |
| SC-10 | Design decisions and trade-offs can be clearly explained | Evaluation criterion |
| SC-11 | Code quality and clarity of explanation are demonstrated | Evaluation criteria |
| SC-12 | An AI note is included if AI tools are used | Ground rule |

There is no hard CGPA cutoff, according to the assignment.

## 8. Constraints and submission conditions

- **Time budget:** About 8 to 12 hours of real work.
- **Due date:** Sunday, 29 November 2026, 11:59 PM IST.
- **Submission method:** Email one view-accessible link (GitHub, Google Drive or OneDrive).
- **Recipients:** kartik@ruggedmonitoring.com and nsuresh@ruggedmonitoring.com.
- **Subject:** `GBPUAT take-home | <Track> | <Your name>`.
- **Individual work:** The candidate must work alone and be able to walk through every decision, including one decision they changed their mind on.
- **AI use:** AI tools are welcome. The submission must include a short note explaining which parts AI produced, how they were checked and one place where the candidate overruled it.

## 9. Open questions / decisions to document

The assignment intentionally provides rough requirements. The project team must research and document its assumptions for:

- Which transformer parameters to simulate.
- What constitutes degraded health.
- How health is represented.
- How and when alerts are generated.
- What historical information is retained and how it is presented.
- The simulated operating behaviour and its limitations.
- The chosen technology stack and design trade-offs.

These are project design decisions, not additional requirements imposed by the assignment.
