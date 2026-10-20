package com.dna.jopt.touroptimizer.java.examples.basic.io_03;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.importing.IOptimizationImporter;
import com.dna.jopt.io.importing.json.OptimizationJSONImporter;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Loads an optimization from a BZip2-compressed JSON file but ignores the saved solution, so the
 * run starts fresh.
 *
 * <p>Scenario: the snapshot that SaveOptimizationToJsonExample writes: eight nodes and the two
 * resources Jack and John on March 6 and 7, 2020, time zone Europe/Berlin, with the properties of
 * that run. The run uses the elements and the properties, not the solution.
 *
 * <p>Look for: the status line OPTIMIZATION_ALGO_STARTING for SmartConstructionAlgo. The run builds
 * its own start solution, so unlike LoadOptimizationFromJsonExample it has no
 * OPTIMIZATION_USING_PROVIDED_SOLUTION line.
 *
 * <p>Prerequisites: None for the license: without a license file the example uses the public
 * evaluation license (see ExampleLicenseHelper). Run SaveOptimizationToJsonExample first, in the
 * same working directory: it writes myopti.json.bz2.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadAndSaveOptimization.md">LoadAndSaveOptimization.md</a>
 */
public class LoadOptimizationToFreshRunExample extends Optimization {

  private static final String JSON_FILE = "myopti.json.bz2";

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
    new LoadOptimizationToFreshRunExample().example();
  }

  @Override
  public String toString() {
    return "Loads an optimization from a BZip2-compressed JSON file but ignores the saved "
        + "solution, so the run starts fresh.";
  }

  /**
   * Loads the optimization without its solution, runs it and prints the result.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException {
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    if (!loadFromJson(this)) {
      return;
    }
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Loads the elements and the properties from the JSON file and drops the saved solution.
   *
   * @param opti the optimization
   * @return true if the snapshot was loaded
   * @throws IOException if a file cannot be read or written
   */
  private static boolean loadFromJson(IOptimization opti) throws IOException {
    // With true, all parts of the snapshot are loaded (the elements, the node connector and the
    // properties), but the run does not start from the saved solution.
    boolean ignoreLoadedSolution = true;
    IOptimizationImporter importer = new OptimizationJSONImporter();
    try (InputStream in = new FileInputStream(JSON_FILE)) {
      boolean loaded = importer.update(in, opti, ignoreLoadedSolution);
      if (!loaded) {
        System.out.println("JSON import failed: " + JSON_FILE);
      }
      return loaded;
    } catch (FileNotFoundException e) {
      System.out.println(JSON_FILE + " not found. Run SaveOptimizationToJsonExample first.");
      return false;
    }
  }

  /**
   * Prints the progress, warning, status and error events of the run.
   *
   * @param opti the optimization
   */
  private static void attachToObservables(IOptimization opti) {
    // Subscribe before the start: the subjects replay only a few past events.
    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> System.out.println(p.getProgressString()));
    opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));
    opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));
    opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
  }

  /**
   * Starts the run, waits for the result and prints it.
   *
   * @param opti the optimization
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
  }
}
