package com.dna.jopt.touroptimizer.java.examples.expert.compareresult;
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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.container.JobAdvantagesController;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.container.job.JobAdvantageResult;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.interpreter.CostAdvantageInterpreter;
import com.dna.jopt.util.optimizationresultanalyzer.advantages.interpreter.ICostAdvantagesInterpreter;
import com.dna.jopt.util.optimizationresultanalyzer.exceptions.ResultStructureNotMatchingException;
import com.dna.jopt.util.optimizationresultmodifier.helper.IModificationTask;
import com.dna.jopt.util.optimizationresultmodifier.wrapper.OptimizationResultModificationWrapper;

/**
 * Holds the data and the steps that the two comparison examples share.
 *
 * <p>Scenario: Jack from Aachen visits Koeln, Essen, Dueren, Wuppertal, Aachen, Krefeld and
 * Duisburg on May 8 and May 9, 2030 (Europe/Berlin). Duisburg is open only on May 8 from 08:00 to
 * 09:45, the other nodes on both days from 08:00 to 17:00. CompareResultsWithNodesMoveExample adds
 * Nuernberg and Heilbronn.
 *
 * <p>The class is not run on its own: CompareResultsWithNodeExchangeExample and
 * CompareResultsWithNodesMoveExample extend it and call its steps.
 *
 * <p>Prerequisites: None: without a license file the examples use the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/compareresult/CompareResult.md">CompareResult.md</a>
 */
public abstract class CompareResultExampleOptimization extends Optimization {

  /**
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  protected static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds seven nodes in the Rhine-Ruhr area and, if asked for, two far away nodes.
   *
   * @param opti the optimization
   * @param addFarAwayNodes true to add Nuernberg and Heilbronn as well
   */
  protected static void addNodes(IOptimization opti, boolean addFarAwayNodes) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2030, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2030, MAY.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2030, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2030, MAY.getValue(), 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 30 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(30);

    // The same importance for every node, so no node is preferred over another.
    int importance = 1;

    INode koeln =
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance);
    opti.addElement(koeln);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, openingHours, visitDuration, importance);
    opti.addElement(dueren);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHours, visitDuration, importance);
    opti.addElement(aachen);

    INode krefeld =
        new TimeWindowGeoNode(
            "Krefeld", 51.305449, 6.631279, openingHours, visitDuration, importance);
    opti.addElement(krefeld);

    if (addFarAwayNodes) {
      INode nuernberg =
          new TimeWindowGeoNode(
              "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance);
      opti.addElement(nuernberg);

      INode heilbronn =
          new TimeWindowGeoNode(
              "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
      opti.addElement(heilbronn);
    }

    // Duisburg closes at 09:45 on May 8, so a route that visits Krefeld first ends the visit at
    // Duisburg after the closing time. This makes the optimizer visit Duisburg before Krefeld.
    List<IOpeningHours> strictOpeningHours = new ArrayList<>();
    strictOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2030, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2030, MAY.getValue(), 8, 9, 45, 0, 0, ZoneId.of("Europe/Berlin"))));

    INode duisburg =
        new TimeWindowGeoNode(
            "Duisburg", 51.47994, 6.75966, strictOpeningHours, visitDuration, importance);
    opti.addElement(duisburg);
  }

  /**
   * Adds Jack, who starts and ends each working day in Aachen.
   *
   * @param opti the optimization
   */
  protected static void addResources(IOptimization opti) {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2030, MAY.getValue(), 8, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2030, MAY.getValue(), 8, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2030, MAY.getValue(), 9, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2030, MAY.getValue(), 9, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // 9 h per day, the length of the shift: a day with Nuernberg and Heilbronn exceeds it
    // (overtime).
    Duration maxWorkingTime = Duration.ofHours(9);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, workingHours);
    opti.addElement(jack);
  }

  /**
   * Prints the progress, warning, status and error events of the run.
   *
   * @param opti the optimization
   */
  protected static void attachToObservables(IOptimization opti) {
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
  protected static IOptimizationResult startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    String banner = "#########################  RESULT ORG  #########################";
    System.out.println("\n\n\n" + banner + "\n\n\n");
    System.out.println(result);
    return result;
  }

  /**
   * Applies the modification tasks to a copy of the result of the run and returns the copy.
   *
   * @param opti the optimization that has run
   * @param tasks the modification tasks
   * @return the modified result
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws InvalidLicenceException if the license is not valid
   * @throws IOException if a file cannot be read or written
   * @throws ConvertException if the optimization cannot be converted for the copy
   * @throws SerializationException if the copy cannot be serialized
   */
  protected static IOptimizationResult applyTasksAndCreateModResult(
      IOptimization opti, List<IModificationTask> tasks)
      throws InterruptedException, ExecutionException, InvalidLicenceException, IOException,
      ConvertException, SerializationException {
    OptimizationResultModificationWrapper wrapper = new OptimizationResultModificationWrapper();
    // The wrapper copies the optimization, applies the tasks to the copy and assesses the copy
    // once, without a new search.
    return wrapper.getNewResult(opti, tasks);
  }

  /**
   * Compares the original and the modified result and prints the report.
   *
   * @param orgResult the original result
   * @param modResult the modified result
   * @throws ResultStructureNotMatchingException if the two results do not have the same routes
   */
  protected static void compareResults(
      IOptimizationResult orgResult, IOptimizationResult modResult)
      throws ResultStructureNotMatchingException {
    Optional<JobAdvantageResult> comparisonResultOptional =
        JobAdvantagesController.compare(orgResult, modResult);

    if (comparisonResultOptional.isPresent()) {
      JobAdvantageResult comparisonResult = comparisonResultOptional.get();

      ICostAdvantagesInterpreter myInterpreter = new CostAdvantageInterpreter();

      System.out.println(
          "\n\n\n#########################  COMPARISON RESULT  #########################\n\n\n");
      System.out.println(myInterpreter.generateTextReport(comparisonResult));
    } else {
      System.out.println("Comparison failed");
    }
  }

  /**
   * Writes a result to a KML file, for a look at the routes on a map.
   *
   * @param result the result
   * @param kmlFile the file name
   * @throws IOException if a file cannot be read or written
   */
  protected static void exportToKml(IOptimizationResult result, String kmlFile)
      throws IOException {
    EntityKMLExporter kmlExporter = new EntityKMLExporter();

    // export flushes the stream but does not close it; the try block closes it.
    try (OutputStream out = new FileOutputStream(kmlFile)) {
      if (!kmlExporter.export(result, out)) {
        System.out.println("KML export failed: " + kmlFile);
      }
    }
  }
}
