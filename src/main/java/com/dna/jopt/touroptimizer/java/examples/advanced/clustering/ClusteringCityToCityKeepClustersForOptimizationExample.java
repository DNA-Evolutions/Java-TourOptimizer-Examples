package com.dna.jopt.touroptimizer.java.examples.advanced.clustering;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Keeps the clusters of the clustering construction as hard zones while the optimization runs, for
 * 66 nodes and 8 resources across Germany.
 *
 * <p>Needs a full license: the example has more elements than the public evaluation license
 * allows. With the public evaluation license the run ends with a license error before the
 * construction starts.
 *
 * <p>Scenario: the nodes and resources of ClusteringCityToCityExample: eight resources start in
 * Koeln, Jena, Hamburg, Koblenz, Wuppertal, Heilbronn, Goettingen and Dortmund, and 66 nodes lie in
 * German cities, three of them at the same place in Koeln. The nodes are open and the resources
 * work on May 6, 2020, from 08:00 to 22:00 (Europe/Berlin).
 *
 * <p>Look for: the routes of the result, and the file
 * ClusteringCityToCityKeepClustersForOptimizationExample.kml, which shows them on a map. Compare
 * them with the result of ClusteringCityToCityExample, which has no optimization after the
 * construction.
 *
 * <p>Prerequisites: your own full license file where ExampleLicenseHelper looks for it.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/clustering/Clustering.md">Clustering.md</a>
 */
