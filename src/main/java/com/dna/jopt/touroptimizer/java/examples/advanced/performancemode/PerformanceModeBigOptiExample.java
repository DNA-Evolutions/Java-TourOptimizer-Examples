package com.dna.jopt.touroptimizer.java.examples.advanced.performancemode;

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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.dna.jopt.config.types.Position;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.IConstraint;
import com.dna.jopt.member.unit.condition.IQualification;
import com.dna.jopt.member.unit.condition.type.TypeConstraint;
import com.dna.jopt.member.unit.condition.type.TypeQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;
import com.google.common.collect.ImmutableList;

/**
 * The Class PerformanceModeExample.
 * 
 * Visit <a
 * href="https://docs.dna-evolutions.com/overview_docs/performancemode/performance_mode.html">https://docs.dna-evolutions.com/overview_docs/performancemode/performance_mode.html</a>
 * for a detailed explanation.
 */
public class PerformanceModeBigOptiExample extends Optimization {

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
	new PerformanceModeBigOptiExample().example();
    }

    public String toString() {
	return "Illustrating the performance mode. This example can't  be run in free license mode.";
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
	// in case you have a valid license.

	// Performance mode gives an average boost of 50% but may miss some ideal
	// solutions
	boolean usePerformanceMode = true;
	
	
	//
	ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// Adding properties
	PerformanceModeBigOptiExample.addProperties(this, usePerformanceMode);

	// Adding Elements
	PerformanceModeBigOptiExample.addElements(this);

	// Attaching to Observables
	PerformanceModeBigOptiExample.attachToObservables(this);

	// Starting the Optimization completable Future and presenting the results
	PerformanceModeBigOptiExample.runPresentAndExportToKml(this);

    }

    public static void addElements(IOptimization opti) {

	/*
	 * MODIFY ME
	 * 
	 */

	// Define center positions
	Position koelnCenterPosRess = Position.of(50.9333, 6.85);
	Position koelnCenterPosNodes = Position.of(50.9333, 6.85);

	// If true, node visitations can happen on different days. The result will look
	// less geographical optimized
	boolean addEventOddSplitting = false;

	boolean addSkillConstraints = false;

	// 1/10 coordinate difference shifts by roughly 12km = 10min driving
	int numRes = 40;
	double resSpacing = 0.01;

	int numNodes = 2000;
	double nodeSpacing = 0.002;

	//
	int numConstrainedNodes = numNodes / 2;
	int numSkillProvidingRess = numRes / 3;

	Duration visitDuration = Duration.ofMinutes(5);

	/*
	 * Nodes
	 */

	List<INode> nodes = getNodes(koelnCenterPosNodes, visitDuration, numNodes, nodeSpacing, addEventOddSplitting);

	/*
	 * Resources
	 */

	List<IResource> ress = getResources(koelnCenterPosRess, numRes, resSpacing, addEventOddSplitting);

	// Add skills
	if (addSkillConstraints) {
	    
	    // We use a seed to get deterministic results - disable to get random assignments
	    long seed = 123456789L;
	    
	    List<INode> nodesWithSkills = generateUniqueNumbers(numConstrainedNodes, numNodes, seed).stream()
		    .map(nodes::get).toList();
	    
	    List<IResource> ressWithQualis = generateUniqueNumbers(numSkillProvidingRess, numRes, seed).stream()
		    .map(ress::get).toList();

	    nodesWithSkills.forEach(n -> {
		IConstraint typeConstraint = new TypeConstraint();
		((TypeConstraint) typeConstraint).addType("efficient");
		n.addConstraint(typeConstraint);
	    });

	    ressWithQualis.forEach(r -> {
		IQualification typeQualification = new TypeQualification();
		((TypeQualification) typeQualification).addType("efficient");
		r.addQualification(typeQualification);
	    });

	}

	/*
	 * Add to opti
	 */

	ress.stream().forEach(opti::addElement);
	nodes.stream().forEach(opti::addElement);
    }

    public static List<Integer> generateUniqueNumbers(int numNumbers, int maxInclusive, long seed) {

	Set<Integer> uniqueNumbers = new HashSet<>();
	Random random = new Random(seed);

	while (uniqueNumbers.size() < numNumbers) {
	    uniqueNumbers.add(random.nextInt(maxInclusive + 1));
	}

	return uniqueNumbers.stream().sorted().toList();
    }

    /**
     * Adds the Properties to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addProperties(IOptimization opti, boolean usePerformanceMode) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	props.setProperty("JOpt.PerformanceMode", "" + usePerformanceMode);

	props.setProperty("JOpt.Clustering.ShowDebugOutput", "true");

	opti.addElement(props);
    }

    /*
     * 
     * 
     */

    public static List<IResource> getResources(Position centerPos, int numRes, double resSpacing,
	    boolean addEventOddSplitting) {

	// Create other positions
	// 1/10 coordinate difference shifts by roughly 12km = 10min driving

	List<Position> poss = samplePhyllotaxis(centerPos, numRes, resSpacing);

	Duration maxWorkingTime = Duration.ofHours(12);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2200.0, KILO(METRE));

	// Create the Resources and return
	return IntStream.range(0, poss.size()).mapToObj(ii -> {

	    boolean addDay = ii % 2 == 0;

	    IResource rep = new CapacityResource("R_" + ii, poss.get(ii).latitude(), poss.get(ii).longitude(),
		    maxWorkingTime, maxDistanceKmW, getWorkingHours(addDay, addEventOddSplitting));
	    rep.setCost(0, 1, 1);

	    return rep;
	}).collect(Collectors.toList());
    }

    public static List<INode> getNodes(Position centerPos, Duration visitDuration, int numNodes, double nodeSapcing,
	    boolean addEventOddSplitting) {

	// Create other positions
	// 1/10 coordinate difference shifts by roughly 12km = 10min driving

	List<Position> poss = samplePhyllotaxis(centerPos, numNodes, nodeSapcing);

	// Create the nodes and return
	return IntStream.range(0, poss.size()).mapToObj(ii -> {
	    List<IOpeningHours> weeklyOpeningHoursOne = new ArrayList<>();

	    boolean addDay = ii % 2 == 0;

	    weeklyOpeningHoursOne.add(new OpeningHours(
		    ZonedDateTime.of(2023, MAY.getValue(), 6 + (addDay ? (addEventOddSplitting ? 1 : 0) : 0), 8, 0, 0,
			    0, ZoneId.of("Europe/Berlin")),
		    ZonedDateTime.of(2023, MAY.getValue(), 6 + (addDay ? (addEventOddSplitting ? 1 : 0) : 0), 22, 0, 0,
			    0, ZoneId.of("Europe/Berlin"))));

	    return new TimeWindowGeoNode("N_" + ii, poss.get(ii).latitude(), poss.get(ii).longitude(),
		    weeklyOpeningHoursOne, visitDuration, 1);
	}).collect(Collectors.toList());
    }

    public static List<IWorkingHours> getWorkingHours(boolean addDay, boolean addEventOddSplitting) {

	List<IWorkingHours> workingHoursOne = new ArrayList<>();
	workingHoursOne.add(new WorkingHours(
		ZonedDateTime.of(2023, MAY.getValue(), 6 + (addDay ? (addEventOddSplitting ? 1 : 0) : 0), 8, 0, 0, 0,
			ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2023, MAY.getValue(), 6 + (addDay ? (addEventOddSplitting ? 1 : 0) : 0), 22, 0, 0, 0,
			ZoneId.of("Europe/Berlin"))));

	return workingHoursOne;
    }

    /*
     * 
     * 
     */

    public static List<Position> samplePhyllotaxis(Position centerPos, int n, double spacing) {

	return samplePhyllotaxis(centerPos.latitude(), centerPos.longitude(), n, spacing);
    }

    public static List<Position> samplePhyllotaxis(double centerLat, double centerLon, int n, double spacing) {
	double theta = Math.PI * (3 - Math.sqrt(5));

	return IntStream.range(0, n).mapToObj(i -> {
	    double radius = spacing * Math.sqrt(i);
	    double angle = i * theta;
	    double x = radius * Math.cos(angle);
	    double y = radius * Math.sin(angle);

	    return Position.of(centerLat + y, centerLon + x);
	}).collect(ImmutableList.toImmutableList());
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
    private static void runPresentAndExportToKml(IOptimization opti)
	    throws InvalidLicenceException, InterruptedException, ExecutionException, TimeoutException {

	// Extracting a completable Future for the optimization result
	CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

	// It is important to block the call, otherwise the Optimization will be
	// terminated
	IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);

	// Presenting the result
	System.out.println(result);

	try {
	    String kmlFile = "" + PerformanceModeBigOptiExample.class.getSimpleName() + ".kml";

	    IEntityExporter exporter = new EntityKMLExporter();
	    exporter.export(result.getContainer(), new FileOutputStream(kmlFile));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
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
