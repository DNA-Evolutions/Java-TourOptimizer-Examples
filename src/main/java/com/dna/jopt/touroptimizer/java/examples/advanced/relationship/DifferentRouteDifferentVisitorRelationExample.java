package com.dna.jopt.touroptimizer.java.examples.advanced.relationship;

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
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

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
import com.dna.jopt.member.unit.relation.node2node.visitor.INode2NodeVisitorRelation;
import com.dna.jopt.member.unit.relation.node2node.visitor.RelativeVisitor2RelatedNodeRelation;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Example DifferentRouteDifferentVisitorRelationExample. In this example two Nodes (Aachen and
 * Dueren) should be visited in a different Route. Further, we want these Routes to be served by
 * different Resources (visitors).
 *
 * @author jrich
 * @version Mar 11, 2021
 * @since Dec 23, 2020
 */
public class DifferentRouteDifferentVisitorRelationExample extends Optimization {

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
    new DifferentRouteDifferentVisitorRelationExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Visiting two Nodes in different Routes served by different Resource using Relations.";
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

    // Set Properties
    this.setProperties();

    this.addNodes();
    this.addResources();

    DifferentRouteDifferentVisitorRelationExample.attachToObservables(this);

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

    // Related properties
    props.setProperty("JOptWeight.Relationships", "100.0"); // Default is 100.0

    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  private static List<IWorkingHours> getDefaultWorkingHours() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHours;
  }

  /** Adds the resources. */
  private void addResources() {

    Duration maxWorkingTime = Duration.ofHours(10);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);

    CapacityResource rep2 =
        new CapacityResource(
            "John", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());
    rep2.setCost(0, 1, 1);
    this.addElement(rep2);
  }

  /** Adds the Nodes. */
  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(150);

    // Without defining the relations, Aachen and Dueren would cluster together in the same Route

    // Define some Nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren);

    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen);

    // Create Relation
    INode2NodeVisitorRelation rel = new RelativeVisitor2RelatedNodeRelation();
    rel.setMasterNode(dueren);
    rel.setRelatedNode(aachen);

    // We want that Aachen and Dueren are visited in different Routes by a different Resource
    boolean isForcedDifferentVisitor = true;

    rel.setIsForcedDifferentRoute(isForcedDifferentVisitor);

    dueren.addNode2NodeRelation(rel);
    aachen.addNode2NodeRelation(rel);
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
