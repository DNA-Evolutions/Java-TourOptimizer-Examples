package com.dna.jopt.touroptimizer.java.examples.expert.customsolution;
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
import com.dna.jopt.member.bucket.entity.Entity;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.bucket.route.LogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.IPillarNode;
import com.dna.jopt.member.unit.node.geo.PillarTimeWindowGeoNode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/** Setting a custom solution for starting the optimization. */
public class CustomSolutionWithAdditionalNodesAndResourcesExample extends Optimization {

  public static void main(String[] args)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
    new CustomSolutionWithAdditionalNodesAndResourcesExample().example();
  }

  public String toString() {
    return "Setting a custom solution for starting the optimization. Further, add"
        + "nodes will be added to the optimization in an optimized way. "
        + "THIS EXAMPLE CAN BE ONLY RUN WITH A VALID FULL LICENSE!";
  }

  public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties();

    // Create an empty entity
    IEntity initialEntity = this.createInitialSolution();

    // Add to Optimization - At this point
    this.setInitialEntity(initialEntity);
    
    // Add more resources
    this.addReassignResources(this.getAdditionalResources());

    // Add more nodes
    this.addReassignNodes(this.getAdditionalNodes());

    // Start
    this.startRunAsync().get();
  }

  private IEntity createInitialSolution() {

    // Create two empty routes
    ILogicEntityRoute firstRoute = new LogicEntityRoute();
    ILogicEntityRoute secondRoute = new LogicEntityRoute();

    // Create a resource
    IResource myRes = getResource(); // Holding two working days

    // Add nodes, Terminations etc. for the routes
    firstRoute.setCurrentVisitingResource(myRes, 0);
    firstRoute.setRouteStart(myRes);
    firstRoute.setRouteTermination(myRes);
    firstRoute.addAllToOptimizableElements(this.getNodeSet1());

    secondRoute.setCurrentVisitingResource(myRes, 1);
    secondRoute.setRouteStart(myRes);
    secondRoute.setRouteTermination(myRes);
    secondRoute.addAllToOptimizableElements(this.getNodeSet2());

    // Create entity
    IEntity entity = new Entity();
    entity.addRoute(firstRoute);
    entity.addRoute(secondRoute);

    return entity;
  }

  private void setProperties() {

    Properties props = new Properties();

    // Effectively skip Simulated and gentic phase and deactivate assisted construction
    props.setProperty("JOptExitCondition.JOptGenerationCount", "10000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");
    // Do not use construction
    props.setProperty("JOpt.Assisted", "FALSE");

    this.addElement(props);
  }

  private IResource getResource() {

    List<IWorkingHours> workingHours = new ArrayList<>();
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

    IResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    rep1.setCost(0, 1, 1);
    return rep1;
  }

  private List<IOptimizationElement> getNodeSet1() {
    List<IOptimizationElement> myNodes1 = new ArrayList<>();

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
    myNodes1.add(koeln);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    myNodes1.add(dueren);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    myNodes1.add(essen);

    return myNodes1;
  }

  private List<IOptimizationElement> getNodeSet2() {
    List<IOptimizationElement> myNodes2 = new ArrayList<>();

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
    TimeWindowGeoNode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    myNodes2.add(nuernberg);

    TimeWindowGeoNode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    myNodes2.add(heilbronn);

    TimeWindowGeoNode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    myNodes2.add(stuttgart);

    return myNodes2;
  }

  private List<INode> getAdditionalNodes() {
    List<INode> myNodesAdditonal = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes
    TimeWindowGeoNode koeln =
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    myNodesAdditonal.add(koeln);

    TimeWindowGeoNode dueren =
        new TimeWindowGeoNode("Dueren2", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    myNodesAdditonal.add(dueren);

    TimeWindowGeoNode essen =
        new TimeWindowGeoNode("Essen2", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    myNodesAdditonal.add(essen);
    
    // Lets add a pillar for our new resource
    OpeningHours pillarOpening = new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 9, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")));
            
    IPillarNode pillarNode =
            new PillarTimeWindowGeoNode(
                "testPillarKoeln",
                50.9333, 6.95,
                pillarOpening);
    myNodesAdditonal.add(pillarNode);
    
    // Let also add a node, which is already present, and will be rejected
    TimeWindowGeoNode essenDuplicate =
            new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
        myNodesAdditonal.add(essenDuplicate);

    return myNodesAdditonal;
  }

  private List<IResource> getAdditionalResources() {
	  
	  List<IResource> ress = new ArrayList<>();

    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 10, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 10, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 11, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource res1 =
        new CapacityResource(
            "JackNew", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    res1.setCost(0, 1, 1);
    
    // Let also add a resource with existing id, which will be rejected

    IResource res2 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    res2.setCost(0, 1, 1);
    
    ress.add(res1);
    ress.add(res2);
    
    return ress;
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
