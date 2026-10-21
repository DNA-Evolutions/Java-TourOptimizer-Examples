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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

/**
 * Picks up Trash and Garbage and empties the truck at optional dumps with UnloadAllLoads, so that
 * the depot does not overflow.
 *
 * <p>Scenario: JackTruck starts empty at 50.1167, 7.68333, with a depot of 30 in all, and works
 * from 08:00 to 20:00 on March 11, 2020 (Europe/Berlin). Three customers at one point in Koeln,
 * open from 08:00 to 18:00, supply 48 in all: CustomerOne 15 Trash and 10 Garbage, CustomerTwo 13
 * Trash and CustomerThree 10 Garbage. UnloadAllNodeOne and UnloadAllNodeTwo lie at the same point;
 * UnloadAllNodeFarAway lies at 57.9333, 11.95, far from all others. The three dumps are optional
 * and take all Trash and Garbage on board.
 *
 * <p>Look for: a visit to UnloadAllNodeOne or UnloadAllNodeTwo between the pickups, so that the
 * load never goes above 30, no visit to UnloadAllNodeFarAway, and no violation.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md">PND.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDOptionalUnloadAllLoadExample extends Optimization {

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
    new PNDOptionalUnloadAllLoadExample().example();
  }

  @Override
  public String toString() {
    return "Picks up Trash and Garbage and empties the truck at optional dumps with UnloadAllLoads,"
        + " so that the depot does not overflow.";
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
   * Adds three customers that supply Trash and Garbage, and three optional dumps.
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

    // The customers and two dumps are at one point in Koeln, so the order of the visits changes
    // the loads on board, not the driving.
    INode customerOne =
        new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerOne.setNodeDepot(createNodeDepot("CustomerOneDepot", 15, 10));
    opti.addElement(customerOne);

    INode customerTwo =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerTwo.setNodeDepot(createNodeDepot("CustomerTwoDepot", 13, 0));
    opti.addElement(customerTwo);

    INode customerThree =
        new TimeWindowGeoNode("CustomerThree", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerThree.setNodeDepot(createNodeDepot("CustomerThreeDepot", 0, 10));
    opti.addElement(customerThree);

    // The optimizer decides whether the route visits an optional node.
    INode unloadAllNodeOne =
        new TimeWindowGeoNode("UnloadAllNodeOne", 50.9333, 6.95, openingHours, visitDuration, 1);
    unloadAllNodeOne.setNodeDepot(createUnloadAllDepot("UnloadAllNodeOneDepot"));
    unloadAllNodeOne.setIsOptional(true);
    opti.addElement(unloadAllNodeOne);

    INode unloadAllNodeTwo =
        new TimeWindowGeoNode("UnloadAllNodeTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
    unloadAllNodeTwo.setNodeDepot(createUnloadAllDepot("UnloadAllNodeTwoDepot"));
    unloadAllNodeTwo.setIsOptional(true);
    opti.addElement(unloadAllNodeTwo);

    // A dump far away, on purpose: the near dumps take the same goods.
    INode unloadAllNodeFarAway =
        new TimeWindowGeoNode(
            "UnloadAllNodeFarAway", 57.9333, 11.95, openingHours, visitDuration, 1);
    unloadAllNodeFarAway.setNodeDepot(createUnloadAllDepot("UnloadAllNodeFarAwayDepot"));
    unloadAllNodeFarAway.setIsOptional(true);
    opti.addElement(unloadAllNodeFarAway);
  }

  /**
   * Creates the depot of a customer with fuzzy supplies of Trash and Garbage.
   *
   * @param depotId the id of the depot
   * @param trashCount the Trash that the customer supplies
   * @param garbageCount the Garbage that the customer supplies
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(String depotId, int trashCount, int garbageCount) {
    ILoad trashLoad = new SimpleLoad("Trash", trashCount, false, true);
    ILoad garbageLoad = new SimpleLoad("Garbage", garbageCount, false, true);

    INodeDepot depot = new SimpleNodeDepot(depotId);

    // A load of 0 is allowed; leaving it out keeps the depot report shorter.
    if (trashCount > 0) {
      depot.add(trashLoad);
    }

    if (garbageCount > 0) {
      depot.add(garbageLoad);
    }

    return depot;
  }

  /**
   * Creates the depot of a dump with an UnloadAllLoad of Trash and one of Garbage.
   *
   * @param depotId the id of the depot
   * @return the node depot
   */
  private static INodeDepot createUnloadAllDepot(String depotId) {
    // An UnloadAllLoad is a request of everything of its good that is on board.
    ILoad trashLoad = new UnloadAllLoad("Trash");
    ILoad garbageLoad = new UnloadAllLoad("Garbage");

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(trashLoad);
    depot.add(garbageLoad);
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
   * Creates the depot of JackTruck: Trash and Garbage, 30 in all, empty at the start.
   *
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot() {
    // Each good may fill all 30 units of the depot when it is alone on the truck; together they
    // share the 30, for example 19 Trash and 11 Garbage.
    ILoadCapacity trashCapacity = new SimpleLoadCapacity("Trash", 30, 0);
    ILoadCapacity garbageCapacity = new SimpleLoadCapacity("Garbage", 30, 0);

    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", 30);
    depot.add(trashCapacity);
    depot.add(garbageCapacity);
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
