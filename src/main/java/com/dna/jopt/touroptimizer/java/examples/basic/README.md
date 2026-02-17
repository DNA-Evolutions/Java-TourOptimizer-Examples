# Basic Examples (Java) — JOpt TourOptimizer

The **Basic** examples are the recommended entry point to learn how to model and run tour optimization problems with **JOpt TourOptimizer for Java**.  
The goal is to get hands-on with the core building blocks—**properties**, **nodes**, **resources**, **connections**, **results**, and **licensing**—before moving to the Advanced/Expert/RESTful sections.

---

## Where this fits in the repository

The examples are organized into four major example categories (hosted on GitHub):

- [Basic](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic)
- [Advanced](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced)
- [Expert](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert)
- [RESTful](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful)

Each section has its own README. This README focuses on the **Basic** package.

---

## Recommended learning path

If you are new to JOpt, follow this progression:

1. **Understand the core elements**  
   Read: [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)

2. **Run the first complete optimization**  
   Start here: `firstoptimization_01/FirstOptimizationExample.java`  
   Tutorial: [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization)

3. **Adopt a production-friendly execution pattern**  
   Learn synchronous vs async vs reactive event handling: `recommendedimplementation_02`

4. **Persist and reload optimization state (JSON / JSON.BZ2)**  
   Learn how to save, checkpoint, and resume runs: `io_03`

5. **Provide your own distances / travel times (external connections)**  
   Learn directed edges and fallback behavior: `connection_04`

6. **Read out results cleanly (routes, stops, costs, violations)**  
   Learn how to turn results into a report/table: `readoutresult_05`

7. **Model non-geo work items**  
   Learn `EventNode` for “tasks without locations” (calls/meetings): `eventnode_06`

8. **Model contractual SLAs as hard constraints (Pillar/Captured nodes)**  
   Learn how nodes “flow around” pillars: `pillar_07`

9. **Set up licensing for real scenarios (> 10 elements)**  
   Learn file-based and JSON-based licensing: `setlicense_08`

---

## Good advice before you start

- **Keep the model small first.** Validate feasibility and modeling correctness before increasing problem size.
- **Always watch progress/status/errors during development.** It accelerates debugging dramatically.
- **Separate hard constraints from costs.**  
  JOpt hard constraints (e.g., **Pillar/Captured nodes**) are satisfied by **architecture**, not by “huge penalties”.

---

## Running the examples

### Option A — Run from your IDE (recommended)
- Import the repository as a Maven project.
- Run the `main(...)` method of the chosen example.

### Option B — Use the sandbox (Docker)
If you want to avoid local Java/Maven/IDE setup, the repository provides a browser-based sandbox described in the repo-level README.

---

## First Example (`firstoptimization_01`)

Start with:

- [FirstOptimizationExample.java](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/firstoptimization_01/FirstOptimizationExample.java)

Tutorial walkthrough:
- [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization)

It covers:
1. Adding properties  
2. Adding nodes  
3. Adding resources  
4. Attaching to observables  
5. Starting the optimization and presenting the result  

---

## Overview of the Basic packages

> Naming convention: packages are numbered (`PACKAGENAME_XX`) to indicate increasing complexity. You do **not** need to follow the numbering strictly, but it is a good guided learning sequence.

### `recommendedimplementation_02` — Execution patterns you should copy
What you learn:
- synchronous vs asynchronous execution
- safe lifecycle handling (don’t terminate your JVM too early)
- callbacks and reactive-style event consumption

Start here after the first optimization:
- [recommendedimplementation_02 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02)

Recommended companion doc:
- `RecommendedImplementation.md` (place next to the example classes)

### `io_03` — Save / load / resume (JSON and JSON.BZ2)
What you learn:
- export optimization state to compressed JSON (`*.json.bz2`)
- reload into an optimization instance
- start a fresh run from the loaded model (ignore old solution)
- checkpoint during a run (save at milestones)
- export result to KML for visualization

Package:
- [io_03 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03)

Recommended companion doc:
- `LoadAndSaveOptimization.md` (place in `io_03`)

### `connection_04` — External node connections (distances / driving times)
What you learn:
- define directed edges (A→B is not implicitly B→A)
- provide partial matrices and rely on fallback connections for the rest
- use `locationId` to consolidate “same physical position” semantics

Package:
- [connection_04 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04)

Relevant documentation:
- [Basic elements (connections)](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)
- [BackupConnector — how distances/times are computed](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times)

Recommended companion doc:
- `ExternalNodeConnection.md` (place in `connection_04`)

### `readoutresult_05` — Read and analyze the result object
What you learn:
- iterate routes and route items (stop sequence)
- extract route/job cost, time, distance
- read violations on route level and per node

Package:
- [readoutresult_05 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05)

Tutorial section:
- [Analyzing the result](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result)

Recommended companion doc:
- `ReadOutResult.md` (place in `readoutresult_05`)

### `eventnode_06` — Nodes without geo locations
What you learn:
- model “work items” like calls/meetings that consume time but not travel distance
- combine `EventNode` and geo nodes in one plan
- export to KML (where applicable)

Package:
- [eventnode_06 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06)

Recommended companion doc:
- `EventNode.md` (place in `eventnode_06`)

### `pillar_07` — Pillar / Captured nodes (hard SLA constraints)
What you learn:
- model strict SLAs as *hard constraints*
- understand how normal nodes “flow around” a pillar appointment
- attach a mandatory resource when needed

Critical concept:
- A pillar is satisfied by **architecture**, not by “very high costs”.

Docs:
- [Special Features — CapturedNode / Pillar](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#case-four-conflict-of-a-capturednode-with-a-node-solved-by-removing-a-capturednode)

Package:
- [pillar_07 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07)

Recommended companion doc:
- `Pillar.md` (place in `pillar_07`)

### `setlicense_08` — Leaving free mode (license setup)
What you learn:
- how licensing works and when it is required (more than 10 elements)
- set license as JSON string or from file
- recommended secure deployment patterns

Docs:
- [License documentation](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)

Package:
- [setlicense_08 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/setlicense_08)

Recommended companion doc:
- `SetLicense.md` (place in `setlicense_08`)

---

## Troubleshooting and best practices

### The optimization terminates immediately
If you start asynchronously and do not block the JVM, the run may terminate early.  
In CLI examples, you typically see a `future.get()` (or similar) to keep the process alive.

### “Why is my distance/time different from Google Maps?”
If you do not supply external edges, fallback connectors are used. Review:
- [BackupConnector distance/time model](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times)

### I need deterministic behavior for tests
- fix your properties and seeds (where applicable),
- keep input ordering stable,
- persist snapshots (`io_03`) to reproduce exact runs.

---

## Further documentation & links

- [Official documentation hub](https://www.dna-evolutions.com/docs/getting-started/home/home)
- [Public Nexus repository](https://public.nexus.dna-evolutions.net/)
- [Public JavaDocs](https://public.javadoc.dna-evolutions.com)
- [Special features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features)
- Repository meta docs:
  - [RELEASE_NOTES.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/RELEASE_NOTES.md)
  - [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md)
  - [FAQ.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/FAQ.md)
- Contact:
  - [www.dna-evolutions.com](https://www.dna-evolutions.com/contact)
  - [info@dna-evolutions.com](mailto:info@dna-evolutions.com)
