package com.dna.jopt.touroptimizer.java.examples.expert.buildinfo;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2023 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */
import java.util.List;
import java.util.Properties;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;

/**
 * Prints the version number and the build time of the JOpt core library in use.
 *
 * <p>Scenario: no optimization runs. The example reads the build properties of the core library
 * with getCoreVersionProperties and prints two of them.
 *
 * <p>Look for: the line with jopt.core.jopt.version.number and the line with
 * jopt.core.build.time.utc.
 *
 * <p>Prerequisites: None: the example runs no optimization and needs no license.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/buildinfo/ExtractBuildInfo.md">ExtractBuildInfo.md</a>
 */
public class ExtractBuildInfoExample {

  /** The build properties that the example prints: the version number and the build time. */
  private static final List<String> PRINTED_KEYS =
      List.of("jopt.core.jopt.version.number", "jopt.core.build.time.utc");

  /**
   * Runs the example.
   *
   * @param args not used
   */
  public static void main(String[] args) {
    new ExtractBuildInfoExample().example();
  }

  @Override
  public String toString() {
    return "Prints the version number and the build time of the JOpt core library in use.";
  }

  /** Reads the build properties of the core library and prints two of them. */
  public void example() {
    // A new optimization is enough: the build properties come from the core library, and nothing
    // runs.
    IOptimization opti = new Optimization();
    printBuildInfo(opti);
  }

  /**
   * Prints the version number and the build time from the build properties of the core library.
   *
   * @param opti the optimization
   */
  private static void printBuildInfo(IOptimization opti) {
    Properties coreVersionProperties = opti.getCoreVersionProperties();
    // The properties hold more keys. The version number and the build time identify the core
    // build in a log or in a support request.
    for (String key : PRINTED_KEYS) {
      System.out.println(key + " = " + coreVersionProperties.getProperty(key));
    }
  }
}
