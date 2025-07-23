package com.dna.jopt.touroptimizer.java.examples.expert.connectionstore;
/*-
 * #%L
 * JOpt TourOptimizer Examples
 * %%
 * Copyright (C) 2017 - 2023 DNA Evolutions GmbH
 * %%
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt',
 * which is part of this source code package.
 * 
 * If not, see <https://www.dna-evolutions.com/agb-conditions-and-terms/>.
 * #L%
 */

import static tech.units.indriya.unit.Units.METRE;
import static java.time.Month.FEBRUARY;
import static javax.measure.MetricPrefix.KILO;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dna.jopt.config.convert.ConvertException;
import com.dna.jopt.config.convert.ExportTarget;
import com.dna.jopt.config.convert.OptimizationConfiguration;
import com.dna.jopt.config.serialize.ConfigSerialization;
import com.dna.jopt.config.serialize.SerializationException;
import com.dna.jopt.config.types.CoreConfig;
import com.dna.jopt.config.types.OptimizationConfig;
import com.dna.jopt.config.types.ext.CoreExtensionManifest;
import com.dna.jopt.framework.body.IOptimization;
import com.dna.jopt.framework.body.Optimization;
import com.dna.jopt.framework.exception.caught.InvalidLicenceException;
import com.dna.jopt.framework.outcomewrapper.IOptimizationResult;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.nodeedge.ConnectionBucket;
import com.dna.jopt.member.unit.nodeedge.INodeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.INodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnector;
import com.dna.jopt.member.unit.nodeedge.NodeEdgeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.TimedConnectionData;
import com.dna.jopt.member.unit.nodeedge.TimedNodeConnectorItem;
import com.dna.jopt.member.unit.nodeedge.time.DayDef;
import com.dna.jopt.member.unit.nodeedge.time.RangeDef;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;


/**
 * Demonstrates how to use a time-dependent connection store in JOpt.
 * 
 * <p>This example shows how to model driving time and distance as a function of 
 * the time of day and day of the week. For instance, driving between two nodes 
 * during weekday rush hours will take longer compared to night or weekend times.
 * 
 * <p>Each node connection can have a {@link ConnectionBucket} defining time-dependent 
 * {@link TimedConnectionData}, allowing fine-grained modeling of realistic traffic scenarios.
 * 
 * <p>In this example, we define different traffic profiles for weekdays and weekends 
 * and attach them to node connections. This enables the optimizer to consider varying 
 * travel times during different parts of the day.
 * 
 * <p>Run the example to observe how routing decisions adapt to traffic conditions over time.
 * 
 * @author jrich
 * @version Jul 22, 2025
 * @since Jul 22, 2025
 */
public class ConnectionStoreExample extends Optimization {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TimeoutException the timeout exception
     * @throws ConvertException the convert exception
     * @throws SerializationException the serialization exception
     */
    public static void main(String[] args)
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException, ConvertException, SerializationException {
	new ConnectionStoreExample().example();
    }

    /**
     * Returns a short description of the example.
     *
     * @return Description of the time-dependent connection store example.
     */
    public String toString() {
	    return "Demonstrates how to apply a time-dependent connection store to node connections, "
		         + "making driving time and distance dynamic based on the time of day. "
		         + "Useful for modeling rush hour effects and weekend traffic patterns.";
    }

    /**
     * Sets up and executes the optimization, including:
     * <ul>
     *   <li>Setting properties</li>
     *   <li>Adding nodes and resources</li>
     *   <li>Defining time-dependent connections</li>
     *   <li>Subscribing to event observables</li>
     *   <li>Running the optimization and displaying results</li>
     * </ul>
     *
     * @throws InterruptedException If the process is interrupted.
     * @throws ExecutionException If execution of the optimization fails.
     * @throws InvalidLicenceException If the license is invalid.
     * @throws IOException If I/O operations fail.
     * @throws TimeoutException If execution exceeds allowed time.
     * @throws ConvertException If configuration export fails.
     * @throws SerializationException If serialization fails.
     */
    public void example()
	    throws InterruptedException, ExecutionException, InvalidLicenceException, IOException, TimeoutException, ConvertException, SerializationException {

	// We use the free mode for the example - please modify the ExampleLicenseHelper
	// in case you
	// have a valid
	// license.

	// ExampleLicenseHelper.setLicense(this);

	// Setting Properties, adding all Elements, and attaching to Observables
	// (1) Adding properties
	ConnectionStoreExample.addProperties(this);

	// (2) Adding Nodes, Resources and connection store
	ConnectionStoreExample.addNodesAndResourcesWithConnectionStore(this);

	// (3) Attaching to Observables
	ConnectionStoreExample.attachToObservables(this);

	// (4) Starting the Optimization completable Future and presenting the results
	ConnectionStoreExample.startAndPresentResult(this);

	// (5) Show the JSON representation of the result
	ConnectionStoreExample.presentJsonResult(this);
    }

