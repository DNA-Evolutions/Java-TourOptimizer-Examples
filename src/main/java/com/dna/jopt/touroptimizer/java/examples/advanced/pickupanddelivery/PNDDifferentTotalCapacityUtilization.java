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
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Shares the total capacity of a depot between two load capacities with different maximums, so
 * that the DelicateFruit on board limits the GlassVials that the truck can pick up.
 *
 * <p>Scenario: Truck starts at 50.1167, 7.68333 with 30 DelicateFruit and 10 GlassVials on board
 * and works from 08:00 to 20:00 on March 11, 2020 (Europe/Berlin). Its depot holds 70 in all;
 * DelicateFruit may use at most 30 of it, GlassVials all 70. CustomerOne requests 25
 * DelicateFruit and supplies 5 GlassVials; CustomerTwo requests 5 DelicateFruit and supplies 40
 * GlassVials. Both customers are at one point in Koeln and open from 08:00 to 18:00.
 *
 * <p>Look for: the order of the visits. Truck visits CustomerOne first and delivers 25
 * DelicateFruit there before it picks up the 40 GlassVials of CustomerTwo, and the result has no
 * violation. The ResourceDepot report shows a used capacity of 40 at the start and 55 at the
 * termination.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PNDDifferentTotalCapacityUtilization.md">PNDDifferentTotalCapacityUtilization.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDDifferentTotalCapacityUtilization extends Optimization {

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
    new PNDDifferentTotalCapacityUtilization().example();
  }

  @Override
  public String toString() {
    return "Shares the total capacity of a depot between two load capacities with different"
        + " maximums, so that the DelicateFruit on board limits the GlassVials that the truck can"
        + " pick up.";
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
   * Adds CustomerOne and CustomerTwo, each with a request of DelicateFruit and a supply of
   * GlassVials.
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

    // Both customers are at one point in Koeln, so only the loads on board decide the order of
    // the visits.
    INode customerOne =
        new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerOne.setNodeDepot(createNodeDepot("CustomerOneDepot", 25, 5));
    opti.addElement(customerOne);

    INode customerTwo =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerTwo.setNodeDepot(createNodeDepot("CustomerTwoDepot", 5, 40));
    opti.addElement(customerTwo);
  }

  /**
   * Creates the depot of a customer: a request of DelicateFruit and a supply of GlassVials.
   *
   * @param depotId the id of the depot
   * @param fruitRequest the DelicateFruit that the customer requests
   * @param glassSupply the GlassVials that the customer supplies
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(String depotId, int fruitRequest, int glassSupply) {
    // The third argument makes the load a request (true) or a supply (false); the fourth allows a
    // partial exchange (fuzzy).
    ILoad fruitLoad = new SimpleLoad("DelicateFruit", fruitRequest, true, true);
    ILoad glassLoad = new SimpleLoad("GlassVials", glassSupply, false, true);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(fruitLoad);
    depot.add(glassLoad);
    return depot;
  }

  /**
   * Adds Truck with its depot.
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

    IResource truck =
        new CapacityResource(
            "Truck", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);
    truck.setResourceDepot(createResourceDepot());
    opti.addElement(truck);
  }

  /**
   * Creates the depot of Truck: 70 in all, of which DelicateFruit may use 30 and GlassVials 70.
   *
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot() {
    // The total capacity of 70, which DelicateFruit and GlassVials share.
    IResourceDepot depot = new SimpleResourceDepot("truckDepot", 70);

    // DelicateFruit may use at most 30 of the 70; Truck starts with 30 on board.
    ILoadCapacity fruitCapacity = new SimpleLoadCapacity("DelicateFruit", 30, 30);

    // GlassVials may use all 70; Truck starts with 10 on board.
    ILoadCapacity glassCapacity = new SimpleLoadCapacity("GlassVials", 70, 10);

    depot.add(fruitCapacity);
    depot.add(glassCapacity);
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
