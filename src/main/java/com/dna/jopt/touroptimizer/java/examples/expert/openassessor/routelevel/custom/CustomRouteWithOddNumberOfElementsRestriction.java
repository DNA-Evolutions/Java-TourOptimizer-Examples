package com.dna.jopt.touroptimizer.java.examples.expert.openassessor.routelevel.custom;
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
import java.util.Optional;

import com.dna.jopt.assessment.costassessor.IEntityCostAssessor;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.EntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.restrictionresult.IEntityRestrictionResult;
import com.dna.jopt.assessment.costassessorrestriction.routelevel.custom.AbstractCustomRouteLevelRestriction;
import com.dna.jopt.framework.inputplausibility.properties.IPropertyProvider;
import com.dna.jopt.member.bucket.route.ILogicEntityRoute;
import com.dna.jopt.member.unit.violation.IViolation;
import com.dna.jopt.member.unit.violation.Violation;

/**
 * A custom route-level restriction: a route should have an odd number of nodes.
 *
 * <p>A route with an even number of nodes costs a penalty of 1000. The restriction books the
 * penalty as injected restriction cost and as a constraint violation, and with a requested result
 * it attaches a violation with the node count.
 *
 * @see <a href="https://github.com/DNA-Evolutions/Java-TourOptimizer-Examples/blob/master/src/main/java/com/dna/jopt/touroptimizer/java/examples/expert/openassessor/OpenAssessorRouteLevel.md">OpenAssessorRouteLevel.md</a>
 */
public class CustomRouteWithOddNumberOfElementsRestriction
    extends AbstractCustomRouteLevelRestriction {

  /**
   * The violation that a route with an even number of nodes gets. The code 999 is a sample value
   * for a custom violation.
   */
  public static final IViolation VIOLATION_NOT_ODD_NUMBER_OF_ELEMENTS =
      new Violation("CONSTRAINTVIOLATION", "ROUTE SIZE", "NOT ODD NUMBER OF ELEMENTS", 999);

  /**
   * Creates the restriction.
   *
   * @param propertyProvider the property provider of the optimization
   */
  public CustomRouteWithOddNumberOfElementsRestriction(IPropertyProvider propertyProvider) {
    super(propertyProvider);
  }

  @Override
  public IEntityRestrictionResult invokeRestriction(
      Optional<ILogicEntityRoute> prevRouteOpt,
      ILogicEntityRoute route,
      IEntityCostAssessor ca,
      boolean resultRequested) {

    EntityRestrictionResult myRestrictionResult = new EntityRestrictionResult();

    // The nodes of the route.
    int numOptimizableRouteElements = route.getRouteOptimizableElements().size();
    double penaltyCost = 0.0;
    IViolation curViolation = null;

    if ((numOptimizableRouteElements % 2) == 0) {
      // A sample penalty for a route with an even number of nodes.
      penaltyCost = 1000.0;

      route
          .getRouteCostAndViolationController()
          .setNumConstraintViolations(
              route.getRouteCostAndViolationController().getNumConstraintViolations() + 1);

      route
          .getRouteCostAndViolationController()
          .setCostInjectedRestriction(
              route.getRouteCostAndViolationController().getCostInjectedRestriction()
                  + penaltyCost);

      route.getRouteCostAndViolationController().addCost(penaltyCost);
    }

    // With a requested result, the restriction also attaches a violation with the node count.
    if (resultRequested && penaltyCost > 0) {
      curViolation =
          CustomRouteWithOddNumberOfElementsRestriction.VIOLATION_NOT_ODD_NUMBER_OF_ELEMENTS.copy();
      curViolation.setValue(
          " Not odd number of nodes restriction. Count: " + numOptimizableRouteElements,
          String.valueOf(numOptimizableRouteElements));
    }

    // addViolation ignores null, so an odd route adds no violation.
    myRestrictionResult.addViolation(curViolation);

    return myRestrictionResult;
  }
}
