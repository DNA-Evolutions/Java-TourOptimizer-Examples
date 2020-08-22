package com.dna.jopt.touroptimizer.java.examples.advanced.pickupanddelivery;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt',
 * which is part of this source code package.
 *
 * If not, see <https://www.dna-evolutions.com/agb-conditions-and-terms/>.
 * #L%
 */
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

import tec.units.ri.quantity.Quantities;

import java.util.List;
import java.util.Optional;

import static java.time.Month.MARCH;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class PNDReportExtractionExample. In this example we parse the PND-Report. For this purpose,
 * we create static parsers for NodeDepot, ResourceDepot, and all their Members.
 *
 * @author Jens Richter
 * @version Jul 27, 2020
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDReportExtractionExample extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws TimeoutException the timeout exception
   */
  public static void main(String[] args)
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException,
          TimeoutException {
    new PNDReportExtractionExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Report extraction example of a pick up and delivery optimization problem.";
  }

  /**
   * Example.
   *
   * @throws InvalidLicenceException the invalid licence exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws TimeoutException the timeout exception
   */
  public void example()
      throws InvalidLicenceException, IOException, InterruptedException, ExecutionException,
          TimeoutException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    this.setProperties();
    this.addNodes();
    this.addRes();

    // 3.) start the optimization
    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // Subscribe to events
    subscribeToEvents(this);

    // It is important to block the call, otherwise optimization will be terminated
    IOptimizationResult result = resultFuture.get(2, TimeUnit.MINUTES);

    // Parse and print out the result
    System.out.println(parseOptimizationResult(result));
  }

  /**
   * Parses the optimization result.
   *
   * @param result the result
   * @return the string
   */
  public static String parseOptimizationResult(IOptimizationResult result) {

    // Getting all route from the solution
    StringBuilder routeSB = new StringBuilder();

    result
        .getResultEntity()
        .getRoutes()
        .stream()
        .map(PNDReportExtractionExample::parseSingleRoute)
        .forEach(routeSB::append);

    return routeSB.toString();
  }

  /**
   * Parses a single route.
   *
   * @param r the r
   * @return the string
   */
  public static String parseSingleRoute(ILogicEntityRoute r) {

    StringBuilder routeSB = new StringBuilder();

    // Add the ResourceDepotReport
    Optional<IResourceDepot> startDepotOpt = r.getCurrentVisitingResource().getResourceDepot();

    if (startDepotOpt.isPresent()) {
      routeSB.append(
          "\n------- ResourceDepot Report (" + r.getCurrentVisitingResource().getId() + ")-------");
      IResourceDepot depot = startDepotOpt.get();
      routeSB.append("\nSTART:");
      routeSB.append("\n---");
      routeSB.append("\n" + PNDReportExtractionExample.parseResourceDepot(depot));

      Optional<IResourceDepot> stopDepotOpt = r.getJoinedDetailController().getCurResourceDepot();
      if (stopDepotOpt.isPresent()) {
        IResourceDepot stopDepot = stopDepotOpt.get();

        routeSB.append("\n\nTERMINATION:");
        routeSB.append("\n---");
        routeSB.append("\n" + PNDReportExtractionExample.parseResourceDepot(stopDepot));
      }
      routeSB.append("\n------- ResourceDepot Report END -------");
    }

    // Now add the NodeDepotReports
    List<ILogicRouteElementDetailItem> details =
        r.getRouteElementsDetailController().getAsSortedListByArrival();

    details.stream().map(PNDReportExtractionExample::parseNodeDetail).forEach(routeSB::append);

    return routeSB.toString();
  }

  /**
   * Parses the node detail containing the NodeDepot
   *
   * @param d the d
   * @return the string
   */
  public static String parseNodeDetail(ILogicRouteElementDetailItem d) {

    StringBuilder detailSB = new StringBuilder();

    Optional<INodeDepot> nodeDepotAfterOpt = d.getNodeDepotAfterVisit();

    if (nodeDepotAfterOpt.isPresent()) {
      INodeDepot visitedNodeDepot = nodeDepotAfterOpt.get();

      Optional<INodeDepot> nodeDepotBeforeOpt = d.getNodeDepotBeforeVisit();

      if (nodeDepotBeforeOpt.isPresent()) {
        INodeDepot intialDepot = nodeDepotBeforeOpt.get();
        detailSB.append("\n");
        detailSB.append(
            " Depot before visit: " + PNDReportExtractionExample.parseNodeDepot(intialDepot));
      }

      detailSB.append("\n");
      detailSB.append(
          " Depot after visit : " + PNDReportExtractionExample.parseNodeDepot(visitedNodeDepot));
      detailSB.append("\n");
    }

    return detailSB.toString();
  }

  /**
   * Parses the NodeDepot.
   *
   * @param depot the depot
   * @return the string
   */
  /*
   *
   * NodeDepot and Load related
   *
   */
  public static String parseNodeDepot(INodeDepot depot) {

    StringBuilder routeSB = new StringBuilder();

    routeSB.append("Id: " + depot.getDepotId());
    routeSB.append(" / Type: " + depot.getTypeName());

    Collection<ILoad> loads = depot.getItems();

    loads.stream().map(PNDReportExtractionExample::parseLoad).forEach(s->routeSB.append(" / Loads: "+s));
    

    return routeSB.toString();
  }

  /**
   * Parses the load of a NodeDepot
   *
   * @param load the load
   * @return the string
   */
  public static String parseLoad(ILoad load) {
    StringBuilder routeSB = new StringBuilder();

    routeSB.append(" Id: " + load.getId() + " /");

    routeSB.append(" Type: " + load.getTypeName() + " /");

    routeSB.append(" Desired Load Exchange: " + load.getLoadValue() + " /");

    routeSB.append(" IsRequest: " + load.isRequest() + " /");

    routeSB.append(" IsFuzzy: " + load.isFuzzyVisit() + " /");

    routeSB.append(" Priority: " + load.getPriority());

    return routeSB.toString();
  }

  /*
   *
   * ResourceDepot and LoadCapacity related
   *
   */

  /**
   * Parses the ResourceDepot.
   *
   * @param depot the depot
   * @return the string
   */
  public static String parseResourceDepot(IResourceDepot depot) {

    StringBuilder routeSB = new StringBuilder();

    routeSB.append("Id: " + depot.getDepotId());
    routeSB.append(" / Type: " + depot.getTypeName() + " ");
    routeSB.append(" / MaxTotalCapacity: " + depot.getMaximalTotalCapacity());
    routeSB.append(" / UsedCapacity: " + depot.getCurrentTotalMatchedLoad());

    List<ILoadCapacity> loads = depot.getItems();

    if (!loads.isEmpty()) {
      for (int ii = 0; ii < loads.size(); ii++) {
        routeSB.append(
            "\n("
                + ii
                + ") LoadCapacity: \n"
                + PNDReportExtractionExample.parseLoadCapacity(loads.get(ii)));
        if (ii < loads.size() - 1) {
          routeSB.append("\n");
        }
      }
    }

    return routeSB.toString();
  }

  /**
   * Parses the loadCapacity of a ResourceDepot
   *
   * @param loadCap the load cap
   * @return the string
   */
  public static String parseLoadCapacity(ILoadCapacity loadCap) {
    StringBuilder routeSB = new StringBuilder();

    routeSB.append(" Id: " + loadCap.getId() + " /");
    routeSB.append(" Type: " + loadCap.getTypeName() + " /");
    routeSB.append(" Load: " + loadCap.getCurrentLoad() + " /");
    routeSB.append(" maxCapacity: " + loadCap.getMaximalIndividualLoadCapacity() + " /");

    return routeSB.toString();
  }

  /*
   *
   *
   */

  /** Sets the properties. */
  public void setProperties() {

    Properties props = new Properties();
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000");

    // We have to tell the optimizer that we have an high interest in capacity planning, Default is
    // 100
    props.setProperty("JOptWeight.Capacity", "200");
    this.addElement(props);
  }

  /** Adds the res. */
  public void addRes() {

    /*
     * Setting the resource JackTruck
     */

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2800.0, KILO(METRE));

    IResource truckJack =
        new CapacityResource(
            "JackTruck", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);

    // Setting a depot to our truck
    truckJack.setResourceDepot(this.createResourceDepot());

    // Adding the truck to our optimization
    this.addElement(truckJack);
  }

  /**
   * Creates the resource depot.
   *
   * @return the resource depot
   */
  public IResourceDepot createResourceDepot() {

    /*
     * Defining a ResourceDepot "JackTruckDepot" that can store DeliverGood and PickupGood.
     *
     */

    // We can store a maximum of 10 DeliverGood on our track (assuming that no other load is
    // present)
    // Further, we start with an initial load of 5 DeliverGoods.
    ILoadCapacity deliverGoodCapacity = new SimpleLoadCapacity("DeliverGood", 10, 5);

    // We can store a maximum of 5 PickupGood on our track (assuming that no other load is present)
    ILoadCapacity pickupGoodCapacity = new SimpleLoadCapacity("PickupGood", 5, 0);

    // Our depot can store a maximum of 10 total items. For example, 7 DeliverGoods
    // and 3 PickupGoods.
    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", 10);

    // Adding the capacities to our depot
    depot.add(deliverGoodCapacity);
    depot.add(pickupGoodCapacity);

    return depot;
  }

  /** Adds the nodes. */
  public void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(30);

    /*
     *
     *
     */

    INode job1 =
        new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job1.setNodeDepot(this.createNodeDepot(2, 3, "CustomerOneDepot"));
    this.addElement(job1);

    //
    INode job2 =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job2.setNodeDepot(this.createNodeDepot(1, 2, "CustomerTwoDepot"));
    this.addElement(job2);

    //

  }

  /**
   * Creates the node depot.
   *
   * @param deliverGoodRequest the deliver good request
   * @param pickupGoodSupply the pickup good supply
   * @param depotId the depot id
   * @return the i node depot
   */
  public INodeDepot createNodeDepot(int deliverGoodRequest, int pickupGoodSupply, String depotId) {

    ILoad deliverGoodLoad = new SimpleLoad("DeliverGood", deliverGoodRequest, true, true);

    ILoad pickupGoodLoad = new SimpleLoad("PickupGood", pickupGoodSupply, false, true);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);
    customerNodeDepot.add(deliverGoodLoad);
    customerNodeDepot.add(pickupGoodLoad);

    return customerNodeDepot;
  }

  /**
   * Subscribe to events.
   *
   * @param opti the opti
   */
  private static void subscribeToEvents(IOptimization opti) {

    // Subscribe to events
    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(
            p -> {
              System.out.println(p.getProgressString());
            });

    opti.getOptimizationEvents()
        .errorSubject()
        .subscribe(
            e -> {
              System.out.println(e.getCause() + " " + e.getCode());
            });

    opti.getOptimizationEvents()
        .warningSubject()
        .subscribe(
            w -> {
              System.out.println(w.getDescription() + w.getCode());
            });

    opti.getOptimizationEvents()
        .statusSubject()
        .subscribe(
            s -> {
              System.out.println(s.getDescription() + " " + s.getCode());
            });
  }
}
