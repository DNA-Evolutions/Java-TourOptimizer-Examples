package com.dna.jopt.touroptimizer.java.examples.advanced.pickupanddelivery.sdo;
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

import static java.time.Month.MAY;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.METRE;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.dna.jopt.member.unit.pnd.load.flexload.CoupledRequestFlexLoad;
import com.dna.jopt.member.unit.pnd.load.flexload.SupplyFlexLoad;
import com.dna.jopt.member.unit.resource.CapacityResource;
import com.dna.jopt.member.unit.resource.IResource;
import com.dna.jopt.touroptimizer.java.examples.ExampleLicenseHelper;

import tech.units.indriya.quantity.Quantities;

/**
 * <b>EXPERIMENTAL FEATURE</b> — {@link CoupledRequestFlexLoad} is an experimental
 * capability of JOpt.TourOptimizer. Behavior, API, and defaults may change in future
 * releases without prior notice.
 *
 * <hr>
 *
 * <p>Demonstrates <b>coupled split delivery</b> using {@link CoupledRequestFlexLoad}.
 *
 * <p>Three customer groups each consist of two split-nodes (S0, S1). The optimizer
 * distributes each group's total pallet demand across its two split-nodes, keeping
 * every individual truck visit within the truck's capacity of
 * {@value #MAX_TRUCK_CAPACITY} pallets. The coupling invariant for each group is:
 * <pre>  S0 + S1 = total demand</pre>
 *
 * <h2>Groups</h2>
 * <table border="1" cellpadding="4">
 *   <tr><th>Group</th><th>Total demand</th><th>Split required?</th></tr>
 *   <tr><td>Cologne</td><td>30 pallets</td>
 *       <td><b>Yes</b> — 30 &gt; 20, two truck visits are mandatory</td></tr>
 *   <tr><td>Leverkusen</td><td>10 pallets</td>
 *       <td>No — one truck visit is sufficient</td></tr>
 *   <tr><td>Dortmund</td><td>20 pallets</td>
 *       <td>No — exactly fills one truck</td></tr>
 * </table>
 *
 * <h2>Fuzzy visits</h2>
 * <p>{@link CoupledRequestFlexLoad} is created with {@code isFuzzyVisit = true}.
 * Fuzzy visits allow the optimizer to exchange a partial load — the truck does not
 * have to deliver the node's full desired amount in one stop. Without fuzzy visits
 * the PND evaluator locks the desired exchange amount, which prevents the optimizer
 * from exploring different split ratios effectively and causes it to get stuck at
 * the initial distribution.
 *
 * <h2>Supply</h2>
 * <p>Four supply nodes in Aachen each carry {@value #INITIAL_SUPPLY_LOAD} pallets
 * (flexible — the optimizer adjusts the value freely). Trucks start empty and reload
 * at supply nodes before serving customers. A supply node is suppressed from routing
 * once its load reaches zero ({@code isIgnoreOnZeroLoad = true}).
 *
 * <h2>Resources</h2>
 * <p>Two trucks, both based in Aachen.
 *
 * <h2>Expected result</h2>
 * <pre>
 * Truck_Aachen_0: Supply_Aachen_0 (20) → Cologne_S0 (20) → Supply_Aachen_2 (20) → Dortmund_S0 (20)
 * Truck_Aachen_1: Supply_Aachen_1 (20) → Cologne_S1 (10) → Leverkusen_S1 (10)
 * </pre>
 * Cologne is split across both trucks (20 + 10 = 30). Dortmund and Leverkusen are
 * each consolidated to a single stop; the unused split-nodes are suppressed.
 *
 * @author Jens Richter
 * @version Apr 2026
 * @since Apr 2026
 */
public class CoupledFlexLoadSplitDeliveryExample extends Optimization {

  /** Maximum pallets one truck can carry per visit. */
  private static final int MAX_TRUCK_CAPACITY = 20;

  /**
   * Starting supply at each Aachen supply node (pallets).
   * The optimizer adjusts this value freely during the run.
   */
  private static final int INITIAL_SUPPLY_LOAD = 20;

  // ─────────────────────────────────────────────────────────────────────────

  public static void main(String[] args)
      throws InvalidLicenceException, IOException, InterruptedException,
             ExecutionException, TimeoutException {
    new CoupledFlexLoadSplitDeliveryExample().example();
  }

  public String toString() {
    return "Coupled split delivery (EXPERIMENTAL): Cologne (mandatory split, 30 pallets),"
        + " Leverkusen (10 pallets), Dortmund (20 pallets) — trucks based in Aachen.";
  }

  // ── Entry point ───────────────────────────────────────────────────────────

