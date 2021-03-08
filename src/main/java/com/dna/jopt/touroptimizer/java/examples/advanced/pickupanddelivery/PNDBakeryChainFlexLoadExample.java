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
import com.dna.jopt.member.unit.pnd.load.flexload.SupplyFlexLoad;
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

import java.util.List;
import static java.time.Month.MARCH;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * In this example two Resource called "JackTruckCologne" and "JohnTruckAachen" are employees of a
 * bakery chain and have to deliver "Bread" to different supermarkets.
 *
 * <p>Each of the Resources is bound to a different bakery. The optimizer takes over
 * the manufacturing planning. The goal is to find the optimal number
 * of bread each bakery has to prepare to satisfy the request of all supermarkets. To achieve this,
 * two SupplyFlexLoad are used. The optimized load of each SupplyFlexLoad describes the optimal number of bread
 * each bakery has to prepare.
 *
 * <p>Hint: This concept can also be combined with the optional Node concept.
 *
 * @author Jens Richter
 * @version Mar 08, 2021
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDBakeryChainFlexLoadExample extends Optimization {


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
    new PNDBakeryChainFlexLoadExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example: Use of SupplyFlexLoads for planning the manufacturing of goods.";
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
    
    System.out.println(result);

  }

  /** Sets the properties. */
  public void setProperties() {

    Properties props = new Properties();
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000");

    // We have to tell the optimizer that we have a high interest in capacity planning, default is
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

    /*
     *
     *
     */

    IResource truckJack =
        new CapacityResource(
            "JackTruckCologne", 50.9333, 6.95, maxWorkingTime, maxDistanceKmW, workingHours);

    // Setting a depot to our truck
    truckJack.setResourceDepot(this.createResourceDepot("JackTruckCologneDepot"));

    // Adding the truck to our optimization
    this.addElement(truckJack);

    /*
     * Add another Truck: JohnTruckAachen
     *
     */

    IResource truckJohn =
        new CapacityResource(
            "JohnTruckAachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    // Setting a depot to our truck
    truckJohn.setResourceDepot(this.createResourceDepot("JohnTruckAachenDepot"));

    // Adding the truck to our optimization
    this.addElement(truckJohn);
  }

  /**
   * Creates the resource depot.
   *
   * @return the resource depot
   */
  public IResourceDepot createResourceDepot(String depotId) {

    /*
     * Defining a ResourceDepot "JackTruckDepot" that can store DeliverGood and PickupGood.
     *
     */

    // We can store a maximum of 20 "Bread" on our truck (assuming that no other load is
    // present)
    ILoadCapacity breadCapacity = new SimpleLoadCapacity("Bread", 20, 0);

    // Adding the capacities to our depot, as we only transport a single good, the maxCapacity equals
    // the individual capacity of the bread capacity.
    IResourceDepot depot = new SimpleResourceDepot(depotId, 20);
    depot.add(breadCapacity);

    return depot;
  }

  /** Adds the nodes. */
  public void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(80);

    /*
     * Define some supermarkets that have bread request.
     *
     */

    INode store1 =
        new TimeWindowGeoNode("StoreEssen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    store1.setNodeDepot(this.createNodeDepot("StoreEssenDepot", 5));
    this.addElement(store1);

    INode store2 =
        new TimeWindowGeoNode("StoreDueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    store2.setNodeDepot(this.createNodeDepot("StoreDuerenDepot", 2));
    this.addElement(store2);

    INode store3 =
        new TimeWindowGeoNode(
            "StoreWuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    store3.setNodeDepot(this.createNodeDepot("StoreWuppertalDepot", 4));
    this.addElement(store3);

    INode store4 =
        new TimeWindowGeoNode(
            "StoreDortmund", 51.51728, 7.48446, weeklyOpeningHours, visitDuration, 1);
    store4.setNodeDepot(this.createNodeDepot("StoreDortmundDepot", 8));
    this.addElement(store4);

    INode store5 =
        new TimeWindowGeoNode(
            "StoreMoenchengladbach", 51.19541, 6.41172, weeklyOpeningHours, visitDuration, 1);
    store5.setNodeDepot(this.createNodeDepot("StoreMoenchengladbachDepot", 1));
    this.addElement(store5);

    INode store6 =
        new TimeWindowGeoNode(
            "StoreLeverkusen", 51.04154, 6.99944, weeklyOpeningHours, visitDuration, 1);
    store6.setNodeDepot(this.createNodeDepot("StoreLeverkusenDepot", 7));
    this.addElement(store6);

    /*
     * SupplyNodes - These nodes will later on manufacture the required bread. They are located
     * at the resources' start locations.
     */

    INode supplyFlexNodeAachen =
        new TimeWindowGeoNode(
            "SupplyFlexNodeAachen", 50.77577, 6.08177, weeklyOpeningHours, visitDuration, 1);
    supplyFlexNodeAachen.setNodeDepot(this.createSupplyFlexDepot("SupplyFlexNodeAachenDepot"));
    
    // Could also be an optional Node, which allows offloading cargo, if desired
    boolean isAachenOptional = false;
    supplyFlexNodeAachen.setIsOptional(isAachenOptional);
    
    this.addElement(supplyFlexNodeAachen);

    INode supplyFlexNodeCologne =
        new TimeWindowGeoNode(
            "SupplyFlexNodeCologne", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    supplyFlexNodeCologne.setNodeDepot(this.createSupplyFlexDepot("SupplyFlexNodeCologneDepot"));

    // Could also be an optional Node, which allows offloading cargo, if desired
    boolean isCologneOptional = false;
    supplyFlexNodeCologne.setIsOptional(isCologneOptional);
    this.addElement(supplyFlexNodeCologne);
  }

  /**
   * Creates the node depot.
   *
   * @param depotId the depot id
   * @param breadCount the bread count
   * @return the i node depot
   */
  public INodeDepot createNodeDepot(String depotId, int breadCount) {

    ILoad breadLoad = new SimpleLoad("Bread", breadCount, true, true);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);

    customerNodeDepot.add(breadLoad);

    return customerNodeDepot;
  }


  /**
   * Creates the supply flex depot.
   *
   * @param depotId the depot id
   * @return the i node depot
   */
  public INodeDepot createSupplyFlexDepot(String depotId) {

    // We assume that a truck most likely needs to start fully loaded with "Bread"
    ILoad breadLoad = new SupplyFlexLoad("Bread", 20);

    INodeDepot nodeDepotWithSupplyFlexLoads = new SimpleNodeDepot(depotId);
    nodeDepotWithSupplyFlexLoads.add(breadLoad);

    return nodeDepotWithSupplyFlexLoads;
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
