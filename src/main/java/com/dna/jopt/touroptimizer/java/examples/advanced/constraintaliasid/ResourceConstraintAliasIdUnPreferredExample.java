package com.dna.jopt.touroptimizer.java.examples.advanced.constraintaliasid;
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

import java.io.IOException;
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
import com.dna.jopt.member.unit.condition.resource.IConstraintResource;
import com.dna.jopt.member.unit.condition.resource.UnPreferredResourceConstraint;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * Steers a team away from nodes with constraint alias ids and unpreferred resource constraints, a
 * soft constraint.
 *
 * <p>Scenario: the teams, nodes and dates of ResourceConstraintAliasIdExample. Jack and Peter
 * (Team Aachen) start in Aachen, Carla and Jessi (Team Heilbronn) in Heilbronn, and all work on
 * May 6, 2020, from 08:00 to 17:00 (Europe/Berlin). Unpreferred resource constraints name Team
 * Heilbronn for Koeln, Heilbronn, Wuppertal and Aachen, Team Aachen for Essen and Dueren, and both
 * teams for Nuernberg.
 *
 * <p>Look for: Koeln, Heilbronn, Wuppertal and Aachen on routes of Jack or Peter, Essen and Dueren
 * on a route of Carla or Jessi, and Nuernberg on a route despite its constraint, with a
 * RESOURCE_NOT_ALLOWED violation.
 *
 * <p>Prerequisites: None: without a license file the example uses the public evaluation license
 * (see ExampleLicenseHelper).
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/constraintaliasid/ResourceConstraintAliasId.md">ResourceConstraintAliasId.md</a>
 */
public class ResourceConstraintAliasIdUnPreferredExample extends Optimization {

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
    new ResourceConstraintAliasIdUnPreferredExample().example();
  }

  @Override
  public String toString() {
    return "Steers a team away from nodes with constraint alias ids and unpreferred resource"
        + " constraints, a soft constraint.";
  }

  /**
   * Builds the optimization, runs it and prints the result.
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
    // The public evaluation license, or your own license if you put a license file where
    // ExampleLicenseHelper looks for it.
    ExampleLicenseHelper.setLicense(this);
    addProperties(this);
    addNodes(this);
    addResources(this);
    attachToObservables(this);
    startAndPresentResult(this);
  }

  /**
   * Adds the properties that keep the run short.
   *
   * @param opti the optimization
   */
  private static void addProperties(IOptimization opti) {
    Properties props = new Properties();
    // Fewer generations and iterations than the defaults keep the run short.
    props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
    opti.addElement(props);
  }

  /**
   * Adds seven nodes and makes one team or both teams unpreferred for each of them with
   * unpreferred resource constraints.
   *
   * @param opti the optimization
   */
  private static void addNodes(IOptimization opti) {
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    // Each visit takes 20 minutes and counts toward the working time, like the driving.
    Duration visitDuration = Duration.ofMinutes(20);

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

    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg", 49.4478, 11.0683, openingHours, visitDuration, importance);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode(
            "Heilbronn", 49.1403, 9.22, openingHours, visitDuration, importance);
    opti.addElement(heilbronn);

    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal", 51.2667, 7.18333, openingHours, visitDuration, importance);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode(
            "Aachen", 50.775346, 6.083887, openingHours, visitDuration, importance);
    opti.addElement(aachen);

    // An unpreferred resource constraint is soft: JOpt avoids the named resources where it can,
    // but a visit is allowed at an extra cost. It names the alias Team Heilbronn, so it steers
    // Carla and Jessi away from these nodes.
    IConstraintResource teamHeilbronnUnPreferred = new UnPreferredResourceConstraint();
    // addResource needs a priority above 0. For an unpreferred resource the priority scales the
    // extra cost of a visit; 1 is the lowest value.
    teamHeilbronnUnPreferred.addResource("Team Heilbronn", 1);
    koeln.addConstraint(teamHeilbronnUnPreferred);
    wuppertal.addConstraint(teamHeilbronnUnPreferred);
    aachen.addConstraint(teamHeilbronnUnPreferred);
    // Carla and Jessi start at Heilbronn, but the constraint steers them away from it.
    heilbronn.addConstraint(teamHeilbronnUnPreferred);

    // Essen and Dueren lie near Aachen, but Team Aachen is unpreferred there.
    IConstraintResource teamAachenUnPreferred = new UnPreferredResourceConstraint();
    teamAachenUnPreferred.addResource("Team Aachen", 1);
    essen.addConstraint(teamAachenUnPreferred);
    dueren.addConstraint(teamAachenUnPreferred);

    // Both teams are unpreferred for Nuernberg. The soft constraint does not take the node out of
    // the plan: one of the four resources visits it, and the result shows the violation.
    IConstraintResource bothTeamsUnPreferred = new UnPreferredResourceConstraint();
    bothTeamsUnPreferred.addResource("Team Aachen", 1);
    bothTeamsUnPreferred.addResource("Team Heilbronn", 1);
    nuernberg.addConstraint(bothTeamsUnPreferred);
  }

  /**
   * Adds Jack and Peter from Aachen as Team Aachen, and Carla and Jessi from Heilbronn as Team
   * Heilbronn.
   *
   * @param opti the optimization
   */
  private static void addResources(IOptimization opti) {
    // 9 h per day, the length of the working hours.
    Duration maxWorkingTime = Duration.ofHours(9);
    // 1200 km per day, more than the longest day of this plan needs.
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource jack =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    jack.setConstraintAliasId("Team Aachen");
    opti.addElement(jack);

    IResource peter =
        new CapacityResource(
            "Peter", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    peter.setConstraintAliasId("Team Aachen");
    opti.addElement(peter);

    IResource carla =
        new CapacityResource(
            "Carla", 49.1403, 9.22, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    carla.setConstraintAliasId("Team Heilbronn");
    opti.addElement(carla);

    IResource jessi =
        new CapacityResource(
            "Jessi", 49.1403, 9.22, maxWorkingTime, maxDistanceKmW, createWorkingHours());
    jessi.setConstraintAliasId("Team Heilbronn");
    opti.addElement(jessi);
  }

  /**
   * Creates the working hours of a resource: May 6, 2020, from 08:00 to 17:00. Each resource gets
   * its own list; JOpt warns when resources share working hours objects.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
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
   * Starts the run, waits for the result and prints it.
   *
   * @param opti the optimization
   * @throws InterruptedException if the wait for the result is interrupted
   * @throws ExecutionException if the optimization run fails
   * @throws TimeoutException if the run does not finish within the wait
   * @throws InvalidLicenceException if the license is not valid
   */
  private static void startAndPresentResult(IOptimization opti)
      throws InterruptedException, ExecutionException, TimeoutException, InvalidLicenceException {
    CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();
    // Wait for the result; without the wait the JVM would end before the run does.
    IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);
    System.out.println(result);
  }
}
