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
import com.dna.jopt.member.unit.pnd.load.flexload.MixedFlexLoad;
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.pnd.util.CargoSpace;
import com.dna.jopt.member.unit.pnd.util.CargoSpaceGood;
import com.dna.jopt.member.unit.pnd.util.ICargoSpace;
import com.dna.jopt.member.unit.pnd.util.ICargoSpaceGood;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

import java.util.List;
import java.util.Optional;

import static java.time.Month.MARCH;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.MetricPrefix.CENTI;
import static tec.units.ri.unit.MetricPrefix.MILLI;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * In this example a resource called "JackTruck" has to deliver/pickup "Pianos" and has to
 * deliver/pickup "Cups". Each customer can decide what to pickup (customer supply) and what needs
 * to be delivered (customer request).
 *
 * <p>It is obvious that a truck can load more units of cups than units of Pianos. Let's assume the
 * cups are transported on pallets with a size of 1219 × 1016 mm (48 × 40 inches). We do not allow
 * to stack the pallets as otherwise, cups could break. Each customer is only allowed to request or
 * provide pallets of cups.
 *
 * <p>A piano has a floor space of 160 x 140 cm (63 x 55 inches). Obviously we cannot stack pianos as
 * well.
 *
 * <p>The truck has floor space of 7 x 2.5 m (23 x 8 feet).
 *
 * <p>The goal is to find a way to tell the Optimizer, what is the maximal amount of both goods a
 * truck can carry without overloading. For this we have to calculate the equivalent of one piano
 * and one pallet of cups.
 *
 * <p>Precisely, we think of a ground unit with the value one (for cargo space this is square
 * meter). What is the equivalent of a Piano and what is the equivalent of a pallet of cups in this
 * ground unit?
 *
 * <p>TODO add Truck size
 *
 * @author Jens Richter
 * @version Jul 27, 2020
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDCapacityFactorExample extends Optimization {

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
    new PNDCapacityFactorExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example: Pick up and delivery optimization with Cargo Space capacity factors.";
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
     * Defining a ResourceDepot "JackTruckDepot"
     *
     */

    /*
     * First we have to do the Cargo calculation
     */

    // Define the cargo space of the truck
    ICargoSpace truckCargoSpace =
        new CargoSpace(Quantities.getQuantity(7.0, METRE), Quantities.getQuantity(2.5, METRE));

    // Define the cargo good for the piano
    ICargoSpaceGood pianoCargoGood =
        new CargoSpaceGood(
            Quantities.getQuantity(160, CENTI(METRE)), Quantities.getQuantity(140, CENTI(METRE)));

    // Define the cargo good for the cups
    ICargoSpaceGood cupsCargoGood =
        new CargoSpaceGood(
            Quantities.getQuantity(1219, MILLI(METRE)), Quantities.getQuantity(1016, MILLI(METRE)));

    // What would be the maximal individual  load of a certain good in our cargoSpace?
    int maxPianoLoad = pianoCargoGood.calculateMaxIndividualLoading(truckCargoSpace);
    int maxCupLoad = cupsCargoGood.calculateMaxIndividualLoading(truckCargoSpace);

    System.out.println("Found maxPianoLoad: " + maxPianoLoad);
    System.out.println("Found maxCupLoad: " + maxCupLoad);

    /*
     * Create the loads
     */
    // Create the loads - we have some initial load
    // Note: The initial load is NOT an abstract unit. Here it is number of Pianos and number of
    // Pallets
    ILoadCapacity pianoCpacity = new SimpleLoadCapacity("Piano", maxPianoLoad, 2);
    ILoadCapacity cupCpacity = new SimpleLoadCapacity("Cup", maxCupLoad, 5);

    /*
     * Create the depot
     */

    // Calculate the max loading in the ground unit - For space this is in square meter
    double maxTotalLoadValue = truckCargoSpace.calculateMaxTotalLoadingCapacity();

    System.out.println("Found max total loading in the ground unit: " + maxTotalLoadValue);

    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", maxTotalLoadValue);

    // Calculate the individual loading factors
    // The loadFactor calculation can fail, in case the maximal individual load would be zero
    Optional<Double> pianoLoadFactorOpt = truckCargoSpace.calculateLoadingCapacityFactor(pianoCargoGood);
    Optional<Double> cupsLoadFactorOpt = truckCargoSpace.calculateLoadingCapacityFactor(cupsCargoGood);

    // Add the factors
    if (pianoLoadFactorOpt.isPresent()) {
      Double pianoLoadFactor = pianoLoadFactorOpt.get();
      depot.add("Piano", pianoLoadFactor);
      System.out.println("Found Piano load factor: " + pianoLoadFactor);
    }

    if (cupsLoadFactorOpt.isPresent()) {
      Double cupsLoadFactor = cupsLoadFactorOpt.get();
      depot.add("Cup", cupsLoadFactor);
      System.out.println("Found Cup load factor: " + cupsLoadFactor);
    }

    // Add the loads
    depot.add(pianoCpacity);
    depot.add(cupCpacity);

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
    INode flexNode =
        new TimeWindowGeoNode("FlexNode", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

    flexNode.setNodeDepot(this.createMixedFlexDepot("FlexNodeDepot"));
    this.addElement(flexNode);
  }

  /**
   * Creates the node depot.
   *
   * @param depotId the depot id
   * @param pianoCount the piano count
   * @param isPianoRequest the is piano request
   * @param cupsPalletCount the cups pallet count
   * @param isCupRequest the is cup request
   * @return the i node depot
   */
  public INodeDepot createNodeDepot(
      String depotId,
      int pianoCount,
      boolean isPianoRequest,
      int cupsPalletCount,
      boolean isCupRequest) {

    ILoad pianoLoad = new SimpleLoad("Piano", pianoCount, isPianoRequest, true);
    ILoad cupLoad = new SimpleLoad("Cup", cupsPalletCount, isCupRequest, true);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);

    if (pianoCount > 0) {
      // Only add if count is bigger than zero.
      // However, this is not necessary but improves the readability of the result.
      // The optimizer can also handle zero requests/supplies
      customerNodeDepot.add(pianoLoad);
    }

    if (cupsPalletCount > 0) {
      customerNodeDepot.add(cupLoad);
    }

    return customerNodeDepot;
  }

  /**
   * Creates the mixed flex depot.
   *
   * @param depotId the depot id
   * @return the i node depot
   */
  public INodeDepot createMixedFlexDepot(String depotId) {

    // Adding MixedFlexLoads that adjust load request or supply during optimization
    ILoad pianoLoad = new MixedFlexLoad("Piano");

    // The initial (before optimization) properties of a MixedFlexLoad are a request of 0.0
    // For helping the optimizer to faster converge, and initial value for FlexLoad and
    // and initial value for the request state can be provided.
    //
    // For example: Our MixedFlexLoad for TV starts as supply (isRequest is false) with a value of
    // 2.0
    ILoad cupLoad = new MixedFlexLoad("Cup", 2, false);

    INodeDepot nodeDepotWithMixedFlexLoads = new SimpleNodeDepot(depotId);
    nodeDepotWithMixedFlexLoads.add(pianoLoad);
    nodeDepotWithMixedFlexLoads.add(cupLoad);

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
