# Release Notes for Core Release 7.5.1

We are excited to announce the release of version 7.5.1 of our core library. This version marks a significant transition and introduces new features, enhancements, and bug fixes. Here's what's new:

## Major Changes
- **Java Version Upgrade**: Our core library has been moved from Java 8 to Java 17. This will be the **last version to include a Java 8 compatible version** along with a corresponding legacy dll version. Future updates will require users who are still on Java 8 or prefer to use dll to switch to our JOpt.TourOptimizer, which is a Spring Application with a Swagger interface. This allows for building clients in a desired language and version.

## New Additions
- **Python Example Client**: We've added a new example client in Python for JOpt.TourOptimizer, providing more flexibility and accessibility for Python developers.

- **New Core Library Features**:
  - We are introducing two new features in our core library:
    - The first feature includes the use of **skills with skill levels and cost models**. 
    - The second feature introduces the **extra-info functionality**, which allows for saving custom strings in an OptimizationElement, such as a Node or a Resource.
  - Standalone examples for these new features will be available in our [Java-TourOptimizer-Examples GitHub repository](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples).

## Improvements
- **Core Library Bug Fixes**: Numerous bugs have been addressed in our core library. A detailed changelog is available [here](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md).

- **DEMO Angular Application Update**: The DEMO application at [demo.dna-evolutions.com](https://demo.dna-evolutions.com/) has been updated to the latest version of Angular. A new feature allows users to drag and drop a fixed number of nodes and resources on a map for optimization.

- **JOpt.TourOptimizer and Other Updates**: Our JOpt.TourOptimizer has been updated to the latest version of Spring, along with many other dependencies. We've also introduced our latest TypeScript REST-Client.

- **REST-Client Examples Update**: We've updated our REST-client examples for Java and C# to serve as a starting point for our customers. These can be found in the Java-REST-Client-Examples and C-Sharp-REST-Client-Examples repositories.

## Additional Notes
- We've made many other minor improvements and optimizations across our platform to enhance performance and user experience.

As always, we are committed to providing high-quality and up-to-date software solutions. We encourage our users to migrate to the newer versions to take full advantage of the latest features and improvements.

---
For a complete list of changes, please refer to the [CHANGELOG.md](https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/CHANGELOG.md).
