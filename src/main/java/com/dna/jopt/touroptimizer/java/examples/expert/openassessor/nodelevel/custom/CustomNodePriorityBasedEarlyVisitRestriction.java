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

/**
 * A custom node-level restriction: every minute between the start of a node's opening hours and
 * the arrival costs, weighted by the importance of the node, so that important nodes are visited
 * early in their opening hours.
 *
 * <p>The reference is the start of the opening hours in which the visit is planned. The
 * restriction books the cost and a late violation on the route, although the visit is inside the
 * opening hours, and it attaches no violation to the result.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/OpenAssessorNodeLevel.md">OpenAssessorNodeLevel.md</a>
 */
public class CustomNodePriorityBasedEarlyVisitRestriction
    extends AbstractCustomNodeLevelRestriction {

  /**
   * Creates the restriction.
   *
   * @param propertyProvider the property provider of the optimization
   */
  public CustomNodePriorityBasedEarlyVisitRestriction(IPropertyProvider propertyProvider) {
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

    int curNodeImportance = node.getImportance();

    // The time between the start of the opening hours of the visit and the arrival.
    long curNodeHoursStartMillis =
        node.getDutyHours()
            .get(holder.getChoosenOpeningHoursIndex())
            .getTimeWindow()
            .getHoursBeginInMillis();

    long deltaMillis = holder.getArrivalTime() - curNodeHoursStartMillis;

    double costLate = 0.0;
    if (deltaMillis > 0) {
      costLate = this.calculatePenaltyCost(deltaMillis, curNodeImportance, route);
    }

    // The cost and the counters are booked like a late visit: as late cost, as a constraint
    // violation and as a late time-window violation.
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
    }

    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();

    route.getRouteCostAndViolationController().addCost(costLate);

    return myRestrictionResult;
  }

  /**
   * Computes the cost of the time after the opening start.
   *
   * @param deltaMillis the time between the opening start and the arrival, in milliseconds
   * @param curNodeImportance the importance of the node
   * @param route the route of the node
   * @return the cost
   */
  private double calculatePenaltyCost(
      long deltaMillis, int curNodeImportance, ILogicEntityRoute route) {

    // The importance times the late-arrival weight (the property
    // JOptWeight.TimeWindow.TimeWindowLateFactor) per minute.
    double cost =
        curNodeImportance
            * this.getPropertyProvider().getPropertyProviderHelper().getWeightLateArrival()
            * deltaMillis
            / 1000.0
            / 60;

    // The cost adjuster turns the minute value into cost with the per-hour cost of the resource.
    cost = this.getCostAdjuster().getAdjustedCost(route, cost, EntityCostAdjuster.TIME_UNIT_MIN);

    return cost;
  }
}
