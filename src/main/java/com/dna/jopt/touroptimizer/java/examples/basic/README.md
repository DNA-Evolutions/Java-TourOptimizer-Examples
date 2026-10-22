# Basic examples

The Basic examples show how to model and run a tour optimization with JOpt.TourOptimizer for Java: properties, nodes, resources, connections, results and licensing. They are for Java developers who start with JOpt, before moving to the Advanced/Expert/RESTful sections. Read the [Basic elements tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements) first.

The number in each package name (`firstoptimization_01` to `setlicense_08`) indicates increasing complexity. You do **not** need to follow the numbering strictly, but we suggest working through them in this order.

| Example | What you learn |
|---|---|
| [First optimization example](firstoptimization_01/FirstOptimizationExample.md) (`firstoptimization_01`) | Builds and runs a complete optimization: properties, nodes, a resource, the events and the result. |
| [Recommended ways to run an optimization](recommendedimplementation_02/RecommendedImplementation.md) (`recommendedimplementation_02`) | The recommended run pattern with the event API, a synchronous run and the legacy callback methods. |
| [Load and save optimization snapshots](io_03/LoadAndSaveOptimization.md) (`io_03`) | Saves an optimization as a snapshot (`*.json.bz2`), also during a run, and loads it to continue, to start fresh or to add nodes. |
| [Load a snapshot and add a pillar](io_03/LoadOptimizationFromJsonAndReassignPillar.md) (`io_03`) | Loads a saved snapshot and adds a pillar node to its solution through reassignment. |
| [Export a result to a KML file](io_03/Export2KMLExample.md) (`io_03`) | Writes the result of a run to a KML file for a map viewer. |
| [Custom distances and driving times with a node connector](connection_04/ExternalNodeConnection.md) (`connection_04`) | Sets your own distances and driving times for some pairs of elements: directed connections, the fallback for the rest, shared location ids. |
| [Reading values from an optimization result](readoutresult_05/ReadOutResult.md) (`readoutresult_05`) | Reads routes, route items, costs and violations from a result and prints them as a report. |
| [Event nodes for tasks without a location](eventnode_06/EventNode.md) (`eventnode_06`) | Plans tasks without a location, such as calls, with `EventNode` among geo nodes. |
| [Pillar nodes with hard time windows](pillar_07/Pillar.md) (`pillar_07`) | Pillar nodes, whose time window must be met, bound to a resource or free; a pillar's time window is enforced by the search, not by a cost weight. |
| [Set a license from a file](setlicense_08/SetLicense.md) (`setlicense_08`) | Sets the license from a file, and the public evaluation license when there is no file. |

## Run the examples

**Option A: run from your IDE (recommended).** Import the repository as a Maven project. Run the `main(...)` method of the chosen example. You need a JDK in the version that the examples pom sets.

