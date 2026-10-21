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

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.serialize.SerializationException;
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
import com.dna.jopt.touroptimizer.java.examples.util.jsonprinter.ResultJsonPrinter;

/**
 * Lets the optimizer plan how much Bread two bakeries make, with a SupplyFlexLoad at each bakery.
 *
 * <p>Scenario: JackTruckCologne starts in Koeln and JohnTruckAachen in Aachen, each empty, with a
 * depot of 20 Bread, and both work from 08:00 to 20:00 on March 11, 2020 (Europe/Berlin). Six
 * stores, open from 08:00 to 18:00, request Bread: StoreEssen 5, StoreDueren 2, StoreWuppertal 4,
 * StoreDortmund 8, StoreMoenchengladbach 1 and StoreLeverkusen 7, 27 in all. The bakery nodes
 * SupplyFlexNodeCologne and SupplyFlexNodeAachen lie at the start positions of the trucks; each
 * holds a SupplyFlexLoad of Bread.
 *
 * <p>Look for: the depot of each bakery node before its visit, with the amount of Bread that the
 * optimizer set: the sum of the requests of the stores on the same route. Then the JSON of the
 * optimization, which holds no license.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md">PND.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDBakeryChainFlexLoadExample extends Optimization {

  /**
   * Runs the example.
   *
   * @param args not used
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to a config
   * @throws SerializationException if the config cannot be serialized
   */
  public static void main(String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    new PNDBakeryChainFlexLoadExample().example();
  }

  @Override
  public String toString() {
    return "Lets the optimizer plan how much Bread two bakeries make, with a SupplyFlexLoad at each"
        + " bakery.";
  }

  /**
   * Builds the optimization, runs it, prints the result and prints the JSON.
   *
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to a config
   * @throws SerializationException if the config cannot be serialized
   */
  public void example()
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException,
      IOException, ConvertException, SerializationException {
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
    printJson(this);
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
   * Adds the six stores with their requests and the two bakery nodes with their SupplyFlexLoads.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 80 minutes, a sample value.
    Duration visitDuration = Duration.ofMinutes(80);

    INode storeEssen =
        new TimeWindowGeoNode("StoreEssen", 51.45, 7.01667, openingHours, visitDuration, 1);
    storeEssen.setNodeDepot(createNodeDepot("StoreEssenDepot", 5));
    opti.addElement(storeEssen);

    INode storeDueren =
        new TimeWindowGeoNode("StoreDueren", 50.8, 6.48333, openingHours, visitDuration, 1);
    storeDueren.setNodeDepot(createNodeDepot("StoreDuerenDepot", 2));
    opti.addElement(storeDueren);

    INode storeWuppertal =
        new TimeWindowGeoNode("StoreWuppertal", 51.2667, 7.18333, openingHours, visitDuration, 1);
    storeWuppertal.setNodeDepot(createNodeDepot("StoreWuppertalDepot", 4));
    opti.addElement(storeWuppertal);

    INode storeDortmund =
        new TimeWindowGeoNode("StoreDortmund", 51.51728, 7.48446, openingHours, visitDuration, 1);
    storeDortmund.setNodeDepot(createNodeDepot("StoreDortmundDepot", 8));
    opti.addElement(storeDortmund);

    INode storeMoenchengladbach =
        new TimeWindowGeoNode(
            "StoreMoenchengladbach", 51.19541, 6.41172, openingHours, visitDuration, 1);
    storeMoenchengladbach.setNodeDepot(createNodeDepot("StoreMoenchengladbachDepot", 1));
    opti.addElement(storeMoenchengladbach);

    INode storeLeverkusen =
        new TimeWindowGeoNode("StoreLeverkusen", 51.04154, 6.99944, openingHours, visitDuration, 1);
    storeLeverkusen.setNodeDepot(createNodeDepot("StoreLeverkusenDepot", 7));
    opti.addElement(storeLeverkusen);

    // The bakery nodes lie at the start positions of the trucks; there the optimizer sets how
    // much Bread each bakery makes.
    INode supplyFlexNodeAachen =
        new TimeWindowGeoNode(
            "SupplyFlexNodeAachen", 50.775346, 6.083887, openingHours, visitDuration, 1);
    supplyFlexNodeAachen.setNodeDepot(createSupplyFlexDepot("SupplyFlexNodeAachenDepot"));
    opti.addElement(supplyFlexNodeAachen);

    INode supplyFlexNodeCologne =
        new TimeWindowGeoNode(
            "SupplyFlexNodeCologne", 50.9333, 6.95, openingHours, visitDuration, 1);
    supplyFlexNodeCologne.setNodeDepot(createSupplyFlexDepot("SupplyFlexNodeCologneDepot"));
    opti.addElement(supplyFlexNodeCologne);
  }

  /**
   * Creates the depot of a store with a fuzzy request of Bread.
   *
   * @param depotId the id of the depot
   * @param breadCount the Bread that the store requests
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(String depotId, int breadCount) {
    ILoad breadLoad = new SimpleLoad("Bread", breadCount, true, true);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(breadLoad);
    return depot;
  }

  /**
   * Creates the depot of a bakery node with a SupplyFlexLoad of Bread.
   *
   * @param depotId the id of the depot
   * @return the node depot
   */
  private static INodeDepot createSupplyFlexDepot(String depotId) {
    // The SupplyFlexLoad starts with 20, the capacity of a truck; the optimizer sets the amount
    // that the bakery makes.
    ILoad breadLoad = new SupplyFlexLoad("Bread", 20);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(breadLoad);
    return depot;
  }

  /**
   * Adds JackTruckCologne and JohnTruckAachen, each with its own depot.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // 12 h, the length of the working hours, a sample value above what the routes of this plan
    // need.
    Duration maxWorkingTime = Duration.ofHours(12);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource truckJack =
        new CapacityResource(
            "JackTruckCologne",
            50.9333,
            6.95,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    truckJack.setResourceDepot(createResourceDepot("JackTruckCologneDepot"));
    opti.addElement(truckJack);

    IResource truckJohn =
        new CapacityResource(
            "JohnTruckAachen",
            50.775346,
            6.083887,
            maxWorkingTime,
            maxDistanceKmW,
            createWorkingHours());
    truckJohn.setResourceDepot(createResourceDepot("JohnTruckAachenDepot"));
    opti.addElement(truckJohn);
  }

  /**
   * Creates the working hours of one truck: 08:00 to 20:00 on March 11, 2020.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
  }

  /**
   * Creates the depot of a truck: up to 20 Bread, empty at the start.
   *
   * @param depotId the id of the depot
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot(String depotId) {
    // Bread is the only good, so its capacity is the whole depot of 20.
    ILoadCapacity breadCapacity = new SimpleLoadCapacity("Bread", 20, 0);

    IResourceDepot depot = new SimpleResourceDepot(depotId, 20);
    depot.add(breadCapacity);
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

  /**
   * Prints the optimization as JSON, without the solution, the node connections and a license.
   *
   * @param opti the optimization
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted to a config
   * @throws SerializationException if the config cannot be serialized
   */
  private static void printJson(IOptimization opti)
      throws IOException, ConvertException, SerializationException {
    // The JSON leaves out the solution and the node connections, so that it stays short.
    boolean keepSolutionInJson = false;
    boolean keepNodeConnectionsInJson = false;

    // The extension holds a timeout of 10 minutes, a sample value, and no key setting, so the
    // printed JSON contains no license.
    JSONConfig extension = JSONConfig.builder().timeOut(Duration.ofMinutes(10)).build();

    String json =
        ResultJsonPrinter.toJsonOptimizationString(
            ResultJsonPrinter.toJsonOptimization(
                opti, extension, keepSolutionInJson, keepNodeConnectionsInJson));
    System.out.println(json);
  }
}
