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
 * Shows fuzzy loads, which allow a partial delivery or pickup, next to loads that allow only a
 * complete one.
 *
 * <p>Scenario: JackTruck starts at 50.1167, 7.68333 with 10 Apple in a depot that holds 30, and
 * works from 08:00 to 20:00 on March 11, 2020 (Europe/Berlin). Four customers at one point in
 * Koeln, open from 08:00 to 18:00: CustomerRequestFuzzy requests 20 Apple and CustomerSupplyFuzzy
 * supplies 40, both fuzzy; CustomerRequest requests 35 and CustomerSupply supplies 35, neither
 * fuzzy. 35 is more than the depot holds, so these two cannot be served in full.
 *
 * <p>Look for: the depot of each customer before and after its visit. A fuzzy load is served in
 * part, and the rest stays in the depot of the customer; a load that is not fuzzy is not served
 * at all. The result reports each of them with a CAPACITY violation: UNDERLOAD for a request,
 * OVERLOAD for a supply.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md">PND.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDSimpleFuzzyExample extends Optimization {

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
    new PNDSimpleFuzzyExample().example();
  }

  @Override
  public String toString() {
    return "Shows fuzzy loads, which allow a partial delivery or pickup, next to loads that allow"
        + " only a complete one.";
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
   * Adds four customers: a fuzzy and a complete request, a fuzzy and a complete supply.
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

    // All customers are at one point in Koeln, so the order of the visits changes the loads on
    // board, not the driving.
    INode requestFuzzy =
        new TimeWindowGeoNode(
            "CustomerRequestFuzzy", 50.9333, 6.95, openingHours, visitDuration, 1);
    requestFuzzy.setNodeDepot(createNodeDepot("CustomerRequestFuzzyDepot", 20, true, true));
    opti.addElement(requestFuzzy);

    INode supplyFuzzy =
        new TimeWindowGeoNode(
            "CustomerSupplyFuzzy", 50.9333, 6.95, openingHours, visitDuration, 1);
    supplyFuzzy.setNodeDepot(createNodeDepot("CustomerSupplyFuzzyDepot", 40, false, true));
    opti.addElement(supplyFuzzy);

    INode request =
        new TimeWindowGeoNode("CustomerRequest", 50.9333, 6.95, openingHours, visitDuration, 1);
    request.setNodeDepot(createNodeDepot("CustomerRequestDepot", 35, true, false));
    opti.addElement(request);

    INode supply =
        new TimeWindowGeoNode("CustomerSupply", 50.9333, 6.95, openingHours, visitDuration, 1);
    supply.setNodeDepot(createNodeDepot("CustomerSupplyDepot", 35, false, false));
    opti.addElement(supply);
  }

  /**
   * Creates the depot of a customer with one load of Apple.
   *
   * @param depotId the id of the depot
   * @param appleCount the Apple that the customer requests or supplies
   * @param isRequest true for a request, false for a supply
   * @param isFuzzy true if a partial exchange is allowed
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(
      String depotId, int appleCount, boolean isRequest, boolean isFuzzy) {
    ILoad appleLoad = new SimpleLoad("Apple", appleCount, isRequest, isFuzzy);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(appleLoad);
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
   * Creates the depot of JackTruck: up to 30 Apple, with 10 on board at the start.
   *
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot() {
    // Apple is the only good, so its capacity is the whole depot of 30.
    ILoadCapacity appleCapacity = new SimpleLoadCapacity("Apple", 30, 10);

    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", 30);
    depot.add(appleCapacity);
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
