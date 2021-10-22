package com.dna.jopt.touroptimizer.java.examples.basic.readoutresult_05;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */
import static java.time.Month.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.bucket.route.controller.detail.ILogicRouteElementDetailItem;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.violation.IViolation;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/** Example on how to access route information and details from result object. */
public class ReadOutResultWithHeadersExample extends Optimization {

  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new ReadOutResultWithHeadersExample().example();
  }

  public String toString() {
    return "Example on how to access route information, headers and details from result object.";
  }

  public void example()
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    resultFuture.get();
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(4);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);
  }

  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren);

    TimeWindowGeoNode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    this.addElement(nuernberg);

    TimeWindowGeoNode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn);

    TimeWindowGeoNode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart);

    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen);
  }

  @Override
  public void onError(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onStatus(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onWarning(int code, String message) {
    //

  }

  @Override
  public void onProgress(String winnerProgressString) {
    System.out.println(winnerProgressString);
  }

  @Override
  public void onProgress(IOptimizationProgress rapoptProgress) {
    //
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult result) {

    /*
     *
     * Data about the result
     *
     */
    StringBuilder routeHeaderbld = new StringBuilder();

    routeHeaderbld.append("\n-------------------------------- --------------------------");
    routeHeaderbld.append("\n--------------------- RESULTS -----------------------------");
    routeHeaderbld.append("\n----------------- -----------------------------------------");
    routeHeaderbld.append("\n Number of Route         : " + result.getJobRouteCount());
    routeHeaderbld.append("\n Total Route Elements    : " + result.getJobElementCount());
    routeHeaderbld.append("\n Total cost              : " + result.getJobCost());
    routeHeaderbld.append("\n-----------------------------------------------------------");
    routeHeaderbld.append(
        "\n Total time        [h]   : " + (int) (result.getJobTimeSeconds() / 1000L / 3600L));
    routeHeaderbld.append(
        "\n Total idle time   [h]   : " + (int) (result.getJobIdleTimeSeconds() / 1000 / 3600));
    routeHeaderbld.append(
        "\n Total prod. time  [h]   : "
            + (int) (result.getJobProductiveTimeSeconds() / 1000 / 3600));
    routeHeaderbld.append(
        "\n Total tran. time  [h]   : "
            + (int) (result.getJobTransitionTimeSeconds() / 1000L / 3600L));
    routeHeaderbld.append(
        "\n Total distance    [km]  : " + (int) (result.getJobTransitionDistanceMeter() / 1000.0));
    routeHeaderbld.append(
        "\n Termi. time       [h]   : "
            + (int) (result.getJobTerminationTransitionTimeSeconds() / 1000.0 / 3600.0));
    routeHeaderbld.append(
        "\n Termi. distance   [km]  : "
            + (int) (result.getJobTerminationTransitionDistanceMeter() / 1000.0));

    System.out.println(routeHeaderbld.toString());

    /*
     *  Access the data about each route
     */
    System.out.println("\n----------------------- -----------------------------------");
    System.out.println("--------------------- ROUTES ------------------------------");
    System.out.println("---------------------- ------------------------------------\n");
    result
        .getRoutes()
        .stream()
        .forEach(
            r -> {

              /*
               *  Header via getJoinedDetailController
               */

              StringBuilder routeSB = new StringBuilder();
              routeSB.append("\n-----------------------------------------------------------");
              routeSB.append("\nRoute information");

              routeSB.append("\nRouteId                : " + r.getRouteId());
              routeSB.append(
                  "\nResource               : " + r.getCurrentVisitingResource().getId());

              routeSB.append(
                  "\nTransit time     [min] : "
                      + (int) (r.getJoinedDetailController().getCurTransitTime() / 1000.0 / 60.0));
              routeSB.append(
                  "\nProductive. time [min] : "
                      + (int)
                          (r.getJoinedDetailController().getCurProductiveTime() / 1000.0 / 60.0));
              routeSB.append(
                  "\nIdle time        [min] : "
                      + (int) (r.getJoinedDetailController().getCurIdleTime() / 1000.0 / 60.0));
              routeSB.append(
                  "\nWhite Idle time  [min] : "
                      + (int)
                          (r.getJoinedDetailController().getCurWhitSpaceIdleTime()
                              / 1000.0
                              / 60.0));
              routeSB.append(
                  "\nInd. Idle time   [min] : "
                      + (int)
                          (r.getJoinedDetailController().getCurInducedIdleTime() / 1000.0 / 60.0));
              routeSB.append(
                  "\nRoute Distance   [km]  : "
                      + (double) ((int) (r.getJoinedDetailController().getCurDistance() / 1.0))
                          / 1000.0);

              routeSB.append(
                  "\nTermi. time      [min] : "
                      + (int)
                          (r.getJoinedDetailController().getTerminationTransitTime()
                              / 1000.0
                              / 60.0));
              routeSB.append(
                  "\nTermi. distance  [km]  : "
                      + (double)
                              ((int)
                                  (r.getJoinedDetailController().getTerminationTransitDistance()
                                      / 1.0))
                          / 1000.0);

              System.out.println(routeSB.toString());

              // Get e.g. cost of a route
              System.out.println("\nRouteCost: " + result.getRouteCost(r));

              /*
               *
               * Route elements
               *
               */

              // Get details of the route
              List<ILogicRouteElementDetailItem> details = result.getOrderedRouteItems(r);
              details.forEach(System.out::println);

              // Get route violations
              List<IViolation> violations = result.getRouteViolations(r);
              violations.forEach(System.out::println);

              // Get node Violations
              details
                  .stream()
                  .forEach(
                      d -> {
                        List<IViolation> nodeVios =
                            r.getRouteCostAndViolationController()
                                .getNodeViolations(d.getElement().getId());
                        if (!nodeVios.isEmpty()) {
                          System.out.println(d.getElement().getId() + ": " + nodeVios);
                        }
                      });
            });
  }
}
