package com.dna.jopt.touroptimizer.java.examples.advanced.waitonearlyarrival;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2020 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'src/main/resources/LICENSE.txt',
 * which is part of this repository.
 *
 * If not, see <https://www.dna-evolutions.com/>.
 * #L%
 */
import static java.time.Month.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * Example WaitOnEarlyArrivalFirstNodeExample. If any of the defined Nodes of this example is the
 * first Node of the Route, the Resource needs to wait at the Node and introduce idle time until the start of the
 * WorkingHours. If the Node is NOT the first Node, the Resource is allowed to start working right away.
 *
 * @author jrich
 * @version Mar 23, 2021
 * @since Dec 23, 2020
 */
public class WaitOnEarlyArrivalFirstNodeExample extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new WaitOnEarlyArrivalFirstNodeExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example on how to define a desired first/last node.";
  }

  /**
   * Example.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public void example()
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Set the Properties
    this.setProperties();

    this.addNodes();
    this.addResources();

    WaitOnEarlyArrivalFirstNodeExample.attachToObservables(this);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise the optimization will be terminated
    System.out.println(resultFuture.get());
  }

  /** Sets the properties. */
  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  /** Adds the resources. */
  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);
  }

  /** Adds the nodes. */
  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHoursEarly = new ArrayList<>();
    weeklyOpeningHoursEarly.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    List<IOpeningHours> weeklyOpeningHoursLate = new ArrayList<>();
    weeklyOpeningHoursLate.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 14, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 19, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    /*
     *
     * If any of the defined Nodes is the first Node of the Route, the Resource needs to wait at
     * the Node until the start of the WorkingHours. If the Node is NOT the first Node, it is allowed
     * to start working right away.
     *
     */

    // Define some Nodes
    // By setting setWaitOnEarlyArrival(false) Resources are generally allowed to start working at a Node as soon as
    // they arrive. Only in the case of the first Node of a Route do they need to introduce some idle time if they
    // are too early since we also set setWaitOnEarlyArrivalFirstNode(true).
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHoursEarly, visitDuration, 1);
    koeln.setWaitOnEarlyArrival(false);
    koeln.setWaitOnEarlyArrivalFirstNode(true);
    this.addElement(koeln);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHoursEarly, visitDuration, 1);
    essen.setWaitOnEarlyArrival(false);
    essen.setWaitOnEarlyArrivalFirstNode(true);
    this.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHoursEarly, visitDuration, 1);
    dueren.setWaitOnEarlyArrival(false);
    dueren.setWaitOnEarlyArrivalFirstNode(true);
    this.addElement(dueren);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, weeklyOpeningHoursEarly, visitDuration, 1);
    aachen.setWaitOnEarlyArrival(false);
    aachen.setWaitOnEarlyArrivalFirstNode(true);
    this.addElement(aachen);

    /*
     * Wuppertal is open from 14-19. We will reach this Node too early. However, as this will not become
     * the first node, we will be allowed to start working right away.
     */
    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, weeklyOpeningHoursLate, visitDuration, 1);
    wuppertal.setWaitOnEarlyArrival(false);
    wuppertal.setWaitOnEarlyArrivalFirstNode(true);
    this.addElement(wuppertal);
  }

  /**
   * Attach to observables.
   *
   * @param opti the opti
   */
  private static void attachToObservables(IOptimization opti) {

    PrintStream out = System.out;

    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> out.println(p.getProgressString()));

    opti.getOptimizationEvents().warningSubject().subscribe(w -> out.println(w.toString()));

    opti.getOptimizationEvents().statusSubject().subscribe(s -> out.println(s.toString()));

    opti.getOptimizationEvents().errorSubject().subscribe(e -> out.println(e.toString()));
  }
}
