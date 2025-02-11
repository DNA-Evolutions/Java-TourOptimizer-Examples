# Release Notes



## **Overview**
- [Release Notes for Core Release 7.5.2](#release-notes-for-core-release-7-5-2)
- [Release Notes for Core Release 7.5.1](#release-notes-for-core-release-7-5-1)
- [General Notes](#general-notes)

---

# **Release Notes for Core Release 7.5.2**

We are excited to announce the release of version **7.5.2** of our **JOpt Core Library**. This version introduces significant **improvements, new features, and optimizations** that enhance stability, performance, and user experience. Below is a detailed overview of what's new.

---

## **Before You Start**
To help you integrate our JOpt library effectively, please visit:
- **[Java Example Repository](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples)** – Learn how to integrate JOpt **natively** into your **Java application**.
- **[REST Client Example Repositories](https://github.com/DNA-Evolutions)** - Find REST client examples for various languages.

If you are developing a **REST application**, check out our **updated OpenAPI schema** for better **client generation support**.

---

## **Major Changes and Achievements**
### **Integration of LLMs (Large Language Models)**
JOpt has always utilized **machine learning** and **intelligent algorithms** at its core. Over the past year, we have made substantial improvements in terms of **stability, user experience, and AI-driven enhancements**. We are also actively developing **LLM integrations** with JOpt to provide **better support and accelerate development**.

**Join Our Closed Beta Program**  
We are currently running a **closed beta program** for our **LLM-powered features**. If you are interested, please reach out to us.  

**Find example training questions [here](https://shared.dna-evolutions.com/share/ai/trainig_questions/public/example/jopt.ai.example.training.questions.jsonl)**.

---

## **Updated REST API Specification**
Generating a REST client based on a complex schema can be challenging. To improve the experience, we now provide **two different schema definitions**, starting from **JOpt TourOptimizer schema version 1.3.0-SNAPSHOT**:

1. **Full Schema**  
   - **Includes the original stricter definition** with additional `"oneOf"` keys.  
   - **Generated with Springdoc 2.8.4**.  
   - **[Download Full Schema](https://github.com/DNA-Evolutions/Java-REST-Client-Examples/blob/master/src/main/resources/swagger/touroptimizer/spec/touroptimizer_spec.json)**.  
   - Works with the **latest Java OpenAPI Generator (7.11.0)** as a **Maven dependency**.

2. **Cleaned Schema**  
   - **Same as the full schema but without `"oneOf"` keys**.  
   - **[Download Cleaned Schema](https://github.com/DNA-Evolutions/Java-REST-Client-Examples/blob/master/src/main/resources/swagger/touroptimizer/spec/touroptimizer_spec_cleaned.json)**.  
   - Works with the **latest @openapitools/openapi-generator-cli (v2.16.3) for Angular**.  
   - Works with the **latest Docker image openapitools/openapi-generator-cli (v7.11.0) for C# and Python**.  

---

## **New Core Library Features**
This release introduces **three powerful new features** to improve optimization efficiency and flexibility.

### **1. Optimization Solution Comparison Tool**  
Our most sophisticated feature this year.

The **Optimization Solution Comparison Tool** allows users to **modify an existing optimization result** and compare it with an adjusted version. This is particularly useful in **real-world optimization scenarios**, where customers may **prefer different routing decisions** due to **visual preferences** or **business constraints**.  

- **Why is this useful?**  
  - Customers often expect **routes to follow a direct geographical path**, but the optimizer may select **a more optimal route** that considers **time windows, constraints, and restrictions**.  
  - This tool allows for **comparing the original optimizer result** with a **modified version** (e.g., changing visit orders of nodes).  
  - The tool generates a **comparison report**, showing the impact on **costs, violations, and performance**.

**Read more about this feature in our [docs](https://docs.dna-evolutions.com/overview_docs/comparison_tool/comparison_tool.html).** Further, get an **[example](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/).**

---

### **2. Performance Mode (Faster Optimization)**
We introduce **Performance Mode**, an **optional setting** that allows optimizations to run **up to 50% faster**.

- **How does it work?**  
  - **Reduces the number of concurrent solutions** during genetic evolution.  
  - **Disables certain optimization operators** that increase computational overhead.  
  - Ideal for **large-scale problems with fewer constraints** (e.g., **long working hours and fewer restrictions**).  

- **When to use it?**  
  - When **speed is a priority** over absolute precision.  
  - When the optimization **does not require highly constrained solutions**.  
  - Suitable for **fleet optimizations with flexible schedules**.

**Enable it with this property:**
```java
props.setProperty("JOpt.PerformanceMode", "true");
```
**Read more about Performance Mode in our [docs](https://docs.dna-evolutions.com/overview_docs/performancemode/performance_mode.html).** Further, get an **[example](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/performancemode/).**

---

### **3. Zone Crossing Penalty**
In many real-world scenarios, **crossing between different zones** (e.g., via **bridges or tunnels**) should be minimized due to **time delays and additional costs**.  

- The **Zone Crossing Penalty** feature allows users to **assign a cost penalty** when a resource **crosses a defined zone boundary**.  
- This ensures that crossings only occur **when absolutely necessary**, making the routes more **practical and cost-effective**.

- **How does it work?**  
  - The optimizer applies **an additional cost multiplier** whenever a vehicle crosses a zone.  
  - This ensures that **crossings only happen at the beginning and end of a shift**, **reducing unnecessary back-and-forth travel**.

**Enable it with this property:**
```java
props.setProperty("JOpt.Clustering.PenlalizeZoneCodeCrossing", true);
props.setProperty("JOpt.Clustering.PenlalizeZoneCodeCrossingMultiplier", "3.0");
```
**Read more about the Zone Crossing Penalty in our [docs](https://docs.dna-evolutions.com/overview_docs/zonecrossing/zonecrossing.html).**
Further, get an **[example](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/zonecrossing/).**

## Improvements
- **Core Library Bug Fixes**: Numerous bugs have been addressed in our core library. A detailed changelog is available [here](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md).

- **Demo Angular Application Update**: The demo application ([source](https://github.com/DNA-Evolutions/Angular-Demo-Application-Source)) at [demo.dna-evolutions.com](https://demo.dna-evolutions.com/) has been updated to the latest version of Angular **(19.1)**.

- **JOpt.TourOptimizer and Other Updates**: Our JOpt.TourOptimizer ([link on GitHub](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer)) has been updated to the latest version of Spring, along with many other dependencies. In addition, we fixed some schema problems.


**Overview of updated REST Clients with examples:**
- [Java Client](https://github.com/DNA-Evolutions/Java-REST-Client-Examples)
- [C# Client](https://github.com/DNA-Evolutions/C-Sharp-REST-Client-Examples)
- [Angular Client inside Demo App](https://github.com/DNA-Evolutions/Angular-Demo-Application-Source)
- [Python Client](https://github.com/DNA-Evolutions/Python-REST-Client-Examples)


Of course you can create your own client in the language of your choice utilizing our OpenAPI description for JOpt TourOptimizer [here](https://github.com/DNA-Evolutions/Java-REST-Client-Examples/blob/master/src/main/resources/swagger/touroptimizer/spec/touroptimizer_spec.json).

---
---
---

# Previous Releases

---
---
---

# Release Notes for Core Release 7.5.1

We are excited to announce the release of version 7.5.1 of our core library. This version marks a significant transition and introduces new features, enhancements, and bug fixes. Here's what's new:

**Before you start:**
Please visit our [Java example repository](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples) to learn how you can integrate our JOpt library natively in your Java application or visit one of our REST client example repositories if you are planning a REST application in a language of your choice.

## Major Changes
- **Java Version Upgrade**: Our core library has been moved from Java 8 to Java 17. This will be the **last version to include a Java 8 compatible version** along with a corresponding legacy dll version. Future updates will require users who are still on Java 8 or prefer to use dll to switch to our JOpt.TourOptimizer, which is a Spring Application with a Swagger interface. This allows for building clients in a desired language and version.

## New Additions
**Python Example Client**: 
We've added a new [example client in Python ](https://github.com/DNA-Evolutions/Python-REST-Client-Examples) for JOpt.TourOptimizer, providing more flexibility and accessibility for Python developers. Please contact us for more information, as using the client requires some personal instructions (<a href="mailto:info@dna-evolutions.com">info@dna-evolutions.com</a>).

**New Core Library Features**:
  - We are introducing two new features in our core library:
  
- The first feature includes the use of [skills with skill levels and cost models](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/condition/ResourceTypeWithExpertiseConditionAndCostModelExample.java). Please also visit our [docs page](https://docs.dna-evolutions.com/overview_docs/skills/Skill_costmodel.html).
    - The second feature introduces the [extra-info functionality](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/ExtraInfoExample.java), which allows for saving custom strings in an OptimizationElement, such as a Node or a Resource.
  - Standalone examples for these new features will be available in our [Java-TourOptimizer-Examples GitHub repository](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples).
  
  **Fire and Forget mode for JOpt-TourOptimizer - Mongo examples:**
  - [Java Client - Fire and Forget with Mongo](https://github.com/DNA-Evolutions/Java-REST-Client-Examples/tree/master/src/main/java/com/dna/jopt/rest/client/example/touroptimizer/fireandforget)
  

## Improvements
- **Core Library Bug Fixes**: Numerous bugs have been addressed in our core library. A detailed changelog is available [here](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md).

- **Demo Angular Application Update**: The demo application ([source](https://github.com/DNA-Evolutions/Angular-Demo-Application-Source)) at [demo.dna-evolutions.com](https://demo.dna-evolutions.com/) has been updated to the latest version of Angular. A new feature allows users to drag and drop a fixed number of nodes and resources on a map for optimization.

- **JOpt.TourOptimizer and Other Updates**: Our JOpt.TourOptimizer ([link on GitHub](https://github.com/DNA-Evolutions/Docker-REST-TourOptimizer)) has been updated to the latest version of Spring, along with many other dependencies. We've also introduced our latest TypeScript REST-Client.

- **REST-Client Examples Update**: We've updated our REST-client examples for Java, C# and Angular to serve as a starting point for our customers. These can be found on GitHub. Further, we added a new Python client.

**Overview REST Clients with examples: **
- [Java Client](https://github.com/DNA-Evolutions/Java-REST-Client-Examples)
- [C# Client](https://github.com/DNA-Evolutions/C-Sharp-REST-Client-Examples)
- [Angular Client inside Demo App](https://github.com/DNA-Evolutions/Angular-Demo-Application-Source)
- [Python Client](https://github.com/DNA-Evolutions/Python-REST-Client-Examples)

Of course you can create your own client in the language of your choice utilizing our OpenAPI description for JOpt TourOptimizer [here](https://github.com/DNA-Evolutions/Java-REST-Client-Examples/blob/master/src/main/resources/swagger/touroptimizer/spec/touroptimizer_spec.json).


---

## General Notes
- We've made many other minor improvements and optimizations across our platform to enhance performance and user experience.

As always, we are committed to providing high-quality and up-to-date software solutions. We encourage our users to migrate to the newer versions to take full advantage of the latest features and improvements.

---
For a complete list of changes, please refer to the [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md).