    /**
     * Start the Optimization and present the result.
     *
     * @param opti the optimization instance
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException    the interrupted exception
     * @throws ExecutionException      the execution exception
     * @throws TimeoutException the timeout exception
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
     * Present json result.
     *
     * @param opti the opti
     * @throws InvalidLicenceException the invalid licence exception
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws TimeoutException the timeout exception
     * @throws ConvertException the convert exception
     * @throws SerializationException the serialization exception
     */
    private static void presentJsonResult(IOptimization opti) throws InvalidLicenceException, InterruptedException,
	    ExecutionException, TimeoutException, ConvertException, SerializationException {

	OptimizationConfig<CoreConfig> exportedConfig = OptimizationConfiguration.exportConfig(ExportTarget.of(opti),
		new CoreExtensionManifest());

	// Strip some info
	exportedConfig = exportedConfig.withCoreBuildOptions(Optional.empty()).withSolution(Optional.empty());

	// Without pretty directly call:
	// String serializedExportedConfig =
	// ConfigSerialization.serialize(exportedConfig);

	String serializedExportedConfig = prettySerialize(exportedConfig);

	System.out.println(serializedExportedConfig);

    }

    /**
     * Adds the Properties to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addProperties(IOptimization opti) {

	Properties props = new Properties();

	props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "100000");
	props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumRepetions", "1");
	props.setProperty("JOpt.NumCPUCores", "4");

	opti.addElement(props);
    }

    /**
     * Adds the Nodes to the Optimization.
     *
     * @param opti the optimization instance
     */
    private static void addNodesAndResourcesWithConnectionStore(IOptimization opti) {

	List<IOptimizationElement> els = new ArrayList<>();

	/*
	 * 
	 * 
	 * RESOURCE
	 * 
	 * 
	 */

	// Define the WorkingHours
	List<IWorkingHours> workingHours = new ArrayList<>();
	workingHours.add(new WorkingHours(
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")), // Friday
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	workingHours.add(new WorkingHours(
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")), // Sunday
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration maxWorkingTime = Duration.ofHours(9);
	Quantity<Length> maxDistanceKmW = Quantities.getQuantity(1200.0, KILO(METRE));

	// Define the Resource
	IResource jack = new CapacityResource("Jessi from Aachen", 50.775346, 6.083887, maxWorkingTime, maxDistanceKmW,
		workingHours);
	els.add(jack);

	/*
	 * 
	 * 
	 * NODES
	 * 
	 * 
	 */

	// Define the OpeningHours
	List<IOpeningHours> week = new ArrayList<>();
	week.add(new OpeningHours(
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	List<IOpeningHours> weekend = new ArrayList<>();
	weekend.add(new OpeningHours(
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	List<IOpeningHours> weekweekend = new ArrayList<>();
	weekweekend.add(new OpeningHours(
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 19, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	weekweekend.add(new OpeningHours(
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
		ZonedDateTime.of(2100, FEBRUARY.getValue(), 21, 17, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

	Duration visitDuration = Duration.ofMinutes(60);

	int importance = 1;

	// Define some Nodes

	INode koeln = new TimeWindowGeoNode("Koeln", 50.9333, 6.95, weekweekend, visitDuration, importance);
	els.add(koeln);

	INode koelnWeek = new TimeWindowGeoNode("KoelnWeek", 50.9333, 6.95, week, visitDuration, importance);
	els.add(koelnWeek);

	INode koelnWeekend = new TimeWindowGeoNode("KoelnWeekend", 50.9333, 6.95, weekend, visitDuration, importance);
	els.add(koelnWeekend);

	INode essen = new TimeWindowGeoNode("Essen", 51.45, 7.01667, weekweekend, visitDuration, importance);
	els.add(essen);

	INode dueren = new TimeWindowGeoNode("Dueren", 50.8, 6.48333, weekweekend, visitDuration, importance);
	els.add(dueren);

	// Add to opti
	opti.addNodes(els.stream().filter(e -> e instanceof INode).map(e -> (INode) e).toList());
	opti.addResources(els.stream().filter(e -> e instanceof IResource).map(e -> (IResource) e).toList());

	// Create time dependent connections

	INodeEdgeConnector connector = new NodeEdgeConnector();
	connector.putNodeConnections(createDummyConnections(els));

	opti.setNodeConnector(connector);

    }

    /**
     * Creates the dummy connections.
     *
     * @param els the els
     * @return the list
     */
    private static List<INodeConnectorItem> createDummyConnections(List<IOptimizationElement> els) {

	double avgResourceSpeedMetersPerHour = 22.0;

	List<INodeConnectorItem> cons = new ArrayList<>();

	for (IOptimizationElement from : els) {

	    for (IOptimizationElement to : els) {

		if (from != to) {

		    double distanceMetre = NodeEdgeConnector.distancePlacePlaceFlatEarth(from.getLongitude(),
			    from.getLatitude(), to.getLongitude(), to.getLatitude());

		    INodeConnectorItem connection = new NodeEdgeConnectorItem();
		    connection.setFromOptimizationElement(from);
		    connection.setToOptimizationElement(to);
		    connection.setDistance(Quantities.getQuantity(distanceMetre, Units.METRE));
		    connection.setDrivingTime(element2ElementDuration(distanceMetre, avgResourceSpeedMetersPerHour));

		    cons.add(connection);

		    if (distanceMetre > 0.0) {
			TimedNodeConnectorItem.setTimedBucketData(connection, createConnectionBucket(connection));
		    }

		}
	    }
	}

	return cons;

    }

    /**
     * Creates the connection bucket.
     *
     * @param connection the connection
     * @return the list
     */
    public static List<ConnectionBucket> createConnectionBucket(INodeConnectorItem connection) {

	ConnectionBucket weekendBucket = new ConnectionBucket(
		RangeDef.of(DayDef.of(DayOfWeek.SATURDAY), DayDef.of(DayOfWeek.SUNDAY)),
		createTimedWeekendConnections(connection));

	ConnectionBucket weekBucket = new ConnectionBucket(
		RangeDef.of(DayDef.of(DayOfWeek.MONDAY), DayDef.of(DayOfWeek.FRIDAY)),
		createTimedWeekConnections(connection));

	return List.of(weekendBucket, weekBucket);

    }

    /*
     * 
     */

    /**
     * Creates the timed week connections.
     *
     * @param connection the connection
     * @return the list
     */
    private static List<TimedConnectionData> createTimedWeekConnections(INodeConnectorItem connection) {
	return createTimedWeekdayConnections(Quantities.getQuantity(connection.getDistanceMeter(), Units.METRE),
		Duration.ofMillis(connection.getBaseTimeMillis()));
    }

    /**
     * Creates the timed weekend connections.
     *
     * @param connection the connection
     * @return the list
     */
    private static List<TimedConnectionData> createTimedWeekendConnections(INodeConnectorItem connection) {
	return createTimedWeekendConnections(Quantities.getQuantity(connection.getDistanceMeter(), Units.METRE),
		Duration.ofMillis(connection.getBaseTimeMillis()));
    }

    /*
     * 
     */
    
    /**
     * Creates a list of {@link TimedConnectionData} objects for weekday traffic conditions (Monday to Friday).
     * 
     * <p>This method defines time-dependent connection data for typical weekday traffic patterns,
     * including morning and evening rush hours. Each {@link LocalTime} in the list represents a
     * time grid point where specific travel conditions (via multipliers) apply. Between these grid points,
     * driving times are linearly interpolated.
     * 
     * <p>Example profile:
     * <ul>
     *   <li><b>6:00</b> — Normal traffic (multiplier 1.0)</li>
     *   <li><b>7:00–9:00</b> — Morning rush hour (multiplier 1.8)</li>
     *   <li><b>13:00</b> — Normal midday traffic (multiplier 1.0)</li>
     *   <li><b>16:00–19:00</b> — Evening rush hour (multiplier 1.6)</li>
     *   <li><b>20:00</b> — Night traffic (multiplier 1.0)</li>
     * </ul>
     * 
     * @param distance The base distance between two nodes.
     * @param driving  The base (unadjusted) driving duration.
     * @return A list of {@code TimedConnectionData} defining weekday traffic variations.
     */
    private static List<TimedConnectionData> createTimedWeekdayConnections(Quantity<Length> distance, Duration driving) {

	// Monday-Friday
	
	double normalMultiplier = 1.0;
	double rushHourMorningMultiplier = 1.8;
	double rushHourEveningMultiplier = 1.6;

	LocalTime normalMorning = LocalTime.of(6, 0);
	LocalTime rushHourMorningStart = LocalTime.of(7, 0);
	LocalTime rushHourMorningEnd = LocalTime.of(9, 0);
	LocalTime normalNoon = LocalTime.of(13, 0);
	LocalTime rushHourEveningStart = LocalTime.of(16, 0);
	LocalTime rushHourEveningEnd = LocalTime.of(19, 0);
	LocalTime normalNight = LocalTime.of(20, 0);

	return List.of(createTimedConnection(normalMorning, distance, driving, normalMultiplier),
		createTimedConnection(rushHourMorningStart, distance, driving, rushHourMorningMultiplier),
		createTimedConnection(rushHourMorningEnd, distance, driving, rushHourMorningMultiplier),
		createTimedConnection(normalNoon, distance, driving, normalMultiplier),
		createTimedConnection(rushHourEveningStart, distance, driving, rushHourEveningMultiplier),
		createTimedConnection(rushHourEveningEnd, distance, driving, rushHourEveningMultiplier),
		createTimedConnection(normalNight, distance, driving, normalMultiplier));

    }

    /**
     * Creates the timed weekend connections.
     *
     * @param distance the distance
     * @param driving the driving
     * @return the list
     */
    private static List<TimedConnectionData> createTimedWeekendConnections(Quantity<Length> distance,
	    Duration driving) {

	double normalMultiplier = 1.0;
	double rushHourMorningMultiplier = 1.05;
	double rushHourEveningMultiplier = 1.05;

	LocalTime normalMorning = LocalTime.of(6, 0);
	LocalTime rushHourMorningStart = LocalTime.of(7, 0);
	LocalTime rushHourMorningEnd = LocalTime.of(9, 0);
	LocalTime normalNoon = LocalTime.of(13, 0);
	LocalTime rushHourEveningStart = LocalTime.of(16, 0);
	LocalTime rushHourEveningEnd = LocalTime.of(19, 0);
	LocalTime normalNight = LocalTime.of(20, 0);

	return List.of(createTimedConnection(normalMorning, distance, driving, normalMultiplier),
		createTimedConnection(rushHourMorningStart, distance, driving, rushHourMorningMultiplier),
		createTimedConnection(rushHourMorningEnd, distance, driving, rushHourMorningMultiplier),
		createTimedConnection(normalNoon, distance, driving, normalMultiplier),
		createTimedConnection(rushHourEveningStart, distance, driving, rushHourEveningMultiplier),
		createTimedConnection(rushHourEveningEnd, distance, driving, rushHourEveningMultiplier),
		createTimedConnection(normalNight, distance, driving, normalMultiplier));

    }

   

    /**
     * Element 2 element duration.
     *
     * @param distanceMeter the distance meter
     * @param speedMeterPerSecond the speed meter per second
     * @return the duration
     */
    public static Duration element2ElementDuration(double distanceMeter, double speedMeterPerSecond) {

	long traveltime = (long) (distanceMeter / speedMeterPerSecond * 1000L);

	return Duration.ofMillis(traveltime);
    }

    /**
     * Creates the timed connection.
     *
     * @param time the time
     * @param distance the distance
     * @param driving the driving
     * @param multiplier the multiplier
     * @return the timed connection data
     */
    private static TimedConnectionData createTimedConnection(LocalTime time, Quantity<Length> distance,
	    Duration driving, double multiplier) {

	return new TimedConnectionData(time, distance, Duration.ofSeconds((long) (driving.getSeconds() * multiplier))

	);
    }

    /*
     * 
     * 
     */

    /**
     * Pretty serialize.
     *
     * @param exportedConfig the exported config
     * @return the string
     * @throws SerializationException the serialization exception
     */
    public static String prettySerialize(OptimizationConfig<CoreConfig> exportedConfig) throws SerializationException {

	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	try {
	    ConfigSerialization.objectMapper().writerWithDefaultPrettyPrinter().writeValue(outStream, exportedConfig);
	} catch (IOException e) {
	    throw new SerializationException("Cannot serialize Config: " + e.getMessage());
	}

	return new String(outStream.toByteArray(), StandardCharsets.UTF_8);
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
