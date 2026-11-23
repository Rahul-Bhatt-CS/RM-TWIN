# Transformer Digital Twin

A take-home assignment project for the **Software (CS and AI)** track of Rugged Monitoring's GBPUAT Pantnagar campus hiring 2026–27.

> **Project status:** Requirements and documentation baseline. Application implementation has not started.

## Project overview

This project addresses the **Digital Twin for electrical assets** option in the assignment. The selected asset for this project documentation is a transformer.

The assignment's mandatory capabilities for this option are:

- A live view of the asset's health using simulated sensor data.
- Alerts when asset health degrades.
- Historical information that can be reviewed.

The assignment gives temperatures, load, dissolved gas and partial discharge as examples of sensor data. It does not state that every example must be implemented.

This project will document its research, assumptions, design decisions and trade-offs. It will not imply physical sensor connectivity or real transformer measurements unless those are actually implemented and evidenced.

## Assignment context

- **Company:** Rugged Monitoring
- **Campus:** GBPUAT Pantnagar
- **Hiring cycle:** 2026–27
- **Track:** Software (CS and AI)
- **Eligible candidates described:** B.Tech / B.E. final-year students and recent graduates
- **Assignment time budget:** About 8–12 hours of real work
- **Deadline:** Sunday, 29 November 2026, 11:59 PM IST

## Features

### Mandatory assignment features

- [ ] Transformer Digital Twin
- [ ] Live asset-health view
- [ ] Simulated sensor data
- [ ] Alerts when health degrades
- [ ] Historical information that can be reviewed

### Sensor-data examples in the assignment

The assignment names the following examples; the implementation will document which are supported:

- Temperature(s)
- Load
- Dissolved gas
- Partial discharge

### Supporting deliverables

- [ ] Short research note: what was studied, sources and why the design was chosen
- [ ] Repository link, deployed link or short demo video
- [ ] README with instructions to run the project
- [ ] AI-use note, if AI tools are used

## Users and roles

The assignment does not prescribe application roles, authentication or authorization.

The product is intended to support a user viewing asset health, seeing degradation alerts and reviewing history. Any additional roles or permission model are implementation choices and should be documented as such.

| User concept | Purpose |
|---|---|
| Monitoring user (generic) | View live asset health, degradation alerts and historical information |

## Tech stack

**Placeholder – to be completed after the technology stack is selected.**

| Layer | Technology | Notes |
|---|---|---|
| Frontend | TBD | TBD |
| Backend | TBD | TBD |
| Database | TBD | TBD |
| Sensor simulation | TBD | Simulated data is required |
| Charts/visualization | TBD | TBD |
| Testing | TBD | TBD |
| Deployment | TBD | TBD |

The assignment permits any technology stack. No particular language, framework, database or cloud provider is mandated.

## Repository structure

**Placeholder – update to match the actual repository.**

```text
.
├── README.md
├── BRD.md
├── PRD.md
├── research/
│   └── research-note.md
├── frontend/             # TBD
├── backend/              # TBD
└── ...
```

## Setup

**Placeholder – replace with verified setup steps for the selected stack.**

Prerequisites:
- TBD
- TBD

Installation:
1. Clone the repository.
2. Install the required dependencies using the actual package manager.
3. Configure the required environment variables.
4. Start the required services.

Exact commands will be added after implementation. Do not rely on these placeholders as executable instructions.

## Environment variables

**Placeholder – list only variables actually used by the implementation. Do not commit secrets.**

| Variable | Purpose | Required | Example |
|---|---|---|---|
| `TBD` | TBD | TBD | TBD |

## Running instructions

**Placeholder – replace with tested commands after implementation.**

### Run backend

```bash
# TBD: backend startup command
```

### Run frontend

```bash
# TBD: frontend startup command
```

### Run the application

1. Start the backend and its configured dependencies.
2. Start the frontend.
3. Open the local URL printed by the frontend development server.
4. Open the transformer Digital Twin.
5. Start or enable the simulated data flow, if the implementation exposes such a control.
6. Verify the live health view, degradation alert and historical view.

The final instructions must reflect the actual implementation and be tested from a clean setup.

## Testing

**Placeholder – update with actual test tools and commands.**

Testing should verify the mandatory assignment flow:

1. Simulated sensor readings are produced and used by the live health view.
2. The live health view reflects subsequent simulated readings.
3. The documented degradation condition causes an alert to be generated and displayed.
4. Historical information remains available for review after newer readings are produced.
5. The end-to-end demonstration works.

Record the actual test framework, commands and results here after implementation.

## Deployment

**Placeholder – complete only after deployment is selected and verified.**

- Frontend URL: TBD
- Backend URL: TBD
- Hosting/platform: TBD
- Deployment instructions: TBD

The assignment does not prescribe a deployment platform. A repository link, deployed link or short demo video is acceptable as the project artifact.

## Demo credentials

**Placeholder – only if authentication is implemented.**

| Role | Username | Password |
|---|---|---|
| TBD | TBD | TBD |

If the application does not require authentication, replace this section with: `Authentication is not required for this demonstration.` Do not invent credentials.

## Research and design rationale

A separate short research note will document:

- What was studied.
- Sources consulted.
- Why the design was chosen.
- Assumptions and limitations.

The assignment expects domain research and an explanation of design decisions and trade-offs. Do not claim standards compliance or validated predictive capability unless that has been independently established.

## AI-use note

The assignment permits AI tools and asks for a short note covering:

1. Which parts AI produced.
2. How the AI-produced work was checked.
3. One place where the candidate overruled AI output.

**Placeholder – complete truthfully before submission.**

## Known limitations and assumptions

- Sensor data for the mandatory live view is simulated.
- The assignment does not prescribe sensor thresholds, health-score formulas, alert severity, sampling intervals or retention periods.
- Any such values or methods used by the project must be identified as researched design choices or explicit assumptions.
- The assignment does not require physical sensor connectivity.
- The assignment does not prescribe application roles or authentication.
- The assignment does not prescribe a technology stack.

## Submission

Submit one view-accessible link (GitHub, Google Drive or OneDrive) by email.

**Recipients**
- kartik@ruggedmonitoring.com
- nsuresh@ruggedmonitoring.com

**Subject**
```text
GBPUAT take-home | <Track> | <Your name>
```

**Deadline:** Sunday, 29 November 2026, 11:59 PM IST.

The candidate must work alone and be prepared to explain every decision, including one decision they changed their mind on.

## Evaluation criteria from the assignment

- Depth of domain research.
- A working end-to-end flow.
- Design decisions and trade-offs.
- Code quality.
- Clarity of explanation.

The assignment states that there is no hard CGPA cutoff.
