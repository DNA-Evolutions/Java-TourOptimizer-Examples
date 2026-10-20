# Base examples

The **Base** examples are the recommended entry point to learn how to model and run tour optimization problems with **JOpt.TourOptimizer for Java**. The goal is to get hands-on with the core building blocks: **properties**, **nodes**, **resources**, **connections**, **results**, and **licensing**, before moving to the Advanced/Expert/RESTful sections. If you are new to JOpt, follow this progression: understand the core elements first (read the [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)), then work through the examples below in order.

Naming convention: packages are numbered (`PACKAGENAME_XX`) to indicate increasing complexity. You do **not** need to follow the numbering strictly, but we suggest working through them in this order.

| Example | What you learn |
|---|---|
| [First optimization example](firstoptimization_01/FirstOptimizationExample.md) (`firstoptimization_01`) | **Run the first complete optimization.** It covers adding properties, adding nodes, adding resources, attaching to observables, and starting the optimization and presenting the result. |
| [Recommended ways to run an optimization](recommendedimplementation_02/RecommendedImplementation.md) (`recommendedimplementation_02`) | **Adopt a production-friendly execution pattern.** The recommended run pattern, a synchronous run and the legacy callback methods. |
| [Load and save optimization snapshots](io_03/LoadAndSaveOptimization.md) (`io_03`) | **Persist and reload optimization state (JSON / JSON.BZ2).** Learn how to save, checkpoint, and resume runs: export optimization state to compressed JSON (`*.json.bz2`); reload into an optimization instance; start a fresh run from the loaded model (ignore old solution); checkpoint during a run (save at milestones). |
| [Load a snapshot and add a pillar](io_03/LoadOptimizationFromJsonAndReassignPillar.md) (`io_03`) | **Add a pillar to a saved plan.** Load a saved optimization state from a JSON snapshot and add a pillar node to its solution through reassignment. |
| [Export a result to a KML file](io_03/Export2KMLExample.md) (`io_03`) | **Visualize the result.** Export result to KML for visualization. |
| [Custom distances and driving times with a node connector](connection_04/ExternalNodeConnection.md) (`connection_04`) | **Provide your own distances / travel times (external connections).** Learn directed edges and fallback behavior: define directed edges (A to B is not implicitly B to A); provide partial matrices and rely on fallback connections for the rest; use `locationId` to consolidate "same physical position" semantics. |
| [Reading values from an optimization result](readoutresult_05/ReadOutResult.md) (`readoutresult_05`) | **Read out results (routes, stops, costs, violations).** Learn how to turn results into a report/table: iterate routes and route items (stop sequence); extract route/job cost, time, distance; read violations on route level and per node. |
| [Event nodes for tasks without a location](eventnode_06/EventNode.md) (`eventnode_06`) | **Model non-geo work items.** Learn `EventNode` for "tasks without locations" (calls/meetings): model "work items" like calls/meetings that consume time but not travel distance; combine `EventNode` and geo nodes in one plan; export to KML (where applicable). |
| [Pillar nodes with hard time windows](pillar_07/Pillar.md) (`pillar_07`) | **Model contractual SLAs as hard constraints (pillar nodes).** Learn how nodes "flow around" pillars: model strict SLAs as *hard constraints*; understand how normal nodes "flow around" a pillar appointment; attach a mandatory resource when needed. Critical concept: a pillar's time window is enforced by the search, not by a cost weight. |
| [Set a license from a file](setlicense_08/SetLicense.md) (`setlicense_08`) | **Set up licensing for real scenarios.** Learn file-based and JSON-based licensing: how licensing works and when it is required; set license as JSON string or from file; recommended secure deployment patterns. |

## Run the examples

**Option A: run from your IDE (recommended).** Import the repository as a Maven project. Run the `main(...)` method of the chosen example. You need a JDK in the version that the examples pom sets.

**Option B: use the sandbox (Docker).** If you want to avoid local Java/Maven/IDE setup, the repository provides a browser-based sandbox described in the [repo-level README](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/README.md). The [sandbox quickstart](https://www.dna-evolutions.com/docs/getting-started/quickstart/jopt_sandboxes_quickstart) describes the sandbox images.

**The license.** Without a license file, the examples use the public evaluation license through `ExampleLicenseHelper`. To use your own license, save it as `src/main/resources/license/YOUR_LICENSE.jli`; [Set a license from a file](setlicense_08/SetLicense.md) explains how, and the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

**The order in io_03.** `LoadOptimizationFromJsonExample`, `LoadOptimizationToFreshRunExample`, `LoadOptimizationFromJsonAndReassignNodes` and `LoadOptimizationFromJsonAndReassignPillar` read the file `myopti.json.bz2`, which `SaveOptimizationToJsonExample` writes. Run that example first.

### Good advice before you start

- **Keep the model small first.** Validate feasibility and modeling correctness before increasing problem size.
- **Always watch progress/status/errors during development.**
- **Separate hard constraints from costs.** JOpt hard constraints (e.g., **pillar nodes**) are enforced by the search.

### Troubleshooting

**The optimization terminates immediately.** If you start asynchronously and do not block the JVM, the run may terminate early. In CLI examples, you typically see a `future.get()` (or similar) to keep the process alive.

**Distance and time differ from Google Maps.** If you do not supply external edges, fallback connectors are used. Review:
- [BackupConnector distance/time model](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times)

**I need deterministic behavior for tests.**
- fix your properties (where applicable),
- keep input ordering stable,
- persist snapshots (`io_03`) to reproduce exact runs.

## Related

The examples are organized into major example categories (hosted on GitHub). Each section has its own README. This README focuses on the **Base** examples, in the folder `basic`.

- [The basic folder](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic): the folder of the Base examples.
- [Advanced](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced): the folder of the Advanced examples, with its README.
- [Expert](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert): the folder of the Expert examples, with its README.
- [RESTful](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful): the folder of the REST examples, with its README.
- [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization): the steps of the first example, explained one by one.
- [Analyzing the result](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result): how to read the result of a run.
- [Special features: conflict of a pillar node with a node](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#case-five-conflict-of-a-capturednode-with-a-node-solved-by-removing-a-capturednode): a conflict between a CapturedNode and a node, solved by removing the CapturedNode.
- [License documentation](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the license JSON, the element limits and how to set a license.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference.
- [Official documentation hub](https://www.dna-evolutions.com/docs/getting-started/home/introduction): the introduction to the documentation.
- [Special features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features): the overview of the special features of JOpt.
- [Public JavaDocs](https://public.javadoc.dna-evolutions.com): the API documentation of JOpt.
- [Public Nexus repository](https://nexus.dna-evolutions.net/): the Maven repository with the JOpt artifacts.
- [RELEASE_NOTES.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/RELEASE_NOTES.md): the release notes of this repository.
- [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md): the change log of this repository.
- [Contact](https://www.dna-evolutions.com/contact): the contact form of DNA Evolutions, or [info@dna-evolutions.com](mailto:info@dna-evolutions.com).

---

Added: 2020-05-27 (JOpt core 7.4.2) | Last updated: 2026-10-20
