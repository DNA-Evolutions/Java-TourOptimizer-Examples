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
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.pnd.load.unload.UnloadAllLoad;
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
 * In this example a resource called "JackTruck" has to pickup "Trash" and "Garbage". Each customer
 * can decide what to pickup.
 *
 * <p>JackTruck starts empty and can use three optional dumps to avoid overloading. One of the
 * optional UnloadAllNodes is so far away, that the optimizer will not use it.
 *
 * <p>Note: This problem could be also solved by using RequestFlexLoads. However, as we know, that
 * we want to unload everything at our dumps, we can help the optimizer by using unloadAll Loads
 * instead.
 *
 * @author Jens Richter
 * @version Jul 27, 2020
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDOptionalUnloadAllLoadExample extends Optimization {

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
    new PNDOptionalUnloadAllLoadExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example: Pick up goods and use UnloadAll loads to avoid overloading.";
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
     * Defining a ResourceDepot "JackTruckDepot" that can store Trash and Garbage.
     *
     */

    // We can store a maximum of 20 Trash on our track (assuming that no other load is
    // present)
    ILoadCapacity trashCpacity = new SimpleLoadCapacity("Trash", 20, 0);

    // We can store a maximum of 15 Garbage on our track (assuming that no other load is present)
    ILoadCapacity garbageCapacity = new SimpleLoadCapacity("Garbage", 15, 0);

    // Our depot can store a maximum of 30 total items. For example, 19 Trash
    // and 11 Garbage.
    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", 30);

    // Adding the capacities to our depot
    depot.add(trashCpacity);
    depot.add(garbageCapacity);

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

    job1.setNodeDepot(this.createNodeDepot("CustomerOneDepot", 15, 10));
    this.addElement(job1);

    //
    INode job2 =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job2.setNodeDepot(this.createNodeDepot("CustomerTwoDepot", 13, 0));
    this.addElement(job2);

    //
    INode job3 =
        new TimeWindowGeoNode("CustomerThree", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job3.setNodeDepot(this.createNodeDepot("CustomerThreeDepot", 0, 10));
    this.addElement(job3);

    // UnloadAll
    INode unloadAllNodeOne =
        new TimeWindowGeoNode(
            "UnloadAllNodeOne", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    unloadAllNodeOne.setNodeDepot(this.createUnloadAllDepot("UnloadAllNodeOneDepot"));
    unloadAllNodeOne.setIsOptional(true);
    this.addElement(unloadAllNodeOne);

    // UnloadAll
    INode unloadAllNodeTwo =
        new TimeWindowGeoNode(
            "UnloadAllNodeTwo", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    unloadAllNodeTwo.setNodeDepot(this.createUnloadAllDepot("UnloadAllNodeTwoDepot"));
    unloadAllNodeTwo.setIsOptional(true);
    this.addElement(unloadAllNodeTwo);

    // UnloadAll Faaar away - This nodes should be found to be not neccessary by the optimizer
    INode unloadAllNodeFarAway =
        new TimeWindowGeoNode(
            "UnloadAllNodeFarAway", 57.9333, 11.95, weeklyOpeningHours, visitDuration, 1);

    unloadAllNodeFarAway.setNodeDepot(this.createUnloadAllDepot("UnloadAllNodeFarAwayDepot"));
    unloadAllNodeFarAway.setIsOptional(true);
    this.addElement(unloadAllNodeFarAway);
  }

  /**
   * Creates the node depot.
   *
   * @param depotId the depot id
   * @param trashCount the trash count
   * @param garbageCount the garbage count
   * @return the i node depot
   */
  public INodeDepot createNodeDepot(String depotId, int trashCount, int garbageCount) {

    ILoad trashLoad = new SimpleLoad("Trash", trashCount, false, true);
    ILoad garbageLoad = new SimpleLoad("Garbage", garbageCount, false, true);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);

    if (trashCount > 0) {
      // Only add if count is bigger than zero.
      // However, this is not necessary but improves the readability of the result.
      // The optimizer can also handle zero requests/supplies
      customerNodeDepot.add(trashLoad);
    }

    if (garbageCount > 0) {
      customerNodeDepot.add(garbageLoad);
    }

    return customerNodeDepot;
  }

  /**
   * Creates the unload all depot.
   *
   * @param depotId the depot id
   * @return the i node depot
   */
  public INodeDepot createUnloadAllDepot(String depotId) {

    // Adding UnloadAllLoads that take whatever is on the truck
    ILoad trashLoad = new UnloadAllLoad("Trash");

    ILoad garbageLoad = new UnloadAllLoad("Garbage");

    INodeDepot nodeDepotUnloadAllLoads = new SimpleNodeDepot(depotId);
    nodeDepotUnloadAllLoads.add(trashLoad);
    nodeDepotUnloadAllLoads.add(garbageLoad);

    return nodeDepotUnloadAllLoads;
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
