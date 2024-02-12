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

**Overview REST Clients wiht examples: **
- [Java Client](https://github.com/DNA-Evolutions/Java-REST-Client-Examples)
- [C# Client](https://github.com/DNA-Evolutions/C-Sharp-REST-Client-Examples)
- [Angular Client inside Demo App](https://github.com/DNA-Evolutions/Angular-Demo-Application-Source)
- [Python Client](https://github.com/DNA-Evolutions/Python-REST-Client-Examples)

Of course you can create your own client in the language of your choice utilizing our OpenAPI description for JOpt TourOptimizer [here](https://github.com/DNA-Evolutions/Java-REST-Client-Examples/blob/master/src/main/resources/swagger/touroptimizer/spec/touroptimizer_spec.json).


## Additional Notes
- We've made many other minor improvements and optimizations across our platform to enhance performance and user experience.

As always, we are committed to providing high-quality and up-to-date software solutions. We encourage our users to migrate to the newer versions to take full advantage of the latest features and improvements.

---
For a complete list of changes, please refer to the [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md).
