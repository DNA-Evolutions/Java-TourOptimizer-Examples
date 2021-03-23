package com.dna.jopt.touroptimizer.java.examples.advanced.visitdurationefficiency;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import tec.units.ri.quantity.Quantities;

/**
 * In a real-life scenario, it is common that Resources have a different performance that influences
 * their time taken for the same job. Therefore we introduced the resource-efficiency-factor.
 * Assuming the originally defined duration of a job at a Node is 30 minutes, an efficiency-factor of 0.5
 * means a Resource can finish the job in 50% of the time (15 minutes). However, sometimes it is not
 * possible to modify the duration, for example when defining a break) or when a minimum duration is necessary at a
 * job.
 *
 * <p>In this example, we look at two Resources, "Jack" and "Clara". They need to visit four Nodes with a default visit
 * duration of 30 minutes. However, two Nodes have a minimum visit duration of 20 minutes and for one of the Nodes,
 * "Essen - fixed duration", the duration cannot be modified. "Clara" is more experienced than Jack, and therefore only
 * needs about 50% time for the same Job. "Clara" show her true potential of 15 Minutes per job only at one Node, since
 * only "Dueren - unlimited" does not have a minimum visitDuration.
 *
 * <p>We expect that Clara gets all the jobs assigned.
 *
 * @author jrich
 * @version Mar 23, 2021
 * @since Dec 11, 2020
 */
public class ResourceVisitDurationEfficiencyExample extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws TimeoutException the timeout exception
   */
  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException,
          TimeoutException {
    new ResourceVisitDurationEfficiencyExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Resource-Efficiency-Factor for a resource.";
  }

  /**
   * Example.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws TimeoutException the timeout exception
   */
  public void example()
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException,
          TimeoutException {

    // Setting the Properties
    this.setProperties();

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // Subscribe to events
    subscribeToEvents(this);

    // It is important to block the call, otherwise the optimization will be terminated
    resultFuture.get(1, TimeUnit.MINUTES);
  }

  /** Sets the properties. */
  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  /** Adds the Resources. */
  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource resJack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    // "Clara" gets an efficiency factor of 0.5 and subsequently needs only half the time for a Job
    // in comparison to "Jack"
    CapacityResource resClara =
        new CapacityResource(
            "Clara", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    resClara.setOverallVisitDurationEfficiencyFactor(0.5); // Default 1.0

    this.addElement(resClara);
    this.addElement(resJack);
  }

  /** Adds the Nodes. */
  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // The default visit duration (equals 100%)
    Duration visitDuration = Duration.ofMinutes(30);

    // The minimum visit duration
    Duration minVisitDuration = Duration.ofMinutes(20);

    // Define some Nodes
    // "Koeln" and "Oberhausen" allow the visitDurationEfficiency to come into play, but limit "Claras" skills by
    // setting a minimum visit duration
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln.setHasRouteDependentVisitDuration(true);
    koeln.setMinimalVisitDuration(minVisitDuration);
    this.addElement(koeln);

    TimeWindowGeoNode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen.setHasRouteDependentVisitDuration(true);
    oberhausen.setMinimalVisitDuration(minVisitDuration);
    this.addElement(oberhausen);

    // "Essen" has the visitDurationEfficiency disabled
    TimeWindowGeoNode essen =
        new TimeWindowGeoNode(
            "Essen - fixed duration", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen.setHasRouteDependentVisitDuration(false); // We do not allow a modification
    this.addElement(essen);

    // "Dueren - unlimited" is the only Node that is not limited by a minimum visitDuration or a fixed visitDuration.
    // We expect "Clara" to be able to do this job in 15 minutes.
    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren - unlimited", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    dueren.setHasRouteDependentVisitDuration(true);
    this.addElement(dueren);
  }

  /**
   * Subscribe to events.
   *
   * @param opti the opti
   */
  private static void subscribeToEvents(IOptimization opti) {

    PrintStream myOut = System.out;

    // Subscribe to events
    opti.getOptimizationEvents()
        .progressSubject()
        .subscribe(p -> myOut.println(p.getProgressString()));

    opti.getOptimizationEvents()
        .errorSubject()
        .subscribe(e -> myOut.println(e.getCause() + " " + e.getCode()));

    opti.getOptimizationEvents()
        .warningSubject()
        .subscribe(w -> myOut.println(w.getDescription() + w.getCode()));

    opti.getOptimizationEvents()
        .statusSubject()
        .subscribe(s -> myOut.println(s.getDescription() + " " + s.getCode()));

    opti.getOptimizationEvents().resultFuture().thenAccept(myOut::println);
  }
}