public class ClusteringCityToCityKeepClustersForOptimizationExample extends Optimization {

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
    new ClusteringCityToCityKeepClustersForOptimizationExample().example();
  }

  @Override
  public String toString() {
    return "Keeps the clusters of the clustering construction as hard zones while the optimization"
        + " runs, for 66 nodes and 8 resources across Germany.";
  }

  /**
   * Builds the optimization, runs it, prints the result and writes it to a KML file.
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
    // Needs your own full license file where ExampleLicenseHelper looks for it: the public
    // evaluation license does not allow this many elements.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    IOptimizationResult result = startAndPresentResult(this);
    exportToKml(result);
  }

  /**
   * Adds the properties that keep the run short and keep the clusters of the construction as hard
   * zones.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    // Turns the clusters that the construction finds into zones.
    props.setProperty("JOpt.Clustering.AutoZoneCodeClusters", "True");
    // Hard zones: no node may leave the cluster of the construction during the optimization.
    props.setProperty("JOpt.Clustering.AutoZoneCodeClusters.isHard", "True");
    opti.addElement(props);
  }

  /**
   * Adds 66 nodes in German cities, with the same opening hours and visit duration.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    // Open in the same frame as the working hours of the resources, so every node matches
    // every resource in time.
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 10 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(10);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    // Koeln1, Koeln2 and Koeln3 lie at the same place.
    opti.addElement(
        new TimeWindowGeoNode("Koeln1", 50.9333, 6.95, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Koeln2", 50.9333, 6.95, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Koeln3", 50.9333, 6.95, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Stuttgart", 48.7667, 9.18333, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Aachen", 50.77577, 6.08177, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Augsburg", 48.36214, 10.89411, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bautzen", 51.17948, 14.4223, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bergisch Gladbach", 50.98514, 7.12722, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Berlin", 52.5337, 13.37788, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bielefeld", 52.02113, 8.5294, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bochum", 51.48379, 7.21629, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bottrop", 51.53118, 6.92325, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Brandenburg", 52.41031, 12.5522, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Braunschweig", 52.26071, 10.5152, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bremen", 53.08112, 8.80786, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Bremerhaven", 53.54426, 8.57798, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Castrop-Rauxel", 51.54618, 7.30039, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Chemnitz", 50.84198, 12.93738, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Cottbus", 51.7702, 14.34905, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Darmstadt", 49.87387, 8.6497, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Dessau", 51.82427, 12.23536, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Dortmund", 51.51728, 7.48446, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Dresden", 51.05347, 13.74316, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Duisburg", 51.43468, 6.76507, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Duesseldorf", 51.22201, 6.78677, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Eberswalde", 52.84717, 13.74223, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Erfurt", 50.97616, 11.03275, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Erlangen", 49.58981, 11.01048, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Frankfurt (Main)", 50.10774, 8.67461, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Frankfurt (Oder)", 52.34464, 14.53733, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Freiburg", 47.99777, 7.85303, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Gelsenkirchen", 51.54398, 7.10001, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Gera", 50.87957, 12.0762, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Goerlitz", 51.15557, 14.96007, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Gotha", 50.9495, 10.7086, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Goettingen", 51.53589, 9.9316, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Greifswald", 54.08114, 13.39714, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Hagen", 51.3623, 7.46365, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Halle", 51.49381, 11.97331, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Hamburg", 53.54897, 9.99337, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Hamm", 51.68386, 7.81882, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Hannover", 52.37419, 9.74212, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Heidelberg", 49.40991, 8.6875, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Herne", 51.53698, 7.22139, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Hildesheim", 52.15702, 9.9519, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Ingolstadt", 48.7642, 11.42493, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Jena", 50.93297, 11.58297, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Kaiserslautern", 49.4426, 7.77014, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Kassel", 51.31861, 9.49618, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Kiel", 54.31799, 10.12714, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Koblenz", 50.3534, 7.60196, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Krefeld", 51.3308, 6.5591, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Leipzig", 51.33813, 12.37524, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Leverkusen", 51.04154, 6.99944, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Luebeck", 53.86924, 10.68987, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Ludwigshafen", 49.47912, 8.42893, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Lutherstadt Wittenberg", 51.86767, 12.6226, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Magdeburg", 52.13144, 11.63274, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Mainz", 49.99615, 8.26936, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode("Moers", 51.44238, 6.60778, openingHours, visitDuration, importance));

    opti.addElement(
        new TimeWindowGeoNode(
            "Moenchengladbach", 51.19541, 6.41172, openingHours, visitDuration, importance));
  }

  /**
   * Adds eight resources in German cities, with the same working hours and limits.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // 12 h per day, a sample value.
    Duration maxWorkingTime = Duration.ofHours(12);
    // 2200 km per day, more than a resource can drive in 24 hours at its average speed
    // (setAvgSpeed).
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    opti.addElement(
        new CapacityResource(
            "JackKoeln", 50.9333, 6.95, maxWorkingTime, maxDistanceKmW, createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "JackJena", 50.93297, 11.58297, maxWorkingTime, maxDistanceKmW, createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "JackHamburg", 53.54897, 9.99337, maxWorkingTime, maxDistanceKmW,
            createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "JackKoblenz", 50.3534, 7.60196, maxWorkingTime, maxDistanceKmW, createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "JackWuppertal", 51.2667, 7.183336, maxWorkingTime, maxDistanceKmW,
            createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "JackHeilbronn", 49.1403, 9.22, maxWorkingTime, maxDistanceKmW, createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "PeterGoettingen", 51.53589, 9.9316, maxWorkingTime, maxDistanceKmW,
            createWorkingHours()));

    opti.addElement(
        new CapacityResource(
            "PeterDortmund", 51.51728, 7.48446, maxWorkingTime, maxDistanceKmW,
            createWorkingHours()));
  }

  /**
   * Creates the working hours of a resource: May 6, 2020, from 08:00 to 22:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    // 08:00 to 22:00, a frame of 14 hours for the 12 h maximal working time: the working hours
    // are a frame for matching resources with the opening hours of the nodes.
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
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
   * Writes the result to ClusteringCityToCityKeepClustersForOptimizationExample.kml in the
   * working directory.
   *
   * @param result the result of the run
   * @throws IOException if a file cannot be read or written
   */
  private static void exportToKml(IOptimizationResult result) throws IOException {
    String kmlFile = "ClusteringCityToCityKeepClustersForOptimizationExample.kml";

    EntityKMLExporter kmlExporter = new EntityKMLExporter();
    kmlExporter.setTitle("ClusteringCityToCityKeepClustersForOptimizationExample");

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
