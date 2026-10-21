package com.dna.jopt.touroptimizer.java.examples.advanced.visitdurationefficiency;
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
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;
import static java.time.Month.MAY;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

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

/**
 * Shortens the visits of one resource with a visit duration efficiency factor, limited by minimal
 * visit durations and by nodes with a fixed duration.
 *
 * <p>Scenario: Jack and Clara start in Aachen and work on May 6 and May 7, 2020, from 08:00 to
 * 17:00 (Europe/Berlin). Four nodes with a visit of 30 minutes each: Koeln and Oberhausen let the
 * visit change with the resource, down to a minimal visit duration of 20 minutes; Essen - fixed
 * duration keeps its 30 minutes; Dueren lets the visit change and has no minimal visit duration
 * of its own. Clara has the visit duration efficiency factor 0.5, Jack has none.
 *
 * <p>Look for: Clara's route with all four nodes and the durations of the visits: 15 minutes at
 * Dueren, 20 minutes at Koeln and Oberhausen, 30 minutes at Essen - fixed duration.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/visitdurationefficiency/ResourceVisitDurationEfficiency.md">ResourceVisitDurationEfficiency.md</a>
 */
public class ResourceVisitDurationEfficiencyExample extends Optimization {

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
    new ResourceVisitDurationEfficiencyExample().example();
  }

  @Override
  public String toString() {
    return "Shortens the visits of one resource with a visit duration efficiency factor, limited"
        + " by minimal visit durations and by nodes with a fixed duration.";
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
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
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
   * Adds four nodes: two with a minimal visit duration, one with a fixed duration and one whose
   * visit follows the factor alone.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // The visit duration that the efficiency factor of the visiting resource scales.
    Duration visitDuration = Duration.ofMinutes(30);

    // The lower limit for Koeln and Oberhausen: Clara's factor of 0.5 would give 15 minutes.
    Duration minVisitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // Koeln and Oberhausen let the visit change with the resource, but not below 20 minutes.
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln.setHasRouteDependentVisitDuration(true);
    koeln.setMinimalVisitDuration(minVisitDuration);
    opti.addElement(koeln);

    TimeWindowGeoNode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausen.setHasRouteDependentVisitDuration(true);
    oberhausen.setMinimalVisitDuration(minVisitDuration);
    opti.addElement(oberhausen);

    // Without setHasRouteDependentVisitDuration(true), the visit keeps its 30 minutes, whoever
    // the visitor is.
    INode essen =
        new TimeWindowGeoNode(
            "Essen - fixed duration", 51.45, 7.01667, openingHours, visitDuration, importance);
    opti.addElement(essen);

    // Dueren lets the visit change and sets no minimal visit duration: Clara's factor takes the
    // visit from 30 to 15 minutes.
    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
    dueren.setHasRouteDependentVisitDuration(true);
    opti.addElement(dueren);
  }

  /**
   * Adds Clara, whose visits take half the time where a node allows it, and Jack.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // 9 h per day, the length of the working hours.
    Duration maxWorkingTime = Duration.ofHours(9);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource clara =
        new CapacityResource(
            "Clara", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    // Clara needs half the visit duration at nodes that let the visit change with the resource.
    // The setter refuses a factor of 0 or below and returns false.
    if (!clara.setOverallVisitDurationEfficiencyFactor(0.5)) {
      System.out.println("Visit duration efficiency factor not set for Clara");
    }
    opti.addElement(clara);

    // Jack has no factor, so his visits take the visit duration of the node.
    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    opti.addElement(jack);
  }

  /**
   * Creates the working hours of a resource: May 6 and May 7, 2020, from 08:00 to 17:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
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
