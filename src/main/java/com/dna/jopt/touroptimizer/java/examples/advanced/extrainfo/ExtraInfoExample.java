package com.dna.jopt.touroptimizer.java.examples.advanced.extrainfo;
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
import java.time.Instant;
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

import com.dna.jopt.config.serialize.ConfigSerialization;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Stores a Java object as a JSON string in the extra info of nodes and a resource, and reads it
 * back from the result.
 *
 * <p>Scenario: Jack from Aachen visits Koeln and Essen on May 6, 2020, from 08:00 to 17:00
 * (Europe/Berlin). Each node carries a NodeExtraInfo with a phone number and a contact person, and
 * Jack a ResourceExtraInfo with a phone number and a birthday. The phone numbers and the birthday
 * are placeholders.
 *
 * <p>Look for: the block "==== EXTRA INFO =====" after the result, with the objects read back from
 * the extra info of Koeln, Essen and Jack from Aachen.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/extrainfo/ExtraInfo.md">ExtraInfo.md</a>
 */
public class ExtraInfoExample extends Optimization {

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
    new ExtraInfoExample().example();
  }

  @Override
  public String toString() {
    return "Stores a Java object as a JSON string in the extra info of nodes and a resource, and"
        + " reads it back from the result.";
  }

  /**
   * Builds the optimization, runs it, prints the result and then the extra info read back from it.
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
    IOptimizationResult result = startAndPresentResult(this);
    printExtraInfo(result);
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
   * Adds Koeln and Essen, each with a NodeExtraInfo as a JSON string in its extra info.
   *
   * @param opti the optimization
   * @throws JsonProcessingException if an extra info object cannot be written as JSON
   */
  private static void addNodes(IOptimization opti) throws JsonProcessingException {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // The phone numbers are placeholders, not real data: +00 is no country code.
    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    String extraInfoKoeln =
        ConfigSerialization.objectMapper()
            .writeValueAsString(new NodeExtraInfo("+00 000 0000001", "Mrs. Einstein"));
    koeln.setExtraInfo(extraInfoKoeln);
    opti.addElement(koeln);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    String extraInfoEssen =
        ConfigSerialization.objectMapper()
            .writeValueAsString(new NodeExtraInfo("+00 000 0000002", "Mrs. Kepler"));
    essen.setExtraInfo(extraInfoEssen);
    opti.addElement(essen);
  }

  /**
   * Adds Jack, who starts in Aachen, with a ResourceExtraInfo as a JSON string in his extra info.
   *
   * @param opti the optimization
   * @throws JsonProcessingException if an extra info object cannot be written as JSON
   */
  private static void addResources(IOptimization opti) throws JsonProcessingException {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 9 h per day, the length of the shift.
    Duration maxWorkingTime = Duration.ofHours(9);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);

    // Placeholders, not real data: +00 is no country code, and the date stands for any birthday.
    String extraInfoJack =
        ConfigSerialization.objectMapper()
            .writeValueAsString(
                new ResourceExtraInfo("+00 000 0000003", Instant.parse("1900-01-01T00:00:00Z")));
    jack.setExtraInfo(extraInfoJack);
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
   * Starts the run, waits for the result, prints it and returns it.
   *
   * @param opti the optimization
   * @return the result of the run
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static IOptimizationResult startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
    return result;
  }

  /**
   * Reads the extra info of the nodes and resources on the routes of the result back into
   * NodeExtraInfo and ResourceExtraInfo objects and prints them.
   *
   * @param result the result of the run
   * @throws JsonProcessingException if an extra info string cannot be read as JSON
   */
  private static void printExtraInfo(IOptimizationResult result) throws JsonProcessingException {
    // The elements come from the routes: a node that is on no route is not read back here.
    List<IOptimizationElement> elements =
        result.getRoutes().stream()
            .map(ILogicEntityRoute::getAllElements)
            .flatMap(List::stream)
            .distinct()
            .toList();

    System.out.println("\n==== EXTRA INFO =====");

    for (IOptimizationElement element : elements) {
      Optional<String> extraInfo = element.getExtraInfo();
      if (element instanceof INode && extraInfo.isPresent()) {
        NodeExtraInfo info =
            ConfigSerialization.objectMapper().readValue(extraInfo.get(), NodeExtraInfo.class);
        System.out.println("\n" + element.getId());
        System.out.println(info);
      }
    }

    System.out.println("========");

    for (IOptimizationElement element : elements) {
      Optional<String> extraInfo = element.getExtraInfo();
      if (element instanceof IResource && extraInfo.isPresent()) {
        ResourceExtraInfo info =
            ConfigSerialization.objectMapper().readValue(extraInfo.get(), ResourceExtraInfo.class);
        System.out.println("\n" + element.getId());
        System.out.println(info);
      }
    }
  }
}
