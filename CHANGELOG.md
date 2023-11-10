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

**v7.5.0-rc5 - v7.5.0-rc8**
- Update: Changelog
- Update: Update version in Readme
- Feature: Add example for constraint alias id
- Feature: Add example for reading in spring JSON and running optimization with JOpt Java core
- Update: JOpt Java Core version

**v7.5.0-rc2 - v7.5.0-rc4**
- Update: Readme
- Update: JOpt Java Core version
- Update: Docker file from common base file for sandbox
- Update: Docker file for sandbox
- Update: Update code server version for sandbox
- Update: Changelog
- Update: FAQ

**v7.5.0-rc1**
- Update: Update version information
- Improvement: Add fallback to public license in ExampleLicenseHelper
- Update: FAQ
- Improvement: Readme
- Feature: Add example for saving Optimization to JSON string

**v7.5.0**
- Update: Update version information

**v7.4.9-rc6**
- Update: Update log4j-core version
- Feature: Add new example for custom default properties
- Update: Use JDK 17 in sandbox

**v7.4.9-rc5**
- No entry

**v7.4.9-rc4**
- Feature: Add new examples

**v7.4.9-rc3**
- No entry

**v7.4.9-rc2**
- Feature: Add export as JSON for Rest
- Improvement: Work on examples
- Feature: Add public JSON license
- Fix: Some README typos
- Feature: Add FAQ

**v7.4.9-rc1**
- No entry

**v7.4.8**
- Fix: Use subfolder by default for license location

**v7.4.7-rc7**
- Fix: Solve medium critical vulnerability in dependency in log4j-core by further bumping version to 2.17.1

**v7.4.7-rc6**
- Fix: Solve highly critical vulnerability in log4j-core by bumping version
- Feature: Add example for reading out headers

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

**v7.5.0-rc8**
- Feature: Per hour production cost
- Improvement: Improve SmartSimualtedAnnealing
- Update: Update the version of commons-compress to solve a vulnerability
- Improvement: Allow back-transformation (JSON to JOpt) of relations without related nodes and anchor details
- Feature: Distance divider feature for joint visit duration clusters
- Improvement: Check if overtime/overdistance increased before adding PILLAR_DROP_NOTOK_SOFT_ROUTEOVERTIME flag
- Fix: Fix a JSON loading bug when unlocated idle time is in use
- Fix: Avoid potential null-pointer in ShiftNodeOperator
- Update: Add SmartSimulatedAnnealingAlgo and make it the default
- Improvement: Improve PillarDropper

**v7.5.0-rc7**
- Feature: Add plugin manager
- Improvement: Improve CostAssessor
- Improvement: Improve PillarDropper
- Fix: Consider reduction time when choosing the opening hour index of a node to avoid bad hours selection

**v7.5.0-rc6**
- Fix: Fix obfuscation issue for cluster result object

**v7.5.0-rc5**
- Feature: Use settings object during pillar dropping to acknowledge soft constraints if desired
- Fix: Do not create/remove reduced Pom
- Update: Update the guava version to solve a vulnerability 

**v7.5.0-rc4**
- Improvement: Improve Pom and OptimizationCutter

**v7.5.0-rc3**
- Improvement: Improve Pom and ClusterUtil

**v7.5.0-rc2**
- Improvement: Add new initial construction improvements
- Fix: Fix a bug where the wrong entity is sent to the result stream
- Fix: Copy unassigned node manager for old results to uncouple unassigned lists
- Feature: Add before and after filtering result callback
- Feature: Add property for max duration and max utilization values during cluster construction
- Fix: Fix missing cost for solo cluster nodes
- Fix: Fix potential null pointer in the toString method of the Entity class
- Update: Only throw a warning if the connection is quasi-orphaned
- Improvement: Make the scheme customizable in JSONOptimization
- Improvement: Add constraint alias id concept
- Fix: Fix bug where hard constrained skill is not honoured when using RouteSeedOperator
- Fix: If an anchor-Item is present, allow empty related items
- Fix: Use route object instead of route index during finalization
- Fix: Avoid potential null pointer in DetailController
- Fix: Fix to remove already unassigned elements during force filtering
- Improvement: Add the concept of drops and try different pillar list sorting strategies
- Improvement: During pillar dropping, first sort by location, then compare by duration flexibility
- Fix: Check if the master node is also a related node during JSON conversion
- Fix: Arrange pillars with location first
- Fix: Fix build warning by adding compiler args

