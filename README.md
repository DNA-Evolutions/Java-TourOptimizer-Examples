# Java-JOpt-TourOptimizer-Examples
This repository is part of our JOpt-TourOptimizer-Sweet for Java. It includes an extensive collection of examples (written in Java). This fully functional Maven project can be cloned and can be used as base for starting with JOpt-TourOptimizer.

The project is roughly subdivided into three major types of examples:

1. Basic Examples
2. Advanced Examples
3. Expert Examples

Each of the examples sections has its own README.

# Further Documentation and Links

- Further documentation - <a href="https://docs.dna-evolutions.com" target="_blank">docs.dna-evolutions.com</a>
- Our company website - <a href="https://www.dna-evolutions.com" target="_blank">www.dna-evolutions.com</a>
- Our official repository - <a href="https://public.repo.dna-evolutions.com" target="_blank">public.repo.dna-evolutions.com</a>
- Our official javadocs - <a href="https://public.javadoc.dna-evolutions.com" target="_blank">public.javadoc.dna-evolutions.com</a>

# Short Introduction
JOpt is a flexible routing optimization-engine written in Java, allowing to solve tour-optimization problems that are highly restricted, for example, regarding time windows, skills, and even mandatory constraints can be applied.

## Getting Started

### Clone this repository
Clone this repository and start any example file.

### Download the jar
The latest native java library of JOpt-TourOptimizer can be either downloaded via our official
<a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus repository</a>, from our <a href="https://www.dna-evolutions.com/" target="_blank">company website</a> or as direct download from here (always links the latest release):

- <a href="https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=shaded" target="_blank">Shaded jar</a>
- <a href="https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=javadoc" target="_blank">Javadoc jar</a>

### As Dependency (Recommended)
However, it is recommended to use our nexus-endpoint as repository and download the jars as dependency into your project.

### Snippet for Maven

For adding the JOpt dependency to your ``pom.xml`` you can use the following snippet (for help on how to set dependencies please visit the <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html" target="_blank">offical maven documentation</a>):


```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.4.2</version>
  <classifier>shaded</classifier>
</dependency>
```

### JavaDocs

In case you want to add our JavaDocs to your project, further add the following dependency:

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.4.2</version>
  <classifier>javadoc</classifier>
</dependency>
```

(The latest JavaDocs version is also available online as <a href="https://public.javadoc.dna-evolutions.com/" target="_blank">browsable page</a>.)

### Repository

In addition, it is mandatory to add our nexus server as a repository source (for help please visit the <a href="https://maven.apache.org/guides/introduction/introduction-to-repositories.html" target="_blank">offical maven documentation</a>).

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

In case you use *Gradle*, *SBT*, *IVY*, *Grape*, *Leiningen*, *Builder*, or others you can browse our <a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus repository</a>, select the desired dependency and look out for the Usage container. Alternatively, you can use an online conversion tool, to convert the maven dependency into your desired format. Please keep in mind, that you will have to add our repository in any case.


### Prerequisites

* As native Java dependency - Install at least Java 8
