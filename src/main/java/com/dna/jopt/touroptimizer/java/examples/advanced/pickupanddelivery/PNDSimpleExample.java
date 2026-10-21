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
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.util.jsonprinter.ResultJsonPrinter;

/**
 * Delivers Fruit to two customers and picks up Bread from them, with simple loads on one truck.
 *
 * <p>Scenario: Truck starts at 50.1167, 7.68333 with 5 Fruit and 10 Bread on board and works
 * from 08:00 to 20:00 on March 11, 2020 (Europe/Berlin). CustomerOne requests 2 Fruit and
 * supplies 10 Bread; CustomerTwo requests 3 Fruit and supplies 10 Bread. Both customers are at
 * one point in Koeln and open from 08:00 to 18:00.
 *
 * <p>Look for: the ResourceDepot report of Truck, with 5 Fruit and 10 Bread at the start and 0
 * Fruit and 30 Bread at the termination, and the depot of each customer before and after its
 * visit; then the JSON of the optimization, which holds no license.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/pickupanddelivery/PND.md">PND.md</a>
 * @see <a href="https://www.dna-evolutions.com/docs/learn-and-explore/feature-guides/pickup_and_delivery">Pickup and delivery</a>
 */
public class PNDSimpleExample extends Optimization {

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
    new PNDSimpleExample().example();
  }

  @Override
  public String toString() {
    return "Delivers Fruit to two customers and picks up Bread from them, with simple loads on one"
        + " truck.";
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
   * Adds CustomerOne and CustomerTwo, each with a request of Fruit and a supply of Bread.
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

    // Both customers are at one point in Koeln, so the order of the visits changes the loads on
    // board, not the driving.
    INode customerOne =
        new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerOne.setNodeDepot(createNodeDepot("CustomerOneDepot", 2, 10));
    opti.addElement(customerOne);

    INode customerTwo =
        new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, openingHours, visitDuration, 1);
    customerTwo.setNodeDepot(createNodeDepot("CustomerTwoDepot", 3, 10));
    opti.addElement(customerTwo);
  }

  /**
   * Creates the depot of a customer: a request of Fruit and a supply of Bread.
   *
   * @param depotId the id of the depot
   * @param fruitRequest the Fruit that the customer requests
   * @param breadSupply the Bread that the customer supplies
   * @return the node depot
   */
  private static INodeDepot createNodeDepot(String depotId, int fruitRequest, int breadSupply) {
    // The third argument makes the load a request (true) or a supply (false); the fourth allows a
    // partial exchange (fuzzy).
    ILoad fruitLoad = new SimpleLoad("Fruit", fruitRequest, true, true);
    ILoad breadLoad = new SimpleLoad("Bread", breadSupply, false, true);

    INodeDepot depot = new SimpleNodeDepot(depotId);
    depot.add(fruitLoad);
    depot.add(breadLoad);
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
   * Creates the depot of Truck: Fruit and Bread, which share a total capacity of 70.
   *
   * @return the resource depot
   */
  private static IResourceDepot createResourceDepot() {
    // Each good may fill all 70 units of the depot when it is alone on the truck; together they
    // share the 70. Truck starts with 5 Fruit and 10 Bread on board.
    ILoadCapacity fruitCapacity = new SimpleLoadCapacity("Fruit", 70, 5);
    ILoadCapacity breadCapacity = new SimpleLoadCapacity("Bread", 70, 10);

    IResourceDepot depot = new SimpleResourceDepot("truckDepot", 70);
    depot.add(fruitCapacity);
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
