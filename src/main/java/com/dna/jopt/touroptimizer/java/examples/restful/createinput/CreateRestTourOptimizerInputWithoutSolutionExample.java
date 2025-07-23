package com.dna.jopt.touroptimizer.java.examples.restful.createinput;

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

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.json.framework.JSONOptimization;
import com.dna.jopt.config.json.types.JSONConfig;
import com.dna.jopt.config.json.types.OptimizationKeySetting;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * The Class CreateRestTourOptimizerInputExample. Create test input for the
 * JOpt-TourOptimizer swagger interface. The constructed example input is valid
 * to be used by the endpoints:
 *
 * <p>
 *
 * <ul>
 * <li>/api/optimize/config/runOnlyResult
 * <li>/api/optimize/config/run
 * </ul>
 *
 * <p>
 * For production purpose, you should generate a client by using our <a href=
 * "https://swagger.dna-evolutions.com/v3/api-docs/OptimizeConfig">swagger
 * annotation</a>.
 *
 * <p>
 * Visit <a href=
 * "https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html">https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html</a>
 * for more details.
 *
 * @author jrich
 * @version Jun 1, 2021
 * @since Jun 1, 2021
 */
public class CreateRestTourOptimizerInputWithoutSolutionExample {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws ConvertException        the convert exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws SerializationException
     */
    public static void main(String[] args) throws ConvertException, InterruptedException, ExecutionException,
	    InvalidLicenceException, IOException, SerializationException {

	new CreateRestTourOptimizerInputWithoutSolutionExample().example();
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
	return "Create test input for the JOpt-TourOptimizer swagger interface. Visit "
		+ "https://docs.dna-evolutions.com/rest/touroptimizer/rest_touroptimizer.html" + " for more details.";
    }

    /**
     * Example.
     *
     * @throws ConvertException        the convert exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws SerializationException
     */
    public void example() throws ConvertException, InterruptedException, ExecutionException, InvalidLicenceException,
	    IOException, SerializationException {

	// Creating a JSONOptimization object
	IOptimization myOpti = new Optimization();

	// Adding some nodes
	CreateRestTourOptimizerInputWithoutSolutionExample.addNodes(myOpti);

	// Adding some resources
	CreateRestTourOptimizerInputWithoutSolutionExample.addResources(myOpti);

	// If not set, an ident will be created
	myOpti.setOptimizationRunIdent("MyJOptRun");

	/*
	 * Do the transformation to a valid Input for JOpt-TourOptimizer
	 */

	String licenseKey = ExampleLicenseHelper.PUBLIC_JSON_LICENSE; // Your license key

	Duration timeOut = Duration.ofMinutes(10);

	// Transform to TourOptimizerInput
	System.out.println("\n\n ====== JSON ======");
	JSONConfig myExtension = JSONConfig.builder().keySetting(OptimizationKeySetting.of(licenseKey)).timeOut(timeOut)
		.build();

	String json = JSONOptimization.asJSON(JSONOptimization.fromOptization(myOpti, Optional.of(myExtension)));

	// Print out the json
	System.out.println(json);
    }

    public static String jsonFromOptimization(IOptimization opti)
	    throws IOException, ConvertException, SerializationException {

	String licenseKey = ExampleLicenseHelper.PUBLIC_JSON_LICENSE; // Replace by your license key

	Duration timeOut = Duration.ofMinutes(10);

	JSONConfig myExtension = JSONConfig.builder().keySetting(OptimizationKeySetting.of(licenseKey)).timeOut(timeOut)
		.build();

	return JSONOptimization.asJSON(JSONOptimization.fromOptization(opti, Optional.of(myExtension)));

    }

    /*
     *
     * Helper
     *
     *
     */

    /**
     * Adds the nodes.
     *
     * @param opti the opti
     */
    private static void addNodes(IOptimization opti) {

	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(20);

	int importance = 1;

	// Define some nodes
	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);
	opti.addElement(koeln);

	INode essen = new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, importance);
	opti.addElement(essen);

	INode dueren = new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, importance);
	opti.addElement(dueren);

	INode nuernberg = new TimeWindowGeoNode("Nuernberg", 49.4478, 11.0683, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(nuernberg);

	INode heilbronn = new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(heilbronn);

	INode wuppertal = new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(wuppertal);

	INode aachen = new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration,
		importance);
	opti.addElement(aachen);
    }

    /**
     * Adds the resources to the optimization.
     *
     * @param opti the optimization instance
     */
    private static void addResources(IOptimization opti) {

	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	IResource jack = new CapacityResource("Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	opti.addElement(jack);
    }
}
