package com.dna.jopt.touroptimizer.java.examples.expert.optimizationscheme.customdefaultproperties;

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

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.body.scheme.DefaultOptimizationScheme;
import com.dna.jopt.framework.body.scheme.IOptimizationScheme;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

/**
 * The Class OptimizationSchemeCustomDefaultPropertiesExample.
 * 
 *  Create an optimization and use a custom scheme to inject custom default properties.
 * 
 */
public class OptimizationSchemeCustomDefaultPropertiesExample extends Optimization {

    /**
     * The main method.
     * 
     *
     * @param args the arguments
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
	new OptimizationSchemeCustomDefaultPropertiesExample().example();
    }
    
    public String toString() {
	return "Create an optimization and use a custom scheme to inject custom default properties.";
    }


    /**
     * Method which executes the necessary parts for the optimization.
     *
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException
     */
    public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

	// We use the free mode for the example - modify ExampleLicenseHelper in case
	// you have a valid
	// license.

	ExampleLicenseHelper.setLicense(this);

	// Setting a modified optimization scheme
	// (1a) Setting scheme
	OptimizationSchemeCustomDefaultPropertiesExample.setScheme(this);

	// Setting properties, adding all elements, and attaching to observables
	// (1) Adding properties. Properties directly set to via addElement getting
	// priority over the custom default properties
	// set via scheme.
	OptimizationSchemeCustomDefaultPropertiesExample.addProperties(this);

	// (2) Adding nodes
	OptimizationSchemeCustomDefaultPropertiesExample.addNodes(this);

	// (3) Adding resources
	OptimizationSchemeCustomDefaultPropertiesExample.addResources(this);

	// (4) Attach to Observables
	OptimizationSchemeCustomDefaultPropertiesExample.attachToObservables(this);

	// Starting the optimization via Completable Future
	// and presenting the result
	// (5) Starting the optimization and presenting the result
	OptimizationSchemeCustomDefaultPropertiesExample.startAndPresentResult(this);
    }

    /**
     * Sets the modified scheme.
     *
     * @param opti the new scheme
     */
    public static void setScheme(IOptimization opti) {

	IOptimizationScheme myScheme = new DefaultOptimizationScheme(opti);

	// Seting default custom properties. Properties directly set to via addElement
	// getting priority over the custom default properties
	// set via scheme. This can be valuable to add hooks to existing pipelines or for debugging purpose
	Properties customDefaultProps = new Properties();

	// This setting will force Optimizer into overtime, as distance saving becomes
	// by far most important
	customDefaultProps.setProperty("JOptWeight.TotalDistance", "10000.0"); // Default is 1.0 (!)

	myScheme.setCustomDefaultProperties(customDefaultProps);

	// Set scheme
	opti.setOptimizationScheme(myScheme);
    }

    /**
     * Start the optimization and present the result.
     *
     * @param opti the optimization instance
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     */
    private static void startAndPresentResult(IOptimization opti)
	    throws InvalidLicenceException, InterruptedException, ExecutionException {
	// Extracting a completable future for the optimization result
	CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

	// It is important to block the call, otherwise optimization will be terminated
	IOptimizationResult result = resultFuture.get();

	// Presenting the result
	System.out.println(result);
    }

    /**
     * Adds the properties to the optimization.
     *
     * @param opti the optimization instance
     */
    private static void addProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "20000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	opti.addElement(props);
    }

    /**
     * Adds the nodes to the optimization.
     *
     * @param opti the optimization instance
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

    /**
     * Attach to different events (observables) of the optimization instance.
     *
     * @param opti the optimization instance
     */
    private static void attachToObservables(IOptimization opti) {

	opti.getOptimizationEvents().progressSubject().subscribe(p -> System.out.println(p.getProgressString()));

	opti.getOptimizationEvents().warningSubject().subscribe(w -> System.out.println(w.toString()));

	opti.getOptimizationEvents().statusSubject().subscribe(s -> System.out.println(s.toString()));

	opti.getOptimizationEvents().errorSubject().subscribe(e -> System.out.println(e.toString()));
    }
}