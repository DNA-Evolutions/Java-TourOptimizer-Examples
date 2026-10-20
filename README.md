# JOpt.TourOptimizer: Java examples

<a href="https://www.dna-evolutions.com/"><img src="https://www.dna-evolutions.com/images/dna_logo.png" width="200" title="DNA Evolutions" alt="DNA Evolutions"></a>

A Maven project with Java examples for JOpt.TourOptimizer by DNA Evolutions. JOpt runs a construction algorithm, simulated annealing and genetic evolution one after the other. Open the project in your IDE, or start the browser sandbox in Docker.

- **Documentation hub:** [dna-evolutions.com/docs/getting-started/home/introduction](https://www.dna-evolutions.com/docs/getting-started/home/introduction)
- **Special features:** [dna-evolutions.com/docs/learn-and-explore/special/special_features](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features)
- **Interactive API:** [dna-evolutions.com/api](https://www.dna-evolutions.com/api/)
- **JavaDocs:** [public.javadoc.dna-evolutions.com](https://public.javadoc.dna-evolutions.com)
- **Nexus repository:** [nexus.dna-evolutions.net](https://nexus.dna-evolutions.net)

---

## Contents

- [What is JOpt](#what-is-jopt)
- [Example categories](#example-categories)
- [Recommended learning path](#recommended-learning-path)
- [Advanced examples](#advanced-examples)
- [Expert examples](#expert-examples)
- [RESTful examples](#restful-examples)
- [Feature selection guide](#feature-selection-guide)
- [Getting started](#getting-started)
- [Browser sandbox (Docker)](#browser-sandbox-docker)
- [Clone and run locally](#clone-and-run-locally)
- [Add JOpt as a Maven dependency](#add-jopt-as-a-maven-dependency)
- [Non-Maven build tools](#non-maven-build-tools)
- [Java 8 legacy support](#java-8-legacy-support)
- [.NET legacy support](#net-legacy-support)
- [Requirements by scenario](#requirements-by-scenario)
- [Further links](#further-links)
- [Contact](#contact)
- [Agreement](#agreement)

---

## What is JOpt

JOpt is a Java library for tour optimization under constraints: time windows, skills, mandatory visits, multi-day planning, load capacities and zone restrictions.

The introduction video on YouTube:

<a href="https://www.youtube.com/watch?v=U4mDQGnZGZs"><img src="https://www.dna-evolutions.com/images/docs/home/jopt_intro_prev.gif" width="600"
title="Introduction to JOpt" alt="Introduction to JOpt"></a>

> The TourOptimizer REST API uses the same snapshot schema as the Java library. The features that are part of the published API schema can be used from any client: Java, TypeScript, Python, C#, or any language with an OpenAPI code generator. The [interactive API](https://www.dna-evolutions.com/api/) shows the schema.

---

## Example categories

The examples are grouped by level. Each level has a README, and each example has a .md page beside its source.

| Category | Description | Link |
|---|---|---|
| **Basic** | First optimizations, nodes, resources, time windows, I/O, result reading, licensing | [Basic examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic) |
| **Advanced** | Constraints, conditions, relations, PND, clustering, zones, overnight, FlexTime, CO2 | [Advanced examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced) |
| **Expert** | Custom assessors, optimization schemes, performance mode, warm-start, exception handling | [Expert examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert) |
| **RESTful** | Bridge between the Java SDK and the REST API: serialize problems to JSON, run JSON payloads locally | [RESTful examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful) |

---

## Recommended learning path

If you are new to JOpt, follow this progression through the Basic examples first, then branch into the Advanced or Expert sections as needed.

### Basic

1. **Read the Basic elements tutorial**: [Basic elements](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)
2. **Run the first optimization**: [`firstoptimization_01/FirstOptimizationExample.java`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/firstoptimization_01/FirstOptimizationExample.java), [Tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization)
3. **Adopt a production execution pattern**: [`recommendedimplementation_02`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02) (the recommended run pattern, a synchronous run and the legacy callback methods)
4. **Persist and resume optimizations**: [`io_03`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03) (JSON/JSON.BZ2 save, load, checkpoint during run, KML export)
5. **Provide your own distances and travel times**: [`connection_04`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04) (directed edges, partial matrices, locationId, backup connector)
6. **Read and analyse results**: [`readoutresult_05`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05) (routes, stop sequences, costs, violations)
7. **Model non-geographic work items**: [`eventnode_06`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06) (tasks without locations, such as calls and meetings)
8. **Model hard SLA constraints**: [`pillar_07`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07) (Pillar and Captured nodes; nodes flow around pillars by architecture, not by cost)
9. **Set up licensing**: [`setlicense_08`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/setlicense_08) (file-based and JSON-based licensing)

---

## Advanced examples

The [Advanced examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced) cover resource-to-node conditions and skills, pickup and delivery (including fuzzy loads, timed transport, and production planning), node relations (same route, same resource, relative time windows), territory management via ZoneCodes and ZoneCrossing, clustering-based construction for large instances, AutoFilter for infeasible node removal, FlexTime, overnight stay and CO2 emission optimization.

For a full feature overview including decision guidance on when to use each capability, see the [Special features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features).

---

## Expert examples

The [Expert examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert) change how the optimizer works. Topics include: injecting custom business rules via the Open Assessor (route-level and node-level), selecting and configuring the algorithm pipeline explicitly, performance mode for large-scale instances, warm-start from an existing plan, custom convergence criteria, deterministic uncaught exception handling for production deployments, result comparison and plan-delta analysis, and deep progress telemetry.

> A rule that must always hold belongs in a structural feature or an Open Assessor hard rule. Penalty weights are for preferences inside the feasible space.

For additional context see the [Special features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features) and the [Optimization properties reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

---

## RESTful examples

The [RESTful examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful) bridge the Java SDK and the TourOptimizer REST API. They cover both directions of the serialization round-trip: building a problem in Java and serializing it to a REST-compatible JSON payload (with or without a pre-computed solution for warm-start workflows), and loading a REST JSON payload to run it locally with the Java core library without a server. These patterns are useful for debugging, hybrid pre-solver architectures, integration testing, and CI/CD test fixtures.

For more context on the REST API itself see the [Docker REST TourOptimizer repository](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer) and the [interactive API documentation](https://www.dna-evolutions.com/api/).

---

## Feature selection guide

Decision rules by problem:

**If the run takes too long:**
Start with **AutoFilter** to remove infeasible nodes early and **Performance Mode** for large-scale presets. For very large instances, evaluate **Clustering construction** for a better starting solution.

**If you need strong business rules:**
Use **Relations** to couple nodes across route, resource, or time. Use **ZoneCodes** for territories (hard or soft) and **ZoneCrossing** to discourage expensive boundary crossings. Use **Pillar/Captured nodes** for contractual SLA hard constraints that must be satisfied by architecture.

**If feasibility depends on the load:**
Use **Pickup and Delivery (PND)** when feasibility depends on load state: underload, overload, timed transport, or production planning.

**If you need customer-specific business logic:**
Use the **Open Assessor** at route or node level. Wire custom restrictions via an optimization scheme. Start with one rule, validate it, then add more.

**If you need multi-day planning:**
Use **Overnight Stay** for resources that stay out overnight, combined with **Relations** and **ZoneCodes** for territory continuity across days.

**If you need explainability:**
Use the **Compare results** tool for plan-delta analysis and the **Open Assessor** to expose domain-specific cost contributions with named reasons.

---

## Getting started

Three ways to start:

1. **[Browser sandbox](#browser-sandbox-docker)**: a browser IDE in a Docker container, no local Java setup needed.
2. **[Clone locally](#clone-and-run-locally)**: import as a Maven project in your IDE.
3. **[Add as dependency](#add-jopt-as-a-maven-dependency)**: integrate JOpt into your own project.

---

## Browser sandbox (Docker)

The sandbox is a browser-based IDE ([code-server](https://github.com/cdr/code-server) / Visual Studio Code) with Java, Maven and the examples pre-installed. No local Java or IDE setup required.

**Docker Hub:** [`dnaevolutions/jopt_example_server:latest`](https://hub.docker.com/r/dnaevolutions/jopt_example_server)

Preview (click to enlarge):

<a href="https://www.dna-evolutions.com/images/docs/home/coderserver.png"><img src="https://www.dna-evolutions.com/images/docs/home/coderserver.png" width="85%"
title="JOpt Example Sandbox" alt="JOpt Example Sandbox"></a>

### Start the sandbox

```bash
docker run -it -d \
  --name jopt-examples \
  -p 127.0.0.1:8042:8080 \
  -v "$PWD/:/home/coder/project" \
  dnaevolutions/jopt_example_server:latest
```

The `-v` flag mounts your current directory into the container so your changes survive restarts. On the first launch the examples are cloned automatically. On subsequent launches the existing files are preserved.

### Open the sandbox

Navigate to [http://localhost:8042](http://localhost:8042) and log in with:

```text
jopt
```

On the first run, Maven downloads the dependencies. A [tutorial video](https://www.youtube.com/watch?v=Jk9ONloaNlk) walks through the sandbox.

For additional sandbox variants (REST client sandbox, Python sandbox, C# sandbox) and a full comparison table, see the [JOpt Sandboxes guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/jopt-sandboxes) and the [sandbox section of the Docker REST TourOptimizer README](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer#docker-sandboxes-browser-based-evaluation).

---

## Clone and run locally

### Steps

```bash
git clone https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples.git
```

Import the project as a Maven project in your IDE and run the `main` method of any example class. Each example is self-contained: it sets up its own optimization input and prints the result to the console.

### Before you run all examples

- **License:** the examples set the public evaluation license through `ExampleLicenseHelper`. Without a license, JOpt allows up to 10 elements (nodes and resources together), the public evaluation license up to 20, and the extended free license up to 30 nodes and 7 resources; the extended free license is available with a free sign-up (see [Extended free license](#extended-free-license)). Some examples need more than the public evaluation license: the clustering examples in `advanced/clustering`, `PerformanceModeBigOptiExample` and `CustomRouteLevelOddNumberOfElementsRestrictionExample`. `ExampleLicenseHelper` uses your license file instead when it exists at `src/main/resources/license/YOUR_LICENSE.jli`.
- **Order in `basic/io_03`:** the loaders that read `myopti.json.bz2` (`LoadOptimizationFromJsonExample`, `LoadOptimizationToFreshRunExample`, `LoadOptimizationFromJsonAndReassignNodes`, `LoadOptimizationFromJsonAndReassignPillar`) and `CreateCustomSolutionFromJSONExample` need that file in the working directory. Run `SaveOptimizationToJsonExample` first.
- **Errors on purpose:** some examples throw an exception on purpose to show error handling: `CapacityCheckExample` and the examples in `expert/uncaughtexception`.

### Code formatter profile

An Eclipse formatter profile for the examples is in `formatter/jopt-examples-formatter.xml` (profile name `JOptExamples`). It indents with 2 spaces per block level and 4 for a continuation line, wraps lines at 100 columns, keeps existing line breaks and comments, and leaves code between `@formatter:off` and `@formatter:on` as written.

To use it in Eclipse:

1. Open **Window > Preferences > Java > Code Style > Formatter**.
2. Click **Import...** and select `formatter/jopt-examples-formatter.xml` in the cloned repository.
3. Check that `JOptExamples` is the active profile and click **Apply and Close**.

---

## Add JOpt as a Maven dependency

### Repository

Add the DNA Evolutions Nexus repository to your `pom.xml`:

```xml
<repository>
    <id>jopt4-maven</id>
    <url>https://nexus.dna-evolutions.net/repository/maven-public/</url>
    <releases>
        <enabled>true</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

See the [official Maven documentation](https://maven.apache.org/guides/introduction/introduction-to-repositories.html) for help with repository configuration.

### Core library

**Always use the latest version. If a release candidate (`rc`) is available, prefer it over the stable release.**

```xml
<!-- Stable -->
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.3-j17</version>
  <classifier>shaded</classifier>
</dependency>
```

```xml
<!-- Latest release candidate (recommended) -->
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.3-rc5-j17</version>
  <classifier>shaded</classifier>
</dependency>
```

### JavaDocs

```xml
<!-- Stable -->
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.3-j17</version>
  <classifier>javadoc</classifier>
</dependency>
```

```xml
<!-- Latest release candidate -->
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.3-rc5-j17</version>
  <classifier>javadoc</classifier>
</dependency>
```

The JavaDocs are also available online as a [browsable reference](https://public.javadoc.dna-evolutions.com/).

### Direct download

- [Shaded JAR (latest)](https://nexus.dna-evolutions.net/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=shaded)
- [JavaDoc JAR (latest)](https://nexus.dna-evolutions.net/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=javadoc)

---

## Non-Maven build tools

JOpt is compatible with Gradle, SBT, IVY, Grape, Leiningen, and other JVM build tools. Browse the [Nexus repository](https://nexus.dna-evolutions.net/#browse/browse:maven-releases), select the desired artifact, and look for the Usage panel to find the snippet for your build tool. Add the DNA Evolutions repository to the build configuration of any tool.

---

## Java 8 legacy support

Version **7.5.2** is the last release to include a Java 8 compatible build. From version 7.5.3 onward, Java 17 is required.

On Java 8, use the [JOpt.TourOptimizer REST service](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer) in a container; a client in any language can be generated from its OpenAPI spec.

```xml
<!-- Last Java 8 compatible release -->
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.2-j8</version>
  <classifier>shaded</classifier>
</dependency>
```

```xml
<!-- JavaDocs for Java 8 release -->
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.2-j8</version>
  <classifier>javadoc</classifier>
</dependency>
```

---

## .NET legacy support

A legacy .NET version of JOpt is available via [IKVM.NET](https://en.wikipedia.org/wiki/IKVM.NET). Version 7.5.2 is the last release to include a legacy DLL.

**IKVM.NET framework:** [ikvm-8.1.5717.0.zip](https://shared.dna-evolutions.com/legacy/net/ikvm_env/ikvm-8.1.5717.0.zip)

| Version | Download |
|---|---|
| 7.5.2 (last legacy build) | [jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.5.1 | [jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.5.0 | [jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.4.9-rc4 | [jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.4.9-rc2 | [jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.4.8 | [jopt.core-7.4.8-with-dep-pg-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.8-with-dep-pg-legacy/jopt.core-7.4.8-with-dep-pg-legacy.zip) |
| 7.4.6 | [jopt.core.pg-7.4.6-shaded.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core.pg-7.4.6-shaded/jopt.core.pg-7.4.6-shaded.zip) |

For new .NET integrations, the recommended path is to use the [JOpt.TourOptimizer REST API](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer) with a generated C# client from the OpenAPI spec.

---

## Requirements by scenario

| Scenario | Requirement |
|---|---|
| Native Java (Java 17+) | Java 17+, Maven 3.6+ |
| Native Java (Java 8 legacy) | Java 8, Maven (version 7.5.2-j8 only) |
| Browser sandbox | Docker, 2 GB RAM minimum, 2 CPU cores |
| .NET legacy | IKVM libraries, .NET 4.x framework (version 7.5.2 only) |

---

## Further links

### Repository files

- [RELEASE_NOTES.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/RELEASE_NOTES.md): release history
- [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md): detailed changelog for the examples and the JOpt library
- [Docker sandboxes](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer#docker-sandboxes-browser-based-evaluation): overview of the available sandbox environments

### Documentation

- [Getting started](https://www.dna-evolutions.com/docs/getting-started)
- [Special features overview](https://www.dna-evolutions.com/docs/learn-and-explore/special/special_features)
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)
- [Interactive API testing](https://www.dna-evolutions.com/api/)
- [JavaDocs (online)](https://public.javadoc.dna-evolutions.com)

### Extended free license

Get an extended free license for customers evaluating the product. It allows up to 30 nodes and 7 resources (37 elements) and is available only with a free [sign-up](https://www.dna-evolutions.com/sign-up) on the DNA Evolutions website:  
[DNA Evolutions Portal](https://www.dna-evolutions.com/portal) *(sign-in required)*

### Code and registry

- [Docker REST TourOptimizer](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer): containerised REST API
- [Nexus repository](https://nexus.dna-evolutions.net): all JOpt releases

### Social

- [LinkedIn](https://www.linkedin.com/company/dna-evolutions/)
- [Docker Hub](https://hub.docker.com/u/dnaevolutions)
- [YouTube tutorials](https://www.youtube.com/channel/UCzfZjJLp5Rrk7U2UKsOf8Fw)
- [SourceForge](https://sourceforge.net/software/product/JOpt.TourOptimizer/)

---

## Contact

For help or questions, use the [contact form](https://www.dna-evolutions.com/contact) or write to [info@dna-evolutions.com](mailto:info@dna-evolutions.com).

---

## Agreement

For license terms and plans, visit the [DNA Evolutions website](https://www.dna-evolutions.com).

---

A product by [DNA Evolutions GmbH](https://www.dna-evolutions.com) &copy;