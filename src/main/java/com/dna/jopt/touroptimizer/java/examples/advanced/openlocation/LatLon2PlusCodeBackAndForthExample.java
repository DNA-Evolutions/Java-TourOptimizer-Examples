package com.dna.jopt.touroptimizer.java.examples.advanced.openlocation;
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
import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.types.Position;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.converter.openlocation.OpenLocation;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;

import tech.units.indriya.quantity.Quantities;

/**
 * Converts the positions of nodes and resources to Plus Codes (Open Location Codes) and back.
 *
 * <p>Scenario: three nodes (Koeln, Oberhausen and Essen) and two resources (JackKoeln and JackJena)
 * with their latitude and longitude. The example runs no optimization; it only reads the position
 * of each element.
 *
 * <p>Look for: for each element, its position, its Plus Code and the position decoded from the
 * code. The decoded position is the centre of the code's cell, close to the original but not the
 * same.
 *
 * <p>Prerequisites: None: the example runs no optimization and needs no license.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/advanced/openlocation/OpenLocationCode.md">OpenLocationCode.md</a>
 */
public class LatLon2PlusCodeBackAndForthExample {

  /**
   * Runs the example.
   *
   * @param args not used
   */
  public static void main(String[] args) {
    new LatLon2PlusCodeBackAndForthExample().example();
  }

  @Override
  public String toString() {
    return "Converts the positions of nodes and resources to Plus Codes (Open Location Codes) and"
        + " back.";
  }

  /** Builds the nodes and resources and prints the conversion of each position. */
  public void example() {
    List<IOptimizationElement> elements = new ArrayList<>();
    elements.addAll(createNodes());
    elements.addAll(createResources());
    convertElements(elements);
  }

  /**
   * Converts the position of each element to a Plus Code and back, and prints the three values.
   *
   * @param elements the nodes and resources
   */
  private static void convertElements(List<IOptimizationElement> elements) {
    for (IOptimizationElement element : elements) {
      Position position = element.getPosition();
      String plusCode = position.toOpenLocationCode();

      OpenLocation location = OpenLocation.builder().code(plusCode).build();
      Position positionBack = location.toPos();

      System.out.println("Element: " + element.getId());
      System.out.println(" Lat/Long (initial): " + position);
      System.out.println(" PlusCode: " + plusCode);
      System.out.println(" Lat/Long (back): " + positionBack);
      System.out.println();
    }
  }

  /**
   * Creates three nodes with their latitude and longitude.
   *
   * @return the nodes
   */
  private static List<IOptimizationElement> createNodes() {
    // A node needs opening hours and a visit duration; this example reads only its position.
    List<IOpeningHours> openingHours = new ArrayList<>();
    openingHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    Duration visitDuration = Duration.ofMinutes(10);
    int importance = 1;

    List<IOptimizationElement> nodes = new ArrayList<>();
    nodes.add(
        new TimeWindowGeoNode("Koeln", 50.9333, 6.95, openingHours, visitDuration, importance));
    nodes.add(
        new TimeWindowGeoNode(
            "Oberhausen", 51.4667, 6.85, openingHours, visitDuration, importance));
    nodes.add(
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, openingHours, visitDuration, importance));
    return nodes;
  }

  /**
   * Creates two resources with their latitude and longitude.
   *
   * @return the resources
   */
  private static List<IOptimizationElement> createResources() {
    // A resource needs working hours and limits; this example reads only its position.
    Duration maxWorkingTime = Duration.ofHours(12);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

    List<IOptimizationElement> resources = new ArrayList<>();
    resources.add(
        new CapacityResource(
            "JackKoeln", 50.9333, 6.95, maxWorkingTime, maxDistanceKmW, createWorkingHours()));
    resources.add(
        new CapacityResource(
            "JackJena", 50.93297, 11.58297, maxWorkingTime, maxDistanceKmW, createWorkingHours()));
    return resources;
  }

  /**
   * Creates the working hours of one resource: May 6, 2020, from 08:00 to 22:00.
   *
   * @return the working hours
   */
  private static List<IWorkingHours> createWorkingHours() {
    List<IWorkingHours> workingHours = new ArrayList<>();
    workingHours.add(
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 22, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return workingHours;
  }
}
