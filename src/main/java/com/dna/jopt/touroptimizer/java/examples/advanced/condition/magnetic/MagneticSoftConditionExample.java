package com.dna.jopt.touroptimizer.java.examples.advanced.condition.magnetic;

/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2026 DNA Evolutions GmbH
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.condition.IConstraint;
import com.dna.jopt.member.unit.condition.node.MagnetoNodeConstraint;
import com.dna.jopt.member.unit.condition.node.MagnetoNodeConstraint.NodeOrder;
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
 * Demonstrates the {@link MagnetoNodeConstraint} ("magnetic" node constraint) in <b>soft</b> mode.
 * <p>
 * A magnetic constraint is attached to a <i>single</i> node and influences the optimizer by adding a
 * <b>preference</b> (soft constraint) regarding which other nodes should (attraction) or should not
 * (repulsion) appear in the <b>same route</b>.
 * </p>
 *
 * <h2>Two modes</h2>
 * <ul>
 *   <li><b>Attraction</b>: the constrained node prefers certain target nodes to be on the same route.</li>
 *   <li><b>Repulsion</b>: the constrained node prefers certain target nodes to be on a different route.</li>
 * </ul>
 *
 * <h2>Order control (attraction mode)</h2>
 * Optionally, you can specify where the constrained node should appear within the route:
 * <ul>
 *   <li>{@link NodeOrder#FRONT}: prefer to be near the start of the route</li>
 *   <li>{@link NodeOrder#BACK}: prefer to be near the end of the route</li>
 *   <li>{@link NodeOrder#NO_ORDER}: no ordering preference</li>
 * </ul>
 *
 * <h2>Important</h2>
 * {@code MagnetoNodeConstraint} is <b>soft-only</b> in JOpt.TourOptimizer:
 * setting {@code isHard=true} is not allowed and will be rejected.
 *
 * <p>
 * This example creates a small Germany-based dataset and adds:
 * </p>
 * <ul>
 *   <li>An <b>attracting</b> magnet on node "Stuttgart" that prefers node "Aachen" to be on the same route,
 *       with Stuttgart preferred at the <b>front</b> of that route.</li>
 *   <li>A <b>repulsive</b> magnet on node "Koeln" that prefers nodes "Wuppertal" and "Essen" to be on a different route.</li>
 * </ul>
 *
 * @author DNA
 * @since Feb 16, 2026
 */
public class MagneticSoftConditionExample extends Optimization {
    /**
     * Runs the example.
     *
     * @param args ignored
     */

    public static void main(String[] args)
	    throws InvalidLicenceException, InterruptedException, ExecutionException, IOException, TimeoutException {
	new MagneticSoftConditionExample().example();
    }

    @Override
    public String toString() {
	return "MagnetoNodeConstraint: attraction (with optional order) + repulsion — soft constraint only";
    }
    /**
     * Builds a small optimization instance, starts the run asynchronously and subscribes to
     * progress/warning/error events.
     * <p>
     * Note: we block on the result future to keep the JVM alive until the optimization finishes.
     * </p>
     */

    public void example()
	    throws InvalidLicenceException, InterruptedException, ExecutionException, IOException, TimeoutException {

	// Set license via helper
	ExampleLicenseHelper.setLicense(this);

	this.addNodes();
	this.addResources();

	// Start the Optimization
	CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

	// Subscribe to events
	subscribeToEvents(this);

	// It is important to block the call, otherwise the optimization will be
	// terminated
	IOptimizationResult result = resultFuture.get(1, TimeUnit.MINUTES);
	
	
	  IEntityExporter kmlExporter = new EntityKMLExporter();
	    kmlExporter.setTitle("TEST EXPORT");

	    try {

	      kmlExporter.export(
		      result.getContainer(),
	          new FileOutputStream(new File("./TempWithCondition.kml")));

	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    }
    }
    /**
     * Creates a small set of working hours for demonstration purposes (two days).
     * In real projects, these typically come from your workforce/vehicle master data.
     *
     * @return default working hours
     */

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
    /**
     * Adds two example resources with identical starting positions and working hour windows.
     * Costs are kept simple to focus on the magnetic constraint behavior.
     */

    private void addResources() {

	Duration maxWorkingTimeJack = Duration.ofHours(10);
	Duration maxWorkingTimeJohn = Duration.ofHours(10);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	IResource rep1 = new CapacityResource("Jack", 50.775346, 6.083887, maxWorkingTimeJack, maxDistanceKmW,
		getDefaultWorkingHours());
	rep1.setCost(0, 1, 1);

	this.addElement(rep1);

	// Note, "John" does not have the "plumbing" Qualification
	IResource rep2 = new CapacityResource("John", 50.775346, 6.083887, maxWorkingTimeJohn, maxDistanceKmW,
		getDefaultWorkingHours());
	rep2.setCost(0, 1, 1);
	this.addElement(rep2);
    }
    /**
     * Adds time-window nodes (cities) and attaches two {@link MagnetoNodeConstraint}s:
     * <ul>
     *   <li>Attraction: Stuttgart prefers Aachen on the same route, with Stuttgart near the route start.</li>
     *   <li>Repulsion: Koeln prefers Wuppertal and Essen to be on different routes.</li>
     * </ul>
     */

    private void addNodes() {

	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours.add(
		new OpeningHours(ZonedDateTime.of(2030, MARCH.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MARCH.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	weeklyOpeningHours.add(
		new OpeningHours(ZonedDateTime.of(2030, MARCH.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MARCH.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(60);

	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);
	this.addElement(koeln);

	INode oberhausen = new TimeWindowGeoNode("Oberhausen", 51.4667, 6.85, weeklyOpeningHours, visitDuration, 1);
	this.addElement(oberhausen);

	INode essen = new TimeWindowGeoNode("Essen", 51.45, 7.01667, weeklyOpeningHours, visitDuration, 1);
	this.addElement(essen);

	INode dueren = new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weeklyOpeningHours, visitDuration, 1);
	this.addElement(dueren);

	INode heilbronn = new TimeWindowGeoNode("Heilbronn", 49.1403, 9.22, weeklyOpeningHours, visitDuration, 1);
	this.addElement(heilbronn);

	INode stuttgart = new TimeWindowGeoNode("Stuttgart", 48.7667, 9.18333, weeklyOpeningHours, visitDuration, 1);
	this.addElement(stuttgart);

	INode wuppertal = new TimeWindowGeoNode("Wuppertal", 51.2667, 7.18333, weeklyOpeningHours, visitDuration, 1);
	this.addElement(wuppertal);

	INode aachen = new TimeWindowGeoNode("Aachen", 50.775346, 6.083887, weeklyOpeningHours, visitDuration, 1);
	this.addElement(aachen);

	/*
	 * Attraction magnet (soft): keep specific nodes together (same route)
	 */

	// The node stuttgart is attracting aachen to be in the same route.
	IConstraint magnetConstraint = new MagnetoNodeConstraint();
	magnetConstraint.setIsHard(false); // MagnetoNodeConstraint is soft-only (hard not allowed)
	((MagnetoNodeConstraint) magnetConstraint).setIsAttractingMagnet(true);
	((MagnetoNodeConstraint) magnetConstraint).addNodeMagnetId("Aachen");

	// Optional ordering preference: Stuttgart should be near the start of its route
	/*
	 * NodeOrder options: FRONT, BACK, NO_ORDER
	 */
	((MagnetoNodeConstraint) magnetConstraint).setOrder(NodeOrder.FRONT);

	// Attach the magnet constraint to Stuttgart
	stuttgart.addConstraint(magnetConstraint);

	/*
	 * Repulsion magnet (soft): keep specific nodes apart (different routes)
	 */
	// Koeln prefers Wuppertal and Essen to NOT be on the same route (repulsion)
	IConstraint repulsiveMagnetConstraint = new MagnetoNodeConstraint();
	repulsiveMagnetConstraint.setIsHard(false); // MagnetoNodeConstraint is soft-only (hard not allowed)
	((MagnetoNodeConstraint) repulsiveMagnetConstraint).setIsAttractingMagnet(false);
	((MagnetoNodeConstraint) repulsiveMagnetConstraint).addNodeMagnetId("Wuppertal");
	((MagnetoNodeConstraint) repulsiveMagnetConstraint).addNodeMagnetId("Essen");

	// Attach the magnet constraint to Stuttgart
	koeln.addConstraint(repulsiveMagnetConstraint);

    }
    /**
     * Subscribes to the reactive optimization event streams (progress, warnings, errors, status) and
     * prints them to {@link System#out}.
     *
     * @param opti the optimization instance
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