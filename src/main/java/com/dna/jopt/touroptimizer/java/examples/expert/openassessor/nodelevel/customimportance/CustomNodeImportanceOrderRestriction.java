package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.nodelevel.customimportance;
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
 * A custom node-level restriction: within a route, the nodes should follow each other in falling
 * importance.
 *
 * <p>A node with a higher importance than the node before it costs 100 per step of importance. The
 * restriction books the cost and a late violation on the route and attaches no violation to the
 * result.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/OpenAssessorNodeLevel.md">OpenAssessorNodeLevel.md</a>
 */
public class CustomNodeImportanceOrderRestriction extends AbstractCustomNodeLevelRestriction {

  /**
   * Creates the restriction.
   *
   * @param propertyProvider the property provider of the optimization
   */
  public CustomNodeImportanceOrderRestriction(IPropertyProvider propertyProvider) {
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

    // The first node of a route follows the resource, not a node: it is compared with itself, so
    // it costs nothing.
    int prevNodeImportance = node.getImportance();

    if (prevElement instanceof INode) {
      prevNodeImportance = ((INode) prevElement).getImportance();
    }

    // A positive delta means that a more important node follows a less important one.
    int deltaImportance = curNodeImportance - prevNodeImportance;

    double costWrongOrderedImportance = 0.0;
    if (deltaImportance > 0) {
      // 100 per step of importance, a sample value that is not passed through the cost adjuster.
      // A rule that should follow the late-arrival weight could use
      // this.getPropertyProvider().getPropertyProviderHelper().getWeightLateArrival() instead.
      costWrongOrderedImportance = 100 * deltaImportance;
    }

    // The cost and the counters are booked like a late visit: as late cost, as a constraint
    // violation and as a late time-window violation.
    if (costWrongOrderedImportance > 0) {
      route
          .getRouteCostAndViolationController()
          .setCostLate(
              route.getRouteCostAndViolationController().getCostLate()
                  + costWrongOrderedImportance);

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

    route.getRouteCostAndViolationController().addCost(costWrongOrderedImportance);

    return myRestrictionResult;
  }
}
