package com.dna.jopt.touroptimizer.java.examples.advanced.autofilter;
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

import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.framework.stream.info.autofilter.NodeFilteringEvent;
import com.dna.jopt.member.unit.filter.auto.filternodeconstraint.IAutoFilterNodeConstraint;
import com.dna.jopt.member.unit.filter.auto.filternodeconstraint.LateAutoFilterConstraint;
import com.dna.jopt.member.unit.filter.auto.reason.INodeFilterReason;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * In this example we are adding a local late Constraint to the Node "Nuernberg". In contrary to setting the
 * Autofilter for late violations for all Nodes, the AutoFilter will only observes "Nuernberg" in this example. If
 * the Resources will arrive late at "Nuernbergo" over several solutions above a certain treshold, the AutoFilter
 * will exclude "Nuernberg" from the optimization.
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 18, 2021
 */
public class AutoFilterParticularNodeExample extends Optimization {

  public static void main(String[] args) throws InvalidLicenceException, InterruptedException, ExecutionException, IOException {
    new AutoFilterParticularNodeExample().example();
  }
  
  public String toString() {
	  return "Simple example for using the selective autofilter for late violations only for a particular node.";
  }


  public void example() throws InvalidLicenceException, InterruptedException, ExecutionException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Set the Properties
    this.setProperties();

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise the optimization will be terminated
    resultFuture.get();
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");

    // Activate the filter mechanism
    props.setProperty("JOpt.SelectiveAutoFilter", "TRUE");

    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

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

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);
  }

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

    Duration visitDuration = Duration.ofMinutes(200);

    // Define some nodes
    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    this.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    this.addElement(nuernberg);
    

    // Assign a local Constraint to "Nuernberg"
    // By adding a local late Constraint to "Nuernberg" this Node and only this Node will be filtered out of the
    // Optimizer by the AutoFilter if it turns out to be responsible for too many late Violations.
    List<IAutoFilterNodeConstraint> nodeConstraints = new ArrayList<IAutoFilterNodeConstraint>();
    IAutoFilterNodeConstraint lateContraint = new LateAutoFilterConstraint();
    nodeConstraints.add(lateContraint);
    nuernberg.setAutoFilterConstraints(nodeConstraints);

    INode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    this.addElement(aachen);
  }

  @Override
  public void onError(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onStatus(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onWarning(int code, String message) {
    System.out.println("code: " + code + " message:" + message);
  }

  @Override
  public void onProgress(String winnerProgressString) {
    System.out.println(winnerProgressString);
  }

  @Override
  public void onNodeFiltering(int code, String message, List<INodeFilterReason> filterReasons) {
    // Nothing to do here
    System.out.println(filterReasons);
  }

  @Override
  public void onNodeFiltering(NodeFilteringEvent nodeFilteringEvent) {
    // Nothing to do here
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
