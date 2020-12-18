# Advanced Examples

The advanced examples are focusing on presenting some of the advanced features of JOpt. The goal is to learn which of your business use-cases can be covered by the features JOpt-TourOptimizer is providing. There is no predefined order in which you should explore the theses examples.

## Good advice before you start
In case you are new to JOpt-TourOptimizer, you might want to start with the <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic" target="_blank">basic examples</a>.


## Advanced Examples
The following examples are part of the advanced example section:

- Package `alternatedestination`: Usually, a route starts at the Resource's home location and ends at the Resource's home location. An alternate destination redefines the stop of the route.
- Package `autofilter`: A way to let JOpt-TourOptimizer automatically remove nodes that are in most cases violating your predefined conditions (For example, TimeWindows, Skills, etc.)
- Package `condition`: Define conditions where Nodes are coupled to Resources. For example, I want this Resource to visit this node. Another example would be a node that requires a particular skill-set provided by the Resource (also adding an expertise level is possible). It is even possible to define these conditions as a hard constraint. Hard constraining means it is guaranteed that these conditions are not violated. 
- Package `connectionefficiency`: Usually, connections between nodes and Resources are provided by an external distance provider (like Google, OpenStreetMap, etc.) when not using the internal Haversine calculation of JOpt-TourOptimizer. Theses connections might need to be modified to account for different Resource-specific properties (the type of vehicle, driving behavior, etc.). To achieve this, an efficiency factor can be applied.
- Package `includevisitduration`: For visiting a node, usually, a time window is defined, including the earliest possible work-start and the time the work has to be done. For example, a node opens at 8 am and closes at 5 pm and has a task to be done that needs 30 minutes of work. However, in some cases, it makes sense to define an arrival window. For example, a person on-side needs to grant access to a building to the Resource between 10 am and 12 pm. The work itself takes 4 hours. When excluding visit duration, the Resource does not need to fish the work within the time window.  
- Package `jointvisitduration`: When nodes share the same location, in some cases, the working time needed for each of the nodes is reduced. For example, delivering a parcel by a mail carrier takes 5 minutes on average. Let's assume four customers (nodes) share the same location. The mail carrier can deliver the parcels for all customers at once what can save time.
- Package `looprun`: When using the converger, or even a custom externally defined algorithm to decide when to stop the optimization, it is helpful to let particular stages of the optimization run in an endless loop.
- Package `openclosedroute`: Usually, a route starts at the Resource's home location and ends at the Resource's home location. When using open routes, the route of the Resource is assumed to be done at the last node.
- Package `overnightstay`: A Resource might need to stay out in a hotel. The Optimizer can auto-schedule these overnight stays. It is possible to enable only certain working days of the Resource for overnight-stays (hard constraint)
- Package `pickupanddelivery`: Pick-up-Delivery (PND) Optimization is used, when Resource delivering goods to nodes or Resources need to pick up goods from nodes. Collection of multiple examples for standard PND-Problems and sophisticated examples including  manufacturing planning, and time-constrained transportation of goods and people. For further help, please visit <a href="https://docs.dna-evolutions.com/overview_docs/pickup_and_delivery/Pickup_and_delivery.html" target="_blank">our documentation on PND</a>.
- Package `progressfrequency`: By default, at every one percent of progress, a report is thrown. However, either a report can be requested at any time, or the frequency for the report can be manipulated.
- Package `readoutfullprogress`: Shows how to use/analyze the progress object during an optimization run. Further, explains how to use the util ProgressParser to extract values from the Progress Object.
- Package `relationship`: Nodes are might coupled with each other. For example, Node A needs to be visited before Node B. Or Node A is not allowed to be visited in the same route as Node B.
- Package `requestresult`: Instead of waiting for the final result of the Optimizer, it is possible to ask the Optimizer for an intermediate result.
- Package `visitdurationefficiency`: In a real-life scenario, it is common that Resources have a different performance that influences their time taken for the same Job. Therefore we introduced the Resource-Efficiency-Factor.
- Package `zonecode`: ZoneCodes are used to define areas a Resource is allowed to visit (can be modified for each working day separately). Further, nodes themselves hold an area identifier. This way, sale/deliver areas can be predefined. Also, it is possible to define this as a hard constraint.

