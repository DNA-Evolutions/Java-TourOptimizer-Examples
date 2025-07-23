package com.dna.jopt.touroptimizer.java.examples.advanced.returnstart;

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
 * The feature "Return To Start":
 * 
 * In this example, we want a resource to return to its start location AFTER a
 * node is visited and BEFORE visiting a subsequent node. If a resource has to
 * return to its home/start location after visiting a node can be defined
 * separately for each node.
 *
 * <p>
 * Note: Here, the intermediate visit of the start location does NOT start a new
 * route. Moreover, the intermediate start location visit is defined as a new
 * node. Therefore, the time a resource spends for an intermediate visit of the
 * start location can be predefined.
 * 
 * 
 * <p>
 * This feature can be used most likely in pick-up and delivery problems.
 * Assuming it is obvious that a resource has to return to its start location
 * after each visit (e.g. to a warehouse) to reload goods before visiting
 * another node, it relaxes the work for the optimizer to define "Return2Start"
 * nodes instead of defining load and capacities for the nodes.
 * 
 *
 * @author jrich
 * @version Jan 24, 2023
 * @since Sep 28, 2021
 */
public class ReturnStartExample extends Optimization {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {
	new ReturnStartExample().example();
    }

    public String toString() {
	return "Return to start after node visit example.";
    }

    /**
     * The method which executes the necessary parts for the Optimization.
     *
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException
     */
    public void example() throws InterruptedException, ExecutionException, InvalidLicenceException, IOException {

	// We use the free mode for the example - please modify the ExampleLicenseHelper
	// in case you
	// have a valid
	// license.

	ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// (1) Adding properties
	ReturnStartExample.addProperties(this);

	// (2) Adding Nodes
	ReturnStartExample.addNodes(this);

	// (3) Adding Resources
	ReturnStartExample.addResources(this);

	// (4) Attaching to Observables
	ReturnStartExample.attachToObservables(this);

	// (5) Starting the Optimization completable Future and presenting the results
	ReturnStartExample.startAndPresentResult(this);
    }

    /**
     * Start the Optimization and present the result.
     *
     * @param opti the optimization instance
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     */
    private static void startAndPresentResult(IOptimization opti)
	    throws InvalidLicenceException, InterruptedException, ExecutionException {

	// Extracting a completable Future for the optimization result
	CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

	// It is important to block the call, otherwise the Optimization will be
	// terminated
	IOptimizationResult result = resultFuture.get();

	// Presenting the result
	System.out.println(result);
    }

    /**
     * Adds the Properties to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "10000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	opti.addElement(props);
    }

    private static void addResources(IOptimization opti) {
	// Define the WorkingHours
	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	// We make the routes open
	workingHours.stream().forEach(w -> w.setIsClosedRoute(false));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Define the Resource
	IResource jack = new CapacityResource("Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	opti.addElement(jack);
    }

    /**
     * Adds the Nodes to the Optimization.
     *
     * @param opti the optimization instance
     * @return
     */
    private static void addNodes(IOptimization opti) {

	/*
	 * 
	 * As returning home does not count as route termination, the Resource has to
	 * stay for a predefined time at the start location after a node visit before
	 * moving on to the next route.
	 * 
	 */

	Duration intermediateStayAtHomeDuration = Duration.ofMinutes(20);

	// Define the OpeningHours of the nodes
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(20);

	int importance = 1;

	// Define some Nodes to be visited
	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);
	koeln.setIsReturnStart(intermediateStayAtHomeDuration);
	opti.addElement(koeln);

	INode essen = new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, importance);
	essen.setIsReturnStart(intermediateStayAtHomeDuration);
	opti.addElement(essen);

	INode dueren = new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, importance);
	dueren.setIsReturnStart(intermediateStayAtHomeDuration);
	opti.addElement(dueren);

	INode wuppertal = new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration,
		importance);
	wuppertal.setIsReturnStart(intermediateStayAtHomeDuration);
	opti.addElement(wuppertal);
    }

    /**
     * Attach to different Events (Observables) of the optimization instance.
     *
     * @param opti the optimization instance
     */
    private static void attachToObservables(IOptimization opti) {

	opti.getOptimizationEvents().progressSubject().subscribe(p -> {
	    System.out.println(p.getProgressString());
	});

	opti.getOptimizationEvents().warningSubject().subscribe(w -> {
	    System.out.println(w.toString());
	});

	opti.getOptimizationEvents().statusSubject().subscribe(s -> {
	    System.out.println(s.toString());
	});

	opti.getOptimizationEvents().errorSubject().subscribe(e -> {
	    System.out.println(e.toString());
	});
    }
}