**v7.5.0-rc1**
- Fix: DutyHoursIncludesVisitDuration in Pillar Constructor is not checked
- Improvement: Check if initial load of Resource is negative in legacy PND
- Fix: Correct annotation name for "saveError". 
- Improvement: Replace deprecated annotations in JSONConfig
- Fix: GeoPillar can get into lateness if positioned after EventPillar
- Improvement: Couple default WHE-NoViolation call to holder lateness
- Update: Set the default for stayNode-boolean to false for WorkingHours
- Improvement:  Add filter that also checks if any resource is allowed to perform an overnight stay, before activating routeTerminationMutator
- Fix: Fix bug where protected nodes can become substitutes
- Improvement: Partial clean up of Construction Algorithm
- Feature: Add operator reselect feature
- Fix: Fix empty optimization NullPointerException bug
- Improvement: Performance Algorithm Quality improvements
- Improvement: Performance Operator improvements
- Improvement: General Performance improvements by utilizing Profiler
- Improvement: Improve selection process of routes in various Operators 
- Improvement: Auto-reduce starting temperature of SA phase after snapshot loading
- Feature: Add Operator for joined nodes
- Fix: Fix bug, "where insertion after the last element" is not supported in multiple Operators
- Fix: Fix bug where zoneId is not taken from input
- Feature: Add shift node flexTime feature alias GlueFeature via ShiftNodeOperator
- Feature: Add custom extra info to Optimization
- Fix: Fix bug where GeoAddress is not correctly loaded
- Fix: NoCapacityOverloadViolation cannot be increased in Node
- Improvement: Also try graceful filtering if curKickNodes has a single element
- Improvement: Do not try to move pillars if no pillar is movable 
- Fix: Overlap just needs to be equal to base visit duration during construction
- Improvement: Allow flexible order of resource moves during construction
- Improvement: Allow creation of single node routes during construction
- Improvement: Use smart construction algorithm as default construction algorithm
- Improvement: Modify misleading debug message about "creation of class failed"
- Improvement: Allow higher initial utilization in case resources are empty during construction
- Feature: Allow to externally set PerformanceMode state
- Improvement: Tune probabilities of Operators to be selected to improve runtime performance
- Improvement: Make probability scores of Operators performance dependent
- Improvement: Remove some dead code
- Improvement: Adding auto-triggered performance mode, updating GE after the init phase, letting 2Opt not run for zero setting

**v7.5.0**
- Fix: Fix bug where the route can not be shown when using location id and improve the description of placemark points
- Update: Update to required mode in swagger annotations
- Improvement: In case the input file is not a bzip file, fallback to non-zipped read in
- Fix: Solve potential memory issue by using pure stream transformation in JSONOptimization
- Improvement: Modify the "Total cost" String in the result


**v7.4.9-rc6**
- Improvement: Resetting some default properties
- Improvement: Ignore zero items in max free path length calculation during construction

**v7.4.9-rc5**
- Update: Immutables, Jackson, swagger annotations, spring data, mongo-db, TestNG, log4j, commons-compress, etc.
- Improvement: Do not allow none-compatible schemes
- Improvement: Improve Insertion Operator
- Fix: Fix disappearing early cost when using flex time
- Fix: Avoid the use of deputy resources as a start or termination element in a route, as this can cause JSON serialization problems
- Improvement: Use offset for time-window overlap calculations in LikelyConnectionAnalyzer
- Improvement: Allow integration of pillars in the construction
- Fix: Do not restrict relationship weight in simulated annealing, as it can cause cost fluctuation
- Feature: Add optional "required-overlap" helper for Operators
- Fix: Honour acceptable overtime during constraint state evaluation in AutoFilterlogic
- Feature: Inject custom default properties via a scheme
- Fix: Stay-out cycle exception when start lies beyond the end of last working hours
- Feature: Allow to use average cost instead of absolute cost- Feature: Enable moving eventNodes- Fix: Remove the typo in "getDepartureTime"
- Fix: Check via an internal loop, if an event without geolocation is existing
- Fix: Use effective last position to avoid invisible pillar lateness
- Improvement: Increase values for 2Opt Threshold
- Improvement: Let COG (centre-of-gravity) move during clustering.
- Improvement: Introduce a small cluster size threshold and increase likely neighbourhood value to 50km. (Construction)
- Improvement: Faster and more efficient 2Opt behaviour for long routes
- Fix: Fix some backwards compatibility issues during JSON Snapshot loading
- Fix: In case the first pillar is an event pillar, do not remove optimizable elements in the route