  public void example()
      throws InvalidLicenceException, IOException, InterruptedException,
             ExecutionException, TimeoutException {

    ExampleLicenseHelper.setLicense(this);

    this.setProperties();
    this.addNodes();
    this.addResources();

    CompletableFuture<IOptimizationResult> resultFuture = this.startRunAsync();
    subscribeToEvents(this);

    IOptimizationResult result = resultFuture.get(2, TimeUnit.MINUTES);

    System.out.println(result);
  }

  // ── Properties ────────────────────────────────────────────────────────────

  private void setProperties() {
    Properties props = new Properties();

    props.setProperty("JOptExitCondition.JOptGenerationCount", "20000");
    props.setProperty("JOpt.Algorithm.PreOptimization.SA.NumIterations", "500000");

    // A moderate capacity penalty keeps the cost landscape navigable: too high a value
    // makes temporary violations so expensive that the optimizer cannot move nodes to
    // reach a better solution; too low a value risks ignoring coupling violations entirely.
    props.setProperty("JOptWeight.Capacity", "10");

    this.addElement(props);
  }

  // ── Nodes ─────────────────────────────────────────────────────────────────

  private void addNodes() {

    Duration defaultVisitDuration = Duration.ofMinutes(60);

    /*
     * Group: Cologne  (total = 30 pallets — mandatory split)
     *
     * 30 > MAX_TRUCK_CAPACITY (20): a single truck cannot fulfil this order.
     * The optimizer must split the 30 pallets across S0 and S1 so that
     * neither value exceeds 20.  Both split-nodes are visited by different
     * trucks.
     * Initial values: S0 = 5, S1 = 30 - 5 = 25. The optimizer redistributes
     * freely from this starting point (e.g. converging to 20 + 10).
     */
    List<INode> groupCologne = new ArrayList<>();
    groupCologne.add(createNode("Cologne_S0", 50.9333, 6.9500, defaultVisitDuration));
    groupCologne.add(createNode("Cologne_S1", 50.9333, 6.9500, defaultVisitDuration));
    attachCoupledLoad(groupCologne, /* requestFirstSplit */ 5, /* total */ 30);

    /*
     * Group: Leverkusen  (total = 10 pallets — optional split)
     *
     * 10 <= MAX_TRUCK_CAPACITY (20): one truck is sufficient.
     * The optimizer consolidates all 10 pallets onto one split-node and
     * suppresses the other via isIgnoreOnZeroLoad.
     * Initial values: S0 = 5, S1 = 10 - 5 = 5.
     */
    List<INode> groupLeverkusen = new ArrayList<>();
    groupLeverkusen.add(createNode("Leverkusen_S0", 51.0459, 6.9929, defaultVisitDuration));
    groupLeverkusen.add(createNode("Leverkusen_S1", 51.0459, 6.9929, defaultVisitDuration));
    attachCoupledLoad(groupLeverkusen, /* requestFirstSplit */ 2, /* total */ 10);

    /*
     * Group: Dortmund  (total = 20 pallets — optional split)
     *
     * 20 == MAX_TRUCK_CAPACITY (20): exactly fills one truck.
     * A single visit is feasible; the optimizer consolidates here too.
     * Initial values: S0 = 10, S1 = 20 - 10 = 10.
     */
    List<INode> groupDortmund = new ArrayList<>();
    groupDortmund.add(createNode("Dortmund_S0", 51.5136, 7.4653, defaultVisitDuration));
    groupDortmund.add(createNode("Dortmund_S1", 51.5136, 7.4653, defaultVisitDuration));
    attachCoupledLoad(groupDortmund, /* requestFirstSplit */ 10, /* total */ 20);

    groupCologne.forEach(this::addElement);
    groupLeverkusen.forEach(this::addElement);
    groupDortmund.forEach(this::addElement);

    /*
     * Supply nodes: Aachen  (4 x 20 pallets, flexible)
     *
     * Trucks start empty and reload here before serving customers.
     * isIgnoreOnZeroLoad = true: a supply node with zero remaining load
     * is automatically suppressed from routing.
     */
    for (int i = 0; i < 4; i++) {
      INode supplyNode = createNode(
          "Supply_Aachen_" + i, 50.7753, 6.0839, Duration.ofMinutes(20));
      supplyNode.setIgnoreOnZeroLoad(true);

      INodeDepot supplyDepot = new SimpleNodeDepot("Depot_Supply_Aachen_" + i);
      supplyDepot.add(new SupplyFlexLoad("Pallets", INITIAL_SUPPLY_LOAD));
      supplyNode.setNodeDepot(supplyDepot);

      this.addElement(supplyNode);
    }
  }

  // ── Resources ─────────────────────────────────────────────────────────────

