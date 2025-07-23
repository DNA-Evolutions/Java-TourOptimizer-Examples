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
import com.dna.jopt.member.unit.pnd.load.flexload.RequestFlexLoad;
import com.dna.jopt.member.unit.pnd.load.flexload.SupplyFlexLoad;
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

import java.util.List;
import static java.time.Month.MARCH;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * In this example a resource called "JackTruck" has to deliver/pickup "Fridges" and has to
 * deliver/pickup "Tvs". Each customer can decide what to pick up (customer supply) and what needs to
 * be delivered (customer request).
 *
 * <p>As planning the initial Load for our truck "JackTruck" can be challenging in mixed pickup and
 * delivery problems, we use a MixedFlexLoad that can adjust its own request or supply during
 * optimization. Even though a FlexLoad needs to be attached to a Node, a FlexLoad usually can be
 * seen as a warehouse that a Resource can use to reload and/or unload goods.
 *
 * <p>Sometimes, a warehouse is only able to provide Load but is not able to store it. Further, a
 * Node maybe can only act as a dump but cannot provide Load. For this purpose, a RequestFlexLoad
 * and a SupplyFlexLoad can be defined. A RequestFlexLoad is a MixedFlexLoad that will only request
 * (like a dump). A SupplyFlexLoad is like a MixedFlexLoad that can only provide Load.
 *
 * <p>Hint: This concept can be also combined with the optional Node concept.
 *
 * @author Jens Richter
 * @version Mar 08, 2021
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDRequestAndSupplyFlexLoadExample extends Optimization {

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
    new PNDRequestAndSupplyFlexLoadExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example of a pick up and delivery using Supply- and RequestFlexLoads.";
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

    // Start the optimization
    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // Subscribe to events
    subscribeToEvents(this);

    // It is important to block the call, otherwise the optimization will be terminated
    IOptimizationResult result = resultFuture.get(2, TimeUnit.MINUTES);

    System.out.println(result);
  }

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

    // We can store a maximum of 10 Fridges on our track (assuming that no other Load is
    // present)
    ILoadCapacity fridgeCpacity = new SimpleLoadCapacity("Fridge", 10, 0);

    // We can store a maximum of 10 TVs on our track (assuming that no other Load is present)
    ILoadCapacity tvCapacity = new SimpleLoadCapacity("TV", 10, 0);

    // Our depot can store a maximum of 15 total items. For example, 10 Fridges
    // and 5 TVs.
    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", 15);

    // Adding the capacities to our depot
    depot.add(fridgeCpacity);
    depot.add(tvCapacity);

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

    job1.setNodeDepot(this.createNodeDepot("CustomerOneDepot", 2, true, 3, false));
    this.addElement(job1);

    //
    INode job2 =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job2.setNodeDepot(this.createNodeDepot("CustomerTwoDepot", 1, false, 4, true));
    this.addElement(job2);

    //
    INode job3 =
        new TimeWindowGeoNode("CustomerThree", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job3.setNodeDepot(this.createNodeDepot("CustomerThreeDepot", 0, false, 1, false));
    this.addElement(job3);

    // Flex
    INode requestFlexNode =
        new TimeWindowGeoNode(
            "RequestFlexNode", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    requestFlexNode.setNodeDepot(this.createRequestFlexDepot("RequestFlexNodeDepot"));
    this.addElement(requestFlexNode);

    INode supplyFlexNode =
        new TimeWindowGeoNode(
            "SupplyFlexNode", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    supplyFlexNode.setNodeDepot(this.createSupplyFlexDepot("SupplyFlexNodeDepot"));
    this.addElement(supplyFlexNode);
  }

  /**
   * Creates the node depot.
   *
   * @param depotId the depot id
   * @param fridgesCount the fridges count
   * @param isFridgeRequest the is fridge request
   * @param tvsCount the tvs count
   * @param isTvRequest the is tv request
   * @return the i node depot
   */
  public INodeDepot createNodeDepot(
      String depotId,
      int fridgesCount,
      boolean isFridgeRequest,
      int tvsCount,
      boolean isTvRequest) {

    ILoad fridgeLoad = new SimpleLoad("Fridge", fridgesCount, isFridgeRequest, true);
    ILoad tvLoad = new SimpleLoad("TV", tvsCount, isTvRequest, true);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);

    if (fridgesCount > 0) {
      // Only add if count is bigger than zero.
      // Strictly speaking this is not necessary but improves the readability of the result.
      // The optimizer can also handle zero requests/supplies
      customerNodeDepot.add(fridgeLoad);
    }

    if (tvsCount > 0) {
      customerNodeDepot.add(tvLoad);
    }

    return customerNodeDepot;
  }

  /**
   * Creates the request flex depot.
   *
   * @param depotId the depot id
   * @return the i node depot
   */
  public INodeDepot createRequestFlexDepot(String depotId) {

    // Adding RequestFlexLoads that adjust load request during optimization
    ILoad fridgeLoad = new RequestFlexLoad("Fridge");

    // The initial (before optimization) property of a RequestFlexLoad is a supply of 0.0
    // To help the optimizer converge faster, the initial value for FlexLoad loadValue can be provided.
    //
    // For example: Our RequestFlexLoad for TV starts with a LoadValue of 1.0 as mandatory request
    ILoad tvLoad = new RequestFlexLoad("TV", 1);

    INodeDepot nodeDepotWithMixedFlexLoads = new SimpleNodeDepot(depotId);
    nodeDepotWithMixedFlexLoads.add(fridgeLoad);
    nodeDepotWithMixedFlexLoads.add(tvLoad);

    return nodeDepotWithMixedFlexLoads;
  }

  /**
   * Creates the supply flex depot.
   *
   * @param depotId the depot id
   * @return the i node depot
   */
  public INodeDepot createSupplyFlexDepot(String depotId) {

    // Adding RequestFlexLoads that adjust load request during optimization
    ILoad fridgeLoad = new SupplyFlexLoad("Fridge");

    // The initial (before optimization) property of a SupplyFlexLoad is a request of 0.0
    // To help the optimizer converge faster, the initial value for FlexLoad loadValue can be provided.

    //
    // For example: Our SupplyFlexLoad for TV starts with a LoadValue of 3.0 as mandatory supply
    ILoad tvLoad = new SupplyFlexLoad("TV", 3);

    INodeDepot nodeDepotWithMixedFlexLoads = new SimpleNodeDepot(depotId);
    nodeDepotWithMixedFlexLoads.add(fridgeLoad);
    nodeDepotWithMixedFlexLoads.add(tvLoad);

    return nodeDepotWithMixedFlexLoads;
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
