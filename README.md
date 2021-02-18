# Java-JOpt-TourOptimizer-Examples


<a href="https://dna-evolutions.com/" target="_blank"><img src="https://docs.dna-evolutions.com/indexres/dna-temp-logo.png" width="110"
title="DNA-Evolutions" alt="DNA-Evolutions"></a>

This repository is part of our JOpt-TourOptimizer-Suite for Java. It includes an extensive collection of examples (written in Java). This fully functional Maven project can be cloned and can be used as a base for starting with JOpt-TourOptimizer.

The project is subdivided into three major types of examples:

1. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic" target="_blank">Basic Examples</a>
2. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced" target="_blank">Advanced Examples</a>
3. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert" target="_blank">Expert Examples</a>

Each of the examples sections has its README.

# Further Documentation and Links

- Further documentation 	- <a href="https://docs.dna-evolutions.com" target="_blank">docs.dna-evolutions.com</a>
- Special features 	- <a href="https://docs.dna-evolutions.com/overview_docs/special_features/Special_Features.html" target="_blank">Overview of special features</a>
- Our company website 	- <a href="https://www.dna-evolutions.com" target="_blank">www.dna-evolutions.com</a>
- Our official repository 	- <a href="https://public.repo.dna-evolutions.com" target="_blank">public.repo.dna-evolutions.com</a>
- Our official JavaDocs 		- <a href="https://public.javadoc.dna-evolutions.com" target="_blank">public.javadoc.dna-evolutions.com</a>
- Our YouTube channel - <a href="https://www.youtube.com/channel/UCzfZjJLp5Rrk7U2UKsOf8Fw" target="_blank">DNA Tutorials</a>
- Documentation on DNA's <a href="https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html" target="_blank">RESTful Spring-TourOptimizer in Docker </a>


# Short Introduction
JOpt is a flexible routing optimization-engine written in Java, allowing to solve tour-optimization problems that are highly restricted, for example, regarding time windows, skills, and even mandatory constraints can be applied.

## Getting Started

### Clone this repository
Clone this repository and start any example file.

### Download the jar
The latest native java library of JOpt-TourOptimizer can be either downloaded via our official
<a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus repository</a>, from our <a href="https://www.dna-evolutions.com/" target="_blank">company website</a> or as a a direct download from here (always links the latest release):

- <a href="https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=shaded" target="_blank">Shaded jar</a>
- <a href="https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=javadoc" target="_blank">Javadoc jar</a>

### As Dependency (Recommended)
However, it is recommended to use our nexus-endpoint as a repository and download the jars as a dependency into your project.

### Snippet for Maven

**We are recommending always using the latest version of JOpt.**

For adding the JOpt dependency to your ``pom.xml`` you can use the following snippet (for help on how to set dependencies, please visit the <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html" target="_blank">official Maven documentation</a>):


```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.4.4</version>
  <classifier>shaded</classifier>
</dependency>
```

or latest (recommended)

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.4.5-rc10</version>
  <classifier>shaded</classifier>
</dependency>
```

**We are recommending always using the latest version of JOpt (rc).**

### JavaDocs

In case you want to add our JavaDocs to your project, further add the following dependency:

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.4.4</version>
  <classifier>javadoc</classifier>
</dependency>
```

or latest

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.4.5-rc10</version>
  <classifier>javadoc</classifier>
</dependency>
```

(The latest JavaDocs version is also available online as a <a href="https://public.javadoc.dna-evolutions.com/" target="_blank">browsable page</a>.)

### Repository

In addition, it is mandatory to add our nexus-server as a repository source (for help, please visit the <a href="https://maven.apache.org/guides/introduction/introduction-to-repositories.html" target="_blank">official Maven documentation</a>).

In your ``pom.xml`` add the following repository:

```xml
<repository>
	<id>jopt4-maven</id>
	<url>https://public.repo.dna-evolutions.com/repository/maven-public/</url>
	<releases>
		<enabled>true</enabled>
	</releases>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```

<br>

### Non-Maven projects

In case you use *Gradle*, *SBT*, *IVY*, *Grape*, *Leiningen*, *Builder*, or others, you can browse our <a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus-repository</a>, select the desired dependency, and look out for the Usage container. Alternatively, you can use an online conversion tool to convert the Maven dependency into your desired format. Please keep in mind that you will have to add our repository in any case.


### Prerequisites

* As native Java dependency - Install at least Java 8
