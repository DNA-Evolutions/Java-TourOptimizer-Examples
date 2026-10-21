package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.nodelevel.custom;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.dna.jopt.assessment.costadjustment.EntityCostAdjuster;
import com.dna.jopt.assessment.costassessor.EvaluatedNodeDataHolder;
import com.dna.jopt.assessment.costassessor.IEntityCostAssessor;
import com.dna.jopt.assessment.costassessorrestriction.nodelevel.custom.AbstractCustomNodeLevelRestriction;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.EntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.IEntityRestrictionResult;
import com.dna.jopt.framework.inputplausibility.properties.IPropertyProvider;
import com.dna.jopt.member.bucket.entity.IEntity;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.IOptimizationElement;
import com.dna.jopt.member.unit.node.INode;
import com.dna.jopt.member.unit.violation.IViolation;
import com.dna.jopt.member.unit.violation.ViolationFactory;

/**
 * A custom node-level restriction: a node whose id starts with "M" should be reached before 11:00
 * (Europe/Berlin) on the day of its visit.
 *
 * <p>Each minute after 11:00 costs ten times the late-arrival weight, times a factor that grows
 * exponentially with the delay. The restriction books the cost and a late violation on the route,
 * and with a requested result it attaches a violation with the minutes late.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/OpenAssessorNodeLevel.md">OpenAssessorNodeLevel.md</a>
 */
public class CustomNodeLetterMRestriction extends AbstractCustomNodeLevelRestriction {

  /**
   * Creates the restriction.
   *
   * @param propertyProvider the property provider of the optimization
   */
  public CustomNodeLetterMRestriction(IPropertyProvider propertyProvider) {
    super(propertyProvider);
  }

  @Override
  public IEntityRestrictionResult invokeRestriction(
      IEntity en,
      ILogicEntityRoute route,
      INode node,
      IOptimizationElement lastGeoLocationElement,
      IOptimizationElement prevElement,
      EvaluatedNodeDataHolder holder,
      IEntityCostAssessor ca,
      boolean resultRequested) {

    // Only nodes whose id starts with "M" have the deadline.
    if (!node.getId().startsWith("M")) {
      return new EntityRestrictionResult();
    }

    // The arrival as local time in Europe/Berlin, the time zone of the example data.
    LocalDateTime arrival =
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(holder.getArrivalTime()), ZoneId.of("Europe/Berlin"));

    // 11:00 on the day of the arrival.
    LocalDateTime deadline =
        LocalDateTime.of(
            arrival.getYear(), arrival.getMonth(), arrival.getDayOfMonth(), 11, 0, 0, 0);

    Duration delta = Duration.between(deadline, arrival);

    double costLate = 0.0;
    if (delta.getSeconds() > 0) {
      costLate = this.calculatePenaltyCost(delta, route);
    }

    // The cost and the counters are booked like a late visit: as late cost, as a constraint
    // violation and as a late time-window violation.
    IViolation curViolation = null;
    if (costLate > 0) {
      route
          .getRouteCostAndViolationController()
          .setCostLate(route.getRouteCostAndViolationController().getCostLate() + costLate);

      route
          .getRouteCostAndViolationController()
          .setNumConstraintViolations(
              route.getRouteCostAndViolationController().getNumConstraintViolations() + 1);
      route
          .getRouteCostAndViolationController()
          .setNumViolationsNodeTimewindowLate(
              route.getRouteCostAndViolationController().getNumViolationsNodeTimewindowLate() + 1);

      // With a requested result, the restriction also attaches a violation with the minutes
      // late, so the result can show why the node costs more.
      if (resultRequested) {
        curViolation = ViolationFactory.VIOLATION_CONSTRAINTVIOLATION_TIMECONSTRAINT_LATE.copy();
        curViolation.setValue(
            " Late M Violation time [min]: " + delta.getSeconds() / 60.0,
            String.valueOf(delta.getSeconds() / 60.0));
      }
    }

    // addViolation ignores null, so a node in time adds no violation.
    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();
    myRestrictionResult.addViolation(curViolation);
    route.getRouteCostAndViolationController().addCost(costLate);

    return myRestrictionResult;
  }

  /**
   * Computes the cost of an arrival after 11:00.
   *
   * @param delta the time between 11:00 and the arrival
   * @param route the route of the node
   * @return the cost
   */
  private double calculatePenaltyCost(Duration delta, ILogicEntityRoute route) {

    // Ten times the late-arrival weight (the property JOptWeight.TimeWindow.TimeWindowLateFactor)
    // per minute late; the factor 10 is a sample value.
    double baseCost =
        10
            * this.getPropertyProvider().getPropertyProviderHelper().getWeightLateArrival()
            * delta.getSeconds()
            / 60.0;

    // The multiplier grows by a factor of about 1.38 per hour late (0.00009 per second, a sample
    // rate), so a longer delay costs more per minute.
    double expMultiplier = CustomNodeLetterMRestriction.exp(0.00009 * delta.getSeconds());

    double cost = baseCost * expMultiplier;

    // The cost adjuster turns the minute value into cost with the per-hour cost of the resource.
    cost = this.getCostAdjuster().getAdjustedCost(route, cost, EntityCostAdjuster.TIME_UNIT_MIN);

    return cost;
  }

  /**
   * Approximates e^val with bit arithmetic (Schraudolph's method) instead of Math.exp. For delays
   * up to six hours the result is within about 4 percent of e^val.
   *
   * @param val the exponent
   * @return about e^val
   */
  private static double exp(double val) {
    final long tmp = (long) (1512775 * val + 1072632447);
    return Double.longBitsToDouble(tmp << 32);
  }
}
