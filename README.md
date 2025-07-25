# Java-JOpt-TourOptimizer-Examples


<a href="https://dna-evolutions.com/" target="_blank"><img src="https://docs.dna-evolutions.com/indexres/dna-temp-logo.png" width="110"
title="DNA-Evolutions" alt="DNA-Evolutions"></a>

This repository is part of our JOpt-TourOptimizer-Suite for Java. It includes an extensive collection of examples (written in Java). This fully functional Maven project can be cloned and used as a base for starting with JOpt-TourOptimizer. Further, a sandbox can be utilized (requiring a running Docker environment), lifting the challenge to set up an IDE.


# Contact

If you need any help, please contact us via our company website <a href="https://www.dna-evolutions.com" target="_blank">www.dna-evolutions.com</a> or write an email to <a href="mailto:info@dna-evolutions.com">info@dna-evolutions.com</a>.


# **Outline**

- [Introduction](#java-jopt-touroptimizer-examples) - Overview of the repository and its role in the JOpt-TourOptimizer-Suite.  
- [Contact](#contact) - How to reach out for help or inquiries.  
- [Architecture](#architecture) - Breakdown of the four major example categories (Basic, Advanced, Expert, RESTful).  
- [Further Documentation and Links](#further-documentation-and-links) - Quick access to official documentation, JavaDocs, DockerHub, SourceForge, and more.  
- [Short Introduction](#short-introduction) - Overview of JOpt and its capabilities in solving tour optimization problems.  
- [Getting Started](#getting-started-with-the-examples) - Different ways to begin using JOpt-TourOptimizer examples.  
- [Using the Sandbox](#use-our-sandbox-in-your-browser-docker-required) - Steps to set up and run the sandbox using Docker.  
- [Cloning the Repository](#clone-this-repository) - Instructions for cloning and using the examples.  
- [Downloading Dependencies](#download-the-jar-directly-or-as-dependency) - How to download the latest JOpt-TourOptimizer JAR or include it as a Maven dependency.  
- [Legacy Support](#java-8-legacy-version) - Java 8 compatibility details and .NET legacy support.  
- [Non-Maven Projects](#non-maven-projects) - Using JOpt with Gradle, SBT, IVY, and other build tools.  
- [Prerequisites](#prerequisites) - System requirements for running JOpt-TourOptimizer.  
- [License Agreement](#agreement) - Information on licensing and terms.  
- [Authors](#authors) - DNA-Evolutions company information.  



# Architecture

This Java project is subdivided into four major types of examples:

1. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic" target="_blank">Basic Examples</a>
2. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced" target="_blank">Advanced Examples</a>
3. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert" target="_blank">Expert Examples</a>
4. <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/tree/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/restful" target="_blank">RESTful Examples</a>

Each of the example-sections has its own README. 


# Further Documentation and Links

- Our website - <a href="https://www.dna-evolutions.com" target="_blank">www.dna-evolutions.com</a>
- Further documentation 	- <a href="https://docs.dna-evolutions.com" target="_blank">docs.dna-evolutions.com</a>
- Special features 	- <a href="https://docs.dna-evolutions.com/overview_docs/special_features/Special_Features.html" target="_blank">Overview of special features</a>
- Our official repository 	- <a href="https://public.repo.dna-evolutions.com" target="_blank">public.repo.dna-evolutions.com</a>
- Our official JavaDocs 		- <a href="https://public.javadoc.dna-evolutions.com" target="_blank">public.javadoc.dna-evolutions.com</a>
- Our YouTube channel - <a href="https://www.youtube.com/channel/UCzfZjJLp5Rrk7U2UKsOf8Fw" target="_blank">DNA Tutorials</a>
- Documentation - <a href="https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html" target="_blank">DNA's RESTful Spring-TourOptimizer in Docker </a>
- Our DockerHub channel - <a href="https://hub.docker.com/u/dnaevolutions" target="_blank">DNA DockerHub</a>
- Our LinkedIn channel - <a href="https://www.linkedin.com/company/dna-evolutions/" target="_blank">DNA LinkedIn</a>
- Our Sourceforge channel - <a href="https://sourceforge.net/software/product/JOpt.TourOptimizer/?pk_campaign=badge&amp;pk_source=vendor" target="_blank">DNA SourceForge</a>

The release notes of this repository
<a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/RELEASE_NOTES.md" target="_blank">RELEASE_NOTES.md</a>.

The changelog of this repository and the underlying JOpt library is available in 
<a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md" target="_blank">CHANGELOG.md</a>.

The FAQ of this repository is available in 
<a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/FAQ.md" target="_blank">FAQ.md</a>.

Overview of available sandboxes is available in <a href="https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer/blob/main/Sandboxes.md" target="_blank">Sandboxes.md</a>.


<a href="https://sourceforge.net/software/product/JOpt.TourOptimizer/?pk_campaign=badge&amp;pk_source=vendor" target="_blank" rel="nofollow">
		<img alt="Partner 2025" src="https://sourceforge.net/cdn/syndication/badge_img/3636727/light-partner" height="120px" width="120px;"></a>


# Short Introduction
JOpt is a flexible routing optimization-engine written in Java, allowing to solve tour-optimization problems that are highly restricted, for example, regarding time windows, skills, and even mandatory constraints can be applied.

Click, to open video:

<a href="https://www.youtube.com/watch?v=U4mDQGnZGZs" target="_blank"><img src="https://dna-evolutions.com/wp-content/uploads/2022/10/jopt_intro_prev.gif" width="600"
title="Introduction Video for DNA's JOpt" alt="Introduction Video for DNA's JOpt"></a>

## Getting Started with the Examples

You can start using our example in different ways.

* [Use our sandbox in your browser (Docker required)](#use-our-sandbox-in-your-browser-docker-required)
* [Clone this repository](#clone-this-repository)
* [Download the Jar directly or as Dependency](#download-the-jar-directly-or-as-dependency)
* [Download our .NET legacy version](#download-our-net-legacy-version)

## Use our sandbox in your browser (Docker required)

If you need help setting up docker, you can follow the [official installation guide](https://docs.docker.com/get-docker/).

In case you want to get started without the hassle of installing Java, Maven and an IDE, we provide a sandbox. The sandbox is based on  [code-server](https://github.com/cdr/code-server) and can be used inside your browser, the interface itself is based on Visual Code. The sandbox is available via DockerHub ([here](https://hub.docker.com/r/dnaevolutions/jopt_example_server)). You have to host the sandbox in your Docker environment (Please provide at least 2-4Gb of Ram and 2 Cores). You can pull the sandbox from our DockerHub account (The Dockerfile for creating the sandbox is included in this repository). The latest version of our examples is cloned by default on launching the Docker container, and you can start testing JOpt right away.

Preview (click to enlarge):

<a href="https://docs.dna-evolutions.com/indexres/coderserver.png" target="_blank"><img src="https://docs.dna-evolutions.com/indexres/coderserver.png" width="85%"
title="Preview of JOpt-Example-Server"></a>

### Starting the sandbox and persist your changes
You must mount a volume to which the examples of this project are downloaded on the container's startup. After re-launching the container, the latest version of our examples is only cloned if the folder is not already existing, keeping your files safe from being overridden.

Launching a sanbox and mount your current directory ('$PWD') or any other directory you want:

```
docker run -it -d --name jopt-examples -p 127.0.0.1:8042:8080 -v "$PWD/:/home/coder/project" dnaevolutions/jopt_example_server:latest
```

### Using the sandbox

After starting the container, you can open [http://localhost:8042/](http://localhost:8042) with your browser and login with the password:

```
jopt
```

During the run of your first example file, some dependencies are downloaded, and it will take some time (below 1 minute depending on your internet connection). In case you need help, contact us.

Please visit our **[tutorial video](https://www.youtube.com/watch?v=Jk9ONloaNlk)** (approx. 3 minutes duration) hosted on YouTube on how to use our sandbox.

## Clone this repository
Clone this repository, import it as Maven project in your IDE and start any example.

## Download the Jar directly or as Dependency
The latest native java library of JOpt-TourOptimizer can be either downloaded via our official
<a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus repository</a>, from our <a href="https://www.dna-evolutions.com/" target="_blank">company website</a> or as a a direct download from here (always links the latest release):

- <a href="https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=shaded" target="_blank">Shaded jar</a>
- <a href="https://public.repo.dna-evolutions.com/service/rest/v1/search/assets/download?sort=version&repository=maven-releases&group=jopt&maven.artifactId=jopt.core.pg&maven.extension=jar&maven.classifier=javadoc" target="_blank">Javadoc jar</a>

### As Dependency (Recommended)
However, it is recommended to use our nexus-endpoint as a repository and download the jars as a dependency into your project. You can also search for older versions of JOpt in our <a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus repository</a>.

### Snippet for Maven

**We are recommending always using the latest version of JOpt.**

**Major Changes (version 7.5.1+):**
- **Java Version Upgrade**: Our core library has been moved from Java 8 to Java 17. Version 7.5.2 will be the **last version we guarantee to include a Java 8 compatible version** along with a corresponding legacy **dll** version. 

Future updates will require users who are still on Java 8 or prefer to use dll to switch to our JOpt.TourOptimizer, which is a Spring Application with a Swagger interface. This allows for building clients in a desired language and version.

For adding the JOpt dependency to your ``pom.xml`` you can use the following snippet (for help on how to set dependencies, please visit the <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html" target="_blank">official Maven documentation</a>):


```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.5.2-j17</version>
  <classifier>shaded</classifier>
</dependency>
```

or latest

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.5.2-rc1-j17</version>
  <classifier>shaded</classifier>
</dependency>
```
 

**We are recommending always using the latest version of JOpt (rc) (if present).**

### JavaDocs

In case you want to add our JavaDocs to your project, further add the following dependency:

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.2-j17</version>
  <classifier>javadoc</classifier>
</dependency>
```

or latest

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
   <version>7.5.2-rc1-j17</version>
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

## Java 8 legacy version

Version 7.5.2 will be the **last version to include a Java 8 compatible version** along with a corresponding legacy dll version. Future updates will require users who are still on Java 8 or prefer to use dll to switch to our JOpt.TourOptimizer, which is a Spring Application with a Swagger interface. This allows for building clients in a desired language and version.

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.2-j8</version>
  <classifier>shaded</classifier>
</dependency>
```


and docs:

```xml
<dependency>
  <groupId>jopt</groupId>
  <artifactId>jopt.core.pg</artifactId>
  <version>7.5.2-j8</version>
  <classifier>javadoc</classifier>
</dependency>
```

## Download our .NET legacy version

We still support a legacy .NET version of JOpt. We utilize <a href="https://en.wikipedia.org/wiki/IKVM.NET" target="_blank">IKVM.NET</a> that is effectively a Java framework running on top of the .NET's framework.

Release dll (archived as zip) as download (7.5.2-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.2-SNAPSHOT-with-dep-pg-legacy.zip" target="_blank">JOpt .Net - 7.5.2</a>


The IKVM.NET framework as download:
- <a href="https://shared.dna-evolutions.com/legacy/net/ikvm_env/ikvm-8.1.5717.0.zip" target="_blank">IKVM.NET Framework</a>

### Older versions:

Release dll (archived as zip) as download (7.5.1-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.1-SNAPSHOT-with-dep-pg-legacy.zip" target="_blank">JOpt .Net - 7.5.1</a>

Release dll (archived as zip) as download (7.5.0-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.5.0-SNAPSHOT-with-dep-pg-legacy.zip" target="_blank">JOpt .Net - 7.5.0</a>

Release candidate dll (archived as zip) as download (7.4.9-rc4-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.4.9-rc4-SNAPSHOT-with-dep-pg-legacy.zip" target="_blank">JOpt .Net - 7.4.9-rc4-SNAPSHOT</a>

Release candidate dll (archived as zip) as download (7.4.9-rc2-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy/jopt.core-7.4.9-rc2-SNAPSHOT-with-dep-pg-legacy.zip" target="_blank">JOpt .Net - 7.4.9-rc2-SNAPSHOT</a>


Release dll (archived as zip) as download (7.4.8-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core-7.4.8-with-dep-pg-legacy/jopt.core-7.4.8-with-dep-pg-legacy.zip" target="_blank">JOpt .Net - 7.4.8</a>

Release dll (archived as zip) as download (7.4.6-legacy):
- <a href="https://shared.dna-evolutions.com/legacy/net/jopt.core.pg-7.4.6-shaded/jopt.core.pg-7.4.6-shaded.zip" target="_blank">JOpt .Net - 7.4.6</a>


## Non-Maven projects

In case you use *Gradle*, *SBT*, *IVY*, *Grape*, *Leiningen*, *Builder*, or others, you can browse our <a href="https://public.repo.dna-evolutions.com/#browse/browse:maven-releases" target="_blank">nexus-repository</a>, select the desired dependency, and look out for the Usage container. Alternatively, you can use an online conversion tool to convert the Maven dependency into your desired format. Please keep in mind that you will have to add our repository in any case.


## Prerequisites

* In your IDE as native Java dependency: Install at least Java 17 and Maven
* In our sandbox: Working Docker environment
* Till and including version 7.5.1: Legacy verion Java 8
* In your IDE as .NET legacy version: IKVM libraries imported in your project and a working .NET 4.X Framework.

---

## Agreement
For reading our license agreement and for further information about license plans, please visit <a href="https://www.dna-evolutions.com" target="_blank">www.dna-evolutions.com</a>.

--- 

## Authors
A product by [dna-evolutions ](https://www.dna-evolutions.com)&copy;