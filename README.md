# JOpt.TourOptimizer — Java Examples

<a href="https://dna-evolutions.com/" target="_blank"><img src="https://www.dna-evolutions.com/images/dna_logo.png" width="200" title="DNA Evolutions" alt="DNA Evolutions"></a>

A fully functional Maven project containing an extensive collection of Java examples for **DNA Evolutions' JOpt.TourOptimizer** — a flexible routing and scheduling optimization engine built around a three-phase pipeline of Construction Heuristics, Simulated Annealing, and Genetic Evolution. Use this repository to get started with JOpt directly in your IDE, or launch the browser-based sandbox with a single Docker command and start coding without installing anything.

- **Documentation hub:** [dna-evolutions.com/docs/getting-started](https://www.dna-evolutions.com/docs/getting-started)
- **Special features:** [dna-evolutions.com/docs/learn-and-explore/special/special_features](https://dna-evolutions.com/docs/learn-and-explore/special/special_features)
- **Interactive API:** [dna-evolutions.com/api](https://dna-evolutions.com/api/)
- **JavaDocs:** [public.javadoc.dna-evolutions.com](https://public.javadoc.dna-evolutions.com)
- **Nexus repository:** [nexus.dna-evolutions.net](https://nexus.dna-evolutions.net)

<a href="https://sourceforge.net/software/product/JOpt.TourOptimizer/?pk_campaign=badge&pk_source=vendor" target="_blank" rel="nofollow">
<img alt="Partner 2025" src="https://sourceforge.net/cdn/syndication/badge_img/3636727/light-partner" height="100px" width="100px"></a>

---

## Overview

- [What is JOpt](#what-is-jopt)
- [Example categories](#example-categories)
- [Recommended learning path](#recommended-learning-path)
- [Feature selection guide](#feature-selection-guide)
- [Getting started](#getting-started)
- [Browser sandbox (Docker)](#browser-sandbox-docker)
- [Clone and run locally](#clone-and-run-locally)
- [Add JOpt as a Maven dependency](#add-jopt-as-a-maven-dependency)
- [Non-Maven build tools](#non-maven-build-tools)
- [Java 8 legacy support](#java-8-legacy-support)
- [.NET legacy support](#net-legacy-support)
- [Prerequisites](#prerequisites)
- [Further links](#further-links)

---

## What is JOpt

JOpt is a flexible routing optimization engine written in Java. It solves tour optimization problems that are highly constrained — time windows, skills, mandatory visit requirements, multi-day planning, load capacities, zone restrictions, and more.

Click to watch the introduction:

<a href="https://www.youtube.com/watch?v=U4mDQGnZGZs" target="_blank"><img src="https://dna-evolutions.com/images/docs/home/jopt_intro_prev.gif" width="600"
title="Introduction to JOpt" alt="Introduction to JOpt"></a>

> All features are available via the REST API using the same snapshot schema used by the Java library. That means the full feature set is usable from any client — Java, TypeScript, Python, C#, or any language with an OpenAPI code generator.

---

## Example categories

The project is organised into four categories of increasing complexity. Each category has its own README and each example has its own markdown documentation placed alongside the source.

| Category | Description | Link |
|---|---|---|
| **Basic** | First optimizations, nodes, resources, time windows, I/O, result reading, licensing | [Basic examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic) |
| **Advanced** | Constraints, conditions, relations, PND, clustering, zones, overnight, FlexTime, CO₂ | [Advanced examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced) |
| **Expert** | Custom assessors, optimization schemes, performance mode, warm-start, exception handling | [Expert examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert) |
| **RESTful** | Java SDK ↔ REST API bridge: serialize problems to JSON, run JSON payloads locally | [RESTful examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful) |

---

## Recommended learning path

If you are new to JOpt, follow this progression through the Basic examples first, then branch into the Advanced or Expert sections as needed.

### Basic

1. **Read the core elements tutorial** — [Basic elements](https://www.dna-evolutions.com/docs/getting-started/tutorials/basic-elements)
2. **Run the first optimization** — [`firstoptimization_01/FirstOptimizationExample.java`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/firstoptimization_01/FirstOptimizationExample.java) · [Tutorial](https://www.dna-evolutions.com/docs/getting-started/tutorials/first-optimization)
3. **Adopt a production execution pattern** — [`recommendedimplementation_02`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/recommendedimplementation_02) — sync vs async vs reactive event handling
4. **Persist and resume optimizations** — [`io_03`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03) — JSON/JSON.BZ2 save, load, checkpoint during run, KML export
5. **Provide your own distances and travel times** — [`connection_04`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/connection_04) — directed edges, partial matrices, locationId, backup connector
6. **Read and analyse results** — [`readoutresult_05`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05) — routes, stop sequences, costs, violations
7. **Model non-geographic work items** — [`eventnode_06`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/eventnode_06) — tasks without locations (calls, meetings)
8. **Model hard SLA constraints** — [`pillar_07`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/pillar_07) — Pillar and Captured nodes (nodes flow around pillars by architecture, not by cost)
9. **Set up licensing** — [`setlicense_08`](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/setlicense_08) — file-based and JSON-based licensing

> **Good advice:** keep the model small first. Validate feasibility and correctness before increasing problem size. Always subscribe to progress/status/error events during development — it accelerates debugging dramatically.

---

## Advanced examples

The [Advanced examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced) cover the full range of operational features: resource-to-node conditions and skills, pickup and delivery (including fuzzy loads, timed transport, and production planning), node relations (same route, same resource, relative time windows), territory management via ZoneCodes and ZoneCrossing, clustering-based construction for large instances, AutoFilter for infeasible node removal, FlexTime, overnight stay, CO₂ emission optimization, and more.

Each sub-package contains its own README and companion Markdown documentation placed alongside the example sources. For a full feature overview including decision guidance on when to use each capability, see the [Special features overview](https://dna-evolutions.com/docs/learn-and-explore/special/special_features).

---

## Expert examples

The [Expert examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert) focus on *how the optimizer behaves* rather than what it models. Topics include: injecting custom business rules via the Open Assessor (route-level and node-level), selecting and configuring the algorithm pipeline explicitly, performance mode for large-scale instances, warm-start from an existing plan, custom convergence criteria, deterministic uncaught exception handling for production deployments, result comparison and plan-delta analysis, and deep progress telemetry.

> Hard constraints must be satisfied by architecture, not by high cost penalties. If a rule must always hold, implement it as a structural feature or Open Assessor hard rule — reserve penalty weights for preferences within the feasible space.

Each sub-package has its own README. For additional context see the [Special features overview](https://dna-evolutions.com/docs/learn-and-explore/special/special_features) and the [Optimization properties reference](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties).

---

## RESTful examples

The [RESTful examples](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful) bridge the Java SDK and the TourOptimizer REST API. They cover both directions of the serialization round-trip: building a problem in Java and serializing it to a REST-compatible JSON payload (with or without a pre-computed solution for warm-start workflows), and loading a REST JSON payload to run it locally with the Java core library without a server. These patterns are useful for debugging, hybrid pre-solver architectures, integration testing, and CI/CD test fixtures.

Each sub-package has its own README with step-by-step guidance. For more context on the REST API itself see the [Docker REST TourOptimizer repository](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer) and the [interactive API documentation](https://dna-evolutions.com/api/).

---

## Feature selection guide

Not sure which feature to use? Use these decision rules.

**If your main pain is runtime:**
Start with **AutoFilter** to remove infeasible nodes early and **Performance Mode** for large-scale presets. For very large instances, evaluate **Clustering construction** for a better starting solution.

**If you need strong business rules:**
Use **Relations** to couple nodes across route, resource, or time. Use **ZoneCodes** for territories (hard or soft) and **ZoneCrossing** to discourage expensive boundary crossings. Use **Pillar/Captured nodes** for contractual SLA hard constraints that must be satisfied by architecture.

**If you model logistics flows, not just routes:**
Use **Pickup and Delivery (PND)** when feasibility depends on load state — underload, overload, timed transport, or production planning.

**If you need customer-specific business logic:**
Use the **Open Assessor** at route or node level. Wire custom restrictions via an optimization scheme. Start with one rule, validate it, then add more.

**If you need multi-day planning:**
Use **Overnight Stay** for resources that stay out overnight, combined with **Relations** and **ZoneCodes** for territory continuity across days.

**If you need explainability:**
Use the **Compare results** tool for plan-delta analysis and the **Open Assessor** to expose domain-specific cost contributions with named reasons.

---

## Getting started

Three ways to start:

1. **[Browser sandbox](#browser-sandbox-docker)** — zero installation, runs in your browser via Docker.
2. **[Clone locally](#clone-and-run-locally)** — import as a Maven project in your IDE.
3. **[Add as dependency](#add-jopt-as-a-maven-dependency)** — integrate JOpt into your own project.

---

## Browser sandbox (Docker)

The sandbox is a browser-based IDE ([code-server](https://github.com/cdr/code-server) / Visual Studio Code) with Java, Maven, and the latest examples pre-installed. No local Java or IDE setup required.

**Docker Hub:** [`dnaevolutions/jopt_example_server`](https://hub.docker.com/r/dnaevolutions/jopt_example_server)

Preview (click to enlarge):

<a href="https://dna-evolutions.com/images/docs/home/coderserver.png" target="_blank"><img src="https://docs.dna-evolutions.com/indexres/coderserver.png" width="85%"
title="JOpt Example Sandbox" alt="JOpt Example Sandbox"></a>

### Start the sandbox

```bash
docker run -it -d \
  --name jopt-examples \
  -p 127.0.0.1:8042:8080 \
  -v "$PWD/:/home/coder/project" \
  dnaevolutions/jopt_example_server:latest
```

The `-v` flag mounts your current directory into the container so your changes survive restarts. On the first launch the latest examples are cloned automatically. On subsequent launches the existing files are preserved.

### Open the sandbox

Navigate to [http://localhost:8042](http://localhost:8042) and log in with:

```
jopt
```

On the first run, Maven downloads the required dependencies — under one minute depending on your connection. A [tutorial video](https://www.youtube.com/watch?v=Jk9ONloaNlk) (~3 minutes) walks through the full sandbox workflow.

**System requirements:** at least 2 GB RAM and 2 CPU cores.

For additional sandbox variants (REST client sandbox, Python sandbox, C# sandbox) and a full comparison table, see the [JOpt Sandboxes guide](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/jopt-sandboxes) and [Sandboxes.md](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer/blob/main/Sandboxes.md).

---

## Clone and run locally

### Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code)

### Steps

```bash
git clone https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples.git
```

Import the project as a Maven project in your IDE and run the `main` method of any example class. Each example is self-contained — it sets up its own optimization input and prints the result to the console.

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
  <version>7.5.3-rc2-j17</version>
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
  <version>7.5.3-rc2-j17</version>
  <classifier>javadoc</classifier>
</dependency>
```

The JavaDocs are also available online as a [browsable reference](https://public.javadoc.dna-evolutions.com/).

### Direct download

- [Shaded JAR (latest)](https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=shaded)
- [JavaDoc JAR (latest)](https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=javadoc)

---

## Non-Maven build tools

JOpt is compatible with Gradle, SBT, IVY, Grape, Leiningen, and other JVM build tools. Browse the [Nexus repository](https://public.repo.dna-evolutions.com/#browse/browse:maven-releases), select the desired artifact, and look for the Usage panel to find the snippet for your build tool. You will need to add the DNA Evolutions repository in your build configuration regardless of the tool.

---

## Java 8 legacy support

Version **7.5.2** is the last release to include a Java 8 compatible build. From version 7.5.3 onward, Java 17 is required.

Users who need to stay on Java 8 are encouraged to migrate to [JOpt.TourOptimizer](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer), the containerised REST API, which allows building clients in any language via the OpenAPI spec.

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
| 7.5.2 (latest legacy) | [jopt.core-7.5.2-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.5.1 | [jopt.core-7.5.1-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.5.0 | [jopt.core-7.5.0-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.4.9-rc4 | [jopt.core-7.4.9-rc4-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.4.9-rc2 | [jopt.core-7.4.9-rc2-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy.zip) |
| 7.4.8 | [jopt.core-7.4.8-legacy.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.8-with-dep-pg-legacy/jopt.core-7.4.8-with-dep-pg-legacy.zip) |
| 7.4.6 | [jopt.core.pg-7.4.6-shaded.zip](https://shared.dna-evolutions.com/legacy/net/jopt.core.pg-7.4.6-shaded/jopt.core.pg-7.4.6-shaded.zip) |

For new .NET integrations, the recommended path is to use the [JOpt.TourOptimizer REST API](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer) with a generated C# client from the OpenAPI spec.

---

## Prerequisites

| Scenario | Requirement |
|---|---|
| Native Java (Java 17+) | Java 17+, Maven 3.6+ |
| Native Java (Java 8 legacy) | Java 8, Maven (version 7.5.2-j8 only) |
| Browser sandbox | Docker, 2 GB RAM minimum, 2 CPU cores |
| .NET legacy | IKVM libraries, .NET 4.x framework (version 7.5.2 only) |

---

## Further links

### Repository files

- [RELEASE_NOTES.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/RELEASE_NOTES.md) — release history
- [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md) — detailed changelog for the examples and the JOpt library
- [FAQ.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/FAQ.md) — frequently asked questions
- [Sandboxes.md](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer/blob/main/Sandboxes.md) — overview of all available sandbox environments

### Documentation

- [Getting started](https://www.dna-evolutions.com/docs/getting-started)
- [Special features overview](https://dna-evolutions.com/docs/learn-and-explore/special/special_features)
- [Optimization properties](https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/optimization_properties)
- [Interactive API testing](https://dna-evolutions.com/api/)
- [JavaDocs (online)](https://public.javadoc.dna-evolutions.com)

### Evaluation license

Get an extended free license designed for small businesses and customers evaluating the product:  
[DNA Evolutions Portal](https://www.dna-evolutions.com/portal) *(sign-in required)*

### Code and registry

- [Docker REST TourOptimizer](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer) — containerised REST API
- [Nexus repository](https://nexus.dna-evolutions.net) — all JOpt releases

### Social

- [LinkedIn](https://www.linkedin.com/company/dna-evolutions/)
- [Docker Hub](https://hub.docker.com/u/dnaevolutions)
- [YouTube tutorials](https://www.youtube.com/channel/UCzfZjJLp5Rrk7U2UKsOf8Fw)
- [SourceForge](https://sourceforge.net/software/product/JOpt.TourOptimizer/)

---

## Contact

For help or questions reach us at [www.dna-evolutions.com/contact](https://www.dna-evolutions.com/contact) or [info@dna-evolutions.com](mailto:info@dna-evolutions.com).

---

## Agreement

For license terms and plans please visit [www.dna-evolutions.com](https://www.dna-evolutions.com).

---

A product by [DNA Evolutions GmbH](https://www.dna-evolutions.com) &copy;