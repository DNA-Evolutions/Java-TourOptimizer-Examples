package com.dna.jopt.touroptimizer.java.examples.advanced.zonecode;
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

import java.io.FileNotFoundException;
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

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.IQualification;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumber;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumberConstraint;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumberQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 *  Using ZoneNumber to divide nodes into areas. A Resource is allowed to visit different areas
 *  on different workingDays.
 *
 * @author jrich
 * @version Aug 14, 2020
 * @since Aug 14, 2020
 */
public class ZoneNumberConstraintExample extends Optimization {

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InvalidLicenceException the invalid licence exception
   * @throws ConvertException the convert exception
   * @throws SerializationException the serialization exception
   * @throws FileNotFoundException the file not found exception
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void main(String[] args)
      throws InvalidLicenceException, ConvertException, SerializationException,
          FileNotFoundException, InterruptedException, ExecutionException, IOException {

    new ZoneNumberConstraintExample().example();
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return "Testing ZoneNumber constraint as part of the ZoneCode feature.";
  }

  /**
   * Example.
   *
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   * @throws FileNotFoundException the file not found exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidLicenceException the invalid licence exception
   * @throws ConvertException the convert exception
   * @throws SerializationException the serialization exception
   */
  public void example()
      throws InterruptedException, ExecutionException, FileNotFoundException, IOException,
          InvalidLicenceException, ConvertException, SerializationException {

    // Set license via helper
    ExampleLicenseHelper.setLicense(this);

    // Properties!
    this.setProperties(this);
    this.addElements(this);

    /*
     *  Use reactive java, in case synch run is used all subscription have to be done before calling
     *  start, otherwise data from subscription will be triggered after run is done
     */

    // Use asynch run here
    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

    // Subscribe to events
    this.getOptimizationEvents()
        .progress
        .subscribe(
            p -> {
              System.out.println(p.getProgressString());
            });

    this.getOptimizationEvents()
        .error
        .subscribe(
            e -> {
              System.out.println(e.getCause() + " " + e.getCode());
            });

    this.getOptimizationEvents()
        .status
        .subscribe(
            s -> {
              System.out.println(s.getDescription() + " " + s.getCode());
            });

    // Get result - This also blocking the execution
    IOptimizationResult result = resultFuture.get();

    // Print result
    System.out.println(result);
  }

  /**
   * Sets the properties.
   *
   * @param opti the new properties
   */
  private void setProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "10000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "500000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    // This property can be used adjust the cost for violating PostCode conditions for the use as
    // soft constraint
    // (10 is the default value)
    props.setProperty("JOptWeight.ZoneCode", "10.0");

    opti.addElement(props);
  }

  /**
   * Adds the elements.
   *
   * @param opti the opti
   */
  private void addElements(IOptimization opti) {

    /*
     *  Define three different zones: "1", "2", and "3"
     */
    ZoneNumber zoneOne = new ZoneNumber(1);
    ZoneNumber zoneTwo = new ZoneNumber(2);
    ZoneNumber zoneThree = new ZoneNumber(3);

    /*
     *  Defining workingHours and attach constraints based on the zones we created
     *  
     *  For example: the first WorkingHour gets ZoneOne and ZoneTwo as constraint. This means
     *  the resource holding this workingHour should only visit ZoneOne and ZoneTwo during
     *  this workingHour 
     */
    IWorkingHours woh1 =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    IWorkingHours woh2 =
        new WorkingHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));

    List<IWorkingHours> weeklyWorkingHours = new ArrayList<>();
    weeklyWorkingHours.add(woh1);
    weeklyWorkingHours.add(woh2);

    // Creating/Adding Zones as constraints
    ZoneNumberConstraint zoneNumberConstraintWOHOne = new ZoneNumberConstraint();

    zoneNumberConstraintWOHOne.setIsHard(true); // Use as hard constraint
    zoneNumberConstraintWOHOne.addZoneCode(zoneOne);
    zoneNumberConstraintWOHOne.addZoneCode(zoneTwo);
    woh1.addConstraint(zoneNumberConstraintWOHOne);

    ZoneNumberConstraint zoneNumberConstraintWOHTwo = new ZoneNumberConstraint();

    zoneNumberConstraintWOHTwo.setIsHard(true); // Use as soft constraint
    zoneNumberConstraintWOHTwo.addZoneCode(zoneThree);
    woh2.addConstraint(zoneNumberConstraintWOHTwo);

    /*
     * 
     *  Creating/Adding the resource and attach constrained workingHours
     *  
     */
    
    Duration maxWorkingTime = Duration.ofHours(13);
    Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

    IResource rep1 =
        new CapacityResource(
            "Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW, weeklyWorkingHours);
    rep1.setCost(0, 1, 1);
    opti.addElement(rep1);

    // Creating/Adding nodes
    List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    weeklyOpeningHours.add(
        new OpeningHours(
            ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
            ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));


    /*
     * 
     *  Define some nodes and add ZoneNumberQualifications based on ZoneNumber we created.
     *  
     *  For example: If a node gets a qualification for zoneOne, it should be only visited by a resource
     *  holding a ZoneNumberOne constraint along its constraints. 
     *  
     *  Note: If a resource is not holding any ZoneNumerConstraint it is free to visit all nodes.
     *  
     *  
     */

    IQualification zoneOneQuali = new ZoneNumberQualification(zoneOne);
    IQualification zoneTwoQuali = new ZoneNumberQualification(zoneTwo);
    IQualification zoneThreeQuali = new ZoneNumberQualification(zoneThree);


    Duration visitDuration = Duration.ofMinutes(20);
    
    //
    INode koeln =
        new TimeWindowGeoNode("Koeln-Z1", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    koeln.addQualification(zoneOneQuali); // Adding qualification for zoneOne
    opti.addElement(koeln);

    // Note: Oberhausen has a qualification for zoneOne AND zoneTwo. Usually, this makes sense, if a node
    //       is located at the geographical boundary of two zones. This way, Resources holding a constraint
    //       for zoneOne and Resources holding a constraint for zoneTwo are allowed to visit the node
    INode oberhausen =
        new TimeWindowGeoNode(
            "Oberhausen-Z1-Z2", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    oberhausen.addQualification(zoneOneQuali);
    oberhausen.addQualification(zoneTwoQuali);
    opti.addElement(oberhausen);

    //
    INode essen =
        new TimeWindowGeoNode("Essen-Z3", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    essen.addQualification(zoneThreeQuali);
    opti.addElement(essen);

    //
    INode dueren =
        new TimeWindowGeoNode("Dueren-Z2", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    dueren.addQualification(zoneTwoQuali);
    opti.addElement(dueren);

    //
    INode nuernberg =
        new TimeWindowGeoNode(
            "Nuernberg-Z3", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    nuernberg.addQualification(zoneThreeQuali);
    opti.addElement(nuernberg);

    //
    INode stuttgart =
        new TimeWindowGeoNode(
            "Stuttgart-Z2", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    stuttgart.addQualification(zoneTwoQuali);
    opti.addElement(stuttgart);

    //
    INode wuppertal =
        new TimeWindowGeoNode(
            "Wuppertal-Z2", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    wuppertal.addQualification(zoneTwoQuali);
    opti.addElement(wuppertal);

    //
    INode aachen =
        new TimeWindowGeoNode(
            "Aachen-Z2", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    aachen.addQualification(zoneTwoQuali);
    opti.addElement(aachen);

  }
}
