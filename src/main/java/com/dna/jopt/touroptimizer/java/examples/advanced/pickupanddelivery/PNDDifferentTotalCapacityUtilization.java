package com.dna.jopt.touroptimizer.java.examples.advanced.pickupanddelivery;

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
import java.util.Properties;
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
import com.dna.jopt.member.unit.hours.IOpeningHours;
import com.dna.jopt.member.unit.hours.IWorkingHours;
import com.dna.jopt.member.unit.hours.OpeningHours;
import com.dna.jopt.member.unit.hours.WorkingHours;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.node.geo.TimeWindowGeoNode;
import com.dna.jopt.member.unit.pnd.capacity.ILoadCapacity;
import com.dna.jopt.member.unit.pnd.capacity.simple.SimpleLoadCapacity;
import com.dna.jopt.member.unit.pnd.depot.node.INodeDepot;
import com.dna.jopt.member.unit.pnd.depot.node.simple.SimpleNodeDepot;
import com.dna.jopt.member.unit.pnd.depot.resource.IResourceDepot;
import com.dna.jopt.member.unit.pnd.depot.resource.simple.SimpleResourceDepot;
import com.dna.jopt.member.unit.pnd.load.ILoad;
import com.dna.jopt.member.unit.pnd.load.simple.SimpleLoad;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

import java.util.List;
import static java.time.Month.MARCH;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * In this example we are checking the possibility of two LoadCapacities of which one can only use one part of the
 * total Capacity, while the other one is not restricted and can potentially use all of the total Capacity.
 * We are transporting two types of goods. Pallets of "DelicateFruit" and pallets of "GlassVials". The
 * "DelicateFruit" needs to be stored under special conditions assuring a certain temperature and humidity. The truck
 * has a separate compartment which can ensure that these conditions are met. The "GlassVials" are well-packed and less
 * sensitive. They can be stored in the normal part of the truck, but also in the compartment for the "DelicateFruit".
 *
 * @author Philipp Eichenberger
 * @version Mar 23, 2021
 * @since Mar 16, 2021
 *     <p>Example of pick up and delivery optimization problem.
 */
public class PNDDifferentTotalCapacityUtilization extends Optimization {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws TimeoutException
     */
    public static void main(String[] args)
            throws InvalidLicenceException, IOException, InterruptedException, ExecutionException,
            TimeoutException {
        new PNDDifferentTotalCapacityUtilization().example();
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
        return "Simple example of a pick up and delivery optimization problem.";
    }

    /**
     * Example.
     *
     * @throws InvalidLicenceException the invalid licence exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     * @throws TimeoutException
     */
    public void example()
            throws InvalidLicenceException, IOException, InterruptedException, ExecutionException,
            TimeoutException {

        // Set license via helper
        ExampleLicenseHelper.setLicense(this);

        this.setProperties();
        this.addNodes();
        this.addRes();

        // Start the optimization
        CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();

        // Subscribe to events
        subscribeToEvents(this);

        // It is important to block the call, otherwise the optimization will be terminated
        IOptimizationResult result = resultFuture.get(2, TimeUnit.MINUTES);

        System.out.println(result);
    }

    /** Sets the properties. */
    public void setProperties() {

        Properties props = new Properties();
        props.setProperty("JOptExitCondition.JOptGenerationCount", "1000");
        props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "1000");

