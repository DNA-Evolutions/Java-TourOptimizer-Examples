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
import com.dna.jopt.member.unit.pnd.load.timed.ITimedLoad;
import com.dna.jopt.member.unit.pnd.load.timed.TimedLoad;
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
 * In this example a Resource called "JackTaxi" has to transport passengers from one location to
 * another location. Each passenger should only stay a maximum of one hour in Jacks car. For this
 * purpose TimedLoads are created.
 *
 * <p>The example is designed to, most likely, show a violation of Lars' transportation job with an
 * overtime value of 19.33 minutes.
 *
 * @author Jens Richter
 * @version Mar 08, 2021
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDTimedLoadExample extends Optimization {

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
    new PNDTimedLoadExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example of passagener transportation with maximal transport time (TimedLoads).";
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

    // We have to tell the optimizer that we have a high interest in capacity planning, the default is
    // 100
    props.setProperty("JOptWeight.Capacity", "200");
    this.addElement(props);
  }

  /** Adds the res. */
  public void addRes() {

    /*
     * Setting the Resource JackTaxi
     */

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2800.0, KILO(METRE));

    IResource taxiJack =
        new CapacityResource(
            "JackTaxi", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);

    // Setting a Depot (passenger cabin) to our taxi
    taxiJack.setResourceDepot(this.createResourceDepot());

    // Adding the taxi to our optimization
    this.addElement(taxiJack);
  }

  /**
   * Creates the resource depot.
   *
   * @return the resource depot
   */
  public IResourceDepot createResourceDepot() {

    /*
     * Defining a ResourceDepot "JackTaxiDepot" (passenger cabin) that can host the passengers.
     *
     */

    // We add each potential passenger to our depot. Notice, if the currentLoad is 0, the passengers still need to be
    // picked up.
    ILoadCapacity maxCapacity = new SimpleLoadCapacity("Max", 1, 0);

    ILoadCapacity peterCapacity = new SimpleLoadCapacity("Peter", 1, 0);

    // Lars is already on board
    ILoadCapacity larsCapacity = new SimpleLoadCapacity("Lars", 1, 1);

    // Our passenger cabin can host a maximum of 3 passengers
    IResourceDepot depot = new SimpleResourceDepot("JackTaxiDepot", 3);

    // Adding the capacities to our depot
    depot.add(maxCapacity);
    depot.add(peterCapacity);
    depot.add(larsCapacity);

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
        new TimeWindowGeoNode("MaxPickup", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job1.setNodeDepot(this.createNodeDepot("MaxPickupDepot", "Max", Duration.ofHours(1), false));
    this.addElement(job1);

    //
    INode job2 =
        new TimeWindowGeoNode("MaxDrop", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job2.setNodeDepot(this.createNodeDepot("MaxDropDepot", "Max", Duration.ofHours(1), true));
    this.addElement(job2);

    //
    INode job3 =
        new TimeWindowGeoNode("PeterPickup", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job3.setNodeDepot(
        this.createNodeDepot("PeterPickupDepot", "Peter", Duration.ofHours(1), false));
    this.addElement(job3);

    //
    INode job4 =
        new TimeWindowGeoNode("PeterDrop", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job4.setNodeDepot(this.createNodeDepot("PeterDropDepot", "Peter", Duration.ofHours(1), true));
    this.addElement(job4);

    INode job5 =
        new TimeWindowGeoNode("LarsDrop", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    job5.setNodeDepot(this.createNodeDepot("LarsDropDepot", "Lars", Duration.ofHours(1), true));
    this.addElement(job5);
  }

  /**
   * Creates the node depot.
   *
   * @param depotId the depot id
   * @param loadId the load id
   * @param maxTime the max time
   * @param isRequest the is request
   * @return the i node depot
   */
  public INodeDepot createNodeDepot(
      String depotId, String loadId, Duration maxTime, boolean isRequest) {

    ITimedLoad load = new TimedLoad(loadId, maxTime, isRequest);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);

    customerNodeDepot.add(load);

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
