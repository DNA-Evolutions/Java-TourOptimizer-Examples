package com.dna.jopt.touroptimizer.java.examples.expert.optionalnode;
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
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;
import static java.time.Month.MAY;

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
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Plans pickups with optional drop stops: three optional nodes take a fixed amount off the truck,
 * one takes everything.
 *
 * <p>Scenario: Jack from Aachen drives a truck that holds 30 units and starts empty. On May 7, 2020
 * (Europe/Berlin) he picks up 65 units at five nodes in Koeln, Oberhausen and Essen. The optional
 * node KoelnOptional unloads everything on board; OberhausenOptional, OberhausenOptional2 and the
 * far DresdenFarAwayOptional take 10 units each.
 *
 * <p>Look for: the unload-all stop (UnloadAll) and at least one drop stop with the load change
 * -10.0 in the route, because 65 units do not fit on the truck with one unload-all stop alone; no
 * capacity violation; DresdenFarAwayOptional is not visited.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/optionalnode/OptionalNode.md">OptionalNode.md</a>
 */
public class ReloadBaseWithUnloadAllExample extends Optimization {

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
    new ReloadBaseWithUnloadAllExample().example();
  }

  @Override
  public String toString() {
    return "Plans pickups with optional drop stops: three optional nodes take a fixed amount off"
        + " the truck, one takes everything.";
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
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds five pickup nodes, three optional nodes that take 10 units each and one optional node
   * that takes everything on board.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes, the drop stops too.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // In the array load model, the load of a node is taken off the load of the truck: a negative
    // value is a pickup, a positive value a drop.
    double[] loadPerNode = { -10 }; // We pick up 10 units
    double[] loadPerNodeHigh = { -15 }; // We pick up 15 units
    double[] unloadPerOptionalNode = { 10 }; // We drop 10 units

    // Only the four nodes with "Optional" in their id are optional. KoelnOptional has no load of
    // its own: setUnloadAll(true) lets the truck drop everything it carries there.
    INode koelnOptional =
        new TimeWindowGeoNode(
            "KoelnOptional", 50.9333, 6.95, openingHours, visitDuration, importance);
    koelnOptional.setIsOptional(true);
    koelnOptional.setUnloadAll(true);
    opti.addElement(koelnOptional);

    INode koeln2 =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln2.setLoad(loadPerNodeHigh);
    opti.addElement(koeln2);

    INode koeln3 =
        new TimeWindowGeoNode("Koeln3", 50.9333, 6.95, openingHours, visitDuration, importance);
    koeln3.setLoad(loadPerNodeHigh);
    opti.addElement(koeln3);

    INode oberhausenOptional =
        new TimeWindowGeoNode(
            "OberhausenOptional", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausenOptional.setIsOptional(true);
    oberhausenOptional.setLoad(unloadPerOptionalNode);
    opti.addElement(oberhausenOptional);

    INode oberhausenOptional2 =
        new TimeWindowGeoNode(
            "OberhausenOptional2", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausenOptional2.setIsOptional(true);
    oberhausenOptional2.setLoad(unloadPerOptionalNode);
    opti.addElement(oberhausenOptional2);

    INode oberhausen2 =
        new TimeWindowGeoNode(
            "Oberhausen2", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausen2.setLoad(loadPerNode);
    opti.addElement(oberhausen2);

    INode oberhausen3 =
        new TimeWindowGeoNode(
            "Oberhausen3", 51.4667, 6.85, openingHours, visitDuration, importance);
    oberhausen3.setLoad(loadPerNode);
    opti.addElement(oberhausen3);

    // 15 units at Essen make the pickups 65 in all: more than the truck holds with one
    // unload-all stop (30 before it, 30 after it), so one drop stop of 10 is needed as well.
    INode essen1 =
        new TimeWindowGeoNode("Essen1", 51.45, 7.01667, openingHours, visitDuration, importance);
    essen1.setLoad(loadPerNodeHigh);
    opti.addElement(essen1);

    // Far away from every pickup: the optimizer has no reason to drive there.
    INode dresdenOptionalFarFarAway =
        new TimeWindowGeoNode(
            "DresdenFarAwayOptional", 51.05347, 13.74316, openingHours, visitDuration, importance);
    dresdenOptionalFarFarAway.setIsOptional(true);
    dresdenOptionalFarFarAway.setLoad(unloadPerOptionalNode);
    opti.addElement(dresdenOptionalFarFarAway);
  }

  /**
   * Adds Jack with a truck that holds 30 units and starts empty.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 10 h and 1200 km per day, more than the route of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    // 30 units fit on the truck, so it is full after two pickups of 15 or three of 10.
    jack.setCapacity(new double[]{ 30 });
    jack.setInitialLoad(new double[]{ 0 });
    opti.addElement(jack);
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
