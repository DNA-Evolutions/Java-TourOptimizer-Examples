package com.dna.jopt.touroptimizer.java.examples.advanced.clustering;
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
import static java.util.Calendar.MAY;
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.types.Position;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;

import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.google.common.collect.ImmutableList;

import tec.units.ri.quantity.Quantities;

/**
 * ATTENTION: This example contains more than 10 elements therefore a valid license is required.
 *
 * <p>Example of clustering construction. Multiple nodes within the City Cologne needs to be
 * visited. Resources are also spread around Cologne. The Nodes and Resources are positioned in a
 * Phyllotaxis pattern. The task is to create a solution purely based on clustering construction
 * without using optimization algorithms.
 *
 * @author jrich
 * @version Mar 26, 2021
 * @since Mar 26, 2021
 */
public class ClusteringInnerCityExample extends Optimization {

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
    new ClusteringInnerCityExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example of clustering construction - InnerCity.";
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

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    // Attach to observables
    ClusteringInnerCityExample.attachToObservables(this);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    IOptimizationResult result = resultFuture.get();

    // Show result
    System.out.println(result);

    // Export to kml
    try {
      String jsonFile = "" + this.getClass().getSimpleName() + ".kml";

      IEntityExporter exporter = new EntityKMLExporter();
      exporter.export(result.getContainer(), new FileOutputStream(jsonFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /** Sets the properties. */
  private void setProperties() {

    Properties props = new Properties();

    // Set Algorithms to zero iterations => Pure Construction
    props.setProperty("JOptExitCondition.JOptGenerationCount", "0");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "0");

    this.addElement(props);
  }

  /** Adds the resources. */
  private void addResources() {

    getResources().stream().forEach(this::addElement);
  }

  public static List<IWorkingHours> getWorkingHours() {

    List<IWorkingHours> workingHoursOne = new ArrayList<>();
    workingHoursOne.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHoursOne;
  }

  public List<IResource> getResources() {

    // 1.) add the nodes to be visited
    Position koelnCenterPos = Position.of(50.9333, 6.95);

    // Create other positions
    // 1/10 coordinate difference shifts by roughly 12km = 10min driving

    double spacing = 0.004;
    List<Position> poss = samplePhyllotaxis(koelnCenterPos, 5, spacing);

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    // Create the Resources and return
    return IntStream.range(0, poss.size())
        .mapToObj(
            ii -> {
              List<IWorkingHours> workingHoursOne = new ArrayList<>();
              workingHoursOne.add(
                  new WorkingHours(
                      ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                      ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

              IResource rep =
                  new CapacityResource(
                      "R_" + ii,
                      poss.get(ii).latitude(),
                      poss.get(ii).longitude(),
                      maxWorkingTime,
                      maxDistanceKmW,
                      getWorkingHours());
              rep.setCost(0, 1, 1);

              return rep;
            })
        .collect(Collectors.toList());
  }

  /** Adds the nodes. */
  private void addNodes() {

    getNodes().stream().forEach(this::addElement);
  }

  public static List<INode> getNodes() {

    Duration visitDuration = Duration.ofMinutes(20);

    // 1.) add the nodes to be visited
    Position koelnCenterPos = Position.of(50.9333, 6.95);

    // Create other positions
    // 1/10 coordinate difference shifts by roughly 12km = 10min driving

    double spacing = 0.002;
    List<Position> poss = samplePhyllotaxis(koelnCenterPos, 100, spacing);

    // Create the nodes and return
    return IntStream.range(0, poss.size())
        .mapToObj(
            ii -> {
              List<IOpeningHours> weeklyOpeningHoursOne = new ArrayList<>();

              weeklyOpeningHoursOne.add(
                  new OpeningHours(
                      ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                      ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

              return new TimeWindowGeoNode(
                  "N_" + ii,
                  poss.get(ii).latitude(),
                  poss.get(ii).longitude(),
                  weeklyOpeningHoursOne,
                  visitDuration,
                  1);
            })
        .collect(Collectors.toList());
  }

  public static List<Position> samplePhyllotaxis(Position centerPos, int n, double spacing) {

    return samplePhyllotaxis(centerPos.latitude(), centerPos.longitude(), n, spacing);
  }

  public static List<Position> samplePhyllotaxis(
      double centerLat, double centerLon, int n, double spacing) {
    double theta = Math.PI * (3 - Math.sqrt(5));

    return IntStream.range(0, n)
        .mapToObj(
            i -> {
              double radius = spacing * Math.sqrt(i);
              double angle = i * theta;
              double x = radius * Math.cos(angle);
              double y = radius * Math.sin(angle);

              return Position.of(centerLat + y, centerLon + x);
            })
        .collect(ImmutableList.toImmutableList());
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
