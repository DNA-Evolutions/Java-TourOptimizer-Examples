# Changelog for Examples and underlying JOpt Library


Summarized changelogs for this example repository and the underlying JOpt Library:

* [Changelog of this example repository](#changelog-examples)
* [Changelog of JOpt Library](#changelog-jopt-library)

Explanation of keys:
- **Deprecated:** Remove, or mark a deprecated method/class
- **Feature:** Add new code or a new behaviour
- **Fix:** Resolve a bug
- **Improvement:** Improve existing code
- **Update:** Update/add a dependency

## Changelog Examples

**v7.4.7-rc5**
- Update: Refactor PND-Depot method "getTypeName()" to "typeName()"; 
- Feature: Add examples for CO2 emission and Return2Start
- Improvement: Add dockerhub and linkedin channel link
- Improvement: Add sandbox tutorial youtube link
- Feature: Add builder pattern example
- Improvement: Improve PickupAndDeliveryExample.java by setting meaningful initial load values


**v7.4.7-rc4**
- Improvement: Different README modifications
- Feature: Add restful examples
- Fix: Missing slash in the sandbox example call
- Feature: Add 7.4.7-rc3 legacy dll

**v7.4.7-rc3**
- Fix: Fix pipeline for automated sandbox docker build
- Feature: Add java sandbox
- Improvement: Remove non-used imports in examples

**v7.4.7-rc2**
- Fix: Set Route id in CustomSolutionExample to avoid bad entity
- Improvement: ExampleLicenseHelper also accepts a licensing path

**v7.4.7-rc1**
- Feature: Add new examples for Clustering and OpenLocation
- Improvement: Big Example Review
- Improvement: Add introduction video
- Improvement: Add download link for legacy dll

**v7.4.6**
- Feature: Add BackupConnector and UncaughtException Handling examples

**v7.4.5-rc10**
- Improvement: Match example for PND with documentation
- Feature: Add examples for different route relation

**v7.4.5-rc9**
- Feature: Add new examples for FirstLastNode, WaitOnEarlyArrivalFirstNode, and several Node Relations

**v7.4.5-rc8**
- No entry

**v7.4.5-rc7**
- No entry

**v7.4.5-rc6**
- Feature: Adding example for ParsedProgress

**v7.4.5-rc5**
- Feature: Examples for TypeWithExpertise, ResourceVisitDuration Efficiency, and ReadOutFullProgress
- Improvement: Add YouTube channel
- Improvement: Change example to match better with documentation site
- Improvement: Match example for PND with documentation
- Improvement: Multiple README improvements

**v7.4.5-rc4**
- Feature: Add example for PNDReportExtraction

**v7.4.5-rc3**
- Feature: Add more examples for Pick up and Delivery, NodeRelations, ZoneNumbers, loaded Optimization as "fresh run", andOptimizationSchemeAlgorithmSelection
- Improvement: README improvements
- Deprecate: Old legacy PND system
- Update: Migrate to log4j2

**v7.4.5-rc2**
- Improvement: README improvements
- Improvement: Improve examples for new PND system

**v7.4.5-rc1**
- Feature: Add initial examples for new Pick up and Delivery system, LoadOptimizationFromJSON and related examples, Progress frequency example, KML export example
- Improvement: README improvements

**v7.4.4**
- Feature: Add more examples for RunOptimizationInLoop
- Feature: Starting repository with a stack of Examples


## Changelog JOpt Library

**v7.4.7-rc5**
- Improvement: Use graceful late-filtering in combination with 2Opt-Optimization and violation revalidation to improve filtering behavior
- Fix: Fix vulnerability in commons-compress by upgrading to version 1.21
- Fix: Check null in genetic evolution before accessing cost of entity and make RouteClusterController synchronized
- Improvement: Refactor schema definitions to allow better swagger TourOptimizer support
- Improvement: Give direct access to SolutionHeader creation
- Improvement: Improve JSON handling
- Improvement: Add non-generic wrapper for OptimizationConfig
- Improvement: Let nodes be in a "real" violation state as a precondition for auto-filtering
- Feature: Margin for pillar lateness
- Improvement: Also return elements via "getAllElements()" if Optimization was not started yet
- Fix: Fix bug where asynchronous execution logic is partially executed on every iteration
- Improvement: Add new correction logic for Resources with too many dutyHours on custom solution usage
- Improvement: Set "50" as default value for "JOptWeight.Capacity"
- Feature: Add CO2 calculation
- Fix: Protect internal connection table from concurrent access
- Feature:  Add "overload" capacity filtering
- Feature: Add late and early violation detection for forcefilter
- Fix: Add asynchronous request with external timeout and remove synchronous block to fix issue of concurrent save and request result
- Fix: Do not create initial generation in genetic evolution before run really starts as the loaded snapshot data can change
- Fix: Avoid oneOf annotation by using DiscriminatorMapping

**v7.4.7-rc4**
- Fix: Avoid using "oneOf" in schema definition, as OpenApiGenerator can't handle it correctly.
- Improvement: Allow access to JsonConig

**v7.4.7-rc3**
- Feature: Draft for Return2Start
- Improvement: JSON handling improvements to reduce the file size of snapshots
- Fix: Objective tuning can modify properties without being actively used
<!--- - Fix: Add new flavor to some algorithms, that were used incorrectly as Construction Algos -->
<!--- - Fix: RouteStart is reset because of wrong Resource detachement -->
- Update: Immutables, Jackson, RxJava
- Update: Add Dependency for Swagger-Annotations
- Fix: Duplicate element message is thrown after loading from snapshot
<!--- - Fix: Placeholder is not taking up flextime -->


**v7.4.7-rc2**
- Improvement: Disable working hours comparison in the clustering algorithm
- Fix: Protected nodes are unassigned by AutoFilter when using gracefulFiltering
- Feature: Add setting to allow optionally execute graceful filtering in AutoNodeFilter
- Improvement: Increase elite-population to 20% by default.
- Improvement: Improve InduecIdleTimeOperator for better handling of start-start, end-end TimeWindow-NodeRelations 
- Fix: Potential NullPointer exception when using PND
<!--- - Mark all pillars in inactive routes as non-optimizable -->
- Feature: Degraded Capacity
- Improvement: Add helpers in the Optimization class for extracting elements
- Improvement: Improve stability of the current best solution. In case another solution has an equal cost, previously solutions might randomly exchange during the run
- Fix: Nodes connections are not correctly reset during loading from snapshot
- Fix: Add check for unintended zero geographical positions
- Deprecated: Remove some deprecated constructors in TimeWindowGeoNode
- Fix: Nullpointer when loading some old snapshots
<!--- - Add new constructor to override iterations in GeneticEvolution -->


**v7.4.7-rc1**
- Feature: OpenLocation support
- Fix: Sometimes, Resources aren't correctly extracted in Route 
<!--- - Add check for real geo-location for target elements during one2Many request building -->
- Improvement: Add helpers in the Optimization class for extracting elements
- Fix: Set a default title for kml exports as otherwise the kml is corrupt and can not be loaded with google maps
- Improvement: Add a warning about unique working hours
- Improvement: Improve "inner-city" clustering
- Improvement: Add a method to find non-distinct workingHours
- Fix: Avoid NullPointer when NodeUnassigner is not present
- Feature: Add a property for "autoZoneCode" creation of found clusters
- Fix: Wrong calculation of angle between elements in the clustering algorithm
- Fix: Solve a vulnerability of dependency Commons-compress by upgrading to version 1.19
- Feature:  Add a threshold for collective punishment based on the importance of nodes
- Fix: BaseVisitDuration is returned instead of JointVisitDuration 
- Improvement: JointVisitDuration can get affected by resource efficiency
- Improvement: TimeWindow provides a helper method to return ZonedDateTime for start and end
<!--- - Add property to suppress route costs of finalized/inactive routes -->
<!--- - Add external provider logic to separate Invoker class and use is pillar dropping -->
- Improvement: Improve external connection provider

**v7.4.6**
- Improvement: Improve performance by only using ClusterSignle2OptOperator for long routes
- Improvement: Improve performance by deactivating AutoFilter specific restrictions, if AutoFilter is not in use
- Fix: Solve Nullpointer when calculating Cluster Advantage


**v7.4.5-rc10**
- Feature: Add ability to use haversine formula in NodeConnector
- Fix: Change the scope of dependency log4j2 to provided to avoid warning messages in dependent projects
- Improvement: Add cluster insertion operator to improve cluster behaviour
- Improvement: Add cluster 2Opt operator to improve cluster behaviour
- Improvement: Add penalty cost-offset for distance
- Fix: Check that start of any TimeWindow is after 1970 (now > 0L)
- Feature: Add graceful filtering to AutoFilter in the context of JoinedVisitDuration
- Fix: LoadExchange is not correctly serialized during JSON creation
- Feature: Add ErrorConsumer in Optimization
<!--- - Improvement: Add method to explicitly tell what entity is the master for finalization -->
<!--- - Fix: Remove connections with protected ids is creating null pointer -->
- Update: Update dependency guava to 30.1
<!--- - Fix: Finalized routes can be accessed -->
- Improvement: Add a nonOptimizable flag to result
<!---  - Improvement: Consider previous lockdown time -->
- Improvement: Add method to extract Routes by Resources
- Improvement: Allow extracting hard violating nodes on Entity and Resource level
<!--- - Improvement: Save connection also during putConnection inside fromElement to avoid double connection calls -->
- Feature: Add forcedSameVisitor relation
<!--- - Improvement: Allow attaching costAssessorHelper and store lockdown time inside node -->

**v7.4.5-rc9**
- Fix: Multiple calls of putRelation2CostItem can lead to wrong costs when using Node relations
- Improvement: Suppress dead code in CostAssessment

**v7.4.5-rc8**
- Feature: Add forcedDifferentRoute relation
- Fix: Filter pillars with removed attached resource due to filtering after loading from snapshot

**v7.4.5-rc7**
- Feature: Add same route concept for Node relations
- Feature: Add waitOnEarlyArrivalFirstNode
- Feature: Add first/ last Node importance
- Improvement: Actively report when no nodes were unassigned during AutoFilter execution

**v7.4.5-rc6**
- Improvement: Add penalty and bonus concept for clustering
- Improvement: Allow joint nodes to reduce their routeTimeCost to enforce cluster behaviour
- Fix: Savings during Construction are not sorted correctly
- Improvement: In the case of joint nodes, allow to add them first during construction
- Improvement: Give higher savings when joint nodes are involved
- Improvement: Include acceptable overtime in picky Validator during construction
- Fix: Do not evaluate nodes that have the same unique id in likely connection analysis
- Fix: Single2Opt is working on a solution, instead of using a copy
- Improvement: Also unassign from subsequent nodes list
- Fix: By default, let Optimizer further optimize after the last strict filtering
- Fix: TimeExceeded is not tuned during Objective tuning
- Improvement: Let "poker for Nodes" be biased by neighbouring joint visit duration nodes and pre-sort nodes by importance
- Feature: Add a new property for JointVisitDurationMaxRadius
- Feature: Add weight for NodeImportance
- Fix: NodeConnectorTable is called too often when using an external connector

**v7.4.5-rc5**
- Feature: Add a new operator that is exchanging late high priority nodes with early low priority nodes
- Feature: Introduce relative lateness at node (a node is visited outside of the current visitors working hours)
- Improvement: In TypeWithExpertise, Penalty Models are checked against preferred resource priority
- Improvement: Constraints must report if they have members
- Fix: Prevent an error in case no resources are present in Cluster Construction algorithms 
- Improvement: Change default construction to use MultiAttempt Construction algorithm
- Improvement: Sort resources before construction/arranging
- Feature: Add resource efficiency feature
- Fix: Adding up different qualifications with expertise is not working on Resource level
- Fix: Casting problem in TypeWithExpertise
- Feature: Add weight for Expertise level preference
- Fix: Termination event-anchor is not triggering right maximal time violation
- Fix: Out of bounds exception in cluster construction
- Feature: Add cost models for TypeWithExpertise
<!--- - Fix: Also protect connection inside elements -->
- Fix: Always create a non-null EdgeConnector
<!--- - Feature: Add protected connection id feature -->
<!--- - Improvement: Clear seen connections of elements as well
- Improvement: Insertion operator avoids empty source routes
- Fix: Mark all routes actively as unassessed, as otherwise route cost can be calculated wrongly when no construction is in use
- Fix: Let the entity-arranger reset, to avoid false duplicate id messages
- Improvement: Add copy logic for TypeWithExpertise
<!--- - Add External connection request -->
- Improvement: Give element cluster the ability to save a likely connection map
- Fix: Non-unique deputy resource id during construction
- Fix: Zero priority setting in Resource Constraint is leading to JSON serialization saving error
- Fix: Exclude result-header in JSON in case route is unassessed
- Fix: Only attach JSON-header if a solution is ready
- Feature: Enable headers in JSON-solution and remove null values in JSON
- Improvement: Remove null values in JSON to reduce the file size
<!--- - Fix: Connected node on startup can't be removed -->
- Fix: Force filtering is not triggering onNodeFitlering event of related nodes
- Improvement: Let AutoFilter also report on nodes that got unassigned due to Node2NodeRelation
- Feature: Draft add multiRoute2Operator
<!--- - Add WrappedOperatorAlgo and WrappedOperatorPostStepManager  -->
<!--- - Add OptimizationCutter  -->
- Fix: Reset nodes working hours to original working hours after construction algorithm is done
<!--- - Enable job violation creation on request level -->
- Fix: Broken node relations can break JSON serialization
- Fix: Driving time becomes zero after snapshot loading
- Improvement: Use fakeHours concept during clustering to achieve a better preferred day matching
- Fix: The wrong element is casted in NodeConnector
- Fix: Prevent node connections that are part of non-optimizable elements from being recalculated
- Improvement: Limit weight value for relationship and duration of an induced idle event during the simulated annealing phase
- Improvement: Add check for overlapping timeWindows
- Feature: Stream properties of the library via status

**v7.4.5-rc4**
- Improvement: Make PillarEventNodes explicitly location-less
- Fix: Compatibility for Java 11 and higher

**v7.4.5-rc3**
- Improvement: Induced idle events are only created if bigger than 2 seconds and avoid multiple unnecessary events
- Feature: Choose construction algorithm as part of OptimizationScheme
- Update: Migrate to log4j2
<!--- - Add algorithm for construction that tries to identify "bad nodes" after construction and reassigns them -->
<!--- - Add algorithm for construction purely based on reassignment -->
<!--- - Allow InducedIdleEvents to be created on request -->
<!--- - Add distancePatternCost to advantage calculation -->
<!--- - Fix bug, were non-started (just loaded optimization) cannot be used for comparison -->
- Feature: Allow setting 2-Opt-Threshold
- Improvement: For SequentilaSpaceSavings Algorithm allow re-assignment instead of creating "trash route" with leftover-nodes
- Fix: Route id is not set correctly in Cluster-Construction
- Feature: Add utility for reassignment of nodes during construction
- Fix: Add null check during pillar dropping to avoid exception
- Feature: In OptimizationImporter, allow  to use an entity only as data provider ignoring an existing solution.
- Feature: Enable RelationModes for NodeRelation
- Fix: Allow the creation of idle events within the same route when the minimal deviation is positive
- Improvement: Add error-codes for pillars positioned before the existing first none-optimizable pillar
- Feature: Add new Pick up and delivery system


**v7.4.5-rc2**
- Fix: Loads can get unintendedly modified during route evaluation
- Fix: PillarDropper is not reporting all drop-conflicts
- Fix: Potential null pointer when a route has no optimizable elements in PillarDropper
<!--- - Add method to create generic TestObserver
<!--- - Set routes to be unassessed after activation attempt was made -->
<!--- - Distinguish between locked down and inactive routes -->
- Fix: Protect optimization from being started multiple times
<!--- - Check if node is moving due to injected pillar (lockdown case) -->
<!--- - Fix a rounding bug where pillars were not identified to be in the past -->
<!--- - Fix error with Start and EndAnchor  -->
<!--- - Do final assessment step after route finalization  -->
- Improvement: Add error-codes for pillar dropping
- Fix: Optimization can't be serialized if not start yet or done within subscription methods
<!--- - Add method for full injection during pillar dropping and make default behaviour -->

**v7.4.5-rc1**
- Fix: Saving to JSON from onPorgress subscription cannot be requested
- Fix: Saving to JSON via request is not working when the final result is provided
- Improvement: Provide completable future when requesting save to JSON
<!--- - Force request save -->
<!--- - Fix bug in modification tool -->
<!--- - PostStepManagers are responsible for performing synch -->
- Fix: Synch solution after each Algorithm execution to avoid strange results
<!--- - Fix: JSON serialization issue by adding additional synching step after poststepmanager executions -->
- Improvement: Replace error by warning in case a binding or excluding resource constraint is referencing a non-existing resource
- Fix: OnProgress count is not correct
- Feature: Adding adjustable onPorgress frequency and requestProgress call
- Fix: Avoid duplicate flags in the result