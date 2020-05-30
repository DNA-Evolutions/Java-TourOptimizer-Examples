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
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.time.Month.MAY;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.convert.ExportTarget;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.BZip2JsonOptimizationIO;
import com.dna.jopt.io.IOptimizationIO;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.IQualification;
import com.dna.jopt.member.unit.condition.workinghour.zone.ukpostcode.UKPostCode;
import com.dna.jopt.member.unit.condition.workinghour.zone.ukpostcode.UKPostCodeConstraint;
import com.dna.jopt.member.unit.condition.workinghour.zone.ukpostcode.UKPostCodeQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/** Using UK Post codes */
public class UKPostCodeExample extends Optimization {

  public static void main(String[] args)
      throws InvalidLicenceException, ConvertException, SerializationException,
          FileNotFoundException, InterruptedException, ExecutionException, IOException {

    new UKPostCodeExample().example();
  }

  public String toString() {
    return "Testing UKPostCode constraint as part of the ZoneCode feature.";
  }

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

    // Save to JSON

    IOptimizationIO<IOptimization> io =
        new BZip2JsonOptimizationIO(); // alternative without compression: new JsonOptimizationIO()

    String jsonFile = this.getClass().getSimpleName() + ".json.bz2";
    io.write(new FileOutputStream(jsonFile), ExportTarget.of(this));
  }

  private void setProperties(IOptimization opti) {

    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
    props.setProperty("JOpt.NumCPUCores", "4");

    // This property can be used adjust the cost for violating PostCode conditions for the use as
    // soft constraint
    // (10 is the default value)
    props.setProperty("JOptWeight.ZoneCode", "10.0");

    opti.addElement(props);
  }

  private void addElements(IOptimization opti) {

    /*
     *
     *  Defining a resource which is allowed (hard constrained) to visit:
     *  B37 and B48 on the first day
     *  B36         on the second day
     *
     *  Further, there is one node which has no matching post code, therefore it will be unassigned.
     */

    // Defining UK Post codes with:
    // =============================
    // String areaIdent,
    // Optional<Integer> districtIdent,
    // Optional<Integer> sectorIdent,
    // Optional<String> unitIdent

    UKPostCode b37 = new UKPostCode("B", Optional.of(37), Optional.empty(), Optional.empty());

    UKPostCode b48 = new UKPostCode("B", Optional.of(48), Optional.empty(), Optional.empty());

    UKPostCode b36 = new UKPostCode("B", Optional.of(36), Optional.empty(), Optional.empty());

    // Defining workingHours
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

    // Crating/Adding constraints
    UKPostCodeConstraint postCodeConstraintWoh1 = new UKPostCodeConstraint();

    postCodeConstraintWoh1.setIsHard(false); // Use as soft constraint
    postCodeConstraintWoh1.addZoneCode(b37);
    postCodeConstraintWoh1.addZoneCode(b48);
    woh1.addConstraint(postCodeConstraintWoh1);

    UKPostCodeConstraint postCodeConstraintWoh2 = new UKPostCodeConstraint();

    postCodeConstraintWoh2.setIsHard(false); // Use as soft constraint
    postCodeConstraintWoh2.addZoneCode(b36);
    woh2.addConstraint(postCodeConstraintWoh2);

    // Creating/Adding resource
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

    Duration visitDuration = Duration.ofMinutes(20);

    // Define some nodes and adding post codes
    INode koeln =
        new TimeWindowGeoNode(
            "KoelnNoMatchingPostCode", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
    IQualification b312Gquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(31), Optional.of(2), Optional.of("G")));
    koeln.addQualification(b312Gquali);
    opti.addElement(koeln);

    INode oberhausen =
        new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
    IQualification b372Gquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(37), Optional.of(2), Optional.of("G")));
    oberhausen.addQualification(b372Gquali);
    opti.addElement(oberhausen);

    INode essen =
        new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
    IQualification b482Gquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(48), Optional.of(2), Optional.of("G")));
    essen.addQualification(b482Gquali);
    opti.addElement(essen);

    INode dueren =
        new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
    IQualification b362Gquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(36), Optional.of(2), Optional.of("G")));
    dueren.addQualification(b362Gquali);
    opti.addElement(dueren);

    INode nuernberg =
        new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration, 1);
    IQualification b362Hquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(36), Optional.of(2), Optional.of("H")));
    nuernberg.addQualification(b362Hquali);
    opti.addElement(nuernberg);

    INode heilbronn =
        new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
    IQualification b482Hquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(48), Optional.of(2), Optional.of("H")));
    heilbronn.addQualification(b482Hquali);
    opti.addElement(heilbronn);

    INode stuttgart =
        new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
    IQualification b362Fquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(36), Optional.of(2), Optional.of("F")));
    stuttgart.addQualification(b362Fquali);
    opti.addElement(stuttgart);

    INode wuppertal =
        new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
    IQualification b372Fquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(37), Optional.of(2), Optional.of("F")));
    wuppertal.addQualification(b372Fquali);
    opti.addElement(wuppertal);

    INode aachen =
        new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
    IQualification b482Fquali =
        new UKPostCodeQualification(
            new UKPostCode("B", Optional.of(48), Optional.of(2), Optional.of("F")));
    aachen.addQualification(b482Fquali);
    opti.addElement(aachen);
  }
}
