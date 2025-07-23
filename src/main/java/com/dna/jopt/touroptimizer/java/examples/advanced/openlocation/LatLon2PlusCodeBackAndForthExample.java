package com.dna.jopt.touroptimizer.java.examples.advanced.openlocation;
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

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.types.Position;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.converter.openlocation.OpenLocation;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;

import tech.units.indriya.quantity.Quantities;

/**
 * Example of transforming a Latitude and Longitude to a Plus Code
 * (https://maps.google.com/pluscodes/) back and forth.
 *
 * @author jrich
 * @version Mar 26, 2021
 * @since Mar 26, 2021
 */
public class LatLon2PlusCodeBackAndForthExample extends Optimization {

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
    new LatLon2PlusCodeBackAndForthExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Example of transforming a Latitude and Longitude to a Plus Code.";
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

    // Convert the elements and present the result
    this.convertElements();
  }

  /** Adds the resources. */
  private void convertElements() {

    List<IOptimizationElement> elements = getNodes();
    elements.addAll(getResources());

    elements.forEach(
        e -> {

          // Get the location code from the position
          Position orgPos = e.getPosition();
          String olc = e.getPosition().toOpenLocationCode();

          OpenLocation loc = OpenLocation.builder().code(olc).build();

          System.out.println(
              "\nElement:"
                  + e.getId()
                  + "\n Lat/Long (initial): "
                  + orgPos
                  + "\n PlusCode: "
                  + olc
                  + "\n Lat/Long (back): "
                  + loc.toPos()
                  + "\n");
        });
  }

  public static List<IWorkingHours> getWorkingHours() {

    List<IWorkingHours> workingHoursOne = new ArrayList<>();
    workingHoursOne.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    return workingHoursOne;
  }

  public List<IOptimizationElement> getResources() {

    List<IOptimizationElement> ress = new ArrayList<>();

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

    ress.add(rep1);
    ress.add(rep2);

    return ress;
  }

  public static List<IOptimizationElement> getNodes() {

    List<IOptimizationElement> nodes = new ArrayList<>();

    List<IOpeningHours> weeklyOpeningHoursOne = new ArrayList<>();

    weeklyOpeningHoursOne.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY, 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY, 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Duration visitDuration = Duration.ofMinutes(10);

    // 1.) add the nodes to be visited
    nodes.add(
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, weeklyOpeningHoursOne, visitDuration, 1));

    nodes.add(
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHoursOne, visitDuration, 1));

    return nodes;
  }
}
