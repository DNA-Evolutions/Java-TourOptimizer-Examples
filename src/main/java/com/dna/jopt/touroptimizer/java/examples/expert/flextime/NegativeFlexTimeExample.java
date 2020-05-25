package com.dna.jopt.touroptimizer.java.examples.expert.flextime;
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

import static java.time.Month.MAY;

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
 * Simple example of negative flextime.
 */
public class NegativeFlexTimeExample extends Optimization {

  public static void main(String[] args) throws InvalidLicenceException, InterruptedException, ExecutionException, IOException {
    new NegativeFlexTimeExample().example();
  }
  
  public String toString() {
	  return "Simple example of negative flextime.";
  }

  public void example() throws InvalidLicenceException, InterruptedException, ExecutionException, IOException {

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

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    this.addElement(props);
  }

  private void addResources() {

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 16, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    
    // We allow to start up to 2 hours earlier. The true boolean is indicating, that the
    // time is only allowed to be used for driving to a node (not working on it).
    rep1.setMaxRouteStartReductionTime(Duration.ofHours(2),true);
    
    // The earlier start should be part of the total working time
    rep1.setReductionTimeIsIncludedInWorkingTime(true);
    
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

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    this.addElement(koeln);

    TimeWindowGeoNode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    this.addElement(oberhausen);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    this.addElement(essen);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(dueren);

    TimeWindowGeoNode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    this.addElement(nuernberg);

    TimeWindowGeoNode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    this.addElement(heilbronn);

    TimeWindowGeoNode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(stuttgart);

    TimeWindowGeoNode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    this.addElement(wuppertal);

    TimeWindowGeoNode aachen =
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
