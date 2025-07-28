package com.dna.jopt.touroptimizer.java.examples.basic.io_03;

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
import static java.time.Month.MARCH;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.convert.ExportTarget;
import com.dna.jopt.config.convert.OptimizationConfiguration;
import com.dna.jopt.config.serialize.ConfigSerialization;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.CoreConfig;
import com.dna.jopt.config.types.OptimizationConfig;
import com.dna.jopt.config.types.ext.CoreExtensionManifest;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationProgress;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.relation.node2node.INode2NodeRelation;
import com.dna.jopt.member.unit.relation.node2node.tempus.NegativeRelativeTimeWindow2RelatedNodeRelation;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.dna.jopt.touroptimizer.java.examples.util.jsonprinter.ResultJsonPrinter;

import tech.units.indriya.quantity.Quantities;

/** Printing the current optimization state to a string using JSON. */
public class SaveOptimizationToJsonStringExample extends Optimization {

    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
	new SaveOptimizationToJsonStringExample().example();
    }

    public String toString() {
	return "Printing the current optimization state to a string using JSON. Be careful: Optimization snapshots can be very big and usually are highly "
		+ "compressed and loaded on the fly.";
    }

    public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

	// Set license via helper
	ExampleLicenseHelper.setLicense(this);

	// Properties!
	this.setProperties();

	this.addNodes();
	this.addResources();

	this.startRunAsync().get();
    }

    private void setProperties() {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOptLicense.CheckAutoLicensce", "FALSE");
	props.setProperty("JOpt.NumCPUCores", "4");

	this.addElement(props);
    }

    private static List<IWorkingHours> getDefaultWorkingHours() {

	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours.add(
		new WorkingHours(ZonedDateTime.of(2030, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	workingHours.add(
		new WorkingHours(ZonedDateTime.of(2030, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MARCH.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	return workingHours;
    }

    private void addResources() {

	Duration maxWorkingTimeJack = Duration.ofHours(8);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	IResource rep1 = new CapacityResource("Jack", 50.775346, 6.083887, maxWorkingTimeJack, maxDistanceKmW,
		getDefaultWorkingHours());
	rep1.setCost(0, 1, 1);

	this.addElement(rep1);

    }

    private void addNodes() {

	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours.add(
		new OpeningHours(ZonedDateTime.of(2030, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(20);

	// Define some nodes

	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
	this.addElement(koeln);

	INode aachen = new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
	this.addElement(aachen);

	// Add a relation
	INode2NodeRelation rel = new NegativeRelativeTimeWindow2RelatedNodeRelation(Duration.ofMinutes(-2000),
		Duration.ofMinutes(10));
	rel.setMasterNode(koeln);
	rel.setRelatedNode(aachen);
	aachen.addNode2NodeRelation(rel);
	koeln.addNode2NodeRelation(rel);
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

	try {
	    ResultJsonPrinter.printResultAsJson(this);
	} catch (IOException | ConvertException e) {
	    e.printStackTrace();
	}

    }

}
