package com.dna.jopt.touroptimizer.java.examples.advanced.capacitycheck;

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
 * 
 *
 * @author jrich
 * @version Aug 24, 2022
 * @since Aug 24, 2022
 */
public class CapacityCheckExample extends Optimization {

    private static final boolean DO_DEACTIVATE_CAPACITY_CHECK = false;

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {
	new CapacityCheckExample().example();
    }

    public String toString() {
	return "By default, JOpt does not accept if available working hours are two times less than the needed working time. "
		+ "This check can be deactivated if absolutely necessary. Use the boolean DO_DEACTIVATE_CAPACITY_CHECK within "
		+ "this example. ";
    }

    /**
     * The method which executes the necessary parts for the Optimization.
     *
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException
     * @throws TimeoutException
     */
    public void example()
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {

	// We use the free mode for the example - please modify the ExampleLicenseHelper
	// in case you
	// have a valid
	// license.

	ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// (1) Adding properties
	CapacityCheckExample.addProperties(this);

	// (2) Adding Nodes
	CapacityCheckExample.addNodes(this);

	// (3) Adding Resources
	CapacityCheckExample.addResources(this);

	// (4) Attaching to Observables
	CapacityCheckExample.attachToObservables(this);

	// (5) Starting the Optimization completable Future and presenting the results
	CapacityCheckExample.startAndPresentResult(this);
    }

    /**
     * Start the Optimization and present the result.
     *
     * @param opti the optimization instance
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws TimeoutException
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
    }

    /**
     * Adds the Properties to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	if (DO_DEACTIVATE_CAPACITY_CHECK) {
	    props.setProperty("JOpt.plausibility.doInputCheck.doCapacityCheck", "FALSE");
	}

	opti.addElement(props);
    }

    /**
     * Adds the Nodes to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addNodes(IOptimization opti) {

	// Define the OpeningHours
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(400);

	int importance = 1;

	// Define some Nodes
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
     * Adds the Resources to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addResources(IOptimization opti) {

	// Define the WorkingHours
	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Define the Resource
	IResource jack = new CapacityResource("Jack from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	opti.addElement(jack);
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