        // We have to tell the optimizer that we have a high interest in capacity planning, the default is
        // 100
        props.setProperty("JOptWeight.Capacity", "200");
        this.addElement(props);
    }

    /** Adds the res. */
    public void addRes() {

        /*
         * Setting the resource JackTruck
         */

        List<IWorkingHours> workingHours = new ArrayList<>();
        workingHours.add(
                new WorkingHours(
                        ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                        ZonedDateTime.of(2020, MARCH.getValue(), 11, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

        Duration maxWorkingTime = Duration.ofHours(10);
        Quantity<Length> maxDistanceKmW = Quantities.getQuantity(2800.0, KILO(METRE));

        IResource truck =
                new CapacityResource(
                        "Truck", 50.1167, 7.68333, maxWorkingTime, maxDistanceKmW, workingHours);

        // Setting a depot to our truck
        truck.setResourceDepot(this.createResourceDepot());

        // Adding the truck to our optimization
        this.addElement(truck);
    }

    /**
     * Creates the resource depot.
     *
     * @return the resource depot
     */
    public IResourceDepot createResourceDepot() {
        /*
         * Creating a ResourceDepot with two LoadCapacities and attach it to a Resource depot "truckDepot"
         */

        // Define the the ResourceDepot and the LoadCapacities "DelicateFruit" and "GlassVial"

        // Our depot can store a total maximum of 70 items.
        IResourceDepot depot = new SimpleResourceDepot("truckDepot", 70);

        // The "DelicateFruit" is so sensitive that it can only be stored in a special compartment for a longer
        // period of time. The compartment that allows us to store "DelicateFruit" has a total Capacity of 30. Provided
        // nothing else is in that compartment our maximum individual Capacity of "DelicateFruit" therefore is 30.
        // We start with an initial load of 5 pallets of "DelicateFruit".
        ILoadCapacity delicateFruitLoadCapacity = new SimpleLoadCapacity("DelicateFruit", 30, 5);

        // The "GlassVials" can be stored in the normal compartment and in the compartment for the "DelicateFruit".
        // Provided the truck is empty, we can load a total of 70 "GlassVial" on the truck.
        // We start with an initial load of 10 pallets of "GlassVials" of the maximum 70.
        ILoadCapacity glassVialCapacity = new SimpleLoadCapacity("GlassVials", 70, 10);

        // Adding the capacities to our depot
        depot.add(delicateFruitLoadCapacity);
        depot.add(glassVialCapacity);

        return depot;
    }

    /** Adds the nodes. */
    public void addNodes() {

        List<IOpeningHours> weeklyOpeningHours = new ArrayList<>();
        weeklyOpeningHours.add(
                new OpeningHours(
                        ZonedDateTime.of(2020, MARCH.getValue(), 11, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                        ZonedDateTime.of(2020, MARCH.getValue(), 11, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

        Duration visitDuration = Duration.ofMinutes(30);

        /*
         * "CustomerOne" wants us to deliver 2 "DelicateFruit" and pick up 10 "Glassvials", whereas "CustomerTwo"
         * asks for 3 "DelicateFruit" and pick up another 10 "GlassVials". Whether the goods are to be delivered or
         * picked up is set in createNodeDepot() below.
         */

        INode job1 =
                new TimeWindowGeoNode("CustomerOne", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

        job1.setNodeDepot(this.createNodeDepot(2, 10, "CustomerOneDepot"));
        this.addElement(job1);

        //
        INode job2 =
                new TimeWindowGeoNode("CustomerTwo", 50.9333, 6.95, weeklyOpeningHours, visitDuration, 1);

        job2.setNodeDepot(this.createNodeDepot(3, 10, "CustomerTwoDepot"));
        this.addElement(job2);

        //

    }

    /**
     * Creates the NodeDepot and add the two Loads to it. The "DelicateFruit" is defined as a request, whereas the
     * "GlassVials" need to be picked up.
     *
     * @param fruitRequest the fruit request
     * @param glassRequest the glass supply
     * @param depotId the depot id
     * @return the i node depot
     */
    public INodeDepot createNodeDepot(int fruitRequest, int glassRequest, String depotId) {

        ILoad deliverGoodLoad = new SimpleLoad("DelicateFruit", fruitRequest, true, true);

        ILoad pickupGoodLoad = new SimpleLoad("GlassVials", glassRequest, false, true);

        INodeDepot customerNodeDepot = new SimpleNodeDepot(depotId);
        customerNodeDepot.add(deliverGoodLoad);
        customerNodeDepot.add(pickupGoodLoad);

        return customerNodeDepot;
    }

    private static void subscribeToEvents(IOptimization opti) {

        // Subscribe to events
        opti.getOptimizationEvents()
                .progressSubject()
                .subscribe(
                        p -> {
                            System.out.println(p.getProgressString());
                        });

        opti.getOptimizationEvents()
                .errorSubject()
                .subscribe(
                        e -> {
                            System.out.println(e.getCause() + " " + e.getCode());
                        });

        opti.getOptimizationEvents()
                .warningSubject()
                .subscribe(
                        w -> {
                            System.out.println(w.getDescription() + w.getCode());
                        });

        opti.getOptimizationEvents()
                .statusSubject()
                .subscribe(
                        s -> {
                            System.out.println(s.getDescription() + " " + s.getCode());
                        });
    }
}
