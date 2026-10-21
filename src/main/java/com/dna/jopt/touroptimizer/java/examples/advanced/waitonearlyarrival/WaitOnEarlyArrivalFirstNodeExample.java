package com.dna.jopt.touroptimizer.java.examples.advanced.waitonearlyarrival;
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
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Lets a resource start work at once when it arrives early, but makes it wait at the first node
 * of a route.
 *
 * <p>Scenario: Jack starts in Aachen and works from 08:00 to 18:00 on May 6, 2020
 * (Europe/Berlin). Koeln, Essen, Dueren and Aachen are open from 10:00 to 16:00, Wuppertal from
 * 14:00 to 19:00. Every node sets setWaitOnEarlyArrival(false) and
 * setWaitOnEarlyArrivalFirstNode(true).
 *
 * <p>Look for: the first node of the route shows idle time, because Jack waits there until the
 * node opens at 10:00. Wuppertal, later in the route, is reached before it opens at 14:00 and
 * served at once; the result reports this early start as an EARLY violation with a cost (Cost
 * Early).
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/waitonearlyarrival/WaitOnEarlyArrivalFirstNode.md">WaitOnEarlyArrivalFirstNode.md</a>
 */
public class WaitOnEarlyArrivalFirstNodeExample extends Optimization {

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
    new WaitOnEarlyArrivalFirstNodeExample().example();
  }

  @Override
  public String toString() {
    return "Lets a resource start work at once when it arrives early, but makes it wait at the"
        + " first node of a route.";
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
   * Adds five nodes that let work start at once on an early arrival, except as the first node.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    // Koeln, Essen, Dueren and Aachen open at 10:00, two hours after Jack starts work.
    List<IOpeningHours> openingHoursEarly = new ArrayList<>();
    openingHoursEarly.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Wuppertal opens at 14:00, four hours after the other nodes, so Jack can reach it before it
    // opens in the middle of his route.
    List<IOpeningHours> openingHoursLate = new ArrayList<>();
    openingHoursLate.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 14, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 19, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // setWaitOnEarlyArrival(false): on an early arrival, work starts at once instead of waiting
    // for the opening time; the result reports the early start as an EARLY violation with a
    // cost. setWaitOnEarlyArrivalFirstNode(true): as the first node of a route, the node makes
    // Jack wait until it opens. The first-node setting counts only together with
    // setWaitOnEarlyArrival(false).
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHoursEarly, visitDuration, importance);
    koeln.setWaitOnEarlyArrival(false);
    koeln.setWaitOnEarlyArrivalFirstNode(true);
    opti.addElement(koeln);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode(
            "Essen", 51.45, 7.01667, openingHoursEarly, visitDuration, importance);
    essen.setWaitOnEarlyArrival(false);
    essen.setWaitOnEarlyArrivalFirstNode(true);
    opti.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode(
            "Dueren", 50.8, 6.48333, openingHoursEarly, visitDuration, importance);
    dueren.setWaitOnEarlyArrival(false);
    dueren.setWaitOnEarlyArrivalFirstNode(true);
    opti.addElement(dueren);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHoursEarly, visitDuration, importance);
    aachen.setWaitOnEarlyArrival(false);
    aachen.setWaitOnEarlyArrivalFirstNode(true);
    opti.addElement(aachen);

    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHoursLate, visitDuration, importance);
    wuppertal.setWaitOnEarlyArrival(false);
    wuppertal.setWaitOnEarlyArrivalFirstNode(true);
    opti.addElement(wuppertal);
  }

  /**
   * Adds Jack, who starts and ends his working day in Aachen.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 10 h per day, the length of the working hours.
    Duration maxWorkingTime = Duration.ofHours(10);
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