**Option B: use the sandbox (Docker).** If you want to avoid local Java/Maven/IDE setup, the repository provides a browser-based sandbox described in the [repo-level README](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/README.md). The [sandbox quickstart](https://www.dna-evolutions.com/docs/getting-started/quickstart/jopt_sandboxes_quickstart) describes the sandbox images.

**The license.** Without a license file, the examples use the public evaluation license through `ExampleLicenseHelper`. To use your own license, save it as `src/main/resources/license/YOUR_LICENSE.jli`; [Set a license from a file](setlicense_08/SetLicense.md) explains how, and the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

**The order in `io_03`.** `LoadOptimizationFromJsonExample`, `LoadOptimizationToFreshRunExample`, `LoadOptimizationFromJsonAndReassignNodes` and `LoadOptimizationFromJsonAndReassignPillar` read the file `myopti.json.bz2`, which `SaveOptimizationToJsonExample` writes. Run that example first.

### Tips for the first runs

- **Keep the model small first.** Validate feasibility and modeling correctness before increasing problem size.
- **Always watch the progress, status and error events while you develop:** subscribe to them, so that errors show during development; it accelerates debugging dramatically.
- **Separate hard constraints from costs.** JOpt hard constraints (e.g., **Pillar/Captured nodes**) are enforced by the search and satisfied by **architecture**, not by "huge penalties": for example, a pillar that cannot be reached stays unassigned and is never served late.

### Troubleshooting

**The optimization terminates immediately.** If you start asynchronously and do not wait for the result, the JVM ends before the run does. The examples wait with `resultFuture.get(5, TimeUnit.MINUTES)`, the synchronous one with `startRunSync(5, TimeUnit.MINUTES)`.

**Distance and time differ from a road map.** Without your own connections, JOpt calculates distances and times with the default backup connector; the [BackupConnector feature guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times) explains how and how to change it.

**Runs differ between tests.**
- fix your properties (where applicable); JOpt has no seed property to fix,
- keep input ordering stable,
- persist snapshots: a snapshot (`io_03`) lets a test start again from the same model and solution; the runs after it can still differ.

## `recommendedimplementation_02`: Execution patterns you should copy
What you learn:
- synchronous vs asynchronous execution
- safe lifecycle handling (don't terminate your JVM too early)
- callbacks and reactive-style event consumption

Start here after the first optimization:
- [recommendedimplementation_02 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02)

Recommended companion doc:
- [Recommended ways to run an optimization](recommendedimplementation_02/RecommendedImplementation.md)

## `io_03`: Save / load / resume (JSON and JSON.BZ2)
What you learn:
- export optimization state to compressed JSON (`*.json.bz2`)
- reload into an optimization instance
- start a fresh run from the loaded model (ignore old solution)
- checkpoint during a run (save at milestones)
- export result to KML for visualization

Package:
- [io_03 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03)

Recommended companion doc:
- [Load and save optimization snapshots](io_03/LoadAndSaveOptimization.md)

## `connection_04`: External node connections (distances / driving times)
What you learn:
- define directed edges (A->B is not implicitly B->A)
- provide partial matrices and rely on fallback connections for the rest
- use `locationId` to consolidate "same physical position" semantics

Package:
- [connection_04 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04)

Relevant documentation:
- [Basic elements (connections)](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)
- [BackupConnector: how distances/times are computed](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/backupconnector#description-how-jopttouroptimizer-calculates-distances-and-times)

Recommended companion doc:
- [Custom distances and driving times with a node connector](connection_04/ExternalNodeConnection.md)

## `readoutresult_05`: Read and analyze the result object
What you learn:
- iterate routes and route items (stop sequence)
- extract route/job cost, time, distance
- read violations on route level and per node

Package:
- [readoutresult_05 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05)

Tutorial section:
- [Analyzing the result](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result)

Recommended companion doc:
- [Reading values from an optimization result](readoutresult_05/ReadOutResult.md)

## `eventnode_06`: Nodes without geo locations
What you learn:
- model "work items" like calls/meetings that consume time but not travel distance
- combine `EventNode` and geo nodes in one plan
- export to KML (where applicable)

Package:
- [eventnode_06 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06)

Recommended companion doc:
- [Event nodes for tasks without a location](eventnode_06/EventNode.md)

## `pillar_07`: Pillar / Captured nodes (hard SLA constraints)
What you learn:
- model strict SLAs as *hard constraints*
- understand how normal nodes "flow around" a pillar appointment
- attach a mandatory resource when needed

Critical concept:
- A pillar is satisfied by **architecture**, not by "very high costs".

Docs:
- [Special Features: CapturedNode / Pillar](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#case-five-conflict-of-a-capturednode-with-a-node-solved-by-removing-a-capturednode)

Package:
- [pillar_07 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07)

Recommended companion doc:
- [Pillar nodes with hard time windows](pillar_07/Pillar.md)

## `setlicense_08`: Leaving free mode (license setup)
What you learn:
- how licensing works and when it is required (more than the elements that JOpt allows [without a license](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license))
- set license as JSON string or from file
- recommended secure deployment patterns

Docs:
- [License documentation](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license)

Package:
- [setlicense_08 (package)](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/setlicense_08)

Recommended companion doc:
- [Set a license from a file](setlicense_08/SetLicense.md)

## Related

- [Advanced](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced): the folder of the Advanced examples, with its README.
- [Expert](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert): the folder of the Expert examples, with its README.
- [RESTful](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful): the folder of the REST examples, with its README.
- [First optimization tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization): the steps of the first example, explained one by one, and [how to read the result](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization#analyzing-the-result).
- [Special features: conflict of a CapturedNode with a node](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#case-five-conflict-of-a-capturednode-with-a-node-solved-by-removing-a-capturednode): the case in which a pillar (CapturedNode) stays unassigned.
- [License guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the license JSON, the element limits and how to set a license.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference.
- [Documentation Hub introduction](https://www.dna-evolutions.com/docs/getting-started/home/introduction): the introduction to the documentation.
- [Public JavaDocs](https://public.javadoc.dna-evolutions.com): the API documentation of JOpt.
- [Public Nexus repository](https://nexus.dna-evolutions.net/): the Maven repository with the JOpt artifacts.
- [RELEASE_NOTES.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/RELEASE_NOTES.md): the release notes of this repository.
- [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md): the change log of this repository.
- [Contact](https://www.dna-evolutions.com/contact): the contact form of DNA Evolutions, or [info@dna-evolutions.com](mailto:info@dna-evolutions.com).

---

Added: 2020-05-27 (JOpt core 7.4.2) | Last updated: 2026-10-23
