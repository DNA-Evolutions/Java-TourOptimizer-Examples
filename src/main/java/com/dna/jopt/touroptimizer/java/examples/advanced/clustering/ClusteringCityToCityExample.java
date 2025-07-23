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
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

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
import javax.measure.Quantity;
import javax.measure.quantity.Length;

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

import tech.units.indriya.quantity.Quantities;

/**
 * ATTENTION: This example contains more than 10 elements therefore a valid license is required.
 *
 * <p>Example of clustering construction. Several cities in Germany serve as centers for nodes to be
 * visited. Resources are also spread around Germany. The task is to create a solution purely based
 * on clustering construction without using optimization algorithms.
 *
 * @author jrich
 * @version Mar 26, 2021
 * @since Mar 26, 2021
 */
public class ClusteringCityToCityExample extends Optimization {

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
    new ClusteringCityToCityExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example of clustering construction - City to City.";
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
    ClusteringCityToCityExample.attachToObservables(this);

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

    List<IResource> ress = new ArrayList<>();

    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    IResource rep1 =
        new CapacityResource(
            "JackKoeln", 50.9333, 6.95, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep1.setCost(0, 1, 1);

    IResource rep2 =
        new CapacityResource(
            "JackJena", 50.93297, 11.58297, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep2.setCost(0, 1, 1);

    IResource rep3 =
        new CapacityResource(
            "JackHamburg", 53.54897, 9.99337, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep3.setCost(0, 1, 1);

    IResource rep4 =
        new CapacityResource(
            "JackKoblenz", 50.3534, 7.60196, maxWorkingTime, maxDistanceKmW, getWorkingHours());

    IResource rep5 =
        new CapacityResource(
            "JackWuppertal", 51.2667, 7.183336, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep5.setCost(0, 1, 1);

    IResource rep6 =
        new CapacityResource(
            "JackHeilbronn", 49.1403, 9.22, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep6.setCost(0, 1, 1);

    IResource rep7 =
        new CapacityResource(
            "PeterGoettingen", 51.53589, 9.9316, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep7.setCost(0, 1, 1);

    IResource rep8 =
        new CapacityResource(
            "PeterDortmund", 51.51728, 7.48446, maxWorkingTime, maxDistanceKmW, getWorkingHours());
    rep8.setCost(0, 1, 1);

    ress.add(rep1);
    ress.add(rep2);
    ress.add(rep3);
    ress.add(rep4);
    ress.add(rep5);
    ress.add(rep6);
    ress.add(rep7);
    ress.add(rep8);

    return ress;
  }

  /** Adds the nodes. */
  private void addNodes() {

    getNodes().stream().forEach(this::addElement);
  }

  public static List<INode> getNodes() {

    List<INode> nodes = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHoursOne = new ArrayList<>();

    weeklyOpeningHoursOne.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(10);

    // 1.) add the nodes to be visited
    nodes.add(
        new TimeWindowGeoNode("Koeln1", 50.9333, 6.95, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Koeln3", 50.9333, 6.95, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Stuttgart", 48.7667, 9.18333, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Aachen", 50.77577, 6.08177, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Augsburg", 48.36214, 10.89411, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bautzen", 51.17948, 14.4223, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bergisch Gladbach", 50.98514, 7.12722, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Berlin", 52.5337, 13.37788, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bielefeld", 52.02113, 8.5294, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bochum", 51.48379, 7.21629, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bottrop", 51.53118, 6.92325, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Brandenburg", 52.41031, 12.5522, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Braunschweig", 52.26071, 10.5152, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bremen", 53.08112, 8.80786, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Bremerhaven", 53.54426, 8.57798, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Castrop-Rauxel", 51.54618, 7.30039, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Chemnitz", 50.84198, 12.93738, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Cottbus", 51.7702, 14.34905, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Darmstadt", 49.87387, 8.6497, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Dessau", 51.82427, 12.23536, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Dortmund", 51.51728, 7.48446, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Dresden", 51.05347, 13.74316, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Duisburg", 51.43468, 6.76507, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Duesseldorf", 51.22201, 6.78677, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Eberswalde", 52.84717, 13.74223, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Erfurt", 50.97616, 11.03275, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Erlangen", 49.58981, 11.01048, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Frankfurt (Main)", 50.10774, 8.67461, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Frankfurt (Oder)", 52.34464, 14.53733, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Freiburg", 47.99777, 7.85303, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Gelsenkirchen", 51.54398, 7.10001, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Gera", 50.87957, 12.0762, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Goerlitz", 51.15557, 14.96007, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Gotha", 50.9495, 10.7086, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Goettingen", 51.53589, 9.9316, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Greifswald", 54.08114, 13.39714, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Hagen", 51.3623, 7.46365, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Halle", 51.49381, 11.97331, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Hamburg", 53.54897, 9.99337, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Hamm", 51.68386, 7.81882, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Hannover", 52.37419, 9.74212, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Heidelberg", 49.40991, 8.6875, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Herne", 51.53698, 7.22139, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Hildesheim", 52.15702, 9.9519, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Ingolstadt", 48.7642, 11.42493, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Jena", 50.93297, 11.58297, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Kaiserslautern", 49.4426, 7.77014, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Kassel", 51.31861, 9.49618, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Kiel", 54.31799, 10.12714, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Koblenz", 50.3534, 7.60196, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Krefeld", 51.3308, 6.5591, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Leipzig", 51.33813, 12.37524, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Leverkusen", 51.04154, 6.99944, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Luebeck", 53.86924, 10.68987, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Ludwigshafen", 49.47912, 8.42893, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Lutterstadt Wittenberg", 51.86767, 12.6226, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Magdeburg", 52.13144, 11.63274, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Mainz", 49.99615, 8.26936, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Moers", 51.44238, 6.60778, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Moenchengladbach", 51.19541, 6.41172, weeklyOpeningHoursOne, visitDuration, 1));

    return nodes;
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
