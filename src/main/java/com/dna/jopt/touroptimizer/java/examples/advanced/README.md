# Advanced examples

The Advanced examples show single features of JOpt.TourOptimizer for Java, mostly one feature per package: conditions on nodes and resources, special time windows, open routes, pickup and delivery, progress and results during a run. They are for Java developers who have built a first optimization; if you have not, start with the [Basic examples](../basic/README.md). There is no required order: start with the feature that matches your problem.

| Example | What you learn | Feature guide |
|---|---|---|
| [Alternate destination at the end of a route](alternatedestination/AlternateDestinationExample.md) (`alternatedestination`) | Ends every route of a resource at another place than its start, while each working day starts at the start position again. | |
| [AutoFilter for late visits](autofilter/AutoFilter.md) (`autofilter`) | Lets the AutoFilter take nodes that are visited late too often out of the run, for all nodes or for one node. | [AutoFilter](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/autofilter) |
| [Build an optimization config with builders](builderpattern/BuilderPatternExample.md) (`builderpattern`) | Builds the whole input as one immutable `OptimizationConfig` with builders and runs it with `JSONOptimization`. | |
| [Capacity check of the input](capacitycheck/CapacityCheckExample.md) (`capacitycheck`) | Shows how the capacity check rejects an input whose visits need at least twice the time that the working hours offer, before a run. | |
| [Clustering construction examples](clustering/Clustering.md) (`clustering`) | Builds the start solution with the clustering construction, for cities across Germany and for many nodes in one city; one class keeps the clusters as hard zones during the optimization. The three classes need a full license. | [Clustering during construction](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/clustering_construction) |
| [CO2 emission as an optimization goal](co2emission/CO2EmissionOptimization.md) (`co2emission`) | Makes the CO2 emission of each vehicle part of the cost with `JOptWeight.CO2Emission`, so that the long route goes to the vehicle with less emission. | [CO2 emission](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/co2_emission) |
| [Mandatory, preferred, banned and unpreferred resources for a node](condition/BindingExcludingResourceCondition.md) (`condition`) | Binds a node to resources with Mandatory (hard) and Preferred (soft) constraints, and excludes resources with Banned (hard) and UnPreferred (soft) constraints, in `MandatoryResourceConditionExample`, `PreferredResourceConditionExample`, `BannedResourceConditionExample` and `UnPreferredResourceConditionExample`. | |
| [ResourceType (skills): hard and soft matching, expertise levels and cost models](condition/ResourceType.md) (`condition`) | Skills as types on resources and nodes: who may and who should visit a node, with expertise levels and cost models. | [Skill with expertise and cost model](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/skill_costmodel) |
| [Skill levels and cost models with BitType](condition/bittype/BitTypeWithExpertiseConditionAndCostModelExample.md) (`condition.bittype`) | Skills with levels as BitTypes, and a cost model that ranks the resources that meet a requirement. | [BitType skills](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/bittype_condition) |
| [Magnetic soft condition between nodes](condition/magnetic/MagneticSoftCondition.md) (`condition.magnetic`) | A soft `MagnetoNodeConstraint`: one node attracts another onto its route, another node repels two nodes from its route. | [MagnetoCondition (MagnetoNodeConstraint)](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/magnetocondition) |
| [Resource connection efficiency: fast and slow vehicles](connectionefficiency/ResourceConnectionEfficiency.md) (`connectionefficiency`) | A factor on the driving times of one resource (`setConnectionTimeEfficiencyFactor`), for vehicles that are faster or slower than the others on the same connections. | |
| [ResourceConstraintAliasId: several resources as one unit in constraints](constraintaliasid/ResourceConstraintAliasId.md) (`constraintaliasid`) | Constraint alias ids to model teams and group-based mandatory assignment rules. | |
| [Extra info on nodes and resources](extrainfo/ExtraInfo.md) (`extrainfo`) | Stores your own objects as JSON in the extra info of nodes and resources and reads them back from the result. | |
| [First and last node of a route](firstlastnode/FirstLastNode.md) (`firstlastnode`) | Asks for one node as the first and another as the last node of its route, with soft settings. | |
| [Opening hours that bound only the arrival](includevisitduration/IncludeVisitDuration.md) (`includevisitduration`) | Lets the opening hours of a node bound only the arrival, so that the visit may end after them. | |
| [Shorter visits for nodes at the same place](jointvisitduration/JointVisitDuration.md) (`jointvisitduration`) | A joint visit duration: a shorter visit for a node that directly follows a node at the same place. | |
| [RunOptimizationInLoop: stopping a solver stage after N loops](looprun/RunOptimizationInLoop.md) (`looprun`) | Runs optimization stages in loops to implement external convergers or stop criteria. | |
| [Open and closed routes](openclosedroute/OpenClosedRoute.md) (`openclosedroute`) | Open routes, which end at their last node instead of at the start position of the resource. | |
| [Plus Codes instead of latitude and longitude](openlocation/OpenLocationCode.md) (`openlocation`) | Converts positions to Plus Codes (Open Location Codes) and back, and builds an optimization from Plus Codes. | |
| [OvernightStay: multi-day routes with stay-out policies](overnightstay/OvernightStay.md) (`overnightstay`) | Multi-day routes with overnight stays ("stay out") and stay policies. | [Overnight stay](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/overnight_stay) |
| [PerformanceMode: genetic optimization with fewer operators](performancemode/PerformanceMode.md) (`performancemode`) | Performance mode for faster optimization on instances with few restrictions. | [Performance mode](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/performance_mode) |
| [Pickup & Delivery (PND) in JOpt.TourOptimizer](pickupanddelivery/PND.md) (`pickupanddelivery`) | Pickup and delivery (PND): classic PND, fuzzy fulfillment, timed loads, flex loads, manufacturing planning and report extraction. | [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery) |
| [Different Total Capacity Utilization: `PNDDifferentTotalCapacityUtilization`](pickupanddelivery/PNDDifferentTotalCapacityUtilization.md) (`pickupanddelivery`) | A truck whose total capacity is shared by two load capacities with different limits. | [Pickup and delivery](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery) |
| [OptimizationProgress: progress callbacks and progress snapshots](progressfrequency/OptimizationProgress.md) (`progressfrequency`) | Progress reporting frequency and requesting progress snapshots at any time. | |
| [Read out full progress: structured KPIs during optimization](readoutfullprogress/ReadOutFullProgress.md) (`readoutfullprogress`) | Extracts the full progress KPIs from the progress object, by hand and with the `ParsedProgress` util. | |
| [Relations: coupling nodes across routes, resources and time](relationship/Relations.md) (`relationship`) | Node relations: same route or visitor, different route or visitor, and relative time window relations; they can be combined. | |
| [RequestResult: an intermediate solution while the optimization runs](requestresult/RequestResult.md) (`requestresult`) | Requests an intermediate best-so-far result while the optimization is still running. | |
| [Return to start (Return2Start): depot returns between visits](returnstart/ReturnStart.md) (`returnstart`) | Forces intermediate returns to the start position of the resource between visits, with a dwell time. | [Return to start](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/return_to_start) |
| [Resource visit duration efficiency: faster or slower resources](visitdurationefficiency/ResourceVisitDurationEfficiency.md) (`visitdurationefficiency`) | Faster and slower resources at the visit, minimum visit durations and nodes with a fixed duration. | |
| [Wait on early arrival (first node)](waitonearlyarrival/WaitOnEarlyArrivalFirstNode.md) (`waitonearlyarrival`) | Allows working at once on an early arrival, but makes the resource wait at the first node of a route. | |
| [ZoneCodes: territories for resources](zonecode/ZoneConde.md) (`zonecode`) | Territories through ZoneCodes and ZoneNumbers; territories can differ per working hours, and border nodes can have several codes. | [ZoneCodes and territories](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecodes) |
| [Bridge and tunnel crossing between zones](zonecrossing/BridgeTunnelCrossingZoneNumberConstraint.md) (`zonecrossing`) | Adds a cost to every route step from a node of one zone to a node of another, such as across a river, so that each route stays on one side. | [Zone crossing penalization](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/zonecrossing) |

