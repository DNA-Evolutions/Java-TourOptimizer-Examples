package com.dna.jopt.touroptimizer.java.examples.advanced.constraintaliasid;

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
import com.dna.jopt.member.unit.condition.resource.IConstraintResource;
import com.dna.jopt.member.unit.condition.resource.MandatoryResourceConstraint;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

public class ResourceConstraintAliasIdExample extends Optimization {

    /**
     * Setting constraint alias ids for resources. In this example we use the feature to define team members.
     * We have different nodes that need to be visited (mandatorily) by a member of a certain team.
     * 
     * We have two teams with a total of four team members:
     * ===
     * Jack => Team Aachen
     * Peter => Team Aachen
     * 
     * Carla => Team Heilbronn
     * Jessi => Team Heilbronn
     * 
     * We want each node to be visited either by a member of "Team Aachen" or "Team Heilbronn". 
     * 
     * We can achieve this by:
     * ===
     * 
     * 1) Setting ConstraintAliasId on Resource level to be either "Team Aachen" or "Team Heilbronn"
     * 2) Adding a mandatory constraint with Resource "Team Aachen" or "Team Heilbronn" to the nodes
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
	new ResourceConstraintAliasIdExample().example();
    }

    public String toString() {
	return "Setting constraint alias ids for resources. In this example we use the feature to define team members.";
    }

    public void example()
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException {

	ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// (1) Adding properties
	ResourceConstraintAliasIdExample.addProperties(this);

	// (2) Adding Nodes and constraints
	ResourceConstraintAliasIdExample.addNodesAndAddConstraints(this);

	// (3) Adding Resources
	ResourceConstraintAliasIdExample.addResources(this);

	// (4) Attaching to Observables
	ResourceConstraintAliasIdExample.attachToObservables(this);

	// (5) Starting the Optimization completable Future and presenting the results
	ResourceConstraintAliasIdExample.startAndPresentResult(this);
    }

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

    private static void addProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	opti.addElement(props);
    }

    private static void addNodesAndAddConstraints(IOptimization opti) {

	// Define the OpeningHours
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(20);

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

	IConstraintResource aachenResMandConstraint = new MandatoryResourceConstraint();
	aachenResMandConstraint.addResource("Team Aachen", 10);
	koeln.addConstraint(aachenResMandConstraint);
	heilbronn.addConstraint(aachenResMandConstraint);
	wuppertal.addConstraint(aachenResMandConstraint);
	aachen.addConstraint(aachenResMandConstraint);

	IConstraintResource heilbronmResMandConstraint = new MandatoryResourceConstraint();
	heilbronmResMandConstraint.addResource("Team Heilbronn", 10);
	essen.addConstraint(heilbronmResMandConstraint);
	dueren.addConstraint(heilbronmResMandConstraint);
	nuernberg.addConstraint(heilbronmResMandConstraint);
    }

    private static void addResources(IOptimization opti) {

	// Define the WorkingHours
	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours
		.add(new WorkingHours(ZonedDateTime.of(2020, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2020, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Define the Resources
	IResource jack = new CapacityResource("Jack", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	jack.setConstraintAliasId("Team Aachen");
	opti.addElement(jack);

	IResource peter = new CapacityResource("Peter", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	peter.setConstraintAliasId("Team Aachen");
	opti.addElement(peter);

	/*
	 * 
	 */

	IResource carla = new CapacityResource("Carla", 49.1403, 9.22, maxWorkingTime, maxDistanceKmW,
		workingHours);
	carla.setConstraintAliasId("Team Heilbronn");
	opti.addElement(carla);

	IResource jessi = new CapacityResource("Jessi", 49.1403, 9.22, maxWorkingTime, maxDistanceKmW,
		workingHours);
	jessi.setConstraintAliasId("Team Heilbronn");
	opti.addElement(jessi);
    }

    /**
     * Attach to different Events (Observables) of the optimization instance.
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
