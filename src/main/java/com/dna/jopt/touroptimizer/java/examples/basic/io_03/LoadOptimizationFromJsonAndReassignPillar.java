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
import static java.time.Month.MARCH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
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
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.IPillarNode;
import com.dna.jopt.member.unit.node.geo.PillarTimeWindowGeoNode;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Loads a saved optimization state from a JSON snapshot and adds a pillar node to its solution
 * through reassignment.
 *
 * <p>Scenario: the snapshot myopti.json.bz2 of SaveOptimizationToJsonExample (Jack and John from
 * Aachen and eight TimeWindowGeoNodes on March 6 and 7, 2020, Europe/Berlin), plus the pillar
 * "Koeln-Pillar" in Koeln, open on March 7, 2020, from 08:00 to 17:00, with a visit of 20 minutes.
 *
 * <p>Look for: the status events OPTIMIZATION_USING_PROVIDED_SOLUTION and ADDITIONAL_ELEMENT
 * ("Reassigning Nodes: Koeln-Pillar"), then the line of "Koeln-Pillar" in the result. It is marked
 * "Pillar" and lies on a route of March 7, inside its time window.
 *
 * <p>Prerequisites: None for the license: without a license file the example uses the public
 * evaluation license (see ExampleLicenseHelper). Run SaveOptimizationToJsonExample first, in the
 * same working directory: it writes myopti.json.bz2.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/io_03/LoadOptimizationFromJsonAndReassignPillar.md">LoadOptimizationFromJsonAndReassignPillar.md</a>
 */
public class LoadOptimizationFromJsonAndReassignPillar extends Optimization {

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
    new LoadOptimizationFromJsonAndReassignPillar().example();
  }

  @Override
  public String toString() {
    return "Loads a saved optimization state from a JSON snapshot and adds a pillar node to its"
        + " solution through reassignment.";
  }

  /**
   * Loads the optimization, adds the pillar, runs it and prints the result.
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
    addReassignPillar(this);
    // The snapshot brings its own properties, and addElement(Properties) replaces them. The
    // properties of the new run are therefore added after the load.
    addProperties(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Loads the elements, the properties and the solution from the JSON file.
   *
   * @param opti the optimization
   * @return true if the snapshot was loaded
   * @throws IOException if a file cannot be read or written
   */
  private static boolean loadFromJson(IOptimization opti) throws IOException {
    IOptimizationImporter importer = new OptimizationJSONImporter();
    try (InputStream in = new FileInputStream(JSON_FILE)) {
      boolean loaded = importer.update(in, opti);
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
   * Adds the pillar "Koeln-Pillar" as a reassign node.
   *
   * @param opti the optimization
   */
  private static void addReassignPillar(IOptimization opti) {
    IOpeningHours openingHours =
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    IPillarNode koelnPillar =
        new PillarTimeWindowGeoNode("Koeln-Pillar", 50.9333, 6.95, openingHours, visitDuration);

    // Unlike addElement, addReassignNodes puts the pillar into the loaded solution.
    opti.addReassignNodes(List.of(koelnPillar));
  }

  /**
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();

    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");

    opti.addElement(props);
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
