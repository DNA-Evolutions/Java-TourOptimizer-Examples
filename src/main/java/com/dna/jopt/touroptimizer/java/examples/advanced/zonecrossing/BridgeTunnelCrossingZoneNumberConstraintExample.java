package com.dna.jopt.touroptimizer.java.examples.advanced.zonecrossing;

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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.Month.MAY;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.Position;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.io.exporting.IEntityExporter;
import com.dna.jopt.io.exporting.kml.EntityKMLExporter;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumber;
import com.dna.jopt.member.unit.condition.workinghour.zone.zonenumber.ZoneNumberQualification;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tec.units.ri.quantity.Quantities;

/**
 * Example of zone crossing penalty as extension of the ZoneCode feature.
 * 
 * In many real-world logistics scenarios, crossing between defined zones can be
 * costly, inefficient, or impractical. Bridges, tunnels, and other
 * infrastructural limitations can make frequent crossings undesirable
 * <br><br>
 * Visit
 * <a
 * href="https://docs.dna-evolutions.com/overview_docs/zonecrossing/zonecrossing.html">https://docs.dna-evolutions.com/overview_docs/zonecrossing/zonecrossing.html</a>
 * for a detailed explanation.
 *
 * @author jrich
 * @version Feb 5, 2025
 * @since Feb 5, 2025
 */
public class BridgeTunnelCrossingZoneNumberConstraintExample extends Optimization {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InvalidLicenceException the invalid licence exception
     * @throws ConvertException        the convert exception
     * @throws SerializationException  the serialization exception
     * @throws FileNotFoundException   the file not found exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws TimeoutException
     */
    public static void main(String[] args) throws InvalidLicenceException, ConvertException, SerializationException,
	    InterruptedException, ExecutionException, IOException, TimeoutException {

	new BridgeTunnelCrossingZoneNumberConstraintExample().example();
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
	return "Example of zone crossing penalty as extension of the ZoneCode feature.";
    }

    /**
     * Example.
     *
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws FileNotFoundException   the file not found exception
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws InvalidLicenceException the invalid licence exception
     * @throws ConvertException        the convert exception
     * @throws SerializationException  the serialization exception
     * @throws TimeoutException
     */
    public void example() throws InterruptedException, ExecutionException, FileNotFoundException, IOException,
	    InvalidLicenceException, ConvertException, SerializationException, TimeoutException {

	boolean doPenalizeZoneCrossings = true; // Modify me

	// Set license via helper
	ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// (1) Adding properties
	BridgeTunnelCrossingZoneNumberConstraintExample.addProperties(this, doPenalizeZoneCrossings);

	// (2) Adding Nodes
	BridgeTunnelCrossingZoneNumberConstraintExample.addNodes(this);

	// (3) Adding Resources
	BridgeTunnelCrossingZoneNumberConstraintExample.addResources(this);

	// (4) Attaching to Observables
	BridgeTunnelCrossingZoneNumberConstraintExample.attachToObservables(this);

	// Starting the optimization via Completable Future
	// and presenting the result
	// (5) Starting the optimization and presenting the result
	IOptimizationResult result = BridgeTunnelCrossingZoneNumberConstraintExample.startAndPresentResult(this);

	// Export to kml
	BridgeTunnelCrossingZoneNumberConstraintExample
		.exportToKml("BridgeCrossingZoneNumberConstraintExample-" + doPenalizeZoneCrossings + ".kml", result);

    }

    /**
     * Start the optimization and present the result.
     *
     * @param opti the optimization instance
     * @return
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws TimeoutException
     */
    private static IOptimizationResult startAndPresentResult(IOptimization opti)
	    throws InvalidLicenceException, InterruptedException, ExecutionException, TimeoutException {
	// Extracting a completable future for the optimization result
	CompletableFuture<IOptimizationResult> resultFuture = opti.startRunAsync();

	// It is important to block the call, otherwise optimization will be terminated
	IOptimizationResult result = resultFuture.get(5, TimeUnit.MINUTES);

	System.out.println(result);

	return result;
    }

    /**
     * Sets the properties.
     *
     * @param opti the new properties
     */
    private static void addProperties(IOptimization opti, boolean doPenalizeZoneCrossings) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "2000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	// This property can be used to adjust the cost for violating PostCode
	// conditions when using
	// soft Constraints. 10 is the default value.
	props.setProperty("JOptWeight.ZoneCode", "10.0");
	props.setProperty("JOpt.Clustering.PenlalizeZoneCodeCrossingMultiplier", "10.0");
	props.setProperty("JOpt.Clustering.PenlalizeZoneCodeCrossing", "" + doPenalizeZoneCrossings);

