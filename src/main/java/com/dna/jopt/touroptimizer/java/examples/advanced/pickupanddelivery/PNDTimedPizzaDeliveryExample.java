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
import java.util.stream.Collectors;

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
import com.dna.jopt.member.unit.pnd.capacity.simple.SimpleLoadCapacity;
import com.dna.jopt.member.unit.pnd.depot.node.INodeDepot;
import com.dna.jopt.member.unit.pnd.depot.node.simple.SimpleNodeDepot;
import com.dna.jopt.member.unit.pnd.depot.resource.IResourceDepot;
import com.dna.jopt.member.unit.pnd.depot.resource.simple.SimpleResourceDepot;
import com.dna.jopt.member.unit.pnd.load.ILoad;
import com.dna.jopt.member.unit.pnd.load.flexload.TimedSupplyFlexLoad;
import com.dna.jopt.member.unit.pnd.load.timed.TimedLoad;
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
 * In this example we have two Pizza restaurants of the same chain. One is situated in Cologne and
 * the other one in Essen. Each restaurant has one delivery man. On every customer order, the
 * restaurants can decide which restaurant is serving the request.
 *
 * <p>The restaurant chain advertises with: "If you don't get your Pizza in 90 minutes, you get if
 * for free!". Therefore, it is desirable that each Pizza is delivered within 90 minutes.
 *
 * <p>Cologne has the following employee: "JackCologne"
 *
 * <p>Essen has the following employee: "JohnEssen"
 *
 * <p>The goal is to deliver each Pizza within 90 minutes. Further, SupplyFlexLoads will be used to
 * automatically decide which restaurant will prepare which pizza.
 * 
 * <p> Note: The example is designed to, most likely, show a violation of PizzaMare's transportation 
 * job with an overtime value of 9.38 minutes.
 *
 * @author Jens Richter
 * @version Jul 27, 2020
 * @since Jul 27, 2020
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDTimedPizzaDeliveryExample extends Optimization {

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
    new PNDTimedPizzaDeliveryExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example of the use of SupplyFlexLoads and TimeLoads for planning the manufacturing of and delivery of pizza.";
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
    this.addNodesAndRess();

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
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");

    // We have to tell the optimizer that we have an high interest in capacity planning, Default is
    // 100
    props.setProperty("JOptWeight.Capacity", "200");
    this.addElement(props);
  }

  /** Adds the nodes. */
  public void addNodesAndRess() {

    List<String> requiredPizzas = this.createAndAddDummyOrders();
    this.addRes(requiredPizzas);

    // Also create the Supply nodes
  }

  public List<String> createAndAddDummyOrders() {

    List<String> requiredPizzas = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(10);

    /*
     *
     */

    INode job1 =
        new TimeWindowGeoNode("MaxDueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    job1.setNodeDepot(this.createNodeDepot("MaxDuerenDepot", "PizzaFunghi"));
    requiredPizzas.add("PizzaFunghi");
    this.addElement(job1);

    INode job2 =
        new TimeWindowGeoNode(
            "AmyGelsenkirchen", 51.54398, 7.10001, weeklyOpeningHours, visitDuration, 1);
    job2.setNodeDepot(this.createNodeDepot("AmyGelsenkirchenDepot", "PizzaRegina"));
    requiredPizzas.add("PizzaRegina");
    this.addElement(job2);

    //    INode job3 =
    //        new TimeWindowGeoNode(
    //            "PhilDueren", 51.54398, 7.10001, weeklyOpeningHours, visitDuration, 1);
    //    job3.setNodeDepot(this.createNodeDepot("PhilDuerenDepot", "PizzaTono"));
    //    requiredPizzas.add("PizzaRegina");
    //    this.addElement(job3);

    INode job4 =
        new TimeWindowGeoNode(
            "TomDuesseldorf", 51.22201, 6.78677, weeklyOpeningHours, visitDuration, 1);
    job4.setNodeDepot(this.createNodeDepot("TomDuesseldorfDepot", "PizzaMargarita"));
    requiredPizzas.add("PizzaMargarita");
    this.addElement(job4);

    INode job5 =
        new TimeWindowGeoNode(
            "SophiaDuisburg", 51.43468, 6.76507, weeklyOpeningHours, visitDuration, 1);
    job5.setNodeDepot(this.createNodeDepot("SophiaDuisburgDepot", "PizzaMexican"));
    requiredPizzas.add("PizzaMexican");
    this.addElement(job5);

    INode job6 =
        new TimeWindowGeoNode(
            "IsabellaBochum", 51.48379, 7.21629, weeklyOpeningHours, visitDuration, 1);
    job6.setNodeDepot(this.createNodeDepot("IsabellaBochumDepot", "PizzaPeperoni"));
    requiredPizzas.add("PizzaPeperoni");
    this.addElement(job6);

    INode job8 =
        new TimeWindowGeoNode(
            "JacobWuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    job8.setNodeDepot(this.createNodeDepot("JacobWuppertalDepot", "PizzaMare"));
    requiredPizzas.add("PizzaMare");
    this.addElement(job8);

    INode supplyEssen =
        new TimeWindowGeoNode("SupplyEssen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    supplyEssen.setNodeDepot(this.createSupplyFlexDepot("SupplyEssenDepot", requiredPizzas));
    this.addElement(supplyEssen);

    INode supplyCologne =
        new TimeWindowGeoNode("SupplyCologne", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    supplyCologne.setNodeDepot(this.createSupplyFlexDepot("SupplyCologneDepot", requiredPizzas));
    this.addElement(supplyCologne);

    /*
     *
     */
    return requiredPizzas;
  }

  public INodeDepot createNodeDepot(String depotId, String pizzaId) {

    ILoad pizzaLoad = new TimedLoad(pizzaId, Duration.ofMinutes(90), true);

    INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);

    customerNodeDepot.add(pizzaLoad);

    return customerNodeDepot;
  }

  public INodeDepot createSupplyFlexDepot(String depotId, List<String> requiredPizzas) {

    List<TimedSupplyFlexLoad> loads =
        requiredPizzas.stream().map(r -> new TimedSupplyFlexLoad(r)).collect(Collectors.toList());

    // Adding the capacities to our depot
    INodeDepot depot = new SimpleNodeDepot(depotId);

    loads.forEach(depot::add);

    return depot;
  }

  public void addRes(List<String> requiredPizzas) {

    /*
     * Setting the resource JackTruck
     */

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MARCH.getValue(), 11, 23, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2800.0, KILO(METRE));

    /*
     *
     *
     */

    IResource jack =
        new CapacityResource(
            "JackCologne", 50.9333, 6.95, maxWorkingTime, maxDistanceKmW, workingHours);
    jack.setResourceDepot(this.createResourceDepot("JackCologneDepot", requiredPizzas));

    //    IResource peter =
    //        new CapacityResource(
    //            "PeterCologne", 50.9333, 6.95, maxWorkingTime, maxDistanceKmW, workingHours);
    //    peter.setResourceDepot(this.createResourceDepot("PeterCologneDepot", requiredPizzas));

    // Adding the truck to our optimization
    this.addElement(jack);
    // this.addElement(peter);

    /*
     *
     *
     */
    IResource john =
        new CapacityResource(
            "JohnEssen", 51.45, 7.01667, maxWorkingTime, maxDistanceKmW, workingHours);
    john.setResourceDepot(this.createResourceDepot("JohnCologneDepot", requiredPizzas));

    this.addElement(john);
  }

  /**
   * Creates the resource depot.
   *
   * @return the resource depot
   */
  public IResourceDepot createResourceDepot(String depotId, List<String> requiredPizzas) {

    List<SimpleLoadCapacity> caps =
        requiredPizzas
            .stream()
            .map(r -> new SimpleLoadCapacity(r, 1, 0))
            .collect(Collectors.toList());

    // Adding the capacities to our depot
    IResourceDepot depot = new SimpleResourceDepot(depotId, requiredPizzas.size());

    caps.forEach(depot::add);

    return depot;
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
