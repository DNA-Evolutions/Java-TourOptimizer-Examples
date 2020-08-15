# Basic Examples

The basic examples give an introduction on how to use JOpt-TourOptimizer. The goal is to create, test, run an optimization, and get to know the basic objects and settings of JOpt-TourOptimizer.

## Good advice before you start
A detailed explanation of the basic elements of JOpt-TourOptimizer can be found <a href="https://docs.dna-evolutions.com/java_examples/tutorials/tutorial_beginner/basic_elements/basic_elements.html" target="_blank">here</a>.

## First Example
The first example you should start with is stored in the package `firstoptimization_01` and is called `FirstOptimizationExample.java` . A detailed explanation of this example can be found <a href="https://docs.dna-evolutions.com/java_examples/tutorials/tutorial_beginner/first_optimization/first_optimization.html" target="_blank">here</a>.

**It covers:**
1. Adding properties
2. Adding nodes
3. Adding resources
4. Attaching to observables
8. Starting the optimization and presenting the result


## Other Basic Examples
The examples in the basic section are numbered (within their package naming like `PACKAGENAME_NUMBER`). It is not necessary to strictly follow the numbering, but it gives a good way to increase the complexity during the first steps with JOpt-TourOptimizer.

- Package `recommendedimplementation_02`: Comparable to `firstoptimization_01`. Further, shows how to use callbacks and reactive java.
- Package `io_03`: Shows how to save an optimization to a JSON object and how to load a JSON into an optimization. Additionally, it shows how to use a JSON file as data-storage for a new fresh run (ignoring a previous solution). Further, it show how to create an KML file from a solution object.
- Package `connection_04`: Shows how to use connections. A connection defines, for example, the distances between two elements.
- Package `readoutresult_05`: Shows how to use/analyze the result object after a successful optimization run is done.
- Package `eventnode_06`: Shows how to create a node that has no geographical location (like a phone call).
- Package `pillar_07`: Shows how to create a node with a fixed (guaranteed) arrival window of a visitor.
- Package `setlicense_08`: Shows how to use a license and leave the free mode of JOpt-TourOptimizer (required if more than ten elements need to be optimized).

