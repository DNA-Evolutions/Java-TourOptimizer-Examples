package com.dna.jopt.touroptimizer.java.examples.advanced.pickupanddelivery;
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
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
import com.dna.jopt.member.unit.pnd.capacity.ILoadCapacity;
import com.dna.jopt.member.unit.pnd.capacity.simple.SimpleLoadCapacity;
import com.dna.jopt.member.unit.pnd.depot.node.INodeDepot;
import com.dna.jopt.member.unit.pnd.depot.node.simple.SimpleNodeDepot;
import com.dna.jopt.member.unit.pnd.depot.resource.IResourceDepot;
import com.dna.jopt.member.unit.pnd.depot.resource.simple.SimpleResourceDepot;
import com.dna.jopt.member.unit.pnd.load.ILoad;
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Reads the ResourceDepot and NodeDepot reports out of the result with the getters of the depots,
 * loads and load capacities, and prints them in its own format.
 *
 * <p>Scenario: JackTruck starts at 50.1167, 7.68333 with 5 DeliverGood on board and works from
 * 08:00 to 20:00 on March 11, 2020 (Europe/Berlin); its depot holds 10 in all, at most 10
 * DeliverGood and 5 PickupGood. CustomerOne requests 2 DeliverGood and supplies 3 PickupGood;
 * CustomerTwo requests 1 DeliverGood and supplies 2 PickupGood. Both customers are at one point in
 * Koeln and open from 08:00 to 18:00.
 *
 * <p>Look for: in place of the full result, the ResourceDepot report of JackTruck at START and at
 * TERMINATION, with 2 DeliverGood and 5 PickupGood at the end, and the depot of each customer
 * before and after its visit.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md">PND.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDReportExtractionExample extends Optimization {

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
    new PNDReportExtractionExample().example();
  }

  @Override
  public String toString() {
    return "Reads the ResourceDepot and NodeDepot reports out of the result with the getters of the"
        + " depots, loads and load capacities, and prints them in its own format.";
  }

  /**
   * Builds the optimization, runs it and prints the depot reports of the result.
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
   * Adds the properties that keep the run short and raise the capacity weight.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    // A capacity weight above the default makes a capacity violation cost more against the route
    // time and distance.
    props.setProperty("JOptWeight.Capacity", "200");
    opti.addElement(props);
  }

  /**
   * Adds CustomerOne and CustomerTwo, each with a request of DeliverGood and a supply of
   * PickupGood.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 30 minutes, a sample value.
    Duration visitDuration = Duration.ofMinutes(30);

    // Both customers are at one point in Koeln, so the order of the visits changes the loads on
    // board, not the driving.
    INode customerOne =
        new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerOne.setNodeDepot(createNodeDepot("CustomerOneDepot", 2, 3));
    opti.addElement(customerOne);

    INode customerTwo =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerTwo.setNodeDepot(createNodeDepot("CustomerTwoDepot", 1, 2));
    opti.addElement(customerTwo);
  }

  /**
   * Creates the depot of a customer: a request of DeliverGood and a supply of PickupGood.
   *
   * @param depotId the id of the depot
   * @param deliverGoodRequest the DeliverGood that the customer requests
   * @param pickupGoodSupply the PickupGood that the customer supplies
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(
      String depotId, int deliverGoodRequest, int pickupGoodSupply) {
    ILoad deliverGoodLoad = new SimpleLoad("DeliverGood", deliverGoodRequest, true, true);
    ILoad pickupGoodLoad = new SimpleLoad("PickupGood", pickupGoodSupply, false, true);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(deliverGoodLoad);
    depot.add(pickupGoodLoad);
    return depot;
  }

  /**
   * Adds JackTruck with its depot.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 10 h, a sample value above what the route of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(10);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource truckJack =
        new CapacityResource(
            "JackTruck", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);
    truckJack.setResourceDepot(createResourceDepot());
    opti.addElement(truckJack);
  }

  /**
   * Creates the depot of JackTruck: up to 10 DeliverGood and 5 PickupGood, 10 in all.
   *
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot() {
    // JackTruck starts with 5 DeliverGood and no PickupGood on board.
    ILoadCapacity deliverGoodCapacity = new SimpleLoadCapacity("DeliverGood", 10, 5);
    ILoadCapacity pickupGoodCapacity = new SimpleLoadCapacity("PickupGood", 5, 0);

    // The two goods share the 10 of the depot, for example 7 DeliverGood and 3 PickupGood.
    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", 10);
    depot.add(deliverGoodCapacity);
    depot.add(pickupGoodCapacity);
    return depot;
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
   * Starts the run, waits for the result and prints the depot reports read out of it.
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
    System.out.println(parseOptimizationResult(result));
  }

  /**
   * Reads the depot reports of all routes of a result.
   *
   * @param result the result of the run
   * @return the reports of all routes
   */
  private static String parseOptimizationResult(IOptimizationResult result) {
    StringBuilder sb = new StringBuilder();
    result.getResultEntity().getRoutes().stream()
        .map(PNDReportExtractionExample::parseSingleRoute)
        .forEach(sb::append);
    return sb.toString();
  }

  /**
   * Reads the ResourceDepot report and the NodeDepot reports of one route.
   *
   * @param route the route
   * @return the reports of the route
   */
  private static String parseSingleRoute(ILogicEntityRoute route) {
    StringBuilder sb = new StringBuilder();

    // The depot of the resource holds the loads at the start of the route; the detail controller
    // of the route holds them after its last visit.
    Optional<IResourceDepot> startDepotOpt =
        route.getCurrentVisitingResource().getResourceDepot();

    if (startDepotOpt.isPresent()) {
      sb.append(
          "\n------- ResourceDepot Report ("
              + route.getCurrentVisitingResource().getId()
              + ")-------");
      sb.append("\nSTART:");
      sb.append("\n---");
      sb.append("\n" + parseResourceDepot(startDepotOpt.get()));

      Optional<IResourceDepot> stopDepotOpt =
          route.getJoinedDetailController().getCurResourceDepot();
      if (stopDepotOpt.isPresent()) {
        sb.append("\n\nTERMINATION:");
        sb.append("\n---");
        sb.append("\n" + parseResourceDepot(stopDepotOpt.get()));
      }
      sb.append("\n------- ResourceDepot Report END -------");
    }

    List<ILogicRouteElementDetailItem> details =
        route.getRouteElementsDetailController().getAsSortedListByArrival();
    details.stream().map(PNDReportExtractionExample::parseNodeDetail).forEach(sb::append);

    return sb.toString();
  }

  /**
   * Reads the depot of a visited node before and after the visit.
   *
   * @param detail the detail of the visit
   * @return the two depot lines, or an empty string if the node has no depot
   */
  private static String parseNodeDetail(ILogicRouteElementDetailItem detail) {
    StringBuilder sb = new StringBuilder();

    Optional<INodeDepot> depotAfterOpt = detail.getNodeDepotAfterVisit();

    if (depotAfterOpt.isPresent()) {
      Optional<INodeDepot> depotBeforeOpt = detail.getNodeDepotBeforeVisit();

      if (depotBeforeOpt.isPresent()) {
        sb.append("\n");
        sb.append(" Depot before visit: " + parseNodeDepot(depotBeforeOpt.get()));
      }

      sb.append("\n");
      sb.append(" Depot after visit : " + parseNodeDepot(depotAfterOpt.get()));
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Reads a NodeDepot with its loads.
   *
   * @param depot the node depot
   * @return the depot in one line
   */
  private static String parseNodeDepot(INodeDepot depot) {
    String loads =
        depot.getItems().stream()
            .map(PNDReportExtractionExample::parseLoad)
            .collect(Collectors.joining(","));

    return "Id: "
        + depot.getDepotId()
        + " / Type: "
        + depot.typeName()
        + " / Loads: ["
        + loads
        + "]";
  }

  /**
   * Reads one load of a NodeDepot.
   *
   * @param load the load
   * @return the load in one line
   */
  private static String parseLoad(ILoad load) {
    return " Id: "
        + load.getId()
        + " / Type: "
        + load.typeName()
        + " / Desired Load Exchange: "
        + load.getLoadValue()
        + " / IsRequest: "
        + load.isRequest()
        + " / IsFuzzy: "
        + load.isFuzzyVisit()
        + " / Priority: "
        + load.getPriority();
  }

  /**
   * Reads a ResourceDepot with its load capacities.
   *
   * @param depot the resource depot
   * @return the depot, one line for the depot and one per load capacity
   */
  private static String parseResourceDepot(IResourceDepot depot) {
    StringBuilder sb = new StringBuilder();

    sb.append("Id: " + depot.getDepotId());
    sb.append(" / Type: " + depot.typeName());
    sb.append(" / MaxTotalCapacity: " + depot.getMaximalTotalCapacity());
    sb.append(" / UsedCapacity: " + depot.getCurrentTotalMatchedLoad());

    List<ILoadCapacity> capacities = depot.getItems();
    for (int ii = 0; ii < capacities.size(); ii++) {
      sb.append("\n(" + ii + ") LoadCapacity: " + parseLoadCapacity(capacities.get(ii)));
    }

    return sb.toString();
  }

  /**
   * Reads one load capacity of a ResourceDepot.
   *
   * @param capacity the load capacity
   * @return the load capacity in one line
   */
  private static String parseLoadCapacity(ILoadCapacity capacity) {
    return "Id: "
        + capacity.getId()
        + " / Type: "
        + capacity.typeName()
        + " / Load: "
        + capacity.getCurrentLoad()
        + " / maxCapacity: "
        + capacity.getMaximalIndividualLoadCapacity();
  }
}