	opti.addElement(props);
    }

    private static void addNodes(IOptimization opti) {

	// Creating/Adding Nodes
	List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2030, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MAY.getValue(), 6, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	weeklyOpeningHours
		.add(new OpeningHours(ZonedDateTime.of(2030, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
			ZonedDateTime.of(2030, MAY.getValue(), 7, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	/*
	 * 
	 * Manhattan
	 * 
	 */

	// See
	// https://docs.dna-evolutions.com/overview_docs/special_features/Special_Features.html#defining-territories-via-zonecodes

	ZoneNumber zoneOne = new ZoneNumber(1); // Manhattan 1
	ZoneNumber zoneTwo = new ZoneNumber(2); // Manhattan 2 - Not necessary just to test multiple zone-codes per node

	ZoneNumberQualification manhattanQuali = new ZoneNumberQualification(zoneOne);
	manhattanQuali.addExtraCode(zoneTwo);

	/*
	 * Jersey City
	 */

	ZoneNumber zoneThree = new ZoneNumber(3); // JerseyCity 1
	ZoneNumber zoneFour = new ZoneNumber(4); // JerseyCity 2 - not necessary just to test multiple zone-codes per
						 // node

	ZoneNumberQualification jerseyQuali = new ZoneNumberQualification(zoneThree);
	jerseyQuali.addExtraCode(zoneFour);

	// Add Resources
	addResources(opti);

	// Add nodes with qualifications
	addManhattanNodes(opti, weeklyOpeningHours, manhattanQuali);
	addJerseyCityNodes(opti, weeklyOpeningHours, jerseyQuali);
    }

    /**
     * Adds the manhattan nodes.
     *
     * @param opti               the opti
     * @param weeklyOpeningHours the weekly opening hours
     * @param zoneNumber         the zone number
     */
    private static void addManhattanNodes(IOptimization opti, List<IOpeningHours> weeklyOpeningHours,
	    ZoneNumberQualification zoneNumber) {

	List<Position> poss = new ArrayList<>();

	poss.add(Position.of(40.764279, -73.988988));
	poss.add(Position.of(40.761822, -73.968600));
	poss.add(Position.of(40.764162, -73.991906));
	poss.add(Position.of(40.723670, -73.998738));
	poss.add(Position.of(40.796056, -73.967102));
	poss.add(Position.of(40.761964, -73.972156));
	poss.add(Position.of(40.737567, -74.009090));
	poss.add(Position.of(40.733846, -74.009090));

	/*
	 * 
	 */

	Duration visitDuration = Duration.ofMinutes(60);

	for (int ii = 0; ii < poss.size(); ii++) {

	    INode curNode = new TimeWindowGeoNode("Manhattan-" + ii, poss.get(ii), weeklyOpeningHours, visitDuration,
		    1);

	    curNode.addQualification(zoneNumber);

	    opti.addElement(curNode);
	}

    }

    /**
     * Adds the jersey city nodes.
     *
     * @param opti               the opti
     * @param weeklyOpeningHours the weekly opening hours
     * @param zoneNumber         the zone number
     */
    private static void addJerseyCityNodes(IOptimization opti, List<IOpeningHours> weeklyOpeningHours,
	    ZoneNumberQualification zoneNumber) {

	List<Position> poss = new ArrayList<>();

	poss.add(Position.of(40.751788, -74.027374));

	poss.add(Position.of(40.725626, -74.037277));

	poss.add(Position.of(40.751106, -74.025960));

	poss.add(Position.of(40.759971, -74.023066));

	poss.add(Position.of(40.746625, -74.026088));

	poss.add(Position.of(40.748330, -74.057274));

	poss.add(Position.of(40.738185, -74.027539));

	poss.add(Position.of(40.740757, -74.026748));

	Duration visitDuration = Duration.ofMinutes(60);

	for (int ii = 0; ii < poss.size(); ii++) {

	    INode curNode = new TimeWindowGeoNode("JerseyCity-" + ii, poss.get(ii), weeklyOpeningHours, visitDuration,
		    1);

	    curNode.addQualification(zoneNumber);

	    opti.addElement(curNode);
	}

    }

    /**
     * Adds the resources.
     *
     * @param opti the opti
     */
    private static void addResources(IOptimization opti) {

	Position pos = Position.of(40.742728, -73.870528);

	/*
	 * Defining WorkingHours and attach Constraints based on the Zones we created
	 * 
	 * For example: the first WorkingHour gets zoneOne and zoneTwo as Constraint.
	 * This means the Resource holding this WorkingHour should only visit ZoneOne
	 * and ZoneTwo during this WorkingHour.
	 */
	IWorkingHours woh1 = new WorkingHours(
		ZonedDateTime.of(2030, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2030, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));

	IWorkingHours woh2 = new WorkingHours(
		ZonedDateTime.of(2030, MAY.getValue(), 7, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2030, MAY.getValue(), 7, 20, 0, 0, 0, ZoneId.of("Europe/Berlin")));

	List<IWorkingHours> weeklyWorkingHours = new ArrayList<>();
	weeklyWorkingHours.add(woh1);
	weeklyWorkingHours.add(woh2);

	/*
	 * 
	 * Creating/Adding the Resource and attach constrained WorkingHours
	 * 
	 */

	Duration maxWorkingTime = Duration.ofHours(13);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	IResource rep1 = new CapacityResource("Jack", pos, maxWorkingTime, maxDistanceKmW, weeklyWorkingHours);
	rep1.setCost(0, 1, 1);
	opti.addElement(rep1);

	IResource rep2 = new CapacityResource("Mel", pos, maxWorkingTime, maxDistanceKmW, weeklyWorkingHours);
	rep2.setCost(0, 1, 1);
	opti.addElement(rep2);

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

    /**
     * Export to kml.
     *
     * @param kmlFile the kml file
     * @param result  the result
     * @throws FileNotFoundException the file not found exception
     */
    static void exportToKml(String kmlFile, IOptimizationResult result) throws FileNotFoundException {

	IEntityExporter exporter = new EntityKMLExporter();
	exporter.export(result.getContainer(), new FileOutputStream(kmlFile));

    }
}
