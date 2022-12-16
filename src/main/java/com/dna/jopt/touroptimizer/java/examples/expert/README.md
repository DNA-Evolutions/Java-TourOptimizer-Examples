# Expert Examples

The expert examples are focusing on manipulating the way the Optimizer works. In this context, some sophisticated features are presented. This expert section can also be considered somehow as "danger-zone" as some of the here presented features can profoundly impact the performance of the Optimizer, or invalid definitions can be created if misused.

However, if followed correctly, these features can highly customize the Optimizer and are ideally suited to meet the individual requirements of your end-customer.

There is no predefined order in which you should explore the theses examples.

## Good advice before you start
You can get a high-level overview of some of the here presented features in our documentation about <a href="https://docs.dna-evolutions.com/overview_docs/special_features/Special_Features.html" target="_blank">special features</a>.

In case you are new to JOpt-TourOptimizer, you might want to start with the <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic" target="_blank">basic</a> and <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced" target="_blank">advanced</a> examples.


## Expert Examples
The following examples are part of the expert example section:

- Package `backupconnector`: By default, if no external connection was provided, JOpt-TourOptimizer uses a haversine algorithm to calculate distance and driving-time between two elements. However, by providing a custom BackUpConnector, a custom algorithm can be applied, or the existing one can be modified.
- Package `buildinfo`: The JOpt-TourOptimizer library saves its build-data inside a text file that can be read out on runtime.
- Package `customsolution`: Instead of using the default construction algorithm for a first starting solution that gets improved during the Optimization run, a custom solution for starting the Optimization can be provided.
- Package `externalcostconvergence`: In some cases, the cost function used to assess the quality of the current best solution is stabilizing (converged) during the Optimization process. Any further computation, therefore, is not necessary. The external cost-converger will gracefully stop the Optimization once a predefined convergence-criteria is reached.
- Package `flextime`: Each Resource can carry multiple WorkingHours. Each WorkingHours starts at a predefined time. However, sometimes it is desirable that a Resource starts working later (to reduce idle time) or start working earlier to reach a node right when it opens. FlexTime effectively replaces the fixed starting time of a WorkingHour by a starting-window.
- Package `openassessor`: By default, JOpt-TourOptimizer already knows a lot of different restrictions. For example, we do not want to be late at a Node. However, sometimes particular use-cases require new restrictions to meet your end-customers' requirements. The OpenAssessor allows creating custom-defined restrictions on the individual Node or Route level.
- Package `optimizationscheme`: Example "selectalgorithms" on how to manipulate the OptimizationScheme of the Optimizer. This allows, for example, to choose the algorithms (and their order) that are used during the Optimization run.
Example "customdefaultproperties" on how to manipulate the OptimizationScheme of the Optimizer for defining custom default properties.
- Package `optionalnode`: The Optimizer decides if an optional Node is visited or not. Usually, every Node is adding cost to the total solution cost. However, in pick-up and delivery problem sets, adding a Node can reduce the overall cost. For example, think of a waste-management scenario in which a Resource can use an optional Node to dump the waste and avoid overloading.
- Package `readoutproperties`: The Optimizer has a lot of different global handles (its properties) to adjust the Optimization process. Even though most of the predefined properties are tuned to solve most of the use-cases, particular properties may need to be adjusted. We are recommending to change as few properties as possible. However, this example shows how to read out all properties that can be modified. Please also visit our documentation about <a href="https://docs.dna-evolutions.com/overview_docs/optimizationproperties/Optimization_Properties.html" target="_blank">optimization-properties</a>.
- Package `uncaughtexception`: In case an uncaught exception occurs as part of the Optimization process, a custom uncaught exception can be attached to the Optimizer. 

