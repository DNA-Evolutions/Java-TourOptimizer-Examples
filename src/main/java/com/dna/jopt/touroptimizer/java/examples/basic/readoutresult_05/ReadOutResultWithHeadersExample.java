package com.dna.jopt.touroptimizer.java.examples.basic.readoutresult_05;
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
import java.util.Locale;
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
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.bucket.route.controller.detail.ILogicRouteElementDetailItem;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.member.unit.violation.IViolation;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Prints a report of the result: a header with the job totals, a header for each route, the route
 * items and the violations.
 *
 * <p>Scenario: the same as in ReadOutResultExample: eight nodes in Germany, open on May 6 and 7,
 * 2020 from 08:00 to 12:00, and the resource Jack from Aachen, in the time zone Europe/Berlin.
 *
 * <p>Look for: the RESULTS header with the job totals (times in hours, distances in kilometers),
 * then for each route the "Route information" header (times in minutes), the route cost, the route
 * items, the overtime and the late arrivals.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/basic/readoutresult_05/ReadOutResult.md">ReadOutResult.md</a>
 */
public class ReadOutResultWithHeadersExample extends Optimization {

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
    new ReadOutResultWithHeadersExample().example();
  }

  @Override
  public String toString() {
    return "Prints a report of the result: a header with the job totals, a header for each route,"
        + " the route items and the violations.";
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
    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds the eight nodes.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    // The windows close at 12:00; the result shows late arrivals on May 7.

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode(
            "Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);

    INode essen =
        new TimeWindowGeoNode(
            "Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode(
            "Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode(
            "Stuttgart", 48.7667, 9.18333, openingHours, visitDuration, importance);
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHours, visitDuration, importance);
    opti.addElement(aachen);
  }

  /**
   * Adds the resource Jack.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Short on purpose: both routes take longer than 4 h, so the result shows overtime.
    Duration maxWorkingTime = Duration.ofHours(4);
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
   * Starts the run, waits for the result and prints the report.
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

    // The job getters return seconds and meters.
    StringBuilder jobHeader = new StringBuilder();
    jobHeader.append("\n-------------------------------- --------------------------");
    jobHeader.append("\n--------------------- RESULTS -----------------------------");
    jobHeader.append("\n----------------- -----------------------------------------");
    jobHeader.append("\n Number of Route         : " + result.getJobRouteCount());
    jobHeader.append("\n Total Route Elements    : " + result.getJobElementCount());
    jobHeader.append("\n Total cost              : " + result.getJobCost());
    jobHeader.append("\n-----------------------------------------------------------");
    jobHeader.append("\n Total time        [h]   : " + toHours(result.getJobTimeSeconds()));
    jobHeader.append(
        "\n Total idle time   [h]   : " + toHours(result.getJobIdleTimeSeconds()));
    jobHeader.append(
        "\n Total prod. time  [h]   : " + toHours(result.getJobProductiveTimeSeconds()));
    jobHeader.append(
        "\n Total tran. time  [h]   : " + toHours(result.getJobTransitionTimeSeconds()));
    jobHeader.append(
        "\n Total distance    [km]  : " + result.getJobTransitionDistanceMeter() / 1000);
    jobHeader.append(
        "\n Termi. time       [h]   : "
            + toHours(result.getJobTerminationTransitionTimeSeconds()));
    jobHeader.append(
        "\n Termi. distance   [km]  : "
            + result.getJobTerminationTransitionDistanceMeter() / 1000);
    System.out.println(jobHeader.toString());

    System.out.println("\n----------------------- -----------------------------------");
    System.out.println("--------------------- ROUTES ------------------------------");
    System.out.println("---------------------- ------------------------------------\n");

    for (ILogicEntityRoute route : result.getRoutes()) {
      // The joined detail controller of a route returns milliseconds and meters.
      StringBuilder routeHeader = new StringBuilder();
      routeHeader.append("\n-----------------------------------------------------------");
      routeHeader.append("\nRoute information");
      routeHeader.append("\nRouteId                : " + route.getRouteId());
      routeHeader.append(
          "\nResource               : " + route.getCurrentVisitingResource().getId());
      routeHeader.append(
          "\nTransit time     [min] : "
              + (int) (route.getJoinedDetailController().getCurTransitTime() / 1000.0 / 60.0));
      routeHeader.append(
          "\nProductive. time [min] : "
              + (int) (route.getJoinedDetailController().getCurProductiveTime() / 1000.0 / 60.0));
      routeHeader.append(
          "\nIdle time        [min] : "
              + (int) (route.getJoinedDetailController().getCurIdleTime() / 1000.0 / 60.0));
      routeHeader.append(
          "\nWhite Idle time  [min] : "
              + (int) (route.getJoinedDetailController().getCurWhitSpaceIdleTime() / 1000.0
                  / 60.0));
      routeHeader.append(
          "\nInd. Idle time   [min] : "
              + (int) (route.getJoinedDetailController().getCurInducedIdleTime() / 1000.0 / 60.0));
      routeHeader.append(
          "\nRoute Distance   [km]  : "
              + (int) route.getJoinedDetailController().getCurDistance() / 1000.0);
      routeHeader.append(
          "\nTermi. time      [min] : "
              + (int) (route.getJoinedDetailController().getTerminationTransitTime()
                  / 1000.0
                  / 60.0));
      routeHeader.append(
          "\nTermi. distance  [km]  : "
              + (int) route.getJoinedDetailController().getTerminationTransitDistance()
                  / 1000.0);
      System.out.println(routeHeader.toString());

      System.out.println("\nRouteCost: " + result.getRouteCost(route));

      List<ILogicRouteElementDetailItem> details = result.getOrderedRouteItems(route);
      details.forEach(System.out::println);

      List<IViolation> violations = result.getRouteViolations(route);
      violations.forEach(System.out::println);

      for (ILogicRouteElementDetailItem detail : details) {
        List<IViolation> nodeViolations =
            route
                .getRouteCostAndViolationController()
                .getNodeViolations(detail.getElement().getId());
        if (!nodeViolations.isEmpty()) {
          System.out.println(detail.getElement().getId() + ": " + nodeViolations);
        }
      }
    }
  }

  /**
   * Formats seconds as hours with two decimals and a dot as the decimal separator on every machine.
   *
   * @param seconds the seconds
   * @return the hours, for example "1.50" for 5400 seconds
   */
  private static String toHours(int seconds) {
    return String.format(Locale.ROOT, "%.2f", seconds / 3600.0);
  }
}