## Run the examples

**Option A: run from your IDE (recommended).** Import the repository as a Maven project and run the `main(...)` method of the chosen example. You need a JDK in the version that the examples pom sets.

**Option B: use the sandbox (Docker).** The repository provides a browser-based sandbox, described in the [repo-level README](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/README.md). The [sandbox quickstart](https://www.dna-evolutions.com/docs/getting-started/quickstart/jopt_sandboxes_quickstart) describes the sandbox images.

**The license.** Without a license file, the examples use the public evaluation license through `ExampleLicenseHelper`. The clustering examples have more elements than that license allows; the [license guide: element limits](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license#free-license) lists what each license allows.

**Events and results.** Most examples subscribe to the progress, warning, status and error events of the run before it starts and wait for the result with a timeout; [Recommended ways to run an optimization](../basic/recommendedimplementation_02/RecommendedImplementation.md) explains this pattern and the legacy callback methods, which some examples use.

## Choosing an example

Packages for some common questions; the table above links their pages.

- **Who visits a node:** `condition` (mandatory, preferred, banned and unpreferred resources, skills as types), `condition.bittype` (skills as bit types), `constraintaliasid` (teams), `zonecode` (territories).
- **Which nodes share a route:** `relationship` (hard relations) and `condition.magnetic` (soft magnets).
- **When a node is visited and for how long:** `includevisitduration`, `jointvisitduration`, `waitonearlyarrival`, `visitdurationefficiency`.
- **Where a route starts and ends:** `openclosedroute`, `alternatedestination`, `firstlastnode`, `returnstart`, `overnightstay`.
- **What a vehicle carries:** `pickupanddelivery`.
- **Nodes that cannot be served in time:** `autofilter` takes nodes that keep violating out of the run; `capacitycheck` rejects an input whose visits need at least twice the time of the working hours, before the run.
- **How the run goes:** `performancemode`, `clustering`, `looprun`, `progressfrequency`, `readoutfullprogress`, `requestresult`.

## Related

- [Basic examples](../basic/README.md): the overview of all Basic examples.
- [Expert](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert): the folder of the Expert examples, with its README.
- [Feature guides](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides): the guides to the features that these examples show.
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties): the property reference.
- [License guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/license): the license JSON, the element limits and how to set a license.
- [Documentation Hub introduction](https://www.dna-evolutions.com/docs/getting-started/home/introduction): the introduction to the documentation.

---

Added: 2020-05-30 (JOpt core 7.4.2) | Last updated: 2026-10-20
