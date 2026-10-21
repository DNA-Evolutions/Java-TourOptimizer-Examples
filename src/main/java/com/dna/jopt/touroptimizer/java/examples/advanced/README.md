# Advanced examples

This package contains the Advanced examples, feature-focused examples that show single features of JOpt.TourOptimizer for Java, mostly one feature per package: conditions on nodes and resources, special time windows, open routes, pickup and delivery, progress and results during a run. The intent is to help you identify which of your real-world business use cases can be covered by the product features and how to model them cleanly. They are for Java developers who have built a first optimization; if you have not, start with the [Basic examples](../basic/README.md). There is no required order: you can start with the feature that matches your current problem class and then branch out into adjacent topics (skills, territories, relations, PND, etc.).

| Example | What you learn | Feature guide |
|---|---|---|
| [Alternate destination at the end of a route](alternatedestination/AlternateDestinationExample.md) (`alternatedestination`) | Ends every route of a resource at another place than its start, while each working day starts at the start position again. | |
| [AutoFilter for late visits](autofilter/AutoFilter.md) (`autofilter`) | Lets the AutoFilter take nodes that are visited late too often out of the run, for all nodes or for one node: automatic filtering, during the run, of nodes that are very likely infeasible (time windows, skills, etc.) to speed up optimization. | [AutoFilter](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter), [Special features: AutoFilter](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#autofilter) |
| [Build an optimization config with builders](builderpattern/BuilderPatternExample.md) (`builderpattern`) | Builds the whole input as one immutable `OptimizationConfig` with builders and runs it with `JSONOptimization`. | |
| [Capacity check of the input](capacitycheck/CapacityCheckExample.md) (`capacitycheck`) | Shows how the capacity check rejects an input whose visits need at least twice the time that the working hours offer, before a run. | |
| [Clustering construction examples](clustering/Clustering.md) (`clustering`) | Builds the start solution with the clustering construction, for cities across Germany and for many nodes in one city; one class keeps the clusters as hard zones during the optimization. The three classes need a full license. | [Clustering during construction](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction) |
| [CO2 emission as an optimization goal](co2emission/CO2EmissionOptimization.md) (`co2emission`) | Defines resource emission factors and makes the CO2 emission of each vehicle part of the cost with `JOptWeight.CO2Emission`, so that the long route goes to the vehicle with less emission. | [CO2 emission](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission) |
| [Mandatory, preferred, banned and unpreferred resources for a node](condition/BindingExcludingResourceCondition.md) (`condition`) | Binds a node to resources with Mandatory (hard) and Preferred (soft) constraints, and excludes resources with Banned (hard) and UnPreferred (soft) constraints, in `MandatoryResourceConditionExample`, `PreferredResourceConditionExample`, `BannedResourceConditionExample` and `UnPreferredResourceConditionExample`. | |
| [Skills with types and expertise levels](condition/ResourceType.md) (`condition`) | Skills as types on resources and nodes, matched as a hard or a soft constraint, with expertise levels and cost models that rank the resources that meet a level, and in `SelectiveTypeConditionExample` with other types on each working day of one resource. | [Skill with expertise and cost model](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/skill_costmodel) |
| [Skill levels and cost models with BitType](condition/bittype/BitTypeWithExpertiseConditionAndCostModelExample.md) (`condition.bittype`) | Skills with levels as BitTypes, and a cost model that ranks the resources that meet a requirement. | [BitType skills](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/bittype_condition) |
| [Magnetic soft condition between nodes](condition/magnetic/MagneticSoftCondition.md) (`condition.magnetic`) | A soft `MagnetoNodeConstraint`: one node attracts another onto its route, another node repels two nodes from its route. | [MagnetoCondition (MagnetoNodeConstraint)](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/magnetocondition) |
| [Connection time efficiency of a resource](connectionefficiency/ResourceConnectionEfficiency.md) (`connectionefficiency`) | A factor on the driving times of one resource (`setConnectionTimeEfficiencyFactor`), for a vehicle that drives faster or slower than the times of the connections assume; the distances stay the same. | |
| [Teams of resources with constraint alias ids](constraintaliasid/ResourceConstraintAliasId.md) (`constraintaliasid`) | Gives resources a team name with `setConstraintAliasId` and names the teams instead of the resources in the four resource constraint types, mandatory, preferred, banned and unpreferred, one class each; once set, the alias replaces the resource id in the lookup. | |
| [Extra info on nodes and resources](extrainfo/ExtraInfo.md) (`extrainfo`) | Stores your own objects as JSON in the extra info of nodes and resources and reads them back from the result. | |
| [First and last node of a route](firstlastnode/FirstLastNode.md) (`firstlastnode`) | Asks for one node as the first and another as the last node of its route, with soft settings using importance values, not hard fixing. | |
| [Opening hours that bound only the arrival](includevisitduration/IncludeVisitDuration.md) (`includevisitduration`) | Lets the opening hours of a node bound only the arrival, so that the visit may end after them (arrival windows vs finish-within windows: include visit duration into time windows). | |
| [Shorter visits for nodes at the same place](jointvisitduration/JointVisitDuration.md) (`jointvisitduration`) | A joint visit duration: a shorter visit for a node that directly follows a node at the same place. | |
| [Running the genetic evolution stage in a loop](looprun/RunOptimizationInLoop.md) (`looprun`) | Runs the genetic evolution stage in continuous mode and ends it after three loops with `jumpToNextStage()`, to implement external convergers or stop criteria. | [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) |
| [Open and closed routes](openclosedroute/OpenClosedRoute.md) (`openclosedroute`) | Open routes, which end at their last node instead of at the start position of the resource. | |
| [Plus Codes instead of latitude and longitude](openlocation/OpenLocationCode.md) (`openlocation`) | Converts positions to Plus Codes ([Open Location Codes](https://maps.google.com/pluscodes/)) and back, and builds an optimization from Plus Codes. | |
| [Overnight stays on a tour over several days](overnightstay/OvernightStay.md) (`overnightstay`) | Lets a resource end its working days at stay nodes on a tour over several days instead of driving home, with a stay-out policy and limits for the stays. | [Overnight stay](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay) |
| [Performance mode examples](performancemode/PerformanceMode.md) (`performancemode`) | Switches on performance mode with `JOpt.PerformanceMode`, for faster optimization on easy/low-restriction instances, and prints the run time, to compare it with a run without performance mode. `PerformanceModeBigOptiExample` builds the setup of the evaluation in the feature guide, 2000 nodes and 40 resources, and needs a full license. | [Performance mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode) |
| [Pickup and delivery examples](pickupanddelivery/PND.md) (`pickupanddelivery`) | An overview of the pickup and delivery (PND) classes, one line each: simple and fuzzy loads, capacity factors, a depot capacity shared by two goods, flex loads, manufacturing planning, unloading at optional dumps, timed loads and the depot reports. | [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery) |
| [A depot capacity shared by two goods](pickupanddelivery/PNDDifferentTotalCapacityUtilization.md) (`pickupanddelivery`) | Two goods with their own maximums share the total capacity of the depot of a truck, so the goods on board decide the order of the visits. | [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery) |
| [Progress output frequency and progress requests](progressfrequency/OptimizationProgress.md) (`progressfrequency`) | Sets the step between two progress events of the simulated annealing and the genetic evolution stage with two properties, and asks the running stage for progress events from a timer with `requestProgress()`. | [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) |
| [Reading the full progress of a run](readoutfullprogress/ReadOutFullProgress.md) (`readoutfullprogress`) | Prints the cost, counts, times and distance of the best solution so far from each progress event, read from the solution itself and with the helper class `ParsedProgress`. | [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) |
| [Relations between nodes](relationship/Relations.md) (`relationship`) | Ties two nodes together with visitor relations (same route, same resource, different routes, different visitors) and time relations (a window for the start of one visit, counted from the start of the other), also combined. A broken relation is a pseudo constraint: it adds a cost weighted by `JOptWeight.Relationships`. | |
| [Requesting a result during the run](requestresult/RequestResult.md) (`requestresult`) | Requests the best solution so far with `requestResult()` while the run goes on and receives it on `requestedResultSubject()`; at the JSON level, `requestAsynchConfigObservable` does both. | [Reactive events and streams](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/reactive_events) |
| [Return to start after a node](returnstart/ReturnStart.md) (`returnstart`) | Sends the resource back to its start location after the visit of a node, set per node with `setIsReturnStart`, for a stay of a given duration before it drives on. | [Return to start](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/return_to_start) |
| [Visit duration efficiency of a resource](visitdurationefficiency/ResourceVisitDurationEfficiency.md) (`visitdurationefficiency`) | A factor on the visit durations of one resource (`setOverallVisitDurationEfficiencyFactor`), for faster or slower resources, here a resource that is faster at the visits, with minimal visit durations and nodes whose visit duration stays fixed. | |
| [Wait on early arrival at the first node](waitonearlyarrival/WaitOnEarlyArrivalFirstNode.md) (`waitonearlyarrival`) | Lets a resource start work at once when it reaches a node before it opens, except at the first node of a route, where it waits; the result reports an early start as an EARLY violation with a cost. | |
| [Territories with zone numbers and UK postcodes](zonecode/ZoneCode.md) (`zonecode`) | Gives each working day of a resource its own territory as a hard constraint, with zone numbers for cities in Germany and with UK postcode districts around Birmingham; a node joins a second zone with `addExtraCode`. | [ZoneCodes and territories](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes), [Defining territories via ZoneCodes](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features#defining-territories-via-zonecodes) |
| [Bridge and tunnel crossing between zones](zonecrossing/BridgeTunnelCrossingZoneNumberConstraint.md) (`zonecrossing`) | Adds a cost to every route step from a node of one zone to a node of another, such as across a river, so that each route stays on one side. | [Zone crossing penalization](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing) |

## Run the examples

**Option A: run from your IDE (recommended).** All examples are plain Java classes with a `main(...)` method (or an equivalent entrypoint), and the typical ways to run one share these steps: open the repository in your IDE (e.g., IntelliJ, Eclipse etc.), import it as a Maven project, navigate to an example package under `.../examples/advanced/...` and run the `main(...)` method of the chosen example class. You need a JDK in the version that the examples pom sets.

**Option B: use the sandbox (Docker).** The repository provides a browser-based sandbox, described in the [repo-level README](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/README.md). The [sandbox quickstart](https://www.dna-evolutions.com/docs/getting-started/quickstart/jopt_sandboxes_quickstart) describes the sandbox images.

**The license.** Without a license file, the examples use the public evaluation license through `ExampleLicenseHelper`. The clustering examples and `PerformanceModeBigOptiExample` have more elements than that license allows and need a full license; the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

**Events and results.** Every example that runs an optimization subscribes to the progress, warning, status and error events of the run before it starts and waits for the result with a timeout, apart from `PNDTimedLoadExample`, `PNDTimedPizzaDeliveryExample`, `PickupAndDeliveryExample` and `PickupAndDeliveryUnloadAllExample`; [Recommended ways to run an optimization](../basic/recommendedimplementation_02/RecommendedImplementation.md) explains this pattern and the legacy callback methods. If you are integrating into services or UIs, these subscriptions to the optimization event streams are the recommended entry points for telemetry and control flows.

## Choosing an example

Packages for some common questions; the table above links their pages.

- **Who visits a node:** `condition` (mandatory, preferred, banned and unpreferred resources, skills as types), `condition.bittype` (skills as bit types), `constraintaliasid` (teams), `zonecode` (territories).
- **Which nodes share a route:** `relationship` (visitor and time relations between two nodes, as pseudo constraints) and `condition.magnetic` (soft magnets).
- **When a node is visited and for how long:** `includevisitduration`, `jointvisitduration`, `waitonearlyarrival`, `visitdurationefficiency`.
- **How fast a resource drives:** `connectionefficiency` (a factor on the driving times of one resource).
- **Where a route starts and ends:** `openclosedroute`, `alternatedestination`, `firstlastnode`, `returnstart` (returns to the start after a node), `overnightstay`.
- **What a vehicle carries:** `pickupanddelivery`.
- **Nodes that cannot be served in time:** `autofilter` takes nodes that keep violating out of the run; `capacitycheck` rejects an input whose visits need at least twice the time of the working hours, before the run.
- **How the run goes:** `performancemode`, `clustering`, `looprun`, `progressfrequency`, `readoutfullprogress`, `requestresult`.

## Guidance on selecting the right feature

### If your main pain is runtime
- Start with **AutoFilter** (remove infeasible nodes early) and **Performance Mode** (reduced genetic operator set).
- If your instance grows large, evaluate **Clustering construction** for better initial solutions and faster convergence.

### If you need strong business rules (structural feasibility or pseudo constraints)
- Use **Relations** for coupling nodes across route/resource/time (a pseudo constraint: a broken relation adds a cost weighted by `JOptWeight.Relationships`).
- Use **ZoneCodes** for territories (hard or soft) and **ZoneCrossing** to discourage expensive boundary crossings.
- Use **Return2Start** or **OvernightStay** to model operationally mandatory depot returns or multi-day tours.

### If you optimize logistics flows, not just routes
- Use **Pickup & Delivery (PND)** when feasibility depends on load state (underload/overload), timed transport, or production planning.

## Notes on documentation files

For many packages, this repository includes a dedicated Markdown document placed alongside the example sources. These documents are intended to be copied into other environments and provide:
- conceptual explanation of the feature,
- implementation guidance and modeling patterns,
- links back to the corresponding Java example(s) and official documentation.

If you are maintaining an internal knowledge base, these Markdown files can be used directly as reference pages.

## Related

- [Basic examples](../basic/README.md): the overview of all Basic examples.
- [Advanced package root](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced): the folder of the Advanced examples and their sources on GitHub.
- [Expert](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert): the folder of the Expert examples, with its README.
- [Feature guides](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides): the guides to the features that these examples show.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference.
- [License guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the license JSON, the element limits and how to set a license.
- [Documentation Hub introduction](https://www.dna-evolutions.com/docs/getting-started/home/introduction): the introduction to the documentation.

---

Added: 2020-05-30 (JOpt core 7.4.2) | Last updated: 2026-10-21
