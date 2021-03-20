package com.dna.jopt.touroptimizer.java.examples.advanced.condition;
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

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.IConstraint;
import com.dna.jopt.member.unit.condition.IQualification;
import com.dna.jopt.member.unit.condition.type.TypeConstraint;
import com.dna.jopt.member.unit.condition.type.TypeQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * In this example we are setting a type/skill for a Resource based on its workingDays. In this case, the
 * Qualifications of the Resource are added to the WorkingHours and not the Resource itself. The Constraints are
 * still set to the Nodes tough.
 *
 * @author DNA
 * @version 15/03/2021
 * @since 15/03/2021
 */
public class SelectiveTypeConditionExample extends Optimization {

  public static void main(String[] args) throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
    new SelectiveTypeConditionExample().example();
  }
  
  public String toString() {
	  return "Setting a type/skill for a resource based on its workingDays.";
  }

  public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Set Properties
    this.setProperties(this);

    this.addNodes(this);
    this.addResources(this);

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // It is important to block the call, otherwise the optimization will be terminated
    resultFuture.get();

  }

  private void setProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOptWeight.NodeType", "10.0"); // Default is 1.0
    props.setProperty("JOpt.NumCPUCores", "4");

    opti.addElement(props);
  }

  private void addResources(IOptimization opti) {

    List<IWorkingHours> workingHours = new ArrayList<>();
    
    IWorkingHours firstWoh = 
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    IWorkingHours secondWoh = 
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    
    IWorkingHours thirdWoh =
            new WorkingHours(
                ZonedDateTime.of(2020, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2020, MAY.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    
    IWorkingHours fourthWoh = 
            new WorkingHours(
                ZonedDateTime.of(2020, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2020, MAY.getValue(), 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    
    IWorkingHours fifthWoh = 
            new WorkingHours(
                ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2020, MAY.getValue(), 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    
    // Setting types
    IQualification typeQualification1 = new TypeQualification();
    ((TypeQualification) typeQualification1).addType("Cooling available");
    
    IQualification typeQualification2 = new TypeQualification();
    ((TypeQualification) typeQualification2).addType("Chain Saw available");
    
    IQualification typeQualification3 = new TypeQualification();
    ((TypeQualification) typeQualification3).addType("Loading ramp available");
    
    IQualification typeQualification4 = new TypeQualification();
    ((TypeQualification) typeQualification4).addType("Second person on truck available");
    
    IQualification typeQualification5 = new TypeQualification();
    ((TypeQualification) typeQualification5).addType("Pallet lift truck available");
    
    IQualification typeQualification6 = new TypeQualification();
    ((TypeQualification) typeQualification6).addType("Certification for dangerous goods");
    
    // First day
    firstWoh.addQualification(typeQualification6); // Expires on third day, since it will not be refreshed
    firstWoh.addQualification(typeQualification1);
    firstWoh.addQualification(typeQualification4);
    firstWoh.addQualification(typeQualification5);
    
    // Second day
    secondWoh.addQualification(typeQualification6); // Expires on third day since it will not be refreshed
    secondWoh.addQualification(typeQualification3);
    secondWoh.addQualification(typeQualification2);
    
    // Third day
    // typeQualification 6 and 1 are not refreshed and therefore are not available anymore
    thirdWoh.addQualification(typeQualification5);
    thirdWoh.addQualification(typeQualification4);

    // Fourth day
    fourthWoh.addQualification(typeQualification5);
    fourthWoh.addQualification(typeQualification4);
    fourthWoh.addQualification(typeQualification3);
    
    // Fifth day
    fifthWoh.addQualification(typeQualification1);
    fifthWoh.addQualification(typeQualification2);
    fifthWoh.addQualification(typeQualification3);
    fifthWoh.addQualification(typeQualification4);
    
    // Adding hours
    workingHours.add(firstWoh);
    workingHours.add(secondWoh);
    workingHours.add(thirdWoh);
    workingHours.add(fourthWoh);
    workingHours.add(fifthWoh);
    
    
    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    CapacityResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    opti.addElement(rep1);
  }

  private void addNodes(IOptimization opti) {

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    
    weeklyOpeningHours.add(
            new OpeningHours(
                ZonedDateTime.of(2020, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2020, MAY.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    
    weeklyOpeningHours.add(
            new OpeningHours(
                ZonedDateTime.of(2020, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2020, MAY.getValue(), 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
            new OpeningHours(
                ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2020, MAY.getValue(), 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    
    Duration visitDuration = Duration.ofMinutes(60);
    
    // Setting constraints
    IConstraint typeConstraint1 = new TypeConstraint();
    ((TypeConstraint) typeConstraint1).addType("Cooling available");
    typeConstraint1.setIsHard(true);
    
    IConstraint typeConstraint2 = new TypeConstraint();
    ((TypeConstraint) typeConstraint2).addType("Chain Saw available");
    typeConstraint2.setIsHard(true);
    
    IConstraint typeConstraint3 = new TypeConstraint();
    ((TypeConstraint) typeConstraint3).addType("Loading ramp available");
    typeConstraint3.setIsHard(true);
    

    IConstraint typeConstraint4 = new TypeConstraint();
    ((TypeConstraint) typeConstraint4).addType("Second person on truck available");
    typeConstraint4.setIsHard(true);
    
    IConstraint typeConstraint5 = new TypeConstraint();
    ((TypeConstraint) typeConstraint5).addType("Pallet lift truck available");
    typeConstraint5.setIsHard(true);
    
    IConstraint typeConstraint6 = new TypeConstraint();
    ((TypeConstraint) typeConstraint6).addType("Certification for dangerous goods");
    typeConstraint6.setIsHard(true);
    

    // Define some Nodes and adding the respective constraints
    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln.addConstraint(typeConstraint6);
    koeln.addConstraint(typeConstraint5);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen.addConstraint(typeConstraint4);
    oberhausen.addConstraint(typeConstraint1);
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen.addConstraint(typeConstraint6);
    essen.addConstraint(typeConstraint1);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    dueren.addConstraint(typeConstraint4);
    dueren.addConstraint(typeConstraint2);
    dueren.addConstraint(typeConstraint3);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    nuernberg.addConstraint(typeConstraint1);
    nuernberg.addConstraint(typeConstraint5);
    nuernberg.addConstraint(typeConstraint6);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    heilbronn.addConstraint(typeConstraint6);
    opti.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    stuttgart.addConstraint(typeConstraint4);
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    wuppertal.addConstraint(typeConstraint1);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    aachen.addConstraint(typeConstraint3);
    opti.addElement(aachen);
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
