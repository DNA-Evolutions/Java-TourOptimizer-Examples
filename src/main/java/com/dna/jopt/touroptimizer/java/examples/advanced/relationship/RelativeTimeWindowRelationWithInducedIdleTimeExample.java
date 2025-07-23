package com.dna.jopt.touroptimizer.java.examples.advanced.relationship;
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
import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

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
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.resource.IConstraintResource;
import com.dna.jopt.member.unit.condition.resource.MandatoryResourceConstraint;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.relation.node2node.tempus.INode2NodeTempusRelation;
import com.dna.jopt.member.unit.relation.node2node.tempus.RelativeTimeWindow2RelatedNodeRelation;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * Connecting two Nodes by defining a relative timeWindow. Relative timeWindows are very useful when
 * defining tasks that have to be fulfilled consecutively. In this example the work orders take 60
 * minutes to fulfill. We ask the relatedNode to start work 40 minutes after the masterNode has
 * arrived, on purpose inducing 20 minutes of idle time for the relatedNode for the sake of this
 * demonstration. The function is very flexible. Separate variables would allow to set a minimal
 * time that has to pass between the tasks or setting a loose timeframe within which both tasks have
 * to be fulfilled.
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 11, 2021
 */
public class RelativeTimeWindowRelationWithInducedIdleTimeExample extends Optimization {

  public static void main(String[] args)
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new RelativeTimeWindowRelationWithInducedIdleTimeExample().example();
  }

  public String toString() {
    return "Connecting Nodes with each other by defining a relative timeWindow. A use case would be that a "
        + "certain\r\n"
        + " workOrder needs to be fulfilled before another one can start. In this example we "
        + "induced 20 minutes of idle time for the relatedNode on purpose.";
  }

  public void example()
      throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties();

    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise optimization will be terminated
    resultFuture.get();
  }

  private void setProperties() {

    Properties props = new Properties();

    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOptWeight.Relationships", "100");
    props.setProperty("JOpt.NumCPUCores", "4");
    props.setProperty("JOpt.UnlocatedIdleTime", "true");

    this.addElement(props);
  }

  private static List<IWorkingHours> getDefaultWorkingHours() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHours;
  }

  private void addResources() {

    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);

    IResource rep2 =
        new CapacityResource(
            "John", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, getDefaultWorkingHours());
    rep2.setCost(0, 1, 1);
    this.addElement(rep2);
  }

  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHoursAachen = new ArrayList<>();
    weeklyOpeningHoursAachen.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHoursAachen.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    List<IOpeningHours> weeklyOpeningHoursEssen = new ArrayList<>();
    weeklyOpeningHoursEssen.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 12, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHoursEssen.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 12, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(60);

    // Define some nodes

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode(
            "Essen", 50.775346, 6.083887, weeklyOpeningHoursEssen, visitDuration, 1);
    IConstraintResource johnMandConstraint = new MandatoryResourceConstraint();
    johnMandConstraint.addResource("John", 10);
    this.addElement(essen);

    TimeWindowGeoNode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, weeklyOpeningHoursAachen, visitDuration, 1);
    IConstraintResource jackMandConstraint = new MandatoryResourceConstraint();
    jackMandConstraint.addResource("Jack", 10);
    this.addElement(aachen);

    // Create a relative timeWindowRelation as delta based on master node
    // The relatedNode has to start it’s task 40 minutes after the arrival of the masterNode. This
    // results in 20
    // minutes idle time for the relatedNode, since the tasks take 60 minutes to finish
    INode2NodeTempusRelation rel =
        new RelativeTimeWindow2RelatedNodeRelation(Duration.ofMinutes(0), Duration.ofMinutes(40));
    rel.setMasterNode(essen);
    rel.setRelatedNode(aachen);
    rel.setTimeComparisonJuncture(true, true);
    essen.addNode2NodeRelation(rel);
    aachen.addNode2NodeRelation(rel);
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
    //

  }

  @Override
  public void onProgress(String winnerProgressString) {
    System.out.println(winnerProgressString);
  }

  @Override
  public void onProgress(IOptimizationProgress rapoptProgress) {
    //
  }

  @Override
  public void onAsynchronousOptimizationResult(IOptimizationResult rapoptResult) {
    System.out.println(rapoptResult);
  }
}
