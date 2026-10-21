package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel;
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
import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel.custom.OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction;

import tech.units.indriya.quantity.Quantities;

/**
 * Attaches a custom route-level restriction: a route with an even number of nodes costs a
 * penalty.
 *
 * <p>Scenario: Jack starts in Aachen and visits 19 nodes in nine places in Germany on five days,
 * May 6 to May 10, 2020, from 08:00 to 17:00 (Europe/Berlin). Several nodes share a place, for
 * example Koeln1 to Koeln3. The restriction CustomRouteWithOddNumberOfElementsRestriction, which
 * the scheme OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction attaches,
 * penalizes each route with an even number of nodes.
 *
 * <p>Look for: the number of nodes of each route in the result, and the violation summary of each
 * route: an even route would show the violation "NOT ODD NUMBER OF ELEMENTS".
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/OpenAssessorRouteLevel.md">OpenAssessorRouteLevel.md</a>
 */
public class CustomRouteLevelOddNumberOfElementsRestrictionExample extends Optimization {

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
    new CustomRouteLevelOddNumberOfElementsRestrictionExample().example();
  }

  @Override
  public String toString() {
    return "Attaches a custom route-level restriction: a route with an even number of nodes costs"
        + " a penalty.";
  }

  /**
   * Builds the optimization, runs it and prints the result.
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
    setScheme(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Sets the scheme that attaches the custom restriction.
   *
   * @param opti the optimization
   */
  private static void setScheme(IOptimization opti) {
    opti.setOptimizationScheme(
        new OpenCostAssessorOptimizationSchemeWithOddNumberOfElementsRestriction(opti));
  }

  /**
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds 19 nodes in nine places with the same opening hours and visit duration.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    for (int day = 6; day <= 10; day++) {
      openingHours.add(
          new OpeningHours(
              ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
              ZonedDateTime.of(
                  2020, MAY.getValue(), day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    }

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // Nodes with the same coordinates stand for several jobs in one place. The example runs with
    // the public evaluation license.
    List<INode> nodes = new ArrayList<>();
    nodes.add(createNode("Koeln1", 50.9333, 6.95, openingHours, visitDuration, importance));
    nodes.add(createNode("Koeln2", 50.9333, 6.95, openingHours, visitDuration, importance));
    nodes.add(createNode("Koeln3", 50.9333, 6.95, openingHours, visitDuration, importance));
    nodes.add(createNode("Oberhausen1", 51.4667, 6.85, openingHours, visitDuration, importance));
    nodes.add(createNode("Oberhausen2", 51.4667, 6.85, openingHours, visitDuration, importance));
    nodes.add(createNode("Essen1", 51.45, 7.01667, openingHours, visitDuration, importance));
    nodes.add(createNode("Essen2", 51.45, 7.01667, openingHours, visitDuration, importance));
    nodes.add(createNode("Essen3", 51.45, 7.01667, openingHours, visitDuration, importance));
    nodes.add(createNode("Dueren1", 50.8, 6.48333, openingHours, visitDuration, importance));
    nodes.add(createNode("Dueren2", 50.8, 6.48333, openingHours, visitDuration, importance));
    nodes.add(createNode("Nuernberg1", 49.4478, 11.0683, openingHours, visitDuration, importance));
    nodes.add(createNode("Heilbronn1", 49.1403, 9.22, openingHours, visitDuration, importance));
    nodes.add(createNode("Heilbronn2", 49.1403, 9.22, openingHours, visitDuration, importance));
    nodes.add(createNode("Heilbronn3", 49.1403, 9.22, openingHours, visitDuration, importance));
    nodes.add(createNode("Stuttgart1", 48.7667, 9.18333, openingHours, visitDuration, importance));
    nodes.add(createNode("Wuppertal1", 51.2667, 7.18333, openingHours, visitDuration, importance));
    nodes.add(createNode("Wuppertal2", 51.2667, 7.18333, openingHours, visitDuration, importance));
    nodes.add(createNode("Wuppertal3", 51.2667, 7.18333, openingHours, visitDuration, importance));
    nodes.add(createNode("Aachen1", 50.775346, 6.083887, openingHours, visitDuration, importance));

    nodes.forEach(opti::addElement);
  }

  /**
   * Creates one node.
   *
   * @param id the id of the node
   * @param latitude the latitude
   * @param longitude the longitude
   * @param openingHours the opening hours
   * @param visitDuration the visit duration
   * @param importance the importance
   * @return the node
   */
  private static INode createNode(
      String id,
      double latitude,
      double longitude,
      List<IOpeningHours> openingHours,
      Duration visitDuration,
      int importance) {
    return new TimeWindowGeoNode(id, latitude, longitude, openingHours, visitDuration, importance);
  }

  /**
   * Adds Jack, who starts and ends each working day in Aachen.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    for (int day = 6; day <= 10; day++) {
      workingHours.add(
          new WorkingHours(
              ZonedDateTime.of(2020, MAY.getValue(), day, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
              ZonedDateTime.of(
                  2020, MAY.getValue(), day, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    }

    // 13 h per day, more than the longest day of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(13);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
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
