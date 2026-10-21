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
import static javax.measure.MetricPrefix.CENTI;
import static javax.measure.MetricPrefix.KILO;
import static javax.measure.MetricPrefix.MILLI;
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

/**
 * Turns the floor space of a truck, a piano and a pallet of cups into capacity factors with
 * CargoSpace, so that one Piano uses more of the depot than one Cup.
 *
 * <p>Scenario: JackTruck starts at 50.1167, 7.68333 with 2 Piano and 5 Cup (pallets of cups) on
 * board and works from 08:00 to 20:00 on March 11, 2020 (Europe/Berlin). Its floor is 7 x 2.5 m,
 * a piano takes 160 x 140 cm and a pallet 1219 x 1016 mm, and neither good is stacked. Three
 * customers and FlexNode are at one point in Koeln, open from 08:00 to 18:00: CustomerOne
 * requests 2 Piano and supplies 3 Cup, CustomerTwo supplies 1 Piano and requests 4 Cup,
 * CustomerThree supplies 1 Cup, and FlexNode holds a MixedFlexLoad of each good.
 *
 * <p>Look for: the lines printed before the run: at most 4 pianos or 10 pallets fit on the
 * floor, the floor holds 17.5 square meters, and the factors are 4.375 for Piano and 1.75 for
 * Cup. Then the ResourceDepot report of JackTruck: its used capacity at the start is 17.5 (2 x
 * 4.375 + 5 x 1.75), the full depot.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md">PND.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDCapacityFactorExample extends Optimization {

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
    new PNDCapacityFactorExample().example();
  }

  @Override
  public String toString() {
    return "Turns the floor space of a truck, a piano and a pallet of cups into capacity factors"
        + " with CargoSpace, so that one Piano uses more of the depot than one Cup.";
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
   * Adds three customers with loads of Piano and Cup, and FlexNode with MixedFlexLoads.
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

    // All nodes are at one point in Koeln, so the order of the visits changes the loads on board,
    // not the driving.
    INode customerOne =
        new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerOne.setNodeDepot(createNodeDepot("CustomerOneDepot", 2, true, 3, false));
    opti.addElement(customerOne);

    INode customerTwo =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerTwo.setNodeDepot(createNodeDepot("CustomerTwoDepot", 1, false, 4, true));
    opti.addElement(customerTwo);

    INode customerThree =
        new TimeWindowGeoNode("CustomerThree", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerThree.setNodeDepot(createNodeDepot("CustomerThreeDepot", 0, false, 1, false));
    opti.addElement(customerThree);

    INode flexNode =
        new TimeWindowGeoNode("FlexNode", 50.9333, 6.95, openingHours, visitDuration, 1);
    flexNode.setNodeDepot(createMixedFlexDepot("FlexNodeDepot"));
    opti.addElement(flexNode);
  }

  /**
   * Creates the depot of a customer with fuzzy loads of Piano and Cup.
   *
   * @param depotId the id of the depot
   * @param pianoCount the pianos that the customer requests or supplies
   * @param isPianoRequest true if the customer requests the pianos, false if it supplies them
   * @param cupCount the pallets of cups that the customer requests or supplies
   * @param isCupRequest true if the customer requests the pallets, false if it supplies them
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(
      String depotId, int pianoCount, boolean isPianoRequest, int cupCount, boolean isCupRequest) {
    ILoad pianoLoad = new SimpleLoad("Piano", pianoCount, isPianoRequest, true);
    ILoad cupLoad = new SimpleLoad("Cup", cupCount, isCupRequest, true);

    INodeDepot depot = new SimpleNodeDepot(depotId);

    // A load of 0 is allowed; leaving it out keeps the depot report shorter.
    if (pianoCount > 0) {
      depot.add(pianoLoad);
    }

    if (cupCount > 0) {
      depot.add(cupLoad);
    }

    return depot;
  }

  /**
   * Creates the depot of FlexNode with a MixedFlexLoad of Piano and one of Cup.
   *
   * @param depotId the id of the depot
   * @return the node depot
   */
  private static INodeDepot createMixedFlexDepot(String depotId) {
    // A MixedFlexLoad whose value and direction the optimizer sets. Without start values it
    // starts as a supply of 0.
    ILoad pianoLoad = new MixedFlexLoad("Piano");

    // Start values can be given: the MixedFlexLoad of Cup starts as a supply (false) of 2, a sample
    // value.
    ILoad cupLoad = new MixedFlexLoad("Cup", 2, false);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(pianoLoad);
    depot.add(cupLoad);
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
   * Creates the depot of JackTruck from the floor space of the truck and of the two goods, and
   * prints the values it computes.
   *
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot() {
    ICargoSpace truckCargoSpace =
        new CargoSpace(Quantities.getQuantity(7.0, METRE), Quantities.getQuantity(2.5, METRE));

    // A CargoSpaceGood with length and width only is not stacked.
    ICargoSpaceGood pianoCargoGood =
        new CargoSpaceGood(
            Quantities.getQuantity(160, CENTI(METRE)), Quantities.getQuantity(140, CENTI(METRE)));
    ICargoSpaceGood cupsCargoGood =
        new CargoSpaceGood(
            Quantities.getQuantity(1219, MILLI(METRE)), Quantities.getQuantity(1016, MILLI(METRE)));

    // How many of one good fit on the floor when no other good is on board.
    int maxPianoLoad = pianoCargoGood.calculateMaxIndividualLoading(truckCargoSpace);
    int maxCupLoad = cupsCargoGood.calculateMaxIndividualLoading(truckCargoSpace);
    System.out.println("Found maxPianoLoad: " + maxPianoLoad);
    System.out.println("Found maxCupLoad: " + maxCupLoad);

    // The initial loads count goods, not square meters: 2 pianos and 5 pallets are on board.
    ILoadCapacity pianoCapacity = new SimpleLoadCapacity("Piano", maxPianoLoad, 2);
    ILoadCapacity cupCapacity = new SimpleLoadCapacity("Cup", maxCupLoad, 5);

    // The total capacity of the depot is the floor space in square meters.
    double maxTotalLoadValue = truckCargoSpace.calculateMaxTotalLoadingCapacity();
    System.out.println("Found max total loading in the ground unit: " + maxTotalLoadValue);

    IResourceDepot depot = new SimpleResourceDepot("JackTruckDepot", maxTotalLoadValue);

    // The factor of a good is the floor space divided by the units of the good that fit on it, in
    // square meters per unit. There is no factor if not one unit fits.
    Optional<Double> pianoLoadFactorOpt =
        truckCargoSpace.calculateLoadingCapacityFactor(pianoCargoGood);
    Optional<Double> cupsLoadFactorOpt =
        truckCargoSpace.calculateLoadingCapacityFactor(cupsCargoGood);

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

    depot.add(pianoCapacity);
    depot.add(cupCapacity);
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