  private void addResources() {
    this.addElement(createTruck("Truck_Aachen_0", 50.7753, 6.0839));
    this.addElement(createTruck("Truck_Aachen_1", 50.7753, 6.0839));
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  /**
   * Creates a {@link TimeWindowGeoNode} with a standard day-shift opening window
   * on 2030-05-06.
   */
  private static INode createNode(String id, double lat, double lon, Duration visitDuration) {
    List<IOpeningHours> opening = new ArrayList<>();
    opening.add(new OpeningHours(
        ZonedDateTime.of(2030, MAY.getValue(), 6, 8, 0, 0, 0, ZoneId.of("Europe/Berlin")),
        ZonedDateTime.of(2030, MAY.getValue(), 6, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"))));
    return new TimeWindowGeoNode(id, lat, lon, opening, visitDuration, 1);
  }

  /**
   * Attaches a {@link CoupledRequestFlexLoad} for load id {@code "Pallets"} to
   * every node in {@code group}.
   *
   * <p>The coupling map encodes which depot ids belong to sibling nodes in the
   * same group. {@code FlexLoadOperator} uses it to maintain the invariant
   * {@code S0 + S1 = coupledTotal} while redistributing load during optimization.
   *
   * <p>{@code isFuzzyVisit = true} is required for effective load redistribution:
   * without it, the PND evaluator locks the exchange amount and the optimizer
   * cannot explore different split ratios.
   *
   * <p>{@code isIgnoreOnZeroLoad} is enabled so that a split-node assigned zero
   * pallets is suppressed from routing automatically.
   *
   * @param group              the two nodes forming one delivery group
   * @param requestFirstSplit  initial pallet value assigned to the first split-node (S0);
   *                           the second split-node (S1) receives {@code coupledTotal - requestFirstSplit}
   * @param coupledTotal       total pallets the group must receive across both nodes
   */
  private static void attachCoupledLoad(
      List<INode> group, int requestFirstSplit, int coupledTotal) {

    Function<INode, String> depotLabel = n -> "NodeDepot_" + n.getId();

    Map<String, List<String>> allDepots = group.stream()
        .collect(Collectors.toMap(
            INode::getId,
            n -> List.of(depotLabel.apply(n))));

    int[] initialValues = {requestFirstSplit, coupledTotal - requestFirstSplit};
    int nodeIndex = 0;

    for (INode node : group) {
      String ownDepotId = depotLabel.apply(node);

      Map<String, List<String>> siblingDepots = new HashMap<>(allDepots);
      siblingDepots.entrySet().removeIf(e -> e.getValue().contains(ownDepotId));

      // isFuzzyVisit = true: allows partial load exchange, which is required
      // for the optimizer to explore different split ratios effectively.
      CoupledRequestFlexLoad load =
          new CoupledRequestFlexLoad("Pallets", initialValues[nodeIndex++], /* isFuzzyVisit */ true);
      load.setCoupledLoadValue(coupledTotal);
      load.setCoupledDepotMap(siblingDepots);

      INodeDepot depot = new SimpleNodeDepot(ownDepotId);
      depot.add(load);
      node.setNodeDepot(depot);

      node.setIgnoreOnZeroLoad(true);
    }
  }

  /**
   * Creates a {@link CapacityResource} truck with capacity {@value #MAX_TRUCK_CAPACITY}
   * pallets and an initial load of 0 (the truck starts empty).
   */
  private static IResource createTruck(String id, double lat, double lon) {
    List<IWorkingHours> working = new ArrayList<>();
    working.add(new WorkingHours(
        ZonedDateTime.of(2030, MAY.getValue(), 6, 6, 0, 0, 0, ZoneId.of("Europe/Berlin")),
        ZonedDateTime.of(2030, MAY.getValue(), 6, 20, 0, 0, 0, ZoneId.of("Europe/Berlin"))));

    Quantity<Length> maxDist = Quantities.getQuantity(500.0, KILO(METRE));
    IResource truck = new CapacityResource(id, lat, lon, Duration.ofHours(12), maxDist, working);
    truck.setCost(0, 1, 1);

    ILoadCapacity pallets = new SimpleLoadCapacity("Pallets", MAX_TRUCK_CAPACITY, 0);
    IResourceDepot depot = new SimpleResourceDepot("ResourceDepot_" + id, MAX_TRUCK_CAPACITY);
    depot.add(pallets);
    truck.setResourceDepot(depot);

    return truck;
  }

  // ── Events ────────────────────────────────────────────────────────────────

  private static void subscribeToEvents(IOptimization opti) {
    opti.getOptimizationEvents().progressSubject()
        .subscribe(p -> System.out.println(p.getProgressString()));
    opti.getOptimizationEvents().errorSubject()
        .subscribe(e -> System.out.println(e.getCause() + " " + e.getCode()));
    opti.getOptimizationEvents().warningSubject()
        .subscribe(w -> System.out.println(w.getDescription() + w.getCode()));
    opti.getOptimizationEvents().statusSubject()
        .subscribe(s -> System.out.println(s.getDescription() + " " + s.getCode()));
  }
}