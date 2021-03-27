package com.dna.jopt.touroptimizer.java.examples.advanced.jointvisitduration;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * In the case where Nodes share the same geographical location the visitDuration for each Node may become
 * different. Therefore, a joint visitiDuration can be defined. In case Nodes (sharing the same
 * geoLocation) are visited directly after another only the first Node gets the full
 * visitDuration. All following Nodes get the joint visit duration. By default, the joint visit
 * duration equals the "normal" visit duration. In this example the joint visitDuration is shortened.
 *
 *
 * @author DNA
 * @version Mar 23, 2021
 * @since Mar 23, 2021
 */
public class JointVisitDurationExample extends Optimization {

  public static void main(String[] args) throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {
    new JointVisitDurationExample().example();
  }
  
  public String toString() {
	  return "In the case nodes share the same geographical location the visitDuration for each node may become different.\r\n" +
	      " Therefore, a joint visitDuration can be defined. In case nodes (sharing the same geoLocation) are visited\r\n" +
	      " directly after each other only the first node gets the full visitDuration. All following nodes get the\r\n" + 
	      " joined visit duration. By default, the joint visit duration equals the \"normal\" visit duration.";
  }

  public void example() throws IOException, InvalidLicenceException, InterruptedException, ExecutionException {

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
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<IWorkingHours>();
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

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    this.addElement(rep1);
  }

  private void addNodes() {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<IOpeningHours>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(30);
    Duration jointVisitDuration = Duration.ofMinutes(15);

    // Define some Nodes
    // We are setting the jointVisitDuration. This shortened visitDuration only comes into play when two Nodes
    // sharing the same geolocation (as defined by longitude and latitude) are visited back to back.
    TimeWindowGeoNode koeln1 =
        new TimeWindowGeoNode("Koeln1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln1.setJointVisitDuration(jointVisitDuration);
    this.addElement(koeln1);

    TimeWindowGeoNode koeln2 =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln2.setJointVisitDuration(jointVisitDuration);
    this.addElement(koeln2);

    TimeWindowGeoNode koeln3 =
        new TimeWindowGeoNode("Koeln3", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln3.setJointVisitDuration(jointVisitDuration);
    this.addElement(koeln3);

    TimeWindowGeoNode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen.setJointVisitDuration(jointVisitDuration);
    this.addElement(oberhausen);

    TimeWindowGeoNode essen1 =
        new TimeWindowGeoNode("Essen1", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen1.setJointVisitDuration(jointVisitDuration);
    this.addElement(essen1);

    TimeWindowGeoNode essen2 =
        new TimeWindowGeoNode("Essen2", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen2.setJointVisitDuration(jointVisitDuration);
    this.addElement(essen2);

    TimeWindowGeoNode essen3 =
        new TimeWindowGeoNode("Essen3", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen3.setJointVisitDuration(jointVisitDuration);
    this.addElement(essen3);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    dueren.setJointVisitDuration(jointVisitDuration);
    this.addElement(dueren);
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

    IEntityExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("" + this.getClass().getSimpleName());

    try {

      kmlExporter.export(
          rapoptResult.getContainer(),
          new FileOutputStream(new File("./" + this.getClass().getSimpleName() + ".kml")));

    } catch (FileNotFoundException e) {
      //
      e.printStackTrace();
    }
  }
}
