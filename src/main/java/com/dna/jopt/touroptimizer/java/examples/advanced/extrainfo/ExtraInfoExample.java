package com.dna.jopt.touroptimizer.java.examples.advanced.extrainfo;

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
import static tec.units.ri.unit.MetricPrefix.KILO;
import static tec.units.ri.unit.Units.METRE;
import static java.time.Month.MAY;

import tec.units.ri.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.serialize.ConfigSerialization;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * This example shows how to add an extra info string to nodes and resources. In
 * this example, we save the JSON String representation of a POJO. Later on, we
 * transform the JSON string back to the POJO.
 * 
 * The provided example demonstrates the method for appending additional
 * information strings to nodes and resources (called extraInfo). It includes a
 * the process of storing a Plain Old Java Object (POJO) as a JSON string as
 * extraInfo. Subsequently, the example guides you through the steps to revert
 * this JSON string back to its original POJO format.
 *
 * @author DNA (jrich)
 * @version Jan 22, 2024
 * @since Jan 22, 2024
 */
public class ExtraInfoExample extends Optimization {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws TimeoutException        the timeout exception
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {
	new ExtraInfoExample().example();
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
	return "Using the extraInfo feature to store a custom JSON string for nodes and resources.";
    }

    /**
     * The method which executes the necessary parts for the Optimization.
     *
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws TimeoutException        the timeout exception
     */
    public void example()
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {

	// We use the free mode for the example - please modify the ExampleLicenseHelper
	// in case you have a valid license.

	ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// Adding properties
	ExtraInfoExample.addProperties(this);

	// Adding Nodes
	ExtraInfoExample.addNodesAndExtraInfo(this);

	// Adding Resources
	ExtraInfoExample.addResourcesAndExtraInfo(this);

	// Subscribe to events
	ExtraInfoExample.subscribeToEvents(this);

	// (5) Starting the Optimization completable Future and presenting the results
	ExtraInfoExample.startAndPresentResult(this);
    }

    /**
     * Start the Optimization and present the result.
     *
     * @param opti the optimization instance
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws TimeoutException        the timeout exception
     */
    private static void startAndPresentResult(IOptimization opti)
	    throws InvalidLicenceException, InterruptedException, ExecutionException, TimeoutException {

	// Extracting a completable Future for the optimization result
	CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

	// It is important to block the call, otherwise the Optimization will be
	// terminated
	IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);

	// Presenting the result
	System.out.println(result);

	// Extrat extra info

	List<IOptimizationElement> allElements = result.getRoutes().stream().map(ILogicEntityRoute::getAllElements)
		.flatMap(List::stream).distinct().toList();

	List<INode> nodes = allElements.stream().filter(INode.class::isInstance).map(INode.class::cast)
		.filter(n -> n.getExtraInfo().isPresent()).toList();

	List<IResource> ress = allElements.stream().filter(IResource.class::isInstance).map(IResource.class::cast)
		.filter(r -> r.getExtraInfo().isPresent()).toList();

	System.out.println("\n==== EXTRA INFO =====");

	nodes.forEach(n ->

	n.getExtraInfo().ifPresent(extra -> {
	    System.out.println("\n" + n.getId());

	    // We can also get the NodeExtraInfo class object
	    try {
		NodeExtraInfo info = ConfigSerialization.objectMapper().readValue(extra, NodeExtraInfo.class);
		System.out.println(info);

	    } catch (JsonProcessingException e) {
		e.printStackTrace();
	    }
	})

	);

	System.out.println("========");

	ress.forEach(r ->

	r.getExtraInfo().ifPresent(extra -> {
	    System.out.println("\n" + r.getId());

	    // We can also get the NodeExtraInfo class object
	    try {
		ResourceExtraInfo info = ConfigSerialization.objectMapper().readValue(extra, ResourceExtraInfo.class);
		System.out.println(info);

	    } catch (JsonProcessingException e) {
		e.printStackTrace();
	    }
	}));
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

    /**
     * Adds the Nodes to the Optimization.
     *
     * @param opti the optimization instance
     * @throws JsonProcessingException the json processing exception
     */
    private static void addNodesAndExtraInfo(IOptimization opti) throws JsonProcessingException {

	// Define the OpeningHours
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(20);

	int importance = 1;

	// Define some Nodes
	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, importance);

	// Creating an extra info
	String extraInfoCologne = ConfigSerialization.objectMapper()
		.writeValueAsString(new NodeExtraInfo("+491798596320", "Mrs. Einstein"));
	koeln.setExtraInfo(extraInfoCologne);

	opti.addElement(koeln);

	//

	INode essen = new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, importance);

	// Creating an extra info
	String extraInfoEssen = ConfigSerialization.objectMapper()
		.writeValueAsString(new NodeExtraInfo("+491738145327", "Mrs. Kepler"));
	essen.setExtraInfo(extraInfoEssen);

	opti.addElement(essen);

    }

    /**
     * Adds the Resources to the Optimization.
     *
     * @param opti the optimization instance
     * @throws JsonProcessingException the json processing exception
     */
    private static void addResourcesAndExtraInfo(IOptimization opti) throws JsonProcessingException {

	// Define the WorkingHours
	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Define the Resource
	IResource jack = new CapacityResource("Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);

	// Creating an extra info
	String extraInfo = ConfigSerialization.objectMapper()
		.writeValueAsString(new ResourceExtraInfo("+491787536331", Instant.parse("1989-02-19T09:30:00.00Z")));

	jack.setExtraInfo(extraInfo);

	opti.addElement(jack);
    }

    /**
     * Subscribe to events.
     *
     * @param opti the opti
     */
    private static void subscribeToEvents(IOptimization opti) {

	PrintStream myOut = System.out;

	// Subscribe to events
	opti.getOptimizationEvents().progressSubject().subscribe(p -> myOut.println(p.getProgressString()));

	opti.getOptimizationEvents().errorSubject().subscribe(e -> myOut.println(e.getCause() + " " + e.getCode()));

	opti.getOptimizationEvents().warningSubject().subscribe(w -> myOut.println(w.getDescription() + w.getCode()));

	opti.getOptimizationEvents().statusSubject()
		.subscribe(s -> myOut.println(s.getDescription() + " " + s.getCode()));

	opti.getOptimizationEvents().resultFuture().thenAccept(myOut::println);
    }
}