**v7.4.9-rc4**
- Fix: LocationId is not correctly used in NodeConnector
- Update: RXJava3 migration
- Update: Guava, Imuutables, Jackson, Swagger Log4j, Commons, Proguard
- Improvement: Try to prevent lockdown of unchanged routes
- Fix: Creating a shallow copy of routes during JSON serialization
- Fix: Make finalized elements synchronized
- Fix: Protect OperatorController and check for null entities
- Feature: Add hook with reduced connection item
- Fix: As pillars may be jumping within their own list, we need to use real arrival items during map creation
- Feature: By default suppress costs of routes that are inactive
- Feature: Add another entrypoint for JSONOptimization
- Feature: Test average cost instead of sum to avoid high cost numbers
- Improvement: JSON improvements
- Fix: Use sync. methods or blocks to avoid concurrency problems
- Fix: Fix route start time calculation for hook
- Improvement: Add static method to remove elements without informing unassigned list
- Feature: Add start time hook to JSON
- Improvement: Add applied hook time in route result report

**v7.4.9-rc3**
- Fix: Check that loadIds are none-zero
- Fix: Javadoc
- Improvement: Internally, use milliseconds precision for visit duration
- Fix: Importance is not correctly converted from Config to Optimization
- Fix: Multiple JSON issues
- Fix:  Do not allow to unassign finalized pillars
- Improvement: Add element details for JSONOptimization export

**v7.4.9-rc2**
- No entry

**v7.4.9-rc1**
- Improvement: Add helpers and improvements, license updates, bug fixes
- Improvement: Config updates
- Fix: During likely connection analysis ignore Resource to Resource connection
- Feature: Enable LikleyConnectionManager
- Feature: Add utility to deep copy nodes
- Feature: Optionally save positions in element connections
- Update: Swagger core
- Fix: Bug where the first node is in falsy joined visit duration state if the start point is at the same position
- Feature: Enable ConnectionProvider
- Improvement: Allow cluster resource split value to be modifiable
- Improvement: Add additional JSON parameters to config
- Update: Guava version
- Feature: Prepare MultiLayer Construction is a separate algorithm
- Fix: Make sure that  the clusterIndex is not smaller than zero
- Improvement: Use implausibility score for nodes that are orphaned. Add invocation index to know the number of clusters that tried to collect a certain node. Add method to directly unassign after construction
- Improvement: Small distances should be likely. Introduce threshold to avoid neglecting good connections
- Improvement: Allow debug output via status stream
- Fix: Honor start time hook during pillar dropping (OPT-5520)
- Improvement: Rewrite takeOverProperties logic for WorkingHours
- Improvement: Remove unused info from snapshot
- Fix:  Add possibility to ignore start/end comparison during pillar dropping as otherwise pillars can get lost if they are also the termination element of  a route
- Fix: Remove unmappable UTF8 characters

**v7.4.8**
- Improvement: Increase build number for licenses
- Improvement: Allow possibility to disable start update in Workinghours
- Fix: Solve xml errors
- Fix: Solve medium critical vulnerability in log4j-core by further bumping version to 2.17.1
- Improvement: Add methods for extracting potential lateness when dropping pillars

**v7.4.7-rc7**
- Fix: Solve medium critical vulnerability in log4j-core by further bumping version to 2.17.0
- Fix: Solve medium critical vulnerability in log4j-core by further bumping version


**v7.4.7-rc6**
- Fix: Solve highly critical vulnerability in log4j-core by bumping version
- Feature: Add constructors to define strength and other values for Goal tuning
- Feature: Add Bound WeightBalancer with flexible initial weight adjustment
- Improvement: Exchange order of node filtering categories in AutoFilter
- Fix: Solve Nullpointer when using no depot on a resource
- Feature: Add AutoFitlerCoupling for more restrictions
- Feature: Add filtering to depot capacity logic
- Improvement: Add an empty dummy chunk to let the GracefulFilter do an evaluation of the unmodified route.
- Feature: Couple AutoFilter WorkingHours exceedance to late filtering
- Feature: Extend onlyApplyPostFlexTimeWhenOvertime to Resource level
- Feature: Only apply post flex-time cutting if onlyApplyPostFlexTimeWhenOvertime is set to true
- Fix: Solve bug in route time calculation during force filtering
- Feature: Allow post time reduction time only on route overtime, if desired
- Fix: Do not allow negative visit durations
- Improvement: Add test for combined reduction- and post-flex-time
- Fix: Solve potential concurrency exception when streaming over list with optimization elements
- Improvement: Further improve behavior of Graceful AutoFilter logic
- Fix: Solve bug where max. margin is not honored and add partial margin concept


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