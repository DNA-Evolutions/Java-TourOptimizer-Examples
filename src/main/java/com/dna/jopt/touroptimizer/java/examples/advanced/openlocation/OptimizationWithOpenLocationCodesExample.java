package com.dna.jopt.touroptimizer.java.examples.advanced.openlocation;
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
import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.dna.jopt.member.unit.converter.openlocation.OpenLocation;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Builds and runs an optimization whose nodes and resources take their positions from Plus Codes
 * (Open Location Codes).
 *
 * <p>Scenario: seven nodes (Koeln, Oberhausen, Essen, Nuernberg, Heilbronn, Stuttgart and
 * Wuppertal) and two resources (JackKoeln and JackHeilbronn), each given as an id and a Plus Code.
 * The nodes are open and the resources work on May 6, 2020, from 08:00 to 22:00 (Europe/Berlin).
 *
 * <p>Look for: the result, with one route per resource; the routes are planned on the positions
 * decoded from the codes.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/OpenLocationCode.md">OpenLocationCode.md</a>
 */
public class OptimizationWithOpenLocationCodesExample extends Optimization {

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
    new OptimizationWithOpenLocationCodesExample().example();
  }

  @Override
  public String toString() {
    return "Builds and runs an optimization whose nodes and resources take their positions from"
        + " Plus Codes (Open Location Codes).";
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
   * Adds seven nodes; each takes its position from a Plus Code.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 10 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(10);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // The node ids and their Plus Codes, in the order the nodes are added.
    Map<String, String> nodeCodes = new LinkedHashMap<>();
    nodeCodes.put("Koeln", "9F28WXM2+82");
    nodeCodes.put("Oberhausen", "9F38FV82+M2");
    nodeCodes.put("Essen", "9F39F228+2M");
    nodeCodes.put("Nuernberg", "8FXHC3X9+48");
    nodeCodes.put("Heilbronn", "8FXF46RC+42");
    nodeCodes.put("Stuttgart", "8FWFQ58M+M8");
    nodeCodes.put("Wuppertal", "9F39758M+M8");

    nodeCodes.forEach(
        (id, code) -> {
          INode node =
              new TimeWindowGeoNode(
                  id, OpenLocation.of(code).toPos(), openingHours, visitDuration, importance);
          opti.addElement(node);
        });
  }

  /**
   * Adds two resources; each takes its start position from a Plus Code.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // 12 h per day, more than any day of this plan needs.
    Duration maxWorkingTime = Duration.ofHours(12);
    // 2200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    // The resource ids and the Plus Codes of their start positions.
    Map<String, String> resourceCodes = new LinkedHashMap<>();
    resourceCodes.put("JackKoeln", "9F28WXM2+82");
    resourceCodes.put("JackHeilbronn", "8FXF46RC+42");

    resourceCodes.forEach(
        (id, code) -> {
          IResource resource =
              new CapacityResource(
                  id,
                  OpenLocation.of(code).toPos(),
                  maxWorkingTime,
                  maxDistanceKmW,
                  createWorkingHours());
          opti.addElement(resource);
        });
  }

  /**
   * Creates the working hours of one resource: May 6, 2020, from 08:00 to 22:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
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